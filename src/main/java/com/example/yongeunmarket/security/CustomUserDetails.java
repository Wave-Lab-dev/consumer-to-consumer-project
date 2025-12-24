package com.example.yongeunmarket.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.entity.UserRole;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

	private Long userId;
	private String password;
	private UserRole role;

	public CustomUserDetails(User user) {
		this.userId = user.getId();
		this.password = user.getPassword();
		this.role = user.getRole();
	}

	public CustomUserDetails(Long userId, UserRole role) {
		this.userId = userId;
		this.password = "";
		this.role = role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return "";
	}
}
