package com.example.yongeunmarket.dto.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductReqDto {

	@NotNull
	private String name;
	@NotNull @Positive
	private BigDecimal price;
	@NotNull @Size(max=500)
	private String description;
}
