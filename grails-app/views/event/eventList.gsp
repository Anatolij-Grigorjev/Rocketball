<%--
  Created by IntelliJ IDEA.
  User: anatolij
  Date: 16/03/16
  Time: 08:44
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>List of existing events</title>
  <style>
  tr + td {
    text-align: right;
  }

  td + td {
    text-align: left;
    width: 300px;
  }
  </style>
</head>

<body>

<g:if test="${eventName}">
  <g:if test="${eventAction == 'save'}">
    <h3 style="background-color: #66cc00">Event named ${eventName} saved with id ${eventId}!</h3>
  </g:if>
  <g:if test="${eventAction == 'del'}">
    <h3 style="background-color: #ff8888">Event named ${eventName} deleted with id ${eventId}!</h3>
  </g:if>
</g:if>

<table border="1px solid black">
  <tr>
    <td>Id</td>
    <td>Event Name</td>
    <td>Valid From</td>
    <td>Valid Till</td>
    <td>Edit</td>
    <td>Delete</td>
  </tr>
  <g:each in="${events}" var="event">
    <tr>
      <td>${event.id}</td>
      <td>${event.eventName}</td>
      <td>${event.eventStart}</td>
      <td>${event.eventEnd}</td>
      <td><g:link controller="event" action="makeForm" params="${[id: event.id]}">EDIT</g:link></td>
      <td><g:link controller="event" action="delete" params="${[id: event.id]}">DELETE</g:link></td>
    </tr>
  </g:each>
</table>
<g:link controller="event" action="makeForm">Add New Event</g:link>
</body>
</html>