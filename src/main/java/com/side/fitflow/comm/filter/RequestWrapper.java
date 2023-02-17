package com.side.fitflow.comm.filter;

import com.side.fitflow.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {
    private byte[] rawData;
    private byte[] newData;
    private JSONObject jsonObject;
    private final JwtTokenProvider jwtTokenProvider;

    public RequestWrapper(HttpServletRequest request, JwtTokenProvider jwtTokenProvider) throws IOException {
        super(request);
        this.jwtTokenProvider = jwtTokenProvider;
        InputStream inputStream = request.getInputStream();
        this.rawData = inputStream.readAllBytes();

        try {
            log.info("URL : {}", request.getRequestURI());

            jsonObject = MyUtil.readJSONStringFromRequestBody(rawData);

            setModAndCreateIp();
            setModAndCreateId(getUserEmailByToken(request));

            newData = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bis = new ByteArrayInputStream(newData);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletRequest getRequest() {
        return super.getRequest();
    }

    public void setModAndCreateIp(){
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = req.getRemoteAddr();
        }
        jsonObject.put("modIp",ip);
        jsonObject.put("createIp",ip);
    }

    public void setModAndCreateId(String userId){
        jsonObject.put("modId",userId);
        jsonObject.put("createId",userId);
    }

    private String getUserEmailByToken(HttpServletRequest request){
        Cookie[] cookie = request.getCookies();
        for(Cookie temp : cookie){
            if (temp.getName().equals("fitflow")){
                return jwtTokenProvider.getMemberEmailByToken(temp.getValue());
            }
        }
        return "GUEST";
    }
}
