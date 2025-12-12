package com.example.yongeunmarket.entity;

public enum UserRole {
	BUYER("BUYER"),
	SELLER("SELLER"),
	ADMIN("ADMIN");

	private final String role;

	UserRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}
