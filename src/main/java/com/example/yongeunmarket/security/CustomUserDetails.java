package com.example.yongeunmarket.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.yongeunmarket.entity.User;

public class CustomUserDetails implements UserDetails {

	private Long userId;
	private String password;

	public CustomUserDetails(User user) {
		this.userId = user.getId();
		this.password = user.getPassword();
	}

	public CustomUserDetails(Long userId) {
		this.userId = userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//수정 예정
		return List.of();
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return "";
	}

	public Long getUserId() {
		return this.userId;
	}
}
