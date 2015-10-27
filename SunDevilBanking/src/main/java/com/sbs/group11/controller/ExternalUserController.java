package com.sbs.group11.controller;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbs.group11.model.Account;
import com.sbs.group11.model.PaymentRequest;
import com.sbs.group11.model.StatementMonthYear;
import com.sbs.group11.model.Transaction;
import com.sbs.group11.model.User;
import com.sbs.group11.service.AccountService;
import com.sbs.group11.service.OTPService;
import com.sbs.group11.service.SendEmailService;
import com.sbs.group11.service.TransactionService;
import com.sbs.group11.service.UserService;

/**
 * UserController: Controls most of our application UI paths for all types of
 * users including internal users.
 * 
 * Matches all urls with the path /home/**
 * 
 * @author Rahul
 */
@Controller
@RequestMapping(value = "/home")
public class ExternalUserController {

	final static Logger logger = Logger.getLogger(ExternalUserController.class);
	final private BigDecimal CRITICAL_VALUE = new BigDecimal(500);

	@Autowired
	private UserService userService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private OTPService otpService;

	@Autowired
	private SendEmailService emailService;

	@Autowired
	SmartValidator validator;

	/**
	 * Gets the home.
	 *
	 * @param model
	 *            the model
	 * @return the home
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHome(ModelMap model) {
		User user = userService.getUserDetails();
		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("title", "Welcome " + user.getFirstName());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);
		return "customer/home";
	}

	/**
	 * Gets the credit and debit page.
	 *
	 * @param model
	 *            the model
	 * @return the credit debit
	 */
	@RequestMapping(value = "/credit-debit", method = RequestMethod.GET)
	public String getCreditDebit(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("title", "Welcome " + user.getFirstName());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);
		return "customer/creditdebit";
	}

	/**
	 * Post credit debit.
	 *
	 * @param model
	 *            The Spring ModelMap
	 * @param request
	 *            HttpServlet request
	 * @param transaction
	 *            ModelAttribute transaction
	 * @param result
	 *            the BindingResult used for validation
	 * @param attr
	 *            RedirectAttribute used to pass error/success messages
	 * @return the string
	 */
	@RequestMapping(value = "/credit-debit", method = RequestMethod.POST)
	public String postCreditDebit(ModelMap model, HttpServletRequest request,
			@ModelAttribute("transaction") Transaction transaction,
			BindingResult result, RedirectAttributes attr) {

		// Get user details
		User user = userService.getUserDetails();
		model.put("user", user);

		// Get user accounts and other data for display
		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);
		model.addAttribute("title", "Welcome " + user.getFirstName());

		BigDecimal amount = transactionService.getBigDecimal(request
				.getParameter("amount"));

		String isCritical = transactionService.isCritical(amount,
				CRITICAL_VALUE);

		// create the transaction object
		transaction = new Transaction(
				transactionService.getUniqueTransactionID(), "Self "
						+ request.getParameter("type"),
				request.getParameter("number"), request.getParameter("number"),
				"pending", request.getParameter("type"), amount, isCritical,
				request.getParameter("number"));

		// Validate the model
		validator.validate(transaction, result);
		if (result.hasErrors()) {
			logger.debug(result);

			// attributes for validation failures
			attr.addFlashAttribute(
					"org.springframework.validation.BindingResult.transaction",
					result);
			attr.addFlashAttribute("transaction", transaction);

			attr.addFlashAttribute("failureMsg",
					"You have errors in your request.");

			// redirect to the credit debit view page
			return "redirect:/home/credit-debit";
		}

		// If account is empty or null, skip the account service check
		Account account = accountService.getValidAccountByNumber(
				request.getParameter("number"), accounts);

		// Exit the transaction if Account doesn't exist
		if (account == null) {
			logger.warn("Someone tried credit/debit functionality for some other account. Details:");
			logger.warn("Credit/Debit Acc No: "
					+ request.getParameter("number"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/credit-debit";
		}

		// Check if Debit amount is < balance in the account
		if (request.getParameter("type").equalsIgnoreCase("debit")
				&& amount.compareTo(account.getBalance()) >= 0) {
			attr.addFlashAttribute(
					"failureMsg",
					"Could not process your transaction. Debit amount cannot be higher than account balance");
			return "redirect:/home/credit-debit";
		}

		transactionService.addTransaction(transaction);

		attr.addFlashAttribute(
				"successMsg",
				"Transaction completed successfully. Transaction should show up on your account shortly.");

		// redirect to the credit debit view page
		return "redirect:/home/credit-debit";
	}

	@RequestMapping(value = "/statements", method = RequestMethod.GET)
	public String getStatements(ModelMap model, HttpServletRequest request) {
		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("title", "Account Statements");
		model.addAttribute("accounts", accounts);

		return "customer/statements";
	}

	@RequestMapping(value = "/statements", method = RequestMethod.POST)
	public String postStatements(ModelMap model, HttpServletRequest request,
			RedirectAttributes attr) {
		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("title", "Account Statements " + user.getFirstName());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);

		// If account is empty or null, skip the account service check
		Account account = accountService.getValidAccountByNumber(
				request.getParameter("number"), accounts);
		// Exit the transaction if Account doesn't exist
		if (account == null) {
			logger.warn("Someone tried statements functionality for some other account. Details:");
			logger.warn("Acc No: " + request.getParameter("number"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("statementFailureMsg",
					"Could not process your request. Please try again or contact the bank.");
			return "redirect:/home/statements";
		}

		List<StatementMonthYear> statements = transactionService
				.getStatementMonths(request.getParameter("number"));
		if (statements.size() > 0) {
			logger.debug("Received statements: " + statements.get(0).getMonth());
		}
		model.addAttribute("statements", statements);
		model.addAttribute("accNumber", request.getParameter("number"));

		return "customer/statements";
	}

	@RequestMapping(value = "/statements/view", method = RequestMethod.POST)
	public String postViewStatement(ModelMap model, HttpServletRequest request,
			RedirectAttributes attr) {

		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());

		// If account is empty or null, skip the account service check
		Account account = accountService.getValidAccountByNumber(
				request.getParameter("number"), accounts);

		if (account == null) {
			logger.warn("Someone tried view statement functionality for some other account. Details:");
			logger.warn("Acc No: " + request.getParameter("number"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("statementFailureMsg",
					"Could not process your request. Please try again or contact the bank.");
			return "redirect:/home/statements";
		}

		List<Transaction> transactions = transactionService
				.getCompletedTransactionsByAccountNummber(
						request.getParameter("number"),
						request.getParameter("month"),
						Integer.parseInt(request.getParameter("year")));

		model.addAttribute("title", "Account Statements");
		model.addAttribute("user", user);
		model.addAttribute("account", account);
		model.addAttribute("transactions", transactions);
		model.addAttribute("statementName", request.getParameter("month") + " "
				+ request.getParameter("year"));

		return "customer/statement";
	}

	@RequestMapping(value = "/statements/download", method = RequestMethod.POST)
	public ModelAndView postDownloadStatement(ModelMap model,
			HttpServletRequest request, RedirectAttributes attr) {

		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());

		// If account is empty or null, skip the account service check
		Account account = accountService.getValidAccountByNumber(
				request.getParameter("number"), accounts);

		if (account == null) {
			logger.warn("Someone tried view statement functionality for some other account. Details:");
			logger.warn("Acc No: " + request.getParameter("number"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("statementFailureMsg",
					"Could not process your request. Please try again or contact the bank.");
			return new ModelAndView("redirect:/home/statements");
		}

		List<Transaction> transactions = transactionService
				.getCompletedTransactionsByAccountNummber(
						request.getParameter("number"),
						request.getParameter("month"),
						Integer.parseInt(request.getParameter("year")));

		model.addAttribute("title", "Account Statements");
		model.addAttribute("user", user);
		model.addAttribute("account", account);
		model.addAttribute("transactions", transactions);
		model.addAttribute("statementName", request.getParameter("month") + " "
				+ request.getParameter("year"));

		return new ModelAndView("pdfView", "model", model);
	}

	@RequestMapping(value = "/fund-transfer", method = RequestMethod.GET)
	public String getFundTransfer(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("title", "Welcome " + user.getFirstName());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);
		return "customer/fundtransfer";
	}

	@RequestMapping(value = "/fund-transfer", method = RequestMethod.POST)
	public String postFundTransfer(ModelMap model, HttpServletRequest request,
			@ModelAttribute("transaction") Transaction senderTransaction,
			BindingResult result, RedirectAttributes attr) {

		// Get user details
		User user = userService.getUserDetails();
		model.put("user", user);

		// Get user accounts and other data for display
		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("fullname",
				user.getFirstName() + " " + user.getLastName());
		model.addAttribute("accounts", accounts);
		model.addAttribute("title", "Welcome " + user.getFirstName());

		// If account is empty or null, skip the account service check
		Account account = accountService.getValidAccountByNumber(
				request.getParameter("senderAccNumber"), accounts);

		// Exit the transaction if Account doesn't exist
		if (account == null) {
			logger.warn("Someone tried credit/debit functionality for some other account. Details:");
			logger.warn("Credit/Debit Acc No: "
					+ request.getParameter("number"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/fund-transfer";
		}

		boolean isTransferAccountValid = transactionService
				.isTransferAccountValid(accounts, request, model, user, attr);

		logger.debug("isTransferAccountValid: " + isTransferAccountValid);
		if (isTransferAccountValid) {

			BigDecimal amount = transactionService.getBigDecimal(request
					.getParameter("amount"));

			String receiverAccNumber = "";
			if (request.getParameter("type").equalsIgnoreCase("internal")) {
				receiverAccNumber = request.getParameter("receiverAccNumber");
				logger.info("internal transfer");
			} else {
				receiverAccNumber = request
						.getParameter("receiverAccNumberExternal");
				logger.info("external transfer");
			}

			logger.debug("receiverAccNumber: " + receiverAccNumber);

			String isCritical = transactionService.isCritical(amount,
					CRITICAL_VALUE);

			// create the transaction object
			senderTransaction = new Transaction(
					transactionService.getUniqueTransactionID(),
					"Fund Transfer", receiverAccNumber,
					request.getParameter("senderAccNumber"), "completed",
					"Debit", amount, isCritical,
					request.getParameter("senderAccNumber"));

			logger.debug("Sender Transaction created: " + senderTransaction);

			// Validate the model
			validator.validate(senderTransaction, result);
			logger.debug("Validated model");

			if (result.hasErrors()) {
				logger.debug("Validation errors: ");
				logger.debug(result);

				// attributes for validation failures
				attr.addFlashAttribute(
						"org.springframework.validation.BindingResult.transaction",
						result);
				attr.addFlashAttribute("transaction", senderTransaction);

				// redirect to the credit debit view page
				return "redirect:/home/fund-transfer";
			}

			logger.debug("No validation errors");

			// Check if Debit amount is < balance in the account
			if (amount.compareTo(account.getBalance()) >= 0) {
				logger.debug("Debit < Balance");
				attr.addFlashAttribute(
						"failureMsg",
						"Could not process your transaction. Debit amount cannot be higher than account balance");
				return "redirect:/home/fund-transfer";
			}

			Transaction receiverTransaction = new Transaction(
					transactionService.getUniqueTransactionID(),
					"Fund Transfer", receiverAccNumber,
					request.getParameter("senderAccNumber"), "completed",
					"Credit", amount, isCritical, receiverAccNumber);

			logger.debug("Receiver Transaction created: " + receiverTransaction);

			try {
				logger.debug("Trying to transfer funds");
				accountService.transferFunds(transactionService,
						accountService, senderTransaction, receiverTransaction,
						amount);
			} catch (Exception e) {
				logger.warn(e);
				attr.addFlashAttribute("failureMsg",
						"Transfer unsucessful. Please try again or contact the bank.");
				return "redirect:/home/fund-transfer";
			}

			attr.addFlashAttribute(
					"successMsg",
					"Transaction completed successfully. Transaction should show up on your account shortly.");

		} else {
			attr.addFlashAttribute("failureMsg",
					"Transfer unsucessful. Please try again or contact the bank.");
		}

		// redirect to the view page
		return "redirect:/home/fund-transfer";
	}

	/**
	 * Gets the payment page customers.
	 *
	 * @param model
	 *            the model
	 * @return view
	 */
	@RequestMapping(value = "/payments", method = RequestMethod.GET)
	public String getPaymentsForCustomer(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Payments");

		List<User> merchants = userService.getUsersOfType("Merchant");
		logger.info(merchants.get(0).toString());
		model.put("merchants", merchants);

		List<Account> userAccounts = accountService
				.getAccountsByCustomerID(user.getCustomerID());
		model.addAttribute("userAccounts", userAccounts);

		return "customer/customerpayments";

	}

	@RequestMapping(value = "/payments", method = RequestMethod.POST)
	public String postPaymentsForCustomer(ModelMap model,
			HttpServletRequest request,
			@ModelAttribute("paymentrequest") PaymentRequest paymentRequest,
			BindingResult result, RedirectAttributes attr) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Payments");

		List<Account> customerAccounts = accountService
				.getAccountsByCustomerID(user.getCustomerID());

		BigDecimal amount = transactionService.getBigDecimal(request
				.getParameter("amount"));

		// Generate OTP
		String otp = null;
		try {
			String sessionId = RequestContextHolder.currentRequestAttributes()
					.getSessionId();
			logger.debug("Got session id: " + sessionId);
			otp = otpService
					.generateOTP(sessionId.getBytes(), 20, 7, false, 20);
		} catch (InvalidKeyException e) {
			logger.warn(e);
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/payments";
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/payments";
		}

		// Validate the PaymentRequest model
		paymentRequest = new PaymentRequest(
				request.getParameter("merchantAccNumber"),
				request.getParameter("customerAccNumber"), null, 1, 0, amount,
				"Debit", otp, 0,
				user.getFirstName() + " " + user.getLastName(), "Merchant Name");

		validator.validate(paymentRequest, result);
		if (result.hasErrors()) {
			logger.debug(result);

			// attributes for validation failures
			attr.addFlashAttribute(
					"org.springframework.validation.BindingResult.paymentrequest",
					result);
			attr.addFlashAttribute("paymentrequest", paymentRequest);
			attr.addFlashAttribute("failureMsg",
					"You have errors in your request.");

			// redirect to the credit debit view page
			return "redirect:/home/payments";
		}

		// If account is empty or null, skip the account service check
		Account customerAccount = accountService.getValidAccountByNumber(
				request.getParameter("customerAccNumber"), customerAccounts);

		// Exit the transaction if Account doesn't exist
		if (customerAccount == null) {
			logger.warn("Someone tried payments functionality for some other account. Details:");
			logger.warn("Customer Acc No: "
					+ request.getParameter("customerAccNumber"));
			logger.warn("Customer ID: " + user.getCustomerID());
			attr.addFlashAttribute("failureMsg",
					"Could not process your request. Please try again or contact the bank.");
			return "redirect:/home/payments";
		}

		// verify that the merchant account exists and is of type merchant
		Account merchantAccount = accountService.getAccountByNumber(request
				.getParameter("merchantAccNumber"));

		if (merchantAccount != null && !merchantAccount.toString().isEmpty()) {

			paymentRequest.setMerchantName(merchantAccount.getUser()
					.getLastName());

			transactionService.initiatePayment(paymentRequest);

			logger.debug("Valid transaction");
			attr.addFlashAttribute(
					"successMsg",
					"Your payment request was made. You will a receive a notification shortly when your payment is approved.");

			return "redirect:/home/payments";

		}

		// log the errors and throw and unsuccessful
		logger.warn("Someone tried payments functionality for some other account. Details:");
		logger.warn("Merchant Acc No: "
				+ request.getParameter("customerAccNumber"));
		logger.warn("Customer ID: " + user.getCustomerID());

		attr.addFlashAttribute("failureMsg",
				"Could not process your request. Please try again or contact the bank.");

		return "redirect:/home/payments";

	}

	/**
	 * Gets the payment requests for customers.
	 *
	 * @param model
	 *            the model
	 * @return view
	 */
	@RequestMapping(value = "/payment-requests", method = RequestMethod.GET)
	public String getPaymentRequestsForCustomer(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Payment Requests");
		Set<Account> accounts = user.getAccounts();
		List<PaymentRequest> requests = new ArrayList<PaymentRequest>();

		for (Account account : accounts) {
			// get all the payment requests for accounts which have been
			// initiated by merchant
			requests.addAll(transactionService.getPaymentsByAccNumber(
					account.getNumber(), 1));
		}

		// add them to the model to be displayed
		model.addAttribute("paymentrequests", requests);
		model.addAttribute("currentTime", new DateTime().toLocalDateTime());

		return "customer/customerpaymentrequests";

	}

	@RequestMapping(value = "/payment-requests", method = RequestMethod.POST)
	public String postPaymentRequestsForCustomer(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Payment Requests");

		// get all the pending requests

		// add them to the model to be displayed

		return "customer/customerpaymentrequests";

	}

	/**
	 * Gets the payments for merchants.
	 *
	 * @param model
	 *            the model
	 * @return the payments for merchants
	 */
	@RequestMapping(value = "/merchant-payments", method = RequestMethod.GET)
	public String getPaymentsForMerchants(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> accounts = accountService.getAccountsByCustomerID(user
				.getCustomerID());
		model.addAttribute("accounts", accounts);

		model.addAttribute("title", "Merchant Payments");

		return "customer/merchantpayments";
	}

	@RequestMapping(value = "/merchant-payments", method = RequestMethod.POST)
	public String postPaymentsForMerchants(ModelMap model,
			HttpServletRequest request,
			@ModelAttribute("paymentrequest") PaymentRequest paymentRequest,
			BindingResult result, RedirectAttributes attr) {
		User user = userService.getUserDetails();
		model.put("user", user);

		List<Account> customerAccounts = accountService
				.getAccountsByCustomerID(user.getCustomerID());

		BigDecimal amount = transactionService.getBigDecimal(request
				.getParameter("amount"));

		// Generate OTP
		String otp = null;
		try {
			String sessionId = RequestContextHolder.currentRequestAttributes()
					.getSessionId();
			logger.debug("Got session id: " + sessionId);
			otp = otpService
					.generateOTP(sessionId.getBytes(), 20, 7, false, 20);
		} catch (InvalidKeyException e) {
			logger.warn(e);
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/merchant-payments";
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
			attr.addFlashAttribute("failureMsg",
					"Could not process your transaction. Please try again or contact the bank.");
			return "redirect:/home/merchant-payments";
		}

		// Validate the PaymentReques model
		paymentRequest = new PaymentRequest(
				request.getParameter("merchantAccNumber"),
				request.getParameter("customerAccNumber"), null, 0, 1, amount,
				request.getParameter("type"), otp, 1, "Customer Name" , user.getLastName());

		validator.validate(paymentRequest, result);
		if (result.hasErrors()) {
			logger.debug(result);

			// attributes for validation failures
			attr.addFlashAttribute(
					"org.springframework.validation.BindingResult.paymentrequest",
					result);
			attr.addFlashAttribute("paymentrequest", paymentRequest);
			attr.addFlashAttribute("failureMsg",
					"You have errors in your request.");

			// redirect to the credit debit view page
			return "redirect:/home/merchant-payments";
		}

		// If account is empty or null, skip the account service check
		Account merchantAccount = accountService.getValidAccountByNumber(
				request.getParameter("merchantAccNumber"), customerAccounts);

		// Exit the transaction if Account doesn't exist
		if (merchantAccount == null) {
			logger.warn("Someone tried payments functionality for some other account. Details:");
			logger.warn("Merchant Acc No: "
					+ request.getParameter("customerAccNumber"));
			logger.warn("Merchant ID: " + user.getCustomerID());
			attr.addFlashAttribute("failureMsg",
					"Could not process your request. Please try again or contact the bank.");
			return "redirect:/home/merchant-payments";
		}

		// verify that the customer account exists and is of type merchant
		Account customerAccount = accountService.getAccountByNumber(request
				.getParameter("customerAccNumber"));

		if (customerAccount != null && !customerAccount.toString().isEmpty()) {
			paymentRequest.setCustomerName(customerAccount.getUser().getFirstName() + " " + customerAccount.getUser().getLastName());
			transactionService.initiatePayment(paymentRequest);

			logger.debug("Valid transaction");
			attr.addFlashAttribute(
					"successMsg",
					"Your payment request was made. You will a receive a notification shortly when your payment is approved.");

			return "redirect:/home/merchant-payments";

		}

		// Account doesn't exist. Mention it since the
		// merchant enters the customer account himself
		attr.addFlashAttribute("failureMsg",
				"Could not process your request. The customer account is invalid.");

		return "redirect:/home/merchant-payments";
	}

	/**
	 * Gets the payment requests for customers.
	 *
	 * @param model
	 *            the model
	 * @return view
	 */
	@RequestMapping(value = "/merchant-payment-requests", method = RequestMethod.GET)
	public String getPaymentRequestsForMerchants(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Merchant Payment Requests");
		
		Set<Account> accounts = user.getAccounts();
		List<PaymentRequest> requests = new ArrayList<PaymentRequest>();

		for (Account account : accounts) {
			// get all the payment requests for accounts which have been
			// initiated by customer
			requests.addAll(transactionService.getPaymentsByAccNumber(
					account.getNumber(), 0));
		}

		// add them to the model to be displayed
		model.addAttribute("paymentrequests", requests);
		model.addAttribute("currentTime", new DateTime().toLocalDateTime());
		
		return "customer/merchantpaymentrequests";

	}

	@RequestMapping(value = "/merchant-payment-requests", method = RequestMethod.POST)
	public String postPaymentRequestsForMerchants(ModelMap model) {
		User user = userService.getUserDetails();
		model.put("user", user);
		model.addAttribute("title", "Payment Requests");

		return "customer/merchantpaymentrequests";

	}
}
