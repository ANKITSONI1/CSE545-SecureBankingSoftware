<%@ tag
	description="Page template for all pages when user has logged in"
	language="java" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon"
	href="${pageContext.servletContext.contextPath}/static/favicon.ico">

<title>${title} | Sun Devil Bank - Group 11</title>

<!-- Bootstrap core CSS -->
<link
	href="${pageContext.servletContext.contextPath}/static/css/bootstrap.min.css"
	rel="stylesheet">
<!-- Bootstrap theme -->
<link
	href="${pageContext.servletContext.contextPath}/static/css/bootstrap-theme.min.css"
	rel="stylesheet">

<!-- Custom styles for this template -->
<link
	href="${pageContext.servletContext.contextPath}/static/css/custom.css"
	rel="stylesheet">

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
					<li
						class="${fn:endsWith(pageContext.request.requestURI, 'home.jsp') ? 'active':''}"><a
						href="#">Home</a></li>
					<li
						class="${fn:endsWith(pageContext.request.requestURI, 'creditdebit.jsp') ? 'active':''}"><a
						href="#">Credit/Debit</a></li>
					<li class="${fn:endsWith(pageContext.request.requestURI, 'fundtransfer.jsp') ? 'active':''}"><a
						href="">Fund Transfer</a></li>
					<li class="${fn:endsWith(pageContext.request.requestURI, 'payments.jsp') ? 'active':''}"><a
						href="">Payments</a></li>
					<li class="${fn:endsWith(pageContext.request.requestURI, 'statements.jsp') ? 'active':''}"><a
						href="">Statements</a></li>
					<li class="${fn:endsWith(pageContext.request.requestURI, 'settings.jsp') ? 'active':''}"><a
						href="">Settings</a></li>
					<li class="${fn:endsWith(pageContext.request.requestURI, 'logout.jsp') ? 'active':''}"><a
						href="">Logout</a></li>
				</ul>

			</div>
			<!-- sidebar -->

			<div class="col-sm-9 col-md-10 main">

				<jsp:doBody />

			</div>
			<!-- /main -->

		</div>
		<!-- /row -->

	</div>
	<!-- /container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script
		src="${pageContext.servletContext.contextPath}/static/js/bootstrap.min.js"></script>
	<script
		src="${pageContext.servletContext.contextPath}/static/js/common.js"></script>
</body>
</html>
