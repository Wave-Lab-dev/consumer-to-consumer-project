package com.example.yongeunmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<CreateProductResDto> save(
		@RequestBody @Valid CreateProductReqDto createProductReqDto) {
		//@AuthenticationPrincipal CustomUserDetails
		// Long userId = CustomUserDetails.getUsername();
		//인증인가 미 구현으로 임시 하드코딩
		CreateProductResDto createProductResDto = productService.createProduct(createProductReqDto, 1L);
		return new  ResponseEntity<>(createProductResDto, HttpStatus.CREATED);
	}
}

