package com.side.fitflow.comm.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class MyUtil {

    // json 형식으로 유입된 HttpServletRequest를 JSOBObject 형태로 return
    public static JSONObject readJSONStringFromRequestBody(byte[] rawData) throws JsonProcessingException {
        StringBuffer json = new StringBuffer();
        ObjectMapper mapper = new ObjectMapper();
        String line = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(rawData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while((line = reader.readLine()) != null) {
                json.append(line);
            }

        }catch(Exception e) {
            e.printStackTrace();
            //log.info("Error reading JSON string: " + e.toString());
        }

        Map<String, String> map = mapper.readValue(json.toString(), Map.class);

        JSONObject jObj = new JSONObject(map);
        return jObj;
    }

}