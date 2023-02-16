package com.side.fitflow.jwt.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class TokenReqDTO {
    private String accessToken;
    private String refreshToken;
}
