package com.example.yongeunmarket.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.dto.product.GetProductResDto;
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

	/**
	 * 상품 생성 단위 테스트
	 */
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
		verify(productRepository).save(any(Product.class));    // productRepository 의존성 사용
		assertThat(resDto.getUserId()).isEqualTo(user.getId());
		assertThat(resDto.getName()).isEqualTo(reqDto.getName());
		assertThat(resDto.getPrice()).isEqualTo(reqDto.getPrice());
		assertThat(resDto.getDescription()).isEqualTo(reqDto.getDescription());
	}

	/**
	 * 상품 전체 조회 단위 테스트
	 */
	@Test
	void givenProducts_whenFindAll_thenReturnGetProductResList() {

		// given
		User user1 = User.builder()
			.email("test1@naver.com")
			.password("1234")
			.build();
		ReflectionTestUtils.setField(user1, "id", 1L);
		Product product1 = Product.builder()
			.user(user1)
			.name("test1Name")
			.price(BigDecimal.valueOf(10000))
			.description("test1")
			.build();

		User user2 = User.builder()
			.email("test2@naver.com")
			.password("1234")
			.build();
		ReflectionTestUtils.setField(user1, "id", 2L);
		Product product2 = Product.builder()
			.user(user2)
			.name("testName")
			.price(BigDecimal.valueOf(20000))
			.description("test2")
			.build();

		given(productRepository.findAll()).willReturn(List.of(product1, product2));

		// when
		List<GetProductResDto> resDtos = productService.findAll();

		//then
		verify(productRepository).findAll();    // productRepository 의존성 사용
		assertThat(resDtos).hasSize(2);
		assertThat(resDtos.get(0).getName()).isEqualTo(product1.getName());
		assertThat(resDtos.get(0).getPrice()).isEqualTo(product1.getPrice());
		assertThat(resDtos.get(0).getDescription()).isEqualTo(product1.getDescription());

		assertThat(resDtos.get(1).getName()).isEqualTo(product2.getName());
		assertThat(resDtos.get(1).getPrice()).isEqualTo(product2.getPrice());
		assertThat(resDtos.get(1).getDescription()).isEqualTo(product2.getDescription());
	}

	/**
	 * 상품 단건 조회 단위 테스트
	 */
	@Test
	void givenProduct_whenFindAll_thenReturnGetProductResList() {

		// given
		User user = User.builder()
			.email("test@naver.com")
			.password("1234")
			.build();
		ReflectionTestUtils.setField(user, "id", 1L);

		Product product = Product.builder()
			.user(user)
			.name("testName")
			.price(BigDecimal.valueOf(10000))
			.description("testDescription")
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);

		given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
		//when
		GetProductResDto resDto = productService.findById(product.getId());

		//then
		verify(productRepository).findById(anyLong());    // productRepository 의존성 사용

		assertThat(resDto.getName()).isEqualTo(product.getName());
		assertThat(resDto.getPrice()).isEqualTo(product.getPrice());
		assertThat(resDto.getDescription()).isEqualTo(product.getDescription());

	}
}