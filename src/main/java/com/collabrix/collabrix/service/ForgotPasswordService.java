package com.collabrix.collabrix.service;

import com.collabrix.collabrix.entity.ForgotPasswordToken;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.ForgotPasswordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForgotPasswordService {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    public ForgotPasswordToken createToken(User user, String id, String otp, String sendTo){
        ForgotPasswordToken token = new ForgotPasswordToken();
        token.setUser(user);
        token.setSendTo(sendTo);
        token.setOtp(otp);
        token.setId(id);
        return forgotPasswordRepository.save(token);
    }

    public ForgotPasswordToken findById(String id){
        Optional<ForgotPasswordToken> token = forgotPasswordRepository.findById(id);
        return token.orElse(null);
    }

    @Transactional
    public ForgotPasswordToken saveToken(ForgotPasswordToken token) {
        return forgotPasswordRepository.save(token);
    }


    public ForgotPasswordToken findByUser(Long userId){
        return forgotPasswordRepository.findByUserId(userId);
    }

    public void deleteToken(ForgotPasswordToken token){
        forgotPasswordRepository.delete(token);
    }
}
