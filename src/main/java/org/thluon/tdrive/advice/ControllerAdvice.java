package org.thluon.tdrive.advice;

import com.github.thientoan3596.dto.ErrorResponseDTO;
import com.github.thientoan3596.exception.BaseException;
import com.github.thientoan3596.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.MissingRequestValueException;
import org.thluon.tdrive.exception.NonEmptyFolderException;

import java.util.List;
@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(MissingRequestValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestValue(MissingRequestValueException ex) {
        return new ResponseEntity<>(
                ErrorResponseDTO.builder()
                        .errors(List.of(
                                new FieldError("Unknown",
                                        ex.getName(),
                                        null,
                                        true,
                                        null,
                                        null,
                                        ex.getName() + " is required.")
                                ))
                        .isFormValidationError(true)
                        .status(HttpStatus.BAD_REQUEST)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler({WebExchangeBindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidationError(WebExchangeBindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ResponseEntity<>(
                ErrorResponseDTO.builder()
                        .errors(bindingResult.getFieldErrors())
                        .isFormValidationError(true)
                        .status(HttpStatus.BAD_REQUEST)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidationError(HandlerMethodValidationException ex) {
        List<FieldError> fieldErrors =
        ex.getAllValidationResults().stream()
                .flatMap(vr->{
                    String fieldName = vr.getMethodParameter().getParameterName();
                    return vr.getResolvableErrors().stream()
                            .map(resolvable-> new FieldError(
                                    "Unknown",
                                    fieldName==null?"":fieldName,
                                    vr.getArgument(),
                                    true,
                                    resolvable.getCodes(), resolvable.getArguments(),
                                    resolvable.getDefaultMessage()==null?"":resolvable.getDefaultMessage()));
                }).toList();
        return new ResponseEntity<>(
                ErrorResponseDTO.builder()
                        .errors(fieldErrors)
                        .isFormValidationError(true)
                        .status(HttpStatus.BAD_REQUEST)
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(NonEmptyFolderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleNonEmptyFolder(NonEmptyFolderException e) {
        FieldError fieldError = new FieldError(e.getModelName(), e.getFieldName(), e.getRejectedValue(), false, null, null, e.getMessage());
        return new ResponseEntity<>(ErrorResponseDTO.builder().errors(List.of(fieldError)).status(HttpStatus.BAD_REQUEST).build(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDTO> handleBaseExceptionNotFound(BaseException e) {
        FieldError fieldError = new FieldError(e.getModelName(), e.getFieldName(), e.getRejectedValue(), false, null, null, e.getMessage());
        return new ResponseEntity<>(ErrorResponseDTO.builder().errors(List.of(fieldError)).status(HttpStatus.NOT_FOUND).build(), HttpStatus.NOT_FOUND);
    }
}
