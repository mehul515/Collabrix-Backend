package com.collabrix.collabrix.service;

import com.collabrix.collabrix.config.JwtProvider;
import com.collabrix.collabrix.entity.User;
import com.collabrix.collabrix.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user==null){
            throw new UsernameNotFoundException("User Not Found with " + username + "!");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);

    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User getUserById(Long userId){
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserProfile(String jwtToken){
        String email  = JwtProvider.getEmailFromToken(jwtToken);
        return userRepository.findByEmail(email);
    }

    public User updatePassword(User user, String newPassword){
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User updateUserProfile(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId).orElse(null);
        if (existingUser == null) {
            return null;
        }

        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setAvatar(updatedUser.getAvatar());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setBio(updatedUser.getBio());

        return userRepository.save(existingUser);
    }
}
