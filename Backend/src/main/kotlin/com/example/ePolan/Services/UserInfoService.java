package com.example.ePolan.Services;

import com.example.ePolan.Model.Dtos.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {

    public UserDto getLoggedUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UserDto loggedUser = new UserDto();
        loggedUser.setEmail(jwt.getClaim("email"));
        loggedUser.setName(jwt.getClaim("given_name"));
        loggedUser.setName(jwt.getClaim("family_name"));
        return loggedUser;
    }
}
