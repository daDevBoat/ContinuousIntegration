<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:choose>
    <c:when test="${empty latestCommit}">
        <p>No latest commit yet.</p>
    </c:when>

    <c:otherwise>
        <p>SHA: <c:out value="${latestCommit.sha}" /></p>
        <p>Timestamp: <c:out value="${latestCommit.time}" /></p>
        <p>Status: <c:out value="${latestCommit.state}" /></p>

        <p>Logs:</p>
        <c:choose>
            <c:when test="${empty latestCommit.logs}">
                <p>No logs</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="log" items="${latestCommit.logs}">
                    <p><c:out value="${log}" /></p>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
