package com.side.fitflow;

import com.dntwk.comm.converter.usergrade.UserGrade;
import com.dntwk.comm.exception.AuthorityExceptionType;
import com.dntwk.comm.exception.BizException;
import com.dntwk.comm.exception.JwtExceptionType;
import com.dntwk.comm.exception.MemberExceptionType;
import com.dntwk.jwt.CustomEmailPasswordAuthToken;
import com.dntwk.jwt.CustomUserDetailsService;
import com.dntwk.jwt.JwtTokenProvider;
import com.dntwk.jwt.dto.TokenDTO;
import com.dntwk.jwt.dto.TokenReqDTO;
import com.dntwk.jwt.entity.RefreshToken;
import com.dntwk.jwt.repository.RefreshTokenRepository;
import com.dntwk.user.dto.CreateUserDTO;
import com.dntwk.user.dto.LoginDTO;
import com.dntwk.user.dto.UserDTO;
import com.dntwk.user.entity.Authority;
import com.dntwk.user.entity.User;
import com.dntwk.user.repository.AuthorityRepository;
import com.dntwk.user.repository.UserRepository;
import com.side.fitflow.jwt.JwtTokenProvider;
import com.side.fitflow.jwt.repository.RefreshTokenRepository;
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
        if (userRepository.existsByUserId(createUserDTO.getUserId())) {
            throw new BizException(MemberExceptionType.DUPLICATE_USER);
        }

        // DB 에서 ROLE_USER를 찾아서 권한으로 추가한다.
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
         *  accessToken 은 JWT Filter 에서 검증되고 옴
         * */
        String originAccessToken = tokenRequestDto.getAccessToken();
        String originRefreshToken = tokenRequestDto.getRefreshToken();

        // refreshToken 검증
        int refreshTokenFlag = tokenProvider.validateToken(originRefreshToken);
        log.debug("refreshTokenFlag = {}", refreshTokenFlag);

        //refreshToken 검증하고 상황에 맞는 오류를 내보낸다.
        if (refreshTokenFlag == -1) {
            throw new BizException(JwtExceptionType.BAD_TOKEN); // 잘못된 리프레시 토큰
        } else if (refreshTokenFlag == 2) {
            throw new BizException(JwtExceptionType.REFRESH_TOKEN_EXPIRED); // 유효기간 끝난 토큰
        }

        // 2. Access Token 에서 Member Email 가져오기
        Authentication authentication = tokenProvider.getAuthentication(originAccessToken);

        log.debug("Authentication = {}",authentication);

        // 3. 저장소에서 Member Email 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByUserKey(authentication.getName())
                .orElseThrow(() -> new BizException(MemberExceptionType.LOGOUT_MEMBER)); // 로그 아웃된 사용자


        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getToken().equals(originRefreshToken)) {
            throw new BizException(JwtExceptionType.BAD_TOKEN); // 토큰이 일치하지 않습니다.
        }

        // 5. 새로운 토큰 생성
        String email = tokenProvider.getMemberEmailByToken(originAccessToken);
        User user = customUserDetailsService.getMember(email);

        String newAccessToken = tokenProvider.createAccessToken(email, user.getUserAuthorities());
        String newRefreshToken = tokenProvider.createRefreshToken(email, user.getUserAuthorities());
        TokenDTO tokenDto = tokenProvider.createTokenDTO(newAccessToken, newRefreshToken);

        log.debug("refresh Origin = {}",originRefreshToken);
        log.debug("refresh New = {} ",newRefreshToken);
        // 6. 저장소 정보 업데이트 (dirtyChecking으로 업데이트)
        refreshToken.updateToken(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }
}
