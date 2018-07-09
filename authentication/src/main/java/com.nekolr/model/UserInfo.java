package com.nekolr.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfo {
    private String userId;
    private String userName;
    private String sex;
    private Integer age;
    private String accountName;
    private String accountPwd;
    private String token;
}
