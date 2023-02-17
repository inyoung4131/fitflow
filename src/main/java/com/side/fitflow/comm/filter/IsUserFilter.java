package com.side.fitflow.comm.filter;

import com.side.fitflow.jwt.JwtTokenProvider;
import com.side.fitflow.comm.converter.usergrade.UserGrade;
import com.side.fitflow.user.component.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
@WebFilter
public class IsUserFilter extends OncePerRequestFilter {
    private final LoginUser loginUser;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getServletPath().startsWith("/post/img")&&!request.getServletPath().startsWith("/favicon")) {
            log.info("**IsUserFilter**");
            String token = "";
            if (request.getCookies() != null) {
                token = Arrays.stream(request.getCookies()).filter(e -> e.getName().equals("fitflow")).findAny().orElse(new Cookie("fitflow", "")).getValue();
            }
            if (!request.getServletPath().startsWith("/auth")) {
                if (StringUtils.hasText(token)) {
                    int flag = jwtTokenProvider.validateToken(token);
                    // 토큰 유효함
                    if (flag == 1) {
                        loginUser.setUserEmail(jwtTokenProvider.getMemberEmailByToken(token));
                        loginUser.setUserGrade(jwtTokenProvider.getAuthenticationToUserGrade(token));
                        filterChain.doFilter(request, response);
                    } else if (flag == 2) { // 토큰 만료
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setCharacterEncoding("UTF-8");
                        response.sendRedirect("/auth/reissue-page?path=" + request.getServletPath());
                        PrintWriter out = response.getWriter();
                        log.debug("doFilterInternal Exception CALL!");
                        out.println("{\"error\": \"ACCESS_TOKEN_EXPIRED\", \"message\" : \"엑세스토큰이 만료되었습니다.\"}");
                    } else {
                        filterChain.doFilter(request, response);
                    }
                } else {
                    loginUser.setUserEmail("GUEST");
                    loginUser.setUserGrade(List.of(UserGrade.GUEST));
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
