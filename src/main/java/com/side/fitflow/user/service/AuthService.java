package com.side.fitflow.user.service;

import com.side.fitflow.jwt.CustomEmailPasswordAuthToken;
import com.side.fitflow.jwt.CustomUserDetailsService;
import com.side.fitflow.jwt.JwtTokenProvider;
import com.side.fitflow.jwt.dto.TokenDTO;
import com.side.fitflow.jwt.dto.TokenReqDTO;
import com.side.fitflow.jwt.entity.RefreshToken;
import com.side.fitflow.jwt.repository.RefreshTokenRepository;
import com.side.fitflow.comm.converter.usergrade.UserGrade;
import com.side.fitflow.comm.exception.AuthorityExceptionType;
import com.side.fitflow.comm.exception.BizException;
import com.side.fitflow.comm.exception.JwtExceptionType;
import com.side.fitflow.comm.exception.MemberExceptionType;
import com.side.fitflow.user.dto.CreateUserDTO;
import com.side.fitflow.user.dto.LoginDTO;
import com.side.fitflow.user.dto.UserDTO;
import com.side.fitflow.user.entity.Authority;
import com.side.fitflow.user.entity.User;
import com.side.fitflow.user.repository.AuthorityRepository;
import com.side.fitflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public UserDTO signup(CreateUserDTO createUserDTO) {
        if (userRepository.existsByUserId(createUserDTO.getUserEmail())) {
            throw new BizException(MemberExceptionType.DUPLICATE_USER);
        }

        // DB ?????? ROLE_USER??? ????????? ???????????? ????????????.
        Authority authority = authorityRepository
                .findByAuthorityName(UserGrade.USER).orElseThrow(()->new BizException(AuthorityExceptionType.NOT_FOUND_AUTHORITY));

        Set<Authority> set = new HashSet<>();
        set.add(authority);


        User user = createUserDTO.toEntity(passwordEncoder,set);
        log.debug("user = {}",user);
        return UserDTO.of(userRepository.save(user));
    }

    @Transactional
    public TokenDTO login(LoginDTO loginReqDTO) {
        CustomEmailPasswordAuthToken customEmailPasswordAuthToken = new CustomEmailPasswordAuthToken(loginReqDTO.getEmail(),loginReqDTO.getPassword());
        Authentication authenticate = authenticationManager.authenticate(customEmailPasswordAuthToken);
        String email = authenticate.getName();
        User user = customUserDetailsService.getMember(email);

        String accessToken = tokenProvider.createAccessToken(email, user.getUserAuthorities());
        String refreshToken = tokenProvider.createRefreshToken(email, user.getUserAuthorities());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userKey(email)
                        .token(refreshToken)
                        .build()
        );

        return tokenProvider.createTokenDTO(accessToken,refreshToken);

    }

    @Transactional
    public TokenDTO reissue(TokenReqDTO tokenRequestDto) {
        /*
         *  accessToken ??? JWT Filter ?????? ???????????? ???
         * */
        String originAccessToken = tokenRequestDto.getAccessToken();
        String originRefreshToken = tokenRequestDto.getRefreshToken();

        // refreshToken ??????
        int refreshTokenFlag = tokenProvider.validateToken(originRefreshToken);
        log.debug("refreshTokenFlag = {}", refreshTokenFlag);

        //refreshToken ???????????? ????????? ?????? ????????? ????????????.
        if (refreshTokenFlag == -1) {
            throw new BizException(JwtExceptionType.BAD_TOKEN); // ????????? ???????????? ??????
        } else if (refreshTokenFlag == 2) {
            throw new BizException(JwtExceptionType.REFRESH_TOKEN_EXPIRED); // ???????????? ?????? ??????
        }

        // 2. Access Token ?????? Member Email ????????????
        Authentication authentication = tokenProvider.getAuthentication(originAccessToken);

        log.debug("Authentication = {}",authentication);

        // 3. ??????????????? Member Email ??? ???????????? Refresh Token ??? ?????????
        RefreshToken refreshToken = refreshTokenRepository.findByUserKey(authentication.getName())
                .orElseThrow(() -> new BizException(MemberExceptionType.LOGOUT_MEMBER)); // ?????? ????????? ?????????


        // 4. Refresh Token ??????????????? ??????
        if (!refreshToken.getToken().equals(originRefreshToken)) {
            throw new BizException(JwtExceptionType.BAD_TOKEN); // ????????? ???????????? ????????????.
        }

        // 5. ????????? ?????? ??????
        String email = tokenProvider.getMemberEmailByToken(originAccessToken);
        User user = customUserDetailsService.getMember(email);

        String newAccessToken = tokenProvider.createAccessToken(email, user.getUserAuthorities());
        String newRefreshToken = tokenProvider.createRefreshToken(email, user.getUserAuthorities());
        TokenDTO tokenDto = tokenProvider.createTokenDTO(newAccessToken, newRefreshToken);

        log.debug("refresh Origin = {}",originRefreshToken);
        log.debug("refresh New = {} ",newRefreshToken);
        // 6. ????????? ?????? ???????????? (dirtyChecking?????? ????????????)
        refreshToken.updateToken(newRefreshToken);

        // ?????? ??????
        return tokenDto;
    }
}
