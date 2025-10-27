package com.felipejf.banktransactions.exception;

public class BusinessException extends RuntimeException{
     public BusinessException(String message){
         super(message);
     }
}
