package com.LHZ.SETSS2026.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {

    private Integer code;
    private String message;
    private Object data;

    // 成功（带数据）
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }

    // 成功（带消息）
    public static Result success(String message) {
        return new Result(200, message, null);
    }

    // 成功（带消息和数据）
    public static Result success(String message, Object data) {
        return new Result(200, message, data);
    }
    // 失败
    public static Result error(String message) {
        return new Result(500, message, null);
    }
}