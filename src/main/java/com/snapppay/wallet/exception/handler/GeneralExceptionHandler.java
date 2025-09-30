package com.snapppay.wallet.exception.handler;

import com.snapppay.wallet.exception.common.ApiResponse;
import com.snapppay.wallet.exception.common.BaseException;
import com.snapppay.wallet.util.MessageSourceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String API_STATUS = "FAILURE";

    private final MessageSource messageSource;

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResponse> handleInternalServerException(HttpServletRequest req, Exception e) {
        ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(new ApiResponse(
                    API_STATUS,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    MessageSourceUtil.getMessageIfExist(messageSource, "internal.server.error")
            ), HttpStatus.INTERNAL_SERVER_ERROR); //todo ::log
        return responseEntity;
    }

    @ExceptionHandler({BaseException.class})
    public ResponseEntity<ApiResponse> handleBaseException(HttpServletRequest req, BaseException e) {
        ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(new ApiResponse(
                API_STATUS,
                String.valueOf(e.getHttpCode()),
                MessageSourceUtil.getMessageIfExist(messageSource, e.getDescriptionKey())
        ), HttpStatus.valueOf(e.getHttpCode())); //todo :: log
        return responseEntity;
    }
}
