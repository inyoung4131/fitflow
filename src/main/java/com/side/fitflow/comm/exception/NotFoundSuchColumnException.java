package com.side.fitflow.comm.exception;

public class NotFoundSuchColumnException extends RuntimeException{
    public NotFoundSuchColumnException(){
        super("해당 컬럼을 찾지 못했습니다");
    }
}
