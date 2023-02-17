package com.side.fitflow.user.entity;

import com.side.fitflow.comm.converter.usergrade.UserGrade;
import com.side.fitflow.comm.converter.usergrade.UserGradeAttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authority {
    @Id
    @Column(length = 50,name="authority_name")
    @Convert(converter= UserGradeAttributeConverter.class)
    private UserGrade authorityName;

    public String getAuthorityName() {
        return this.authorityName.toString();
    }
}