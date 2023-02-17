package com.side.fitflow.comm.converter.usergrade;

import com.side.fitflow.comm.converter.AbstractDbcodeEnumAttributeConverter;

import javax.persistence.Converter;

@Converter
public class UserGradeAttributeConverter extends AbstractDbcodeEnumAttributeConverter<UserGrade> {
    private static final String enumName = "사용자 등급";
    public UserGradeAttributeConverter(){
        super(UserGrade.class,false,enumName);
    }
}