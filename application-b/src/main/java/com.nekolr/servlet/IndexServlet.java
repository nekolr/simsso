package com.nekolr.servlet;

import com.nekolr.util.URLDownLoader;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "indexServlet", urlPatterns = "*.do")
public class IndexServlet extends HttpServlet {

    /**
     * SSO 验证服务地址
     * serverKey 作为每个应用的 key，方便 SSO 验证成功后返回每个应用需要的不同的用户信息
     */
    private static final String VALIDATION_URL = "http://sso.nekolr.com/validate?serverKey=appB&token=";

    /**
     * SSO 授权服务地址
     * returnUrl 作为授权成功后重定向的地址
     */
    private static final String SSO_URL = "http://sso.nekolr.com/sso?returnUrl=";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if ("/index.do".equals(uri)) {
            doHandle(req, resp);
        } else if ("/logout.do".equals(uri)) {
            doLogout(req, resp);
        }
    }

    private void doLogout(HttpServletRequest req, HttpServletResponse resp) {
        req.getSession().invalidate();
    }

    private void doHandle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String token = req.getParameter("token");
        HttpSession session = req.getSession();
        // 请求中有 token
        if (!StringUtils.isEmpty(token)) {
            // 请求 SSO 服务验证 Token 是否合法
            String result = URLDownLoader.get(VALIDATION_URL + token);
            if (!StringUtils.isEmpty(result)) {
                // 获取 SSO 服务返回的该应用可以使用的用户信息
                String accountName = (String) com.nekolr.util.StringUtils.getFromJsonString(result, "accountName");
                Integer age = (Integer) com.nekolr.util.StringUtils.getFromJsonString(result, "age");
                // 设置会话
                session.setAttribute("accountName", accountName);
                session.setAttribute("age", age);
                req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
                return;
            } else {
                // 没有结果，说明 Token 非法
                return;
            }
        } else if (ObjectUtils.notEqual(session.getAttribute("accountName"), null)) {
            // 有会话，则为已经授权成功
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
        } else {
            // 重定向至授权服务
            resp.sendRedirect(SSO_URL + getBasePath(req));
        }
    }

    /**
     * 获取服务基地址
     *
     * @param request
     * @return
     */
    private String getBasePath(HttpServletRequest request) {
        String returnUrl = request.getParameter("returnUrl");
        return returnUrl == null
                ? request.getScheme() +
                "://" + request.getServerName() +
                ":" + request.getServerPort() + request.getContextPath() + "/" : returnUrl;
    }
}
