package com.nekolr.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLDownLoader {

    /**
     * HTTP GET 请求
     *
     * @param urlStr
     * @return
     */
    public static String get(String urlStr) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader input = null;
        try {
            URL url = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Content-type", "application/json;charset=UTF-8");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();
            if (200 == httpURLConnection.getResponseCode()) {
                input = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
