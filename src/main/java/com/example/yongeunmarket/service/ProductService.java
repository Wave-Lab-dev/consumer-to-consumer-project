package com.example.yongeunmarket.service;

import org.springframework.stereotype.Service;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ProductRepository;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	@Transactional
	public CreateProductResDto createProduct(CreateProductReqDto reqDto, Long userId) {

		User user = getUserOrThrow(userId);

		Product product = Product.builder()
			.user(user)
			.name(reqDto.getName())
			.price(reqDto.getPrice())
			.description(reqDto.getDescription())
			.build();
		product = productRepository.save(product);

		return CreateProductResDto.builder()
			.productId(product.getId())
			.userId(user.getId())
			.name(product.getName())
			.price(product.getPrice())
			.description(product.getDescription())
			.createdAt(product.getCreatedAt())
			.build();
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new EntityNotFoundException("user 가 존재하지 않음"));
	}
}
