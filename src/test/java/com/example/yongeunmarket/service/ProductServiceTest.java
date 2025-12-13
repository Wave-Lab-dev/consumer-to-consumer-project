package com.example.yongeunmarket.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.repository.ProductRepository;
import com.example.yongeunmarket.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void givenCreateProductReq_whenCreateProduct_thenReturnCreateProductRes() {

		// given
		CreateProductReqDto reqDto = CreateProductReqDto.builder()
			.name("testName")
			.price(BigDecimal.ONE)
			.description("testDescription")
			.build();

		User user = User.builder()
			.email("test@naver.com")
			.password("1234")
			.build();
		ReflectionTestUtils.setField(user, "id", 1L);

		Product product = Product.builder()
			.user(user)
			.name("testName")
			.price(BigDecimal.ONE)
			.description("testDescription")
			.build();

		given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(productRepository.save(any(Product.class))).willReturn(product);

		// when
		CreateProductResDto resDto = productService.createProduct(reqDto, user.getId());

		//then
		verify(productRepository).save(any(Product.class));	// productRepository 의존성 사용
		assertThat(resDto.getUserId()).isEqualTo(user.getId());
		assertThat(resDto.getName()).isEqualTo(reqDto.getName());
		assertThat(resDto.getPrice()).isEqualTo(reqDto.getPrice());
		assertThat(resDto.getDescription()).isEqualTo(reqDto.getDescription());
	}

}