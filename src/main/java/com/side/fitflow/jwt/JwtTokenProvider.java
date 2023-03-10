package com.side.fitflow.jwt;

import com.side.fitflow.jwt.dto.TokenDTO;
import com.side.fitflow.comm.converter.usergrade.UserGrade;
import com.side.fitflow.comm.exception.AuthorityExceptionType;
import com.side.fitflow.comm.exception.BizException;
import com.side.fitflow.user.entity.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";

    private final long ACCESS_TOKEN_EXPIRE_TIME;
    private final long REFRESH_TOKEN_EXPIRE_TIME;
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-expire-time}") long accessTime,
                            @Value("${jwt.refresh-token-expire-time}") long refreshTime
    ) {
        this.ACCESS_TOKEN_EXPIRE_TIME = accessTime;
        this.REFRESH_TOKEN_EXPIRE_TIME = refreshTime;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    protected String createToken(String email, Set<Authority> auth, long tokenValid) {

        Claims claims = Jwts.claims().setSubject(email);


        claims.put(AUTHORITIES_KEY,
                auth.stream()
                        .map(Authority::getAuthorityName)
                        .collect(Collectors.joining(","))
        );

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims) // ?????? ?????? ?????? ??????
                .setIssuedAt(now) // ?????? ?????? ??????
                .setExpiration(new Date(now.getTime() + tokenValid)) // ?????? ????????????
                .signWith(key, SignatureAlgorithm.HS256) // ?????? ???????????? ??????
                .compact();
    }

    /**
     * @param email
     * @param auth
     * @return ????????? ?????? ??????
     */
    public String createAccessToken(String email, Set<Authority> auth) {
        return this.createToken(email, auth, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * @param email
     * @param auth
     * @return ???????????? ?????? ??????
     */
    public String createRefreshToken(String email, Set<Authority> auth) {
        return this.createToken(email, auth, REFRESH_TOKEN_EXPIRE_TIME);
    }

    /**
     * @param token
     * @return ?????? ?????? ???????????? ???????????? ?????? ????????? ?????? ????????????.
     */
    public String getMemberEmailByToken(String token) {
        // ????????? claim ??? sub ?????? ????????? ?????? ????????????.
        return this.parseClaims(token).getSubject();
    }

    /**
     * @param accessToken
     * @param refreshToken
     * @return TOEKN DTO??? ????????????.
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken) {
        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType(BEARER_TYPE)
                .build();
    }

    public Authentication getAuthentication(String accessToken) throws BizException {

        // ?????? ?????????
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null || !StringUtils.hasText(claims.get(AUTHORITIES_KEY).toString())) {
            throw new BizException(AuthorityExceptionType.NOT_FOUND_AUTHORITY); // ???????????? ????????? ????????? ????????????.
        }

        log.debug("claims.getAuth = {}", claims.get(AUTHORITIES_KEY));
        log.debug("claims.getEmail = {}", claims.getSubject());

        // ??????????????? ?????? ?????? ????????????
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        authorities.stream().forEach(o -> {
            log.debug("getAuthentication -> authorities = {}", o.getAuthority());
        });

        // UserDetails ????????? ???????????? Authentication ??????
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new CustomEmailPasswordAuthToken(principal, "", authorities);
    }

    public int validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return 1;
        } catch (ExpiredJwtException e) {
            log.info("????????? JWT ???????????????.");
            return 2;
        } catch (Exception e) {
            log.info("????????? ???????????????.");
            return -1;
        }
    }


    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) { // ????????? ????????? ????????? ?????? ????????? ???
            return e.getClaims();
        }
    }

    public List<UserGrade> getAuthenticationToUserGrade(String accessToken) throws BizException {

        // ?????? ?????????
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null || !StringUtils.hasText(claims.get(AUTHORITIES_KEY).toString())) {
            throw new BizException(AuthorityExceptionType.NOT_FOUND_AUTHORITY); // ???????????? ????????? ????????? ????????????.
        }

        // ??????????????? ?????? ?????? ????????????
        return Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(UserGrade::valueOf)
                        .collect(Collectors.toList());
    }
}
