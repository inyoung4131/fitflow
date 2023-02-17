package com.side.fitflow.comm.converter;

import lombok.Getter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Getter
@Converter
public class AbstractDbcodeEnumAttributeConverter<E extends Enum<E> & ConverterCommonType> implements AttributeConverter<E, String> {

    private Class<E> targetEnumClass;

    private boolean nullable;

    private String enumName;

    public AbstractDbcodeEnumAttributeConverter( Class<E> targetEnumClass,boolean b,String enumName) {
        this.targetEnumClass=targetEnumClass;
        this.nullable=b;
        this.enumName=enumName;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        if(!nullable&& attribute==null){
            throw new IllegalArgumentException(String.format("%s는 NULL로 저장 할 수 없습니다.", enumName));
        }
        return EnumValueConvertUtils.toDbCode(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if(!nullable && dbData==null || dbData=="" || dbData.length()==0){
            throw new IllegalArgumentException(String.format("%s는 DB에 NULL 혹은 Empty로(%s) 저장 되어 있습니다.", enumName,dbData));
        }
        return EnumValueConvertUtils.ofDbCode(targetEnumClass,dbData);
    }
}
