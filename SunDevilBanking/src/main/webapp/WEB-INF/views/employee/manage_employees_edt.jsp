<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="${pageContext.servletContext.contextPath}/static/favicon.ico">

    <title>SBS Group 11 System Admin Home</title>

    <!-- Bootstrap core CSS -->
    <link href="${pageContext.servletContext.contextPath}/static/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap theme -->
    <link href="${pageContext.servletContext.contextPath}/static/css/bootstrap-theme.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="${pageContext.servletContext.contextPath}/static/css/custom.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body role="document">

    <div class="container container-main" role="main">

      <div class="row">
        
        <div class="col-sm-3 col-md-2 sidebar">

          
<ul class="nav nav-sidebar">
             <li><a href="${pageContext.servletContext.contextPath}/admin/sysadmin-home">Home</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/admin/manage-employee">Add Employees</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/admin/systemLog-sys-admin">System Log</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/admin/requests-pending">Pending Requests</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/admin/sysadmin-setting">Settings</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/admin/sys-admin-PII">Access PII</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/logout">Logout</a></li>
          </ul>
          
          
        </div> <!-- sidebar -->

        <div class="col-sm-9 col-md-10 main">

          <div class="page-header">
            <h1>SBS - Welcome System Admin</h1>
          </div>

          
		  <br>
	
	<h2>Edit Settings:</h2>
		  <br>

	<div id="Manage_Employees">
		<form:form method="POST"   modelAttribute="user" action = "employee_success">
			
<div class="modal-body">
				<form:input type="hidden" path="customerID" id="customerID"/>
				<p>
					<label>First Name:</label>
					<form:input path="firstName" id="firstName"  minlength='2' maxlength='35' required='required'/>
				</p>
				<p>
					<label>Last Name:</label>
					<form:input path="lastName" id="lastName" minlength='3' maxlength='70' required='required'/>
				</p>
				<p>
					<label>Address:</label>
					<form:input path="addressLine1" id="addressLine1" type="text" 
						Class="form-control" placeholder="ex: 1009 E University Dr" minlength='5' maxlength='50' required='required' />
				</p>
				
				<p>
					<label>Email:</label>
					<label> ${email} </label>
				</p>
				
				<p>
					<label>Phone No:</label>
					<form:input path="phone"  id="phone" type="number" required='required'
						class="form-control" placeholder="ex:986-712-345" />
				</p>
				
				<p>
					<label>Zip Code:</label>
					<form:input path="zipCode" id="zipCode" type="number" required='required'
						class="form-control" placeholder="ex:85281" />
				</p>
			
				<p>
					<label>State:</label>
					<form:input path="state" id="state" type="text" minlength='2' maxlength='2' required='required'
						class="form-control" placeholder="AZ" />
				</p>	
				<p>
				<label>UserType:</label>
				<form:select path="userType" items="${userTypes}" class="form-control" />
				</p>
				<div class="modal-footer">
					<button type="submit" class="btn btn-success" name= "delete" value="delete">Delete</button>
				</div>
				<div class="modal-footer">
					<button type="submit" class="btn btn-success" name = "update" value = "update">Update</button>
				</div>
				
				<div id="virtualKeyboard"></div>
			</div>
		</form:form>
	    </div>
	  <div id="virtualKeyboard">        
      </div>

    </div> <!-- /container -->
    </div>
    </div>


    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/static/js/bootstrap.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/static/js/jsKeyboard.js"></script>
    <script src="${pageContext.servletContext.contextPath}/static/js/common.js"></script>
  </body>
</html>
		