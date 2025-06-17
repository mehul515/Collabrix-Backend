package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.config.JwtProvider;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.UserRepository;
import com.collabrix.collabrix.request.LoginRequest;
import com.collabrix.collabrix.request.VerifyOtpRequest;
import com.collabrix.collabrix.response.AuthResponse;
import com.collabrix.collabrix.service.EmailService;
import com.collabrix.collabrix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.collabrix.collabrix.utils.OtpUtils.generateOtp;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;


    @PostMapping("/signup")
    public ResponseEntity<String> createUserHandler(@RequestBody User user) throws Exception {
        User isUserExists = userRepository.findByEmail(user.getEmail());
        if (isUserExists != null) {
            throw new Exception("Email already exists with another account!");
        }

        User newUser = new User();
        newUser.setFullName(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        String otp = generateOtp();
        newUser.setOtp(otp);
        newUser.setOtpGeneratedAt(LocalDateTime.now());

        userRepository.save(newUser);


        emailService.sendVerificationOtpEmail(user.getEmail(), otp);

        System.out.println("OTP sent to user: " + newUser.getOtp());

        return new ResponseEntity<>("OTP sent to your email.", HttpStatus.OK);
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody VerifyOtpRequest request) throws Exception {
        String email = request.getEmail();
        String otp = request.getOtp();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found.");
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new Exception("Invalid OTP.");
        }

        user.setIsVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        userRepository.save(user);

        // Authenticate and generate JWT
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setMessage("Signup verified");
        res.setJwt(jwt);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Check if user exists
        User user = userRepository.findByEmail(username);
        if (user == null) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }

        // Check if user is verified
        if (!user.isVerified()) {
            return new ResponseEntity<>("Email not verified. Please verify your email before logging in.", HttpStatus.FORBIDDEN);
        }

        // Authenticate user
        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = JwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setMessage("Login Success");
        res.setJwt(jwt);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }



    private Authentication authenticate(String username, String password) {

        UserDetails userDetails = userService.loadUserByUsername(username);
        if(userDetails==null){
            throw new BadCredentialsException("Invalid username!");
        }

        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid password!");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    }
}
