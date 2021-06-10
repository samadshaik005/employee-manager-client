package com.nagarro.springboot.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.nagarro.springboot.dto.UserRegistrationDto;
import com.nagarro.springboot.model.User;

public interface UserService extends UserDetailsService{
	User save(UserRegistrationDto registrationDto);
}
