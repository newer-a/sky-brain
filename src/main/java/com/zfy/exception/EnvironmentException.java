package com.zfy.exception;

/** 环境接口的参数或上游服务错误，供Controller异常处理转换成HTTP响应。 */
public class EnvironmentException extends RuntimeException {
    private final int status;

    public EnvironmentException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
