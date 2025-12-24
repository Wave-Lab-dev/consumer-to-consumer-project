package com.example.yongeunmarket.entity;

public enum UserRole {
	ADMIN,
	BUYER,
	SELLER;

	public String getAuthority() {
		return "ROLE_" + this.name();
	}
}
