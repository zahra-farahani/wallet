package com.snapppay.wallet.exception.handler;

import com.snapppay.wallet.exception.common.ApiResponse;
import com.snapppay.wallet.exception.common.BaseException;
import com.snapppay.wallet.util.MessageSourceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String API_STATUS = "FAILURE";

    private final MessageSource messageSource;

    @ExceptionHandler({BaseException.class})
    public ResponseEntity<ApiResponse> handleBaseException(HttpServletRequest req, BaseException e) {
        ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(new ApiResponse(
                API_STATUS,
                String.valueOf(e.getHttpCode()),
                MessageSourceUtil.getMessageIfExist(messageSource, e.getDescriptionKey())
        ), HttpStatus.valueOf(e.getHttpCode())); //todo :: log
        return responseEntity;
    }

    // handle validation by overriding Spring's method
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = (fieldError != null)
                ? MessageSourceUtil.getMessageIfExist(messageSource, fieldError.getDefaultMessage())
                : MessageSourceUtil.getMessageIfExist(messageSource, "user.default.validation.error");

        ApiResponse body = new ApiResponse(API_STATUS, HttpStatus.BAD_REQUEST.name(), message);
        return ResponseEntity.badRequest().body(body);
    }

    // fallback: override handleExceptionInternal instead of using @ExceptionHandler(Exception.class)
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = MessageSourceUtil.getMessageIfExist(messageSource, "internal.server.error");
        int sc = status != null ? status.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
        HttpStatus httpStatus = HttpStatus.resolve(sc);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ApiResponse apiBody = new ApiResponse(API_STATUS, httpStatus.name(), message);
        return new ResponseEntity<>(apiBody, headers, httpStatus);
    }
}
