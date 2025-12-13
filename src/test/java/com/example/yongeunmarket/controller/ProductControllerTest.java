package com.example.yongeunmarket.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.example.yongeunmarket.dto.product.CreateProductReqDto;
import com.example.yongeunmarket.dto.product.CreateProductResDto;
import com.example.yongeunmarket.entity.Product;
import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ProductController.class) //controller 레이어만 컨테이너에 추가
//@Import(SecurityConfig.class)
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;

	// @BeforeEach
	// void setUp() {
	// 	SecurityContextHolder.getContext().setAuthentication(
	// 		new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList())
	// 	);
	// }

	@Test
	void givenCreateProductReq_whenCreateProduct_thenReturnCreateProductRes() throws Exception {
		// given
		CreateProductReqDto reqDto = CreateProductReqDto.builder()
			.name("testName")
			.price(BigDecimal.valueOf(10000))
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
			.price(BigDecimal.valueOf(10000))
			.description("testDescription")
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);
		CreateProductResDto resDto = CreateProductResDto.builder()
			.productId(product.getId())
			.userId(user.getId())
			.name("testName")
			.price(product.getPrice())
			.description("testDescription")
			.createdAt(product.getCreatedAt())
			.build();

		given(productService.createProduct(any(CreateProductReqDto.class), anyLong())).willReturn(resDto);

		// when & then
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reqDto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.productId").value(1L)) //$ 는 json 의 최상위
			.andExpect(jsonPath("$.userId").value(1L))
			.andExpect(jsonPath("$.name").value("testName"))
			.andExpect(jsonPath("$.price").value(10000))
			.andExpect(jsonPath("$.description").value("testDescription"));
	}
}