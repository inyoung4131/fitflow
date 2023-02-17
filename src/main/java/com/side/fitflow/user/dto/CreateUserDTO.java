package com.side.fitflow.user.dto;

import com.side.fitflow.user.entity.Authority;
import com.side.fitflow.user.entity.User;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreateUserDTO {
    private Long userId;

    private String userEmail;

    private String userPwd;

    private String userNickname;

    private Date createDt;

    @Setter
    private String createIp;

    public User toEntity(PasswordEncoder passwordEncoder, Set<Authority> authorities){
        return User.builder()
                .userEmail(userEmail)
                .userPwd(passwordEncoder.encode(userPwd))
                .userNickname(userNickname)
                .userAuthorities(authorities)
                .createDt(new Date())
                .createIp(createIp)
                .build();
    }
}
