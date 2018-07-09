package com.nekolr.util;

import java.util.UUID;

public class TokenGenerator {

    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
