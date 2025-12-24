package rheon.wsd_lora_community.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rheon.wsd_lora_community.global.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("CustomException: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Binding 예외 처리
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        log.error("BindException: {}", e.getMessage());
        List<ErrorResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_TYPE_VALUE.getCode(),
                ErrorCode.INVALID_TYPE_VALUE.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * HTTP 메서드 불일치 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                ErrorCode.METHOD_NOT_ALLOWED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 접근 거부 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("AccessDeniedException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.HANDLE_ACCESS_DENIED.getCode(),
                ErrorCode.HANDLE_ACCESS_DENIED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Exception: {}", e.getMessage(), e);
        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
