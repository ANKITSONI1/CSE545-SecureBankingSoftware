<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:auth>
	
	<form class="form-signin">
        <h2 class="form-signin-heading">Please Login</h2>
        <label for="inputEmail" class="sr-only">Email address</label>
        <input type="text" id="inputEmail" class="form-control" placeholder="Email address" required="" autofocus="" autocomplete="off">
        <label for="inputPassword" class="sr-only">Password</label>
        <input type="password" id="inputPassword" class="form-control" placeholder="Password" required="" autocomplete="off">
        <div class="g-recaptcha" data-sitekey="6LcQrwwTAAAAAP1rFCMhODCuHWbbkgC9mJ2Qm6gz"></div>
        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>

        <p class="center"><a href="forgotpass">Forgot Password?</a></p>
    </form>

    <div id="virtualKeyboard"></div>
    
</t:auth>