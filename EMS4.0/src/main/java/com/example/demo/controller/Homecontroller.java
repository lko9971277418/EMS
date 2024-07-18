package com.example.demo.controller;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.EMSMAIN;
import com.example.demo.dao.Userdao;
import com.example.demo.dao.adminDao;
import com.example.demo.entities.Admin;
import com.example.demo.entities.User;
import com.example.demo.entities.UserLoginDateTime;
import com.example.demo.helper.Message;
import com.example.demo.service.servicelayer;

import cn.apiclub.captcha.Captcha;

@Controller
public class Homecontroller {
	@Autowired
	private servicelayer servicelayer;
	@Autowired
	private adminDao adminDao;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private Userdao userdao;

	@GetMapping("/")
	public String defaultpage(@ModelAttribute User user, Model model) {

		System.out.println("hi");
//		model.addAttribute("title", "Microsoft");
//		getCaptcha(user);
//		String Captcha_Created = user.getHidden();
//		EMSMAIN.captcha_validate_map.put(Captcha_Created, new Date());
//		int otp = (int) (Math.random() * 900000000) + 100000000;
//		System.out.println("MATH RANDOM  "+otp);
		model.addAttribute(user);
		return "index";
	}

	@GetMapping("/signup/{id}")
	public String homeee(@PathVariable("id") Integer id, Model model, User user, HttpSession session) {
//		int adminID1 = (int) session.getAttribute("adminId");

//		System.out.println("adminid1 " + adminID1);
//		user.setAaid(adminID1);
		Optional<Admin> admin = adminDao.findById(id);
		if (admin != null) {
			Admin admin1 = admin.get();
			user.setAaid(admin1.getAid());
		}
//		int var=user.getAaid();
		System.out.println("hi");
		getCaptcha(user);
		String hiddenCaptcha = user.getHidden();
		EMSMAIN.captcha_validate_map.put(hiddenCaptcha, new Date());
		session.setAttribute("hiddenCaptcha", user.getHidden());
		System.out.println(user.getHidden());
		return "signup";
	}

