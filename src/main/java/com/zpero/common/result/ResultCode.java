package com.zpero.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),

    UNAUTHORIZED(401, "未授权"),

    FORBIDDEN(403, "禁止访问"),

    NOT_FOUND(404, "资源未找到"),

    CONFLICT(409, "数据冲突"),

    ERROR(500, "服务器错误");

    private final Integer code;

    private final String message;


    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
