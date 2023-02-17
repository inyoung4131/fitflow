package com.side.fitflow.user.dto;

import com.side.fitflow.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDTO {
    private String userId;

    public static UserDTO of(User user) {
        return new UserDTO(user.getUserEmail());
    }
}
