<%--
  Created by IntelliJ IDEA.
  User: anatolij
  Date: 15/03/16
  Time: 13:48
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Input event info</title>
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
  <h3 style="background-color: #66cc00">Event named ${eventName} saved with id ${eventId}!</h3>
</g:if>

<g:form controller="event" action="saveEvent">
  <table>
    <tr>
      <td><label>Event Name:</label></td>
      <td>
        <g:textField name="eventName" value=""/>
      </td>
    </tr>
    <tr>
      <td><label>Event Start Date:</label></td>
      <td>
        <g:datePicker name="eventStart" value=""/>
      </td>
    </tr>
    <tr>
      <td><label>Event End Date:</label></td>
      <td>
        <g:datePicker name="eventEnd" value=""/>
      </td>
    </tr>
    <tr>
      <td><label>Event Radius:</label></td>
      <td>
        <g:textField name="eventRadius" value="1000"/>
      </td>
    </tr>
    <tr>
      <td><label>Event Longitude:</label></td>
      <td>
        <g:textField name="eventLng" value=""/>
      </td>
    </tr>
    <tr>
      <td><label>Event Latitude:</label></td>
      <td>
        <g:textField name="eventLat" value=""/>
      </td>
    </tr>
  </table>
  <g:submitButton name="submit" value="Save Event"/>
</g:form>

</body>
</html>