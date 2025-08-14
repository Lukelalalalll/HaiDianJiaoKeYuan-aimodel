package com.zklcsoftware.basic.web;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.zklcsoftware.basic.exception.RestException;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = Logger.getLogger(RestExceptionHandler.class);

    /**
     * REST接口异常
     *
     * @param request
     * @param ex
     * @return
     */
    @ExceptionHandler(RestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleRestException(HttpServletRequest request, RestException ex) {
        logger.error("发生业务异常", ex);
        Response response = new Response(ex.getCode(), ex.getMessage());
        return response;
    }
}
