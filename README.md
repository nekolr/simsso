# 设置
- 修改 hosts，加入如下配置。
```
127.0.0.1 sso.nekolr.com
127.0.0.1 www.app-a.com
127.0.0.1 www.app-b.com
```
- 修改 nginx 配置文件 nginx.conf，加入如下配置。
```
# authentication server
#
server {
    listen       80;
    server_name  sso.nekolr.com;

    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout  5m;

    ssl_ciphers  HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers  on;

    location / {
        proxy_pass http://127.0.0.1:8080;
    }

    error_page  404              /404.html;
}

# appA server
#
server {
    listen       8001;
    server_name  www.app-a.com;

    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout  5m;

    ssl_ciphers  HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers  on;

    location / {
        proxy_pass http://127.0.0.1:8081;
    }

    error_page  404              /404.html;
}

# appB server
#
server {
    listen       8002;
    server_name  www.app-b.com;

    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout  5m;

    ssl_ciphers  HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers  on;

    location / {
        proxy_pass http://127.0.0.1:8082;
    }

    error_page  404              /404.html;
}
```

# 单点登录步骤

- 1. 用户未登录时访问子站 A，子站 A 服务器检测到用户没登录（没有本站 session），于是通知浏览器跳转到 SSO 服务站点，并在跳转的 URL 参数中带上当前页面地址，以便登录后自动跳转回本页。  

- 2. SSO 服务站点检测到用户没有登录，于是显示登录界面。  

  - 1.1. 用户提交登录请求到服务端，服务端验证通过，创建对应的用户登录凭据（token）。然后服务端通知浏览器把该 token 作为 SSO 服务站点的 cookie 存储起来，并将 token 作为参数带着跳转回子站 A。
  - 1.2. 子站 A 服务端检测到浏览器请求的 URL 中带了单点登录的 token，于是把这个 token 发到 SSO 服务站点验证（不能随便加上一个 token 就让通过是吧）。  
  
- 3. SSO 服务端拿 token 查询出用户信息，把账号信息中允许子站 A 访问的部分信息返回给子站 A。  

- 4. 子站 A 根据返回的信息生成用户在本站的会话，把会话对应 cookie 写入浏览器，从而完成在本站的登入以及会话的保持。之后用户再次访问子站 A 时，都会带上这个 cookie，从而保持在本站的登录状态。  

- 5. 用户再访问子站 B，子站 B 的服务器检测到用户没登录，于是通知浏览器跳转到 SSO 服务站点。  

- 6. 浏览器访问 SSO 服务站点时会带上上述创建的包含 token 的 cookie。SSO 服务站点根据该 token 能找到对应用户，于是通知浏览器跳转回子站 B，并在跳转回去的 URL 参数中带上这个 token。  

- 7. 子站 B 服务端检测到浏览器请求的 URL 中带上了单点登录的 token，于是又会走上述对应步骤验证 token，完成用户在本站的自动登录。  
