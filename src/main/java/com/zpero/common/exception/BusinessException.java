package com.zpero.common.exception;


import lombok.Getter;

@Getter
public class BusinessException extends  RuntimeException{

    private final Integer code;

    private String message;

    public BusinessException(String message){
       this.message = message;
       this.code = 500;
    }

    public BusinessException(Integer code , String message){
        super(message);
        this.message = message;
        this.code = code;
    }
}
