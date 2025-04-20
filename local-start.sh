#!/bin/bash
#title           :local-start.sh
#description     :Starter Script to lauch service locally with docker
#author          :thluon
#date            :2025-04-16
#version         :0.1.0
#usage           :local-start.sh -h
#notes           :Require openssl for auto generate-password
#==============================================================================
usage() {
  echo "Usage: $0 [-p|--pasword DB_PASSWORD] [--init-templates PATH] [--no-init-template] [--mounted-drive PATH] [--generate-password|-gp] [--fresh-run|-fr] [-l]"
  echo "  -p                      Provide custom DB password"
  echo "  --mounted-drive         Provide PATH to mysql mounted drive"
  echo "  --init-templates         Provide PATH to init directory where *sql.template files reside"
  echo "                    The generate .sql will be insame directory. Default location is at MOUNTED_DRIVE/../init"
  echo "  --no-init-template      No init template, then wont sub env to template"
  echo "  --generate_password     Force generating a new password (default ./mysql/.data/"
  echo "  --fresh-run             Fresh run (clear existing mounted data)"
  echo "  -l                      Enable logging"
  exit 1
}
LOGGING_ENABLED=false
FRESH_RUN_MODE=false
GENERATE_PASSWORD_MODE=false
MOUNTED_DRIVE=./mysql/.data
NO_INIT_TEMPLATE=false
while [[ $# -gt 0 ]]; do
  case "$1" in
  -p | --password)
    USER_DEFINED_DB_PASSWORD="$2"
    shift 2
    ;;
  --mounted-drive)
    MOUNTED_DRIVE="$2"
    shift 2
    ;;
  --init-templates)
    INIT_TEMPLATE_DIR="$2"
    shift 2
    ;;
  --no-init-template)
    NO_INIT_TEMPLATE=true
    shift
    ;;
  -gp | --generate_password)
    GENERATE_PASSWORD_MODE=true
    shift
    ;;
  -fr | --fresh-run)
    FRESH_RUN_MODE=true
    shift
    ;;
  -l)
    LOGGING_ENABLED=true
    shift
    ;;
  -*)
    usage
    ;;
  *)
    break
    ;;
  esac
done
pre_check() {
  if [ ! -f ".env" ]; then
    echo ".env file does not exist"
    exit 1
  fi
}
pre_check

update_env() {
  if grep -q "^DB_PASSWORD=" .env; then
    sed -i "s#^DB_PASSWORD=.*#DB_PASSWORD=${DB_PASSWORD}#" .env
  else
    echo "DB_PASSWORD=${DB_PASSWORD}" >>.env
  fi
  if [ "$LOGGING_ENABLED" = true ]; then
    echo "exported [DB_PASSWORD=${DB_PASSWORD}] to .env"
  fi
}
generate_password() {
  DB_PASSWORD=$(openssl rand -base64 32 | tr -d '\n')
}
has_db_password_env() {
  if grep -q "^DB_PASSWORD=" .env; then
    return 0
  else
    return 1
  fi
}
is_wsl2() {
  grep -q "microsoft" /proc/version && grep -q "WSL2" /proc/sys/kernel/osrelease
}
log() {
  if [ "${LOGGING_ENABLED}" = true ]; then
    echo $1
  fi
}
if [ "${NO_INIT_TEMPLATE}" = false ] && [ -z "${INIT_TEMPLATE_DIR}" ]; then
  INIT_TEMPLATE_DIR=$(echo "$MOUNTED_DRIVE" | sed 's/[^/]*$/init/')
fi

if [ ! -d "${MOUNTED_DRIVE}" ]; then
  echo "${MOUNTED_DRIVE} no such file or directory!"
  FRESH_RUN_MODE=true
fi