	@PostMapping("/verify_admin")
	public String homeee(@Valid Admin admin, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, String password,
			String Captcha, String email, HttpSession session, HttpServletRequest request) {

		try {
			if (!agreement) {
				System.out.println("you have not agreed terms and conditions");
				throw new Exception("you have not agreed terms and conditions");
			}
			if (result.hasErrors()) {
				System.out.println(result);
			} else {
				Optional<Admin> adminn = adminDao.findByUserName(email);
				admin = adminn.get();
				System.out.println("aaid " + admin.getAid());
				System.out.println("admin " + admin);
				System.out.println("hi " + Captcha);
				String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
				System.out.println("hi2 " + hiddenCaptcha);
				System.out.println("hi1 " + adminn.get());
				if (email.equals(admin.getEmail()) && password.equals("admin")) {
					servicelayer.validate_home_captcha();
					boolean found = false;
					Set<Map.Entry<String, Date>> entrySet_data = EMSMAIN.captcha_validate_map.entrySet();
					for (Map.Entry<String, Date> entry : entrySet_data) {
						String hidden_captcha = entry.getKey();
						if (Captcha.equals(hidden_captcha)) {
							// Match found, do something
							found = true;
							break; // Exit the loop once a match is found
						}
					}

					if (found) {
						int adminId = admin.getAid();
						session.setAttribute("adminId", adminId);
						System.out.println("email " + email);
//			            model.addAttribute("title","Send OTP");
						int otp = (int) (Math.random() * 9000) + 1000;
						EMSMAIN.OTP_validate_map.put(otp, new Date());
						EMSMAIN.admin_send_otp.put(email, otp);
						System.out.println("OTP " + otp);
//						String subject = "Google : Admin Verification";
//						String message = "" + "<div style='border:1px solid #e2e2e2;padding:20px'>" + "<h1>" + "OTP :"
//								+ "<b>" + otp + "</n>" + "</h1>" + "</div>";
//						String to = email;
//						boolean flag = this.emailService.sendEmail(message, subject, to);
//						System.out.println(flag);
//						if (flag) {
//							session.setAttribute("myotp", otp);
//							session.setAttribute("email", email);
//							System.out.println("email is   " + email);
//							System.out.println("otp is   " + otp);
						return "redirect:/verify-otp2/" + admin.getAid();
//						} else {
//							session.setAttribute("message", "check your email id");
//							return "redirect:/verify_admin_get";
//						}
					} else {
						session.setAttribute("message", new Message("Wrong Captcha", "alert-danger"));
						return "redirect:/verify_admin_get";
					}
				} else {
					System.out.println("invalid credentials");
					session.setAttribute("message",
							new Message("Please Enter Correct Admin Credentials", "alert-danger"));

					return "redirect:/verify_admin_get";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			getCaptchaa(admin);
//			System.out.println(hiddenCaptcha);
			EMSMAIN.captcha_validate_map.put(admin.getCaptcha(), new Date());
			if (e.getMessage().equals("No value present")) {
				session.setAttribute("message",
						new Message("Something went wrong !! : Admin Not Registered", "alert-danger"));
				String exceptionAsString = e.toString();
				// Get the current class
				Class<?> currentClass = Homecontroller.class;

				// Get the name of the class
				String className = currentClass.getName();
				String errorMessage = e.getMessage();
				StackTraceElement[] stackTrace = e.getStackTrace();
				String methodName = stackTrace[0].getMethodName();
				int lineNumber = stackTrace[0].getLineNumber();
				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			} else {
				String exceptionAsString = e.toString();
				// Get the current class
				Class<?> currentClass = Homecontroller.class;

				// Get the name of the class
				String className = currentClass.getName();
				String errorMessage = e.getMessage();
				StackTraceElement[] stackTrace = e.getStackTrace();
				String methodName = stackTrace[0].getMethodName();
				int lineNumber = stackTrace[0].getLineNumber();
				System.out.println("METHOD NAME " + methodName + " " + lineNumber);
				servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

				session.setAttribute("message",
						new Message("Something went wrong !! : " + e.getMessage(), "alert-danger"));
			}
		}
		getCaptchaa(admin);
		EMSMAIN.captcha_validate_map.put(admin.getHidden(), new Date());
		session.setAttribute("hiddenCaptcha", admin.getHidden());
		System.out.println(admin.getHidden());
		return "redirect:/verify_admin_get";
	}

	private void getCaptcha(User user) {
		Captcha captcha = com.example.demo.service.servicelayer.createCaptcha(250, 80);
		user.setHidden(captcha.getAnswer());
		user.setCaptcha("");
		user.setImageCaptcha(com.example.demo.service.servicelayer.encodeCaptcha(captcha));
		System.out.println("impoted" + user.getImageCaptcha());
	}

	private String getCaptchaa(Admin admin) {
		Captcha captcha = com.example.demo.service.servicelayer.createCaptcha(250, 80);
		admin.setHidden(captcha.getAnswer());
		admin.setCaptcha("");
		admin.setImageCaptcha(com.example.demo.service.servicelayer.encodeCaptcha(captcha));
		String result = admin.getImageCaptcha();
		System.out.println("impoted" + admin.getImageCaptcha());
		return result;
	}

	@GetMapping("/verify-otp2/{id}")
	public String verify_otp(Model model, @PathVariable("id") Integer id, Admin admin) {
		try {
			Optional<Admin> admin_get = adminDao.findById(id);
			if (admin_get != null) {
				Admin admin2 = admin_get.get();
				System.out.println("OTP ADMIN " + admin2.getAid());
				model.addAttribute("admin", admin2);
			}
			return "verify_otp2";
		} catch (Exception e) {
			return "redirect:/swr";
		}
	}

	@PostMapping("/verify-otp2/{id}")
	public String verifyOtpp(@PathVariable("id") Integer id, @RequestParam("otp") int otp, HttpSession session,
			Model model) {
		try {
			model.addAttribute("title", "Verify OTP");
//		Integer myOtp = (Integer) session.getAttribute("myotp");
//		System.out.println(" user otp" + otp);
//		System.out.println(" our otp" + myOtp);
			String email = (String) session.getAttribute("email");
//		System.out.println("emailll " + email);
			boolean flag = false;
			Set<Map.Entry<Integer, Date>> myOtp = EMSMAIN.OTP_validate_map.entrySet();
			for (Map.Entry<Integer, Date> entry : myOtp) {
				Integer myotp1 = entry.getKey();
				if (myotp1 == otp) {
					flag = true;
					break;
				}
			}
			if (flag) {
				Optional<Admin> user = this.adminDao.findByUserName(email);
				if (user == null) {
					session.setAttribute("message", "User does not exist !!");
					return "redirect:/admin_authenticate";
				}
				return "redirect:/signup/" + id;
			} else {
				session.setAttribute("message", "you have entered wrong otp");
				return "redirect:/verify-otp2/" + id;
			}
		} catch (Exception e) {
			return "redirect:/swr";
		}
	}

	@PostMapping("/do-register")
	public String home(@Valid User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			@RequestParam("profileImage") MultipartFile file, HttpSession session) {
		System.out.println("account locked/not locked " + user.isAccountNonLocked());
//		System.out.println("hi " + Captcha);
//		String hiddenCaptcha = (String) session.getAttribute("hiddenCaptcha");
//		System.out.println("hi2 " + hiddenCaptcha);
		System.out.println("hi1 " + user.getHidden());
//		getCaptcha(user);
		System.out.println(user.getDob());
		try {
			boolean flag = false;
			if (!agreement) {
				System.out.println("you have not agreed terms and conditions");
				servicelayer.AllIntanceVariableClear(user);
				System.out.println("---> " + user.getUsername());
				System.out.println(" -- >" + user.getDob());
				throw new Exception("you have not agreed terms and conditions");
			}

			if (file.isEmpty()) {
				System.out.println("File Is Empty And Default Image Is Set");
				user.setImage_Url("default.jpg");
			} else {
				System.out.println("File Is Uploaded And Custom Image Is Uploaded");
				user.setImage_Url(file.getOriginalFilename());
//				File savefile = new ClassPathResource("static/img").getFile();
//				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
//				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				System.out.println(file.getOriginalFilename());
			}
			if (result.hasErrors()) {

				System.out.println(result);
			}
			Set<Map.Entry<String, Date>> captcha = EMSMAIN.captcha_validate_map.entrySet();
			for (Map.Entry<String, Date> entry : captcha) {
				String get_captcha = entry.getKey();
				if (user.getCaptcha().equals(get_captcha)) {
					flag = true;
				}
			}
			if (flag) {
				User result1 = servicelayer.register(user);
				System.out.println(result1);
				if (result1 == null) {
					throw new Exception("Due to some unexceptional error occured");
				} else {
					session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
					servicelayer.AllIntanceVariableClear(user);
					System.out.println("pass");
					getCaptcha(user);
					EMSMAIN.captcha_validate_map.put(user.getHidden(), new Date());
					session.setAttribute("hiddenCaptcha", user.getHidden());
					System.out.println(user.getHidden());
					return "signup";
				}
			} else {
				session.setAttribute("message", new Message("Wrong Captcha", "alert-danger"));

				getCaptcha(user);
				String hiddenCaptcha = user.getHidden();
				EMSMAIN.captcha_validate_map.put(hiddenCaptcha, new Date());
				session.setAttribute("hiddenCaptcha", user.getHidden());
				System.out.println(user.getHidden());
				return "signup";
			}
		} catch (Exception e) {
			e.printStackTrace();
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

//			getCaptcha(user);
//			System.out.println(hiddenCaptcha);
//			String Captcha_Created=user.getHidden();
//			EMSMAIN.captcha_validate_map.put(Captcha_Created, new Date());
			servicelayer.AllIntanceVariableClear(user);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));

		}
		getCaptcha(user);
		String Captcha_Created = user.getHidden();
		EMSMAIN.captcha_validate_map.put(Captcha_Created, new Date());
		session.setAttribute("hiddenCaptcha", user.getHidden());
		System.out.println(user.getHidden());
		return "signup";
	}

