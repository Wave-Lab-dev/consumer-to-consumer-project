package com.example.yongeunmarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 모든 경로에 대해 CORS를 적용합니다.
			.allowedOriginPatterns("http://localhost:3000") // 허용할 오리진을 명시합니다. (프론트엔드 개발 서버 주소)
			.allowedMethods("GET", "POST", "PUT", "PATCH") // 허용할 HTTP 메서드를 지정합니다.
			.allowedHeaders("*") // 모든 헤더를 허용합니다.
			.allowCredentials(true) // 자격 증명(쿠키, HTTP 인증 등)을 허용합니다.
			.maxAge(3600); // pre-flight 요청의 결과를 캐시할 시간 (초)
	}
}