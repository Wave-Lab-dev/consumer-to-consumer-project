package com.example.yongeunmarket.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 20)
	private String name;

	@Column(nullable = false)
	private BigDecimal price = BigDecimal.valueOf(0);

	@Column(nullable = false, length = 500)
	private String description;

	@Builder
	public Product(User user, String name, BigDecimal price, String description) {
		this.user = user;
		this.name = name;
		this.price = price;
		this.description = description;
	}

	public void updateProduct(String name, BigDecimal price, String description){
		this.name = name;
		this.price = price;
		this.description = description;
	}
}
