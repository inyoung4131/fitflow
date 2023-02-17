package com.side.fitflow.comm.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnumValueConvertUtils {

    /*
     * 코드명을 받아서 코드번호로 바꿈
     * */
    public static <T extends Enum<T> & ConverterCommonType> T ofDbCode(Class<T> enumClass, String dbCode){
        if(dbCode==null || dbCode=="" || dbCode.length()==0){
            return null;
        }

        return EnumSet.allOf(enumClass).stream()
                .filter(v -> v.getdbcode().equals(dbCode))
                .findAny()
                .orElseThrow(()-> new EnumUserGradeException(String.format("desc=[%s],dbcode=[%s]가 존재하지 않습니다.",enumClass.getName(),dbCode)));
    }

    /*NULL 확인 및 코드명 받아서 코드명 바꿈*/
    public static <T extends Enum<T> & ConverterCommonType> String toDbCode(T enumValue){
        if(enumValue == null){
            return "";
        }

        return enumValue.getdbcode();
    }

}
