package com.side.fitflow.comm.filter;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ReissueWrapper extends HttpServletRequestWrapper {
    private byte[] rawData;
    private byte[] newData;
    private JSONObject jsonObject;

    public ReissueWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream inputStream = request.getInputStream();
        this.rawData = inputStream.readAllBytes();

        try {
            jsonObject = MyUtil.readJSONStringFromRequestBody(rawData);

            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("fitflow")) {
                    jsonObject.put("accessToken",cookie.getValue());
                } else if (cookie.getName().equals("_fitflow")) {
                    jsonObject.put("refreshToken",cookie.getValue());
                }
            }

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
}
