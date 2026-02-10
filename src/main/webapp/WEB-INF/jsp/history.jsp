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
        <c:forEach var="commit" items="${historyList}">
          <li>
            <strong><a href="/commit/${commit.sha}"><c:out value="${commit.sha}" /></a></strong>
            | <c:out value="${commit.state}" />
            | <c:out value="${commit.time}" />
          </li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
</body>
</html>
