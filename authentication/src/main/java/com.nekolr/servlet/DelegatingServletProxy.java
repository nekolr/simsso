package com.nekolr.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * HttpServlet静态代理
 * <p>
 * 在应用中一般普通的POJO都是由Spring来管理的，所以自动注入不会产生问题。
 * 但是在Web应用中有两个东西都是由Servlet容器来维护管理的，一个是Filter，一个是Servlet。
 * Spring提供了org.springframework.web.filter.DelegatingFilterProxy 来代理Web容器中的Filter。
 */
public class DelegatingServletProxy extends HttpServlet {

    private String targetBean;
    private Servlet proxy;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        proxy.service(req, res);
    }

    @Override
    public void init() throws ServletException {
        this.targetBean = getServletName();
        this.getServletBean();
        proxy.init(getServletConfig());
    }

    private void getServletBean() {
        // 获取 当前Web应用下的 Spring Root 容器
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        // 这里需要web.xml配置的servlet-name和Servlet类上Component注解的value一致
        this.proxy = (Servlet) wac.getBean(targetBean);
    }
}
