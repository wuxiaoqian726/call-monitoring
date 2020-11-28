package com.raymond.callmonitoring.common;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

public class JSONUtils {

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj).toString();
    }

    public static byte[] toJsonByte(Object obj) {
        return JSON.toJSONString(obj).toString().getBytes(Charset.forName("UTF-8"));
    }

    public static <T> T toObject(String json, Class<T> clazz){
        return JSON.parseObject(json,clazz);
    }
}
