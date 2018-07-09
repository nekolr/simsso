package com.nekolr.db;

import com.nekolr.model.UserInfo;
import com.nekolr.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class UserInfoServiceImpl implements UserInfoService {

    private static List<UserInfo> userInfoList;

    @Override
    public UserInfo findUserInfo(String accountName, String accountPwd) {
        return userInfoList.stream()
                .filter(user -> user.getAccountName().equals(accountName) && user.getAccountPwd().equals(accountPwd))
                .findFirst()
                .orElse(null);
    }

    @Override
    public UserInfo findUserInfoByToken(String token) {
        return userInfoList.stream()
                .filter(user -> token.equals(user.getToken()))
                .findFirst()
                .orElse(null);
    }


    /**
     * 需要考虑高并发下的情况
     *
     * @param userInfo
     * @param token
     * @return
     */
    @Override
    public boolean updateUserInfo(UserInfo userInfo, String token) {
        for (Iterator<UserInfo> iterator = userInfoList.iterator(); iterator.hasNext(); ) {
            UserInfo tmpUserInfo = iterator.next();
            if (userInfo.getUserId().equals(tmpUserInfo.getUserId())) {
                tmpUserInfo.setToken(token);
                return true;
            }
        }
        return false;
    }

    static {
        userInfoList = JsonUtils.json2Bean("db.json", UserInfo.class);
    }

}
