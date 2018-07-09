package com.nekolr.db;

import com.nekolr.model.UserInfo;

public interface UserInfoService {

    UserInfo findUserInfo(String accountName, String accountPwd);

    UserInfo findUserInfoByToken(String token);

    boolean updateUserInfo(UserInfo userInfo, String token);
}
