<%--
  Created by IntelliJ IDEA.
  User: anatolij
  Date: 15/03/16
  Time: 13:48
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Login page for event admin</title>
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
<h3>Please authenticate yourself!</h3>
<g:form controller="event" action="performAuth">
  <table>
    <tr>
      <td><label>Name:</label></td>
      <td>
        <g:textField name="name" value="${name}" disabled="disabled"/>
      </td>
    </tr>
    <tr>
      <td><label>Email:</label></td>
      <td>
        <g:textField name="email" value=""/>
      </td>
    </tr>
    <tr>
      <td><label>Password:</label></td>
      <td>
        <g:passwordField value="" name="password"/>
      </td>
    </tr>
    <tr>
      <g:submitButton name="submit" value="Continue"/>
    </tr>
  </table>

</g:form>

</body>
</html>