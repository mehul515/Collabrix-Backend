package com.collabrix.collabrix.controller;

import com.collabrix.collabrix.entity.ForgotPasswordToken;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.request.ForgotPasswordTokenRequest;
import com.collabrix.collabrix.request.ResetPasswordRequest;
import com.collabrix.collabrix.response.AuthResponse;
import com.collabrix.collabrix.service.EmailService;
import com.collabrix.collabrix.service.ForgotPasswordService;
import com.collabrix.collabrix.service.UserService;
import com.collabrix.collabrix.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping()
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/api/user/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt){
        jwt = jwt.substring(7);
        User user = userService.getUserProfile(jwt);
        user.setPassword("");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/api/user")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String jwt){
        List<User> users = userService.getAllUsers();
        for(User user : users){
            user.setPassword("");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/api/user/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email){
        User user = userService.getUserByEmail(email);
        user.setPassword("");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/api/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id){
        User user = userService.getUserById(id);
        user.setPassword("");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PostMapping("/auth/user/forgot-password")
    public ResponseEntity<AuthResponse> sendForgotPasswordOtp(@RequestBody ForgotPasswordTokenRequest req) throws Exception {
        User user = userService.getUserByEmail(req.getSendTo());
        String otp = OtpUtils.generateOtp();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        ForgotPasswordToken token = forgotPasswordService.findByUser(user.getId());

        if (token == null) {
            token = forgotPasswordService.createToken(user, id, otp, req.getSendTo());
        } else {
            token.setOtp(otp);
            token.setSendTo(req.getSendTo());
            forgotPasswordService.saveToken(token); // make sure this method calls save()
        }

        emailService.sendVerificationOtpEmail(user.getEmail(), token.getOtp());

        AuthResponse response = new AuthResponse();
        response.setJwt(token.getId());
        response.setMessage("Password reset OTP sent successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/user/reset-password/verify-otp")
    public ResponseEntity<String> resetPassword(@RequestParam String id, @RequestBody ResetPasswordRequest req) throws Exception{
        ForgotPasswordToken forgotPasswordToken = forgotPasswordService.findById(id);
        boolean isVerified = forgotPasswordToken.getOtp().equals(req.getOtp());
        if(isVerified){
            userService.updatePassword(forgotPasswordToken.getUser(), req.getPassword());
        }

        return new ResponseEntity<>("Password Update Successfully", HttpStatus.OK);

    }

    @PutMapping("/api/user/profile/update")
    public ResponseEntity<User> updateUserProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody User updatedUser
    ) {
        jwt = jwt.substring(7);
        User existingUser = userService.getUserProfile(jwt);

        if (existingUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User savedUser = userService.updateUserProfile(existingUser.getId(), updatedUser);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

}
