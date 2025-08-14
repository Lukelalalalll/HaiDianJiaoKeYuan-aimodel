package com.zklcsoftware.common.web.exception;

import lombok.Data;

@Data
public class EngineException extends RuntimeException {
    private String code;
    private String message;

    public EngineException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public EngineException(String message) {
        super(message);
        this.message = message;
    }

    public EngineException(EngineErrorCodeEnmu engineErrorCodeEnmu) {
        super(engineErrorCodeEnmu.getName());
        this.message = engineErrorCodeEnmu.getName();
        this.code = engineErrorCodeEnmu.getCode() + "";
    }
}
