<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:choose>
    <c:when test="${empty latestCommit}">
        <p>No latest commit yet.</p>
    </c:when>

    <c:otherwise>
        <p>SHA: <c:out value="${latestCommit.sha}" /></p>
        <p>Timestamp: <c:out value="${latestCommitTime}" /></p>
        <p>Status: <c:out value="${latestCommit.state}" /></p>
        <p>Note: <c:out value="${latestCommit.message}" /></p>
    </c:otherwise>
</c:choose>
