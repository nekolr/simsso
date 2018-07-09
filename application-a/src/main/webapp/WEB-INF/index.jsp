<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>应用A的首页</title>
</head>
<body>
<h2>欢迎你，<%=session.getAttribute("accountName")%>&nbsp;&nbsp;<a id="invalidate" href="http://sso.nekolr.com/logout">注销</a></h2>
<h3>性别：<%=session.getAttribute("sex")%>
</h3>
</body>
</html>
