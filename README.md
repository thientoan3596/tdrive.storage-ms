# Spring Webflux TDRIVE - Storage service

Storage service of TDRIVE project.

## Prerequisites

- **Java:** Version 17 or higher is required to use this library.
- **Docker:** Required for containerization and running the service locally or in a Docker Compose setup.

## Getting Started

#### How to run locally standalone (for development)

1. **Clone the repository:**

```bash
git clone <repository-url>
cd <repository-directory>
```

2. **Set up environment variables:**
   Copy the .env.example file to .env and modify the variables according to your local setup.

```bash
cp .env.example .env
```

3. **Prepare MySQL (Optional):**
   Create directory for MySQL service (mounted data, and init sql files) (optional)
   `example`

```bash
mkdir -p mysql/.data mysq./init
```

> .data/ is where to mount your MySQL service and persist data after runs.
> init/ places .sql files which use to create database on first run of MySQL service.
> Please build and run that MySQL service. 4. Building image and run container:

```bash
docker build -t <image-name> .
docker run --env-file .env <image-name>
```

**BONUS:**
You can quickly skip step 3 and 4 with the following bash:

```bash
./local-start.sh
```

#### Usage Options for `local-start.sh`:

| Option                         | Description                                                                                                                                                                                  |
| ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `-p`                           | Provide a custom DB password.                                                                                                                                                                |
| `--mounted-drive`              | Provide the PATH to the MySQL mounted drive.                                                                                                                                                 |
| `--init-templates` \*          | Provide the PATH to the init directory where `*.sql.template` files reside. The generated `.sql` files will be placed in the same directory. Default location is at `MOUNTED_DRIVE/../init`. |
| `--no-init-template`           | Disable init templates, meaning no environment variables will be substituted in the templates.                                                                                               |
| `--generate-password` or `-gp` | Force the generation of a new password (default location: `./mysql/.data/`).                                                                                                                 |
| `--fresh-run` or `-fr`         | Fresh run (clear existing mounted data).                                                                                                                                                     |
| `-l`                           | Enable logging.                                                                                                                                                                              |

What it does:

- Substitute environment into .sql.template and generate .sql files (see \*)
- Start docker compose
  Alternatively, you can use docker-compose.yml to run your own way.

#### Run with Docker Compose for Full Project Setup.

1. Set up MySQL service as described in previous section.
   > Tip: You can use the .sql.template for reference.
2. Set up Storage services using provided Dockerfile, or create your own.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
