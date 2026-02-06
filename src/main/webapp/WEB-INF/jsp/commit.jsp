<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<p>SHA: <c:out value="${commitInfo['sha']}" /></p>
<p>Build date: <c:out value="${commitInfo['build-date']}" /></p>
<p>Logs: <c:out value="${commitInfo['build-logs']}" /></p>
