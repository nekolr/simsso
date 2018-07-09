package com.nekolr.util;

import com.alibaba.fastjson.JSON;

public class StringUtils {

    public static Object getFromJsonString(String jsonString, String key) {
        return JSON.parseObject(jsonString).get(key);
    }
}
