package com.side.fitflow.jwt;

import com.side.fitflow.comm.exception.BizException;
import com.side.fitflow.comm.exception.MemberExceptionType;
import com.side.fitflow.user.entity.Authority;
import com.side.fitflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws BizException {
        log.debug("CustomUserDetailsService -> email = {}",email);
        return userRepository.findByUserId(email)
                .map(this::createUserDetails)
                .orElseThrow(() -> new BizException(MemberExceptionType.NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public com.side.fitflow.user.entity.User getMember(String email) throws BizException {
        return userRepository.findByUserId(email)
                .orElseThrow(()->new BizException(MemberExceptionType.NOT_FOUND_USER));
    }

    // DB 에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(com.side.fitflow.user.entity.User user) {
        // Collections<? extends GrantedAuthority>
        List<SimpleGrantedAuthority> authList = user.getUserAuthorities()
                .stream()
                .map(Authority::getAuthorityName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        authList.forEach(o-> log.debug("authList -> {}",o.getAuthority()));

        return new User(
                user.getUserEmail(),
                user.getUserPwd(),
                authList
        );
    }
}
