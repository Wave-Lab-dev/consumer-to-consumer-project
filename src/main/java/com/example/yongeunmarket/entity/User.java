package com.example.yongeunmarket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	private String imageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private UserRole role;

	@Builder
	public User(String email, String password, UserRole role, String imageUrl) {
		this.email = email;
		this.password = password;
		this.role = role;
		this.imageUrl = imageUrl;
	}

	public void updateImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void updatePassword(String password) {
		this.password = password;
	}
}
