package com.example.yongeunmarket.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductResDto {

	private Long productId;
	private Long userId;
	private String name;
	private BigDecimal price;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
