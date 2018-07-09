package com.nekolr.util;

import com.alibaba.fastjson.JSONArray;

import java.io.*;
import java.util.List;

public class JsonUtils {

    private static String readJsonFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream(fileName);
             BufferedInputStream input = new BufferedInputStream(inputStream)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "utf-8"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * JSON 字符串转实体类
     *
     * @param fileName 文件名
     * @param type     实体类的类型
     * @return
     */
    public static <T> List<T> json2Bean(String fileName, Class<T> type) {
        return JSONArray.parseArray(readJsonFile(fileName), type);
    }
}