if [ "$FRESH_RUN_MODE" = true ]; then
  echo "Starting Fresh Run Mode"
  if [[ "${USER_DEFINED_DB_PASSWORD}" ]]; then
    DB_PASSWORD="${USER_DEFINED_DB_PASSWORD}"
    log "User-defined password [${USER_DEFINED_DB_PASSWORD}]"
  elif [[ "${GENERATE_PASSWORD_MODE}" = true ]]; then
    generate_password
    log "Generated password ${DB_PASSWORD}"
  elif [[ $(has_db_password_env) -eq 0 ]]; then
    DB_PASSWORD=$(grep "^DB_PASSWORD=" .env | cut -d "=" -f2-)
    log "Reuse exisiting password [${DB_PASSWORD}]"
  else
    generate_password
    log "No [DB_PASSWORD] found! Generated password [${DB_PASSWORD}]"
  fi
  update_env
  if [ -d "${MOUNTED_DRIVE}" ]; then
    error=$(sudo rm -rf ${MOUNTED_DRIVE} 2>&1)
    if [ $? -ne 0 ]; then
      echo "Failed to remove MOUNTED_DRIVE: $error"
      echo "Unable to clear the mounted drive!\nConsider manually delete it and rerun!"
      exit 1
    else
      log "Deleted ${MOUNTED_DRIVE}"
    fi
  fi
  error=$(mkdir ${MOUNTED_DRIVE})
  if [ $? -ne 0 ]; then
    echo "$error"
    exit 1
  else
    log "Created ${MOUNTED_DRIVE}"
  fi
else
  echo "Starting Non-fresh Run Mode"
  if [[ "${USER_DEFINED_DB_PASSWORD}" ]]; then
    DB_PASSWORD="${USER_DEFINED_DB_PASSWORD}"
    log "User-defined password [${USER_DEFINED_DB_PASSWORD}]"
  elif [[ "${GENERATE_PASSWORD_MODE}" = true ]]; then
    generate_password
    log "Generated password ${DB_PASSWORD}"
  elif [[ $(has_db_password_env) -eq 0 ]]; then
    DB_PASSWORD=$(grep "^DB_PASSWORD=" .env | cut -d "=" -f2-)
    log "Reuse exisiting password [${DB_PASSWORD}]"
  else
    generate_password
    log "No [DB_PASSWORD] found! Generated password [${DB_PASSWORD}]"
  fi
  update_env
  if [ ! -d "${MOUNTED_DRIVE}" ]; then
    error=$(mkdir ${MOUNTED_DRIVE})
    if [ $? -ne 0 ]; then
      echo "$error"
      exit 1
    else
      log "Created ${MOUNTED_DRIVE}"
    fi
  fi
fi

if ! grep -q "^DB_ROOT_PASSWORD=" .env; then
  echo "DB_ROOT_PASSWORD=$(openssl rand -base64 32 | tr -d '\n')" >>.env
  echo "WARNING! No DB_ROOT_PASSWORD, generated one!"
  return 0
fi

set -a
. .env
set +a

if [ "${NO_INIT_TEMPLATE}" = false ] && [ ! -d "${INIT_TEMPLATE_DIR}" ]; then
  echo "WARNIG: ${INIT_TEMPLATE_DIR} no such file or directory!\nIgnoring parsing sql template!"
elif [ "${NO_INIT_TEMPLATE}" = false ]; then
  templates=()
  while IFS= read -r -d '' template; do
    templates+=("$template")
  done < <(find "$INIT_TEMPLATE_DIR" -maxdepth 1 -type f -name "*.template" -print0)
  echo "Found ${#templates[@]} template(s)."
  for template in "${templates[@]}"; do
    required_vars=$(grep -o '\${\([^}]*\)}' "$template" | sed 's/${\([^}]*\)}/\1/' | sort | uniq)
    for var in $required_vars; do
      if [ -z "${!var}" ]; then
        echo "Error: $var is not set"
        exit 1
      fi
    done
    target="${template%.template}"
    envsubst <"$template" >"$target"
    log "Generated ${target}"
  done
fi

# There is a bug related to docker with wsl2 causing high vmemwsl usage without releasing.
# Instead of directly calling docker compose inside WSL, use cmd.exe instead.
# Theoretically, it should make no different if using docker wsl container, but in reality this make significant different.
# Hypotheiscally, this is wsl2 kernel issue.
if is_wsl2; then
  cmd.exe /c "docker-compose up -d"
else
  docker compose up -d
fi

