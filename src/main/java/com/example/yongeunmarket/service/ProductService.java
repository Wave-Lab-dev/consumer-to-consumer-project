package com.example.yongeunmarket.service;

import static com.example.yongeunmarket.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.dto.product.GetProductResDto;
import com.example.yongeunmarket.dto.product.UpdateProductReqDto;
import com.example.yongeunmarket.dto.product.UpdateProductResDto;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.exception.BusinessException;
import com.example.yongeunmarket.repository.ProductRepository;
import com.example.yongeunmarket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	@Transactional
	public CreateProductResDto createProduct(CreateProductReqDto createProductReqDto, Long userId) {

		User user = getUserOrThrow(userId);

		Product product = Product.builder()
			.user(user)
			.name(createProductReqDto.getName())
			.price(createProductReqDto.getPrice())
			.description(createProductReqDto.getDescription())
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

	@Transactional(readOnly = true)
	public List<GetProductResDto> findAll() {

		List<GetProductResDto> results = new ArrayList<>();
		for (Product product : productRepository.findAll()) {
			results.add(GetProductResDto.builder()
				.productId(product.getId())
				.userId(product.getUser().getId())
				.name(product.getName())
				.price(product.getPrice())
				.description(product.getDescription())
				.createdAt(product.getCreatedAt())
				.build());
		}
		return results;
	}

	@Transactional(readOnly = true)
	public GetProductResDto findById(Long productId) {

		Product product = getProductOrThrow(productId);
		return GetProductResDto.builder()
			.productId(product.getId())
			.userId(product.getUser().getId())
			.name(product.getName())
			.price(product.getPrice())
			.description(product.getDescription())
			.createdAt(product.getCreatedAt())
			.build();
	}

	@Transactional
	public UpdateProductResDto updateProduct(
		UpdateProductReqDto updateProductReqDto, Long productId, Long userId) {

		Product product = getProductOrThrow(productId);
		// 상품이 현재 로그인한 user 가 만든 product 인지 검증
		if (!userId.equals(product.getUser().getId())) {
			throw new BusinessException(ACCESS_DENIED);    //403
		}

		product.updateProduct(
			updateProductReqDto.getName(),
			updateProductReqDto.getPrice(),
			updateProductReqDto.getDescription());

		return UpdateProductResDto.builder()
			.productId(product.getId())
			.userId(userId)
			.name(product.getName())
			.price(product.getPrice())
			.description(product.getDescription())
			.createdAt(product.getCreatedAt())
			.updatedAt(product.getUpdatedAt())
			.build();
	}

	@Transactional
	public void deleteProduct(Long productId, Long userId) {

		Product product = getProductOrThrow(productId);
		// 상품이 현재 로그인한 user 가 만든 product 인지 검증
		if (!userId.equals(product.getUser().getId())) {
			throw new BusinessException(ACCESS_DENIED);
		}

		productRepository.delete(product);
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BusinessException(RESOURCE_NOT_FOUND));
	}

	private Product getProductOrThrow(Long productId) {
		return productRepository.findById(productId).orElseThrow(
			() -> new BusinessException(RESOURCE_NOT_FOUND));
	}

}
