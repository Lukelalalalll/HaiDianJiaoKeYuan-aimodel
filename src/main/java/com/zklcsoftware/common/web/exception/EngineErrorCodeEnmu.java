package com.zklcsoftware.common.web.exception;

import org.springframework.util.StringUtils;

public enum EngineErrorCodeEnmu {
    UNDEFINE_ERROR(1, "undefine_error"),
    SYSTEM_ERROR(2, "system_error"),
    BIZ_ERROR(3, "biz_error"),
    SCHEMA_NOT_EXIT_ERROR(4, "请求模型不存在"),
    REQ_NOT_EXIT_ERROR(5, "请求方法不存在"),
    FILE_NOT_EXIT_ERROR(6, "模型文件不存在"),
    PARAM_NOT_EXIT_ERROR(11, "请求参数缺失"),
    WHERE_NOT_EXIT_ERROR(15, "无法确定操作条件，拒绝执行该操作，请使用规范语法"),
    GRAPHQL_NULL_ERROR(15, "GraphQL is null");


    private String name;
    private int code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    EngineErrorCodeEnmu(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static EngineErrorCodeEnmu getByCode(int code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        } else {
            EngineErrorCodeEnmu[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                EngineErrorCodeEnmu errorEnum = var1[var3];
                if (errorEnum.getCode() == code) {
                    return errorEnum;
                }
            }

            return null;
        }
    }
}