	@GetMapping("/signin")
	public String homee(@ModelAttribute User user, Model model, HttpSession session,
			UserLoginDateTime userLoginDateTime,
			@RequestParam(value = "expiredsession", defaultValue = "false") boolean expiredsession,
			HttpServletResponse response, HttpServletRequest request) {
		try {
			System.out.println("hi");
			session = request.getSession();
			EMSMAIN.session_map_data.put(session.getId(), new Date());
			if (expiredsession) {
				System.out.println(new Date() + " Expired Time");
				session.setAttribute("message", new Message("Session Expired", "alert-danger"));
			}
			System.out.println(")))))) " + session.getAttribute("messge"));
			getCaptcha(user);
			EMSMAIN.login_captcha.put(user.getHidden(), new Date());
			return "signin";
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = AdminController.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//			session.setAttribute("message", new Message("Please Diconnect VPN Because VPN may Interrupt application !!", "alert-danger"));
			return "signin";
//			}

		}

	}

	@GetMapping("/forgot")
	public String openEmailForm(Model model) {
//		model.addAttribute("title","Forgot Password");
		return "forgot_email_form";

	}

	@GetMapping("/swr")
	public String somethingWentWrong() {
		return "SomethingWentWrong";
	}

	@GetMapping("/verify_admin_get")
	public String homeeee(Model model, Admin admin, HttpSession session, HttpServletRequest httpServletRequest)
			throws UnknownHostException {

		try {

//			boolean res1=VPNChecker.vpnchecker();
//			 if(res1)
//	            {
//	            	session.setAttribute("message", "VPN is Connected ,Please Disconnect Your VPN");
//	            	return "redirect:/swr";
//	            }
			System.out.println("hi");
			getCaptchaa(admin);
//			String str1 = httpServletRequest.getSession().getId();
			EMSMAIN.captcha_validate_map.put(admin.getHidden(), new Date());
			session.setAttribute("hiddenCaptcha", admin.getHidden());
//			allCaptcha.put(str1, res);
//			System.out.println(" " + allCaptcha.size());
////		System.out.println(emsmain.allCaptcha.isEmpty());
//			System.out.println(allCaptcha);
			String res = admin.getHidden();
			System.out.println(res);
			EMSMAIN.captcha_validate_map.put(res, new Date());
			System.out.println("CAPTCHA MAP-> " + EMSMAIN.captcha_validate_map);
			System.out.println("CAPTCHA MAP SIZE -> " + EMSMAIN.captcha_validate_map.size());
//			System.out.println(admin.getHidden() + " " + str1 + " " + res);
			return "authenticate_admin";
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}
	}

