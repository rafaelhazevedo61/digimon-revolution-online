package com.dro.shared.exception;

import com.dro.shared.audit.ErrorAuditService;
import com.dro.shared.audit.TransactionOutcome;
import com.dro.shared.exception.dto.ErrorResponse;
import com.dro.shared.exception.dto.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorAuditService errorAuditService;

    public GlobalExceptionHandler(ErrorAuditService errorAuditService) {
        this.errorAuditService = errorAuditService;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                exception.getCode(),
                exception.getStatus().value(),
                TransactionOutcome.REJECTED
        );
        ErrorResponse response = ErrorResponse.of(
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(response);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.CONFLICT,
                HttpStatus.CONFLICT.value(),
                TransactionOutcome.REJECTED
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ApiErrorCode.CONFLICT,
                "The World Boss was updated by another player. Refresh and try again.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value(),
                TransactionOutcome.NOT_APPLICABLE
        );
        List<FieldErrorResponse> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR,
                "Request validation failed",
                request.getRequestURI(),
                fields
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value(),
                TransactionOutcome.NOT_APPLICABLE
        );
        List<FieldErrorResponse> fields = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR,
                "Request validation failed",
                request.getRequestURI(),
                fields
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                TransactionOutcome.NOT_APPLICABLE
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.BAD_REQUEST,
                "Malformed request body or invalid field value",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                TransactionOutcome.NOT_APPLICABLE
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.BAD_REQUEST,
                "Invalid value for parameter: " + exception.getName(),
                request.getRequestURI(),
                List.of(new FieldErrorResponse(exception.getName(), "Invalid value"))
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value(),
                TransactionOutcome.NOT_APPLICABLE
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR,
                "Missing required request parameter",
                request.getRequestURI(),
                List.of(new FieldErrorResponse(exception.getParameterName(), "Parameter is required"))
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.CONFLICT,
                HttpStatus.CONFLICT.value(),
                TransactionOutcome.REJECTED
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ApiErrorCode.CONFLICT,
                "O estado do Digimon foi alterado por outra operação. Atualize a tela e tente novamente.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        errorAuditService.record(
                request,
                exception,
                ApiErrorCode.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                TransactionOutcome.UNKNOWN
        );
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ApiErrorCode.INTERNAL_ERROR,
                "Unexpected internal error",
                request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(response);
    }
}