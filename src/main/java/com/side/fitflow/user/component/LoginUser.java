package com.side.fitflow.user.component;

import com.side.fitflow.comm.converter.usergrade.UserGrade;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
public class LoginUser {
    private String userEmail;
    private List<UserGrade> userGrade;
}
