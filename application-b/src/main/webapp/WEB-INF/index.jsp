<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>应用B的首页</title>
</head>
<body>
<h2>欢迎你，<%=session.getAttribute("accountName")%>&nbsp;&nbsp;<a id="invalidate" href="http://sso.nekolr.com/logout">注销</a></h2>
<h3>年龄：<%=session.getAttribute("age")%>
</h3>
</body>
</html>
