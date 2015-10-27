<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>

<t:page>

	<div class="page-header">
		<h1>View Payment Requests</h1>
	</div>

	<div id="payment-requests">
		<table class="table table-bordered">
    	<thead>
        	<tr>
            	<th width="40%">Payment Request By:</th>
            	<th>Amount</th>
            	<th>Type</th>
            	<th>Action (Please enter otp sent in the email to approve the payment)</th>
        	</tr>
    	</thead>
        <tbody>
        	<c:forEach items="${paymentrequests}" var="payment">
        	<tr>
        		<td>Name: ${fn:escapeXml(payment.customerName)} <br> Account No: ${fn:escapeXml(payment.customerAccNumber)}</td>
        		<td>$${fn:escapeXml(payment.amount)}</td>
        		<td>Debit</td>
        		<td>
        			<c:choose>
        				<c:when test="${payment.OTPExpiry le currentTime}">
        					Payment has expired
        				</c:when>
        				<c:otherwise>
        				<form:form action="merchant-payment-requests" method="POST" modelAttribute="transaction">
        					<input type="text" name="otp" value="" class="form-control" placeholder="OTP (required to approve)">
		        			<br><button type="submit" name="submit" value="accept" class="btn btn-success">Accept</button>		        			
		        			<input type="hidden" name="paymentrequest" value="${payment.id}">
		        			<button type="submit" name="submit" value="decline" class="btn btn-danger">Decline</button>		        				        				
        				</form:form>
        				</c:otherwise>
        			</c:choose>
        		</td>
        	</tr>
        	</c:forEach>
        </tbody>
        </table>
	</div>
	
</t:page>