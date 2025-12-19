package com.example.yongeunmarket.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.yongeunmarket.dto.product.UpdateProductReqDto;
import com.example.yongeunmarket.dto.product.UpdateProductResDto;
import com.example.yongeunmarket.dto.product.GetProductResDto;
import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.security.CustomUserDetails;
import com.example.yongeunmarket.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	public ResponseEntity<CreateProductResDto> registerProduct(
		@RequestBody @Valid CreateProductReqDto createProductReqDto,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {

		Long userId = customUserDetails.getUserId();
		CreateProductResDto createProductResDto = productService.createProduct(createProductReqDto, userId);
		return new ResponseEntity<>(createProductResDto, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<GetProductResDto>> getProducts() {

		List<GetProductResDto> getProductResDtos = productService.findAll();
		return new ResponseEntity<>(getProductResDtos, HttpStatus.OK);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<GetProductResDto> getProduct(@PathVariable Long productId) {

		GetProductResDto getProductResDto = productService.findById(productId);
		return new ResponseEntity<>(getProductResDto, HttpStatus.OK);
	}

	@PatchMapping("/{productId}")
	public ResponseEntity<UpdateProductResDto> modifyProduct(
		@RequestBody @Valid UpdateProductReqDto updateProductReqDto, @PathVariable Long productId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {

		Long userId = customUserDetails.getUserId();
		UpdateProductResDto updateProductResDto = productService.updateProduct(updateProductReqDto, productId, userId);
		return new ResponseEntity<>(updateProductResDto, HttpStatus.OK);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> removeProduct(@PathVariable Long productId
		, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

		Long userId = customUserDetails.getUserId();
		productService.deleteProduct(productId, userId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

