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
    width: 500px;
  }
  </style>
</head>

<body>

<g:form controller="event" action="saveEvent">
  <table>
    <tr style="display: none">
      <g:textField name="id" value="${id}" style="display: none"/>
    </tr>
    <tr>
      <td><label>Event Name:</label></td>
      <td>
        <g:textField name="eventName" value="${eventName}"/>
      </td>
    </tr>
    <tr>
      <td><label>Event Start Date:</label></td>
      <td>
        <g:datePicker name="eventStart" value="${eventStart}"/>
      </td>
    </tr>
    <tr>
      <td><label>Event End Date:</label></td>
      <td>
        <g:datePicker name="eventEnd" value="${eventEnd}"/>
      </td>
    </tr>
    <tr>
      <td><label>Event Radius:</label></td>
      <td>
        <g:field type="number" name="eventRadius" value="${eventRadius ?: 100}"/>
      </td>
    </tr>
    <tr>
      <td><label>Event Longitude:</label></td>
      <td>
        <g:field type="decimal" name="eventLng" value="${eventLng}"/>
      </td>
    </tr>
    <tr>
      <td><label>Event Latitude:</label></td>
      <td>
        <g:field type="decimal" name="eventLat" value="${eventLat}"/>
      </td>
    </tr>
  </table>
  <g:submitButton name="submit" value="Save Event"/>
</g:form>

</body>
</html>