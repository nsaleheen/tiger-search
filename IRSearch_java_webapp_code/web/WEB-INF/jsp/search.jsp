<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.List" %>

<%
    List<String> results = (List<String>) request.getAttribute("result");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>IR search</title>
</head>
<body>
<br><br><br><br><br>
<div align="center">
    <form action="<%=application.getContextPath()%>/search.do" method="post">
        <input type="text" name="searchText">
        <input type="submit" value="Search">
    </form>
</div>
<br>
<% if (results == null || results.size() == 0) { %>
<p><h3>No result found...</h3></p>
<% } else { %>
    <p><h3>Relevant webpages:</h3></p>
    <% for (String s : results) {%>
        <P><a href="<%=s %>"><%=s %>
        </a></P>
    <%}
} %>
</body>
</html>