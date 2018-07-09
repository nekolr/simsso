<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>login</title>
    <script>
        /**
         * 获取 URL 中的参数
         * @param name
         * @param url
         * @returns {*}
         */
        function param(name, url) {
            if (!url) url = window.location.href;
            name = name.replace(/[\[\]]/g, "\\$&");
            var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
                results = regex.exec(url);
            if (!results) return null;
            if (!results[2]) return '';
            return decodeURIComponent(results[2].replace(/\+/g, " "));
        }

        /**
         * 处理 returnUrl
         * @param token
         */
        function redirectToReturnUrl(token) {
            var returnUrl = param('returnUrl');
            if (returnUrl) {
                returnUrl = decodeURIComponent(returnUrl);
                var connectChar = returnUrl.indexOf('?') > -1 ? '&' : '?';
                return returnUrl + connectChar + 'token=' + token;
            }
        }

        /**
         * 获取 Cookie
         * @param cname
         * @returns {string}
         */
        function getCookie(cname) {
            var name = cname + "=";
            var decodedCookie = decodeURIComponent(document.cookie);
            var ca = decodedCookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) == ' ') {
                    c = c.substring(1);
                }
                if (c.indexOf(name) == 0) {
                    return c.substring(name.length, c.length);
                }
            }
            return "";
        }
    </script>
</head>
<body>
<form action="/login" method="post">
    账号：<input type="text" id="accountName" name="accountName"/>
    密码：<input type="password" id="accountPwd" name="accountPwd"/>
    <button type="button" id="btn">确定</button>
    <iframe style="display:none;" id="appA"></iframe>
    <iframe style="display:none;" id="appB"></iframe>
</form>
</body>
<script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.min.js"></script>
<script>
    $(function () {
        // 登录事件
        $("#btn").on("click", function () {
            $.ajax({
                url: $("form").get(0).action,
                type: "POST",
                dataType: "json",
                contentType: "application/x-www-form-urlencoded",
                async: true,
                data: {
                    accountName: $("#accountName").val(),
                    accountPwd: $("#accountPwd").val()
                },
                success: function (msg) {
                    window.location.href = redirectToReturnUrl(msg.token);
                },
                error: function (msg) {
                    console.log(msg);
                }
            });
        });
        // 注销
        if (param("action") === "logout") {
            // 因为子站有不是 https 的站点，无法 https 内嵌 http 的 iframe，必须先自己跳转到非 https，注销完成后，再跳转回 https
            // 缺陷很明显，服务器不能强制 HTTPS
            if (window.location.protocol == 'https:') {
                var location = window.location.href.replace('https://', 'http://');
                if (param('action') !== 'logout') {
                    var concatChar = location.indexOf('?') > -1 ? '&' : '?';
                    location = location + concatChar + 'action=logout';
                }
                window.location.href = location;
                return;
            }
            $.ajax({
                url: "/delete-session",
                type: "POST",
                contentType: "application/x-www-form-urlencoded",
                async: true,
                success: function (msg) {
                    document.querySelector("#appA").src = "http://www.app-a.com:8001/logout.do";
                    document.querySelector("#appB").src = "http://www.app-b.com:8002/logout.do";
                    window.location.href = "/login.jsp";
                },
                error: function (msg) {
                    console.log(msg);
                }
            });
        }
        // 有 Cookie 跳转到应用
        if (getCookie("sim_sso") && getCookie("sim_sso") != "\"\"") {
            window.location.href = redirectToReturnUrl(getCookie("sim_sso"));
        }
    })
</script>
</html>
