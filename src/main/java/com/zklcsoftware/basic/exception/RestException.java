package com.zklcsoftware.basic.exception;


import com.zklcsoftware.basic.web.Response;

/**
 * REST服务异常
 * 此异常由代码显示抛出
 * 发生在REST接口调用异常时
 */
public class RestException extends RuntimeException {

    private int code; // 错误码

    public RestException() {
        super();
        code = Response.CODE_SERVER_ERROR;
    }

    public RestException(int code) {
        super();
        this.code = code;
    }

    public RestException(String message) {
        super(message);
        code = Response.CODE_SERVER_ERROR;
    }

    public RestException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    public RestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }
}
