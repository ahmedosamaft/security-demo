package com.example.security_demo.controllers;

import com.example.security_demo.models.User;
import com.example.security_demo.repositories.UserRepository;
import com.example.security_demo.services.JwtService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.users.AbstractUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setName(user.getName());
        newUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        newUser.setRole(user.getRole());
        user = userRepository.save(newUser);
        String token = jwtService.generateToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping
    public ResponseEntity<?> login(@RequestBody User user) {
        User foundUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if(foundUser == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(!bCryptPasswordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String token = jwtService.generateToken(foundUser);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", foundUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
