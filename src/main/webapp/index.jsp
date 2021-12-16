<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World!" %>
</h1>
<br/>
<a href="hello-servlet">Hello Servlet (刪除資料庫並重建)</a>
<br/>
<br/>
<br/>
<button type="button" onclick="
            if (window.location.pathname.slice(-10) === '/index.jsp') {
                window.location = window.location.pathname.substr(0, window.location.length - 9) + 'views/StockInfo/index.html'
            } else if (window.location.pathname.slice(-1) === '/') {
                window.location = window.location.pathname + 'views/StockInfo/index.html'
            }
            else{
                window.location = window.location.pathname + '/views/StockInfo/index.html'
            }
            "> StockInfo 列表畫面</button>
<br/>
</body>
</html>