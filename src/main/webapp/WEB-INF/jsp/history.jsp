<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>History</title>
</head>
<body>
  <h1>History</h1>

  <c:choose>
    <c:when test="${empty historyList}">
      <p>No history yet.</p>
    </c:when>

    <c:otherwise>
      <ul>
        <c:forEach var="commitInfo" items="${historyList}">
          <li>
            <strong><a href="/commit/${commitInfo['sha']}"><c:out value="${commitInfo['sha']}" /></a></strong>
            — <c:out value="${commitInfo['build-date']}" />
            <c:out value="${commitInfo['build-logs']}" />
          </li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
</body>
</html>
