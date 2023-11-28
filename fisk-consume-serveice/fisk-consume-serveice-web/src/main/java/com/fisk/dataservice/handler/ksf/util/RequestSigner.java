package com.fisk.dataservice.handler.ksf.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.security.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import javax.net.ssl.HttpsURLConnection;

public class RequestSigner {
//    public static void main(String[] args) throws IOException {
//        String source_sys_id = "KSF";
//        String secret = "b2ec161205631efcdf62318959e9b036";
//        String url = "https://esbapit.wilmar.cn/v1/example-api/example?id=1";
//        String json = "";
//        Map<String, Object> body = (Map<String, Object>) JSON.parse(json);
//        URI uri = URI.create(url);
//        Map<String, String> urlParams = getParamsFromURI(uri);
//        System.out.println("uuid:"+UUID.randomUUID());
//
//        Map<String, Object> params = new LinkedHashMap<>();
//        params.put("source_sys_id", source_sys_id);
//        String l = String.valueOf(System.currentTimeMillis());
//        System.out.println("时间戳:"+l);
//        params.put("submit_time", l);
//        StringBuilder formattedString = new StringBuilder();
//
//        if (body != null){
//            formattedString.append("body=");
//            params.put("body", body);
//        }
//        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
//            params.put(entry.getKey(),entry.getValue());
//        }
//
//        // 将body转换成json字符串
//        ObjectMapper mapper = new ObjectMapper();
//        String bodyJson = "";
//        try {
//            bodyJson = mapper.writeValueAsString(params);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String stringA = convertJsonToFormattedString(bodyJson,formattedString);
//        String stringSignTemp = stringA+secret;
//        String sign = DigestUtils.sha256Hex(stringSignTemp).toUpperCase();
//        System.out.println(sign);
//    }

    // 计算SHA-256
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertJsonToFormattedString(String jsonBody,StringBuilder formattedString) {
        JSONObject jsonObject = JSON.parseObject(jsonBody);
        JSONObject jsonObject1 = sortJSONObject(jsonObject);
        traverseJson( jsonObject1, formattedString);
        String formatString = formattedString.toString().substring(0, formattedString.length() - 1);
        return formatString;
    }

    public static void traverseJson(JSONObject jsonObject, StringBuilder formattedString) {
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (ObjectUtils.isEmpty(value)){
                continue;
            }
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                formattedString.append(key).append("=");
                for (Object item : jsonArray) {
                    if (item instanceof JSONObject) {
                        traverseJson( (JSONObject) item, formattedString);
                    } else {
                        formattedString.append(item).append("&");
                    }
                }
            } else if (value instanceof JSONObject) {
                traverseJson( (JSONObject) value, formattedString);
            } else {
                formattedString.append(key).append("=").append(value).append("&");
            }
        }
    }


    public static JSONObject sortJSONObject(JSONObject jsonObject) {
        Map<String, Object> map = new TreeMap<>(jsonObject.getInnerMap());

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                entry.setValue(sortJSONObject((JSONObject) entry.getValue()));
            } else if (entry.getValue() instanceof JSONArray) {
                entry.setValue(sortJSONArray((JSONArray) entry.getValue()));
            }
        }

        return new JSONObject(map);
    }

    public static JSONArray sortJSONArray(JSONArray jsonArray) {
        List<Object> list = new ArrayList<>(jsonArray);
        List<Object> sortedList = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof JSONObject) {
                sortedList.add(sortJSONObject((JSONObject) obj));
                Collections.sort(sortedList, Comparator.comparing(Object::toString));
            }else {
                sortedList.add(obj);
            }
        }
        JSONArray sortedJsonArray = new JSONArray();
        sortedJsonArray.addAll(sortedList);

        return sortedJsonArray;
    }

    public static Map<String, String> getParamsFromURI(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }
}
