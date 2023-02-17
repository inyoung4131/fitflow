package com.side.fitflow.comm.converter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EnumUserGradeException extends RuntimeException{
    public EnumUserGradeException(String message){
        super(message);
    }
}
