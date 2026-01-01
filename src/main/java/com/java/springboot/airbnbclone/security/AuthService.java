package com.java.springboot.airbnbclone.security;

import com.java.springboot.airbnbclone.dtos.LoginDto;
import com.java.springboot.airbnbclone.dtos.SignUpRequestDto;
import com.java.springboot.airbnbclone.dtos.UserDto;
import com.java.springboot.airbnbclone.entities.User;
import com.java.springboot.airbnbclone.entities.enums.Role;
import com.java.springboot.airbnbclone.exceptions.UserAlreadyExistsException;
import com.java.springboot.airbnbclone.services.UserService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        User user = userService.findUserByEmail(signUpRequestDto.getEmail());

        if(user!=null){
            throw new UserAlreadyExistsException("User with email " +user.getEmail()+ "already exists");
        }

        User newUser = new User();
        newUser.setEmail(signUpRequestDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser.setName(signUpRequestDto.getName());
        newUser.setRoles(Set.of(Role.GUEST, Role.HOTEL_MANAGER)); // for testing purpose. Actual Hotel Manager role should be provided by Admin

        User savedUser = userService.saveUserToDB(newUser);
        return modelMapper.map(savedUser, UserDto.class);
    }


    public String[] login(LoginDto loginDto) {

        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                        (loginDto.getEmail(), loginDto.getPassword()));

        User user = (User) authentication.getPrincipal();
        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user.getEmail());
        tokens[1] = jwtService.generateRefreshToken(user.getEmail());
        return tokens;
    }

    public String refreshToken(String refreshToken) {

        String email = jwtService.validateToken(refreshToken);
        User user = userService.findUserByEmail(email);

        return jwtService.generateAccessToken(user.getEmail());
    }
}