	@GetMapping("/swrr")
	public String swr() {
		return "SomethingWentWrong";
	}

	@PostMapping("/send-otp")
	public String sendotp(@RequestParam("email") String email, HttpSession session, Model model) {
		Optional<User> user = userdao.findByUserName(email);
		try {
			System.out.println("---------" + user);
			if (user.isPresent()) {
				System.out.println("email " + email);
				model.addAttribute("title", "Send OTP");
				int otp = (int) (Math.random() * 9000) + 1000;
//			EMSMAIN.captcha_validate_map.put(otp, new Date());
				System.out.println("OTP " + otp);
//            boolean res= VPNChecker.vpnchecker();
//            if(res)
//            {
//            	throw new Exception();
//            }
				EMSMAIN.forgot_password_email_sent.put(email, otp);
				EMSMAIN.OTP_validate_map.put(otp, new Date());
					session.setAttribute("myotp", otp);
					session.setAttribute("email", email);
					System.out.println("email is   " + email);
					System.out.println("otp is   " + otp);
					return "verify_otp";
			} else {
				session.setAttribute("message", "User is Not Registered , Please Signup User Details");
				return "forgot_email_form";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}

	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model model) {
		try {
			model.addAttribute("title", "Verify OTP");
			Integer myOtp = (Integer) session.getAttribute("myotp");
			System.out.println(" user otp" + otp);
			System.out.println(" our otp" + myOtp);
			String email = (String) session.getAttribute("email");
			System.out.println("emailll " + email);
			if (myOtp == otp) {
				Optional<User> user = this.userdao.findByUserName(email);
				if (user == null) {
					session.setAttribute("message", "User does not exist !!");
					return "forgot_email_form";
				} 
				return "password_change_form";
			} else {
				session.setAttribute("message", "You Have Entered Wrong OTP");
				return "verify_otp";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,
			@RequestParam("newconfirmpassword") String newconfirmpassword, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			System.out.println(email);
			Optional<User> user = userdao.findByUserName(email);

			if (newpassword.equals(newconfirmpassword)) {
				User user2 = user.get();
				System.out.println(user2.getPassword());
				if (this.bCryptPasswordEncoder.matches(newpassword, user2.getPassword())) {

					session.setAttribute("message", "Old Password And New Password Same");
					return "password_change_form";

				} else {

					System.out.println(user2.getPassword());
					user2.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
					this.userdao.save(user2);
					session.setAttribute("message",new Message("Password Changed Successfully",	"alert-success"));
					return "signin";

				}
			} else {
				session.setAttribute("message", "Password Mismatch , Please Enter Same Password In Both Field");
				return "password_change_form";
			}
		} catch (Exception e) {
			String exceptionAsString = e.toString();
			// Get the current class
			Class<?> currentClass = Homecontroller.class;

			// Get the name of the class
			String className = currentClass.getName();
			String errorMessage = e.getMessage();
			StackTraceElement[] stackTrace = e.getStackTrace();
			String methodName = stackTrace[0].getMethodName();
			int lineNumber = stackTrace[0].getLineNumber();
			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);

			return "redirect:/swr";
		}
	}

//	@PostMapping("/create_order")
//	@ResponseBody
//	public String create_order(@RequestBody Map<String, Object> data) throws Exception {
//		try {
//			int amount = Integer.parseInt(data.get("amount").toString());
//			RazorpayClient razorpay = new RazorpayClient("rzp_test_icIfOJXJUlRjph", "L90mE03ZqQXO5rgWxRnn8JCn");
//			System.out.println("RAZORPAY " + razorpay);
//			JSONObject orderRequest = new JSONObject();
//			orderRequest.put("amount", amount * 100);
//			orderRequest.put("currency", "INR");
//			orderRequest.put("receipt", "txn_235425");
////		JSONObject notes = new JSONObject();
////		notes.put("notes_key_1","Tea, Earl Grey, Hot");
////		orderRequest.put("notes",notes);
//
//			Order order = razorpay.Orders.create(orderRequest);
//			System.out.println(order);
//			return order.toString();
//		} catch (Exception e) {
//			String exceptionAsString = e.toString();
//			// Get the current class
//			Class<?> currentClass = AdminController.class;
//
//			// Get the name of the class
//			String className = currentClass.getName();
//			String errorMessage = e.getMessage();
//			StackTraceElement[] stackTrace = e.getStackTrace();
//			String methodName = stackTrace[0].getMethodName();
//			int lineNumber = stackTrace[0].getLineNumber();
//			System.out.println("METHOD NAME " + methodName + " " + lineNumber);
//			servicelayer.insert_error_log(exceptionAsString, className, errorMessage, methodName, lineNumber);
//
//			return "redirect:/swr";
//		}
//	}
//
//	@GetMapping("/payment")
//	public String payment() {
//		return "payment";
//	}
//	
//	    @GetMapping("/api/authenticated")
//	    @ResponseBody
//	    public Map<String, Boolean> isAuthenticated(HttpServletRequest request) {
//	        Map<String, Boolean> response = new HashMap<>();
//	        // Check authentication status
//	        response.put("authenticated", request.getUserPrincipal() != null);
//	        System.out.println(">>>>>>>>>>>>>>>> "+response);
//	        return response;
//	    }
}
