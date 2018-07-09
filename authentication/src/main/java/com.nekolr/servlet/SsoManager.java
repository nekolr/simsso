package com.nekolr.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nekolr.db.UserInfoService;
import com.nekolr.model.UserInfo;
import com.nekolr.util.TokenGenerator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(value = "ssoManager")
public class SsoManager extends HttpServlet {
    /**
     * 存放在 cookie 中 token 的 key
     */
    private static final String SSO_COOKIE_NAME = "sim_sso";

    /**
     * 应用的 serverKey
     */
    private static final String APP_A = "appA";
    private static final String APP_B = "appB";

    @Resource
    private UserInfoService userInfoService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        switch (uri) {
            case "/":
                resp.sendRedirect("/login.jsp");
                break;
            case "/login":
                handleLogin(req, resp);
                break;
            case "/sso":
                handleSso(req, resp);
                break;
            case "/delete-session":
                deleteSession(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            case "/validate":
                handleValidate(req, resp);
                break;
        }
    }

    /**
     * 注销验证中心的用户登录
     *
     * @param req
     * @param resp
     */
    private void deleteSession(HttpServletRequest req, HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        String token = null;
        if (ObjectUtils.notEqual(cookies, null)) {
            // 删除 Cookie
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSO_COOKIE_NAME)) {
                    token = cookie.getValue();
                    cookie.setMaxAge(-1);
                    cookie.setValue(null);
                } else if (cookie.getName().equals("accountName")) {
                    cookie.setMaxAge(-1);
                    cookie.setValue(null);
                }
                resp.addCookie(cookie);
            }
        }
        if (!StringUtils.isEmpty(token)) {
            // 清空 token
            UserInfo userInfo = userInfoService.findUserInfoByToken(token);
            if (ObjectUtils.notEqual(userInfo, null)) {
                userInfoService.updateUserInfo(userInfo, null);
            }
        }
    }

    /**
     * 验证 token 的有效性
     *
     * @param req
     * @param resp
     */
    private void handleValidate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getParameter("token");
        String serverKey = req.getParameter("serverKey");
        UserInfo userInfo;
        if (!StringUtils.isEmpty(token) && !StringUtils.isEmpty(serverKey)) {
            // 验证 Token
            userInfo = userInfoService.findUserInfoByToken(token);
            // Token 有效
            if (ObjectUtils.notEqual(userInfo, null)) {
                // 根据 serverKey 过滤用户信息
                userInfo = filterByServerKey(userInfo, serverKey);
                // 返回用户信息
                resp.getWriter().write(JSON.toJSONString(userInfo));
                return;
            }
        }
    }

    /**
     * 跳转到登录页面，方便访问应用的注销
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/login.jsp?action=logout");
    }

    /**
     * 处理 SSO 请求
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    private void handleSso(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 一开始访问的站点
        String returnUrl = req.getParameter("returnUrl");

        Cookie[] cookies = req.getCookies();
        String token = null;
        if (ObjectUtils.notEqual(cookies, null)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSO_COOKIE_NAME)) {
                    if (cookie.getMaxAge() <= 0) {
                        resp.sendRedirect("/login.jsp?returnUrl=" + returnUrl);
                        return;
                    } else {
                        token = cookie.getValue();
                    }
                }
            }
        }
        if (!StringUtils.isEmpty(token) && ObjectUtils.notEqual(userInfoService.findUserInfoByToken(token), null)) {
            // cookie 中有 token，并且 token 是有效的，重定向至开始的站点
            resp.sendRedirect(returnUrl + "?token=" + token);
        } else {
            resp.sendRedirect("login.jsp?returnUrl=" + returnUrl);
        }
    }

    /**
     * 处理登录
     *
     * @param req
     * @param resp
     */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accountName = req.getParameter("accountName");
        // 拿到明文密码后，应该加密后与数据库中比对，此处省略（数据库也没有加密）
        String accountPwd = req.getParameter("accountPwd");

        UserInfo userInfo = userInfoService.findUserInfo(accountName, accountPwd);
        if (ObjectUtils.notEqual(userInfo, null)) {
            // 生成 Token（真实场景中应该使用账号拼接时间戳后加密生成）
            String token = TokenGenerator.getUuid();
            // 更新 UserInfo 的 token
            boolean result = userInfoService.updateUserInfo(userInfo, token);
            if (result) {
                // 写入 Token
                resp.addCookie(new Cookie(SSO_COOKIE_NAME, token));
                // 方便登录页面显示用户名
                resp.addCookie(new Cookie("accountName", accountName));
                // 此时需要重定向到一开始请求的站点，在前端处理
//                resp.sendRedirect(req.getParameter("returnUrl"));
                JSONObject responseText = new JSONObject();
                responseText.put("token", token);
                resp.getWriter().write(responseText.toJSONString());
                return;
            } else {
                resp.setStatus(500);
            }
        } else {
            resp.setStatus(422);
        }
    }

    /**
     * 通过 serverKey 过滤用户信息
     *
     * @param userInfo
     * @param serverKey
     * @return
     */
    private UserInfo filterByServerKey(UserInfo userInfo, String serverKey) {
        if (serverKey.equals(APP_A)) {
            // 应用 A 可以获取用户的账号、性别
            UserInfo returnUserInfo = new UserInfo();
            returnUserInfo.setAccountName(userInfo.getAccountName());
            returnUserInfo.setSex(userInfo.getSex());
            return returnUserInfo;
        } else if (serverKey.equals(APP_B)) {
            // 应用 A 可以获取用户的账号、年龄
            UserInfo returnUserInfo = new UserInfo();
            returnUserInfo.setAccountName(userInfo.getAccountName());
            returnUserInfo.setAge(userInfo.getAge());
            return returnUserInfo;
        } else {
            return null;
        }
    }
}
