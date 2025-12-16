package com.example.yongeunmarket.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.example.yongeunmarket.service.S3UploadService;

@WebMvcTest(controllers = S3UploadController.class) //controller 레이어만 컨테이너에 추가
	//@Import(SecurityConfig.class)
class S3UploadControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private S3UploadService s3UploadService;

	@Test
	void givenValidFile_whenSaveFile_thenUploadFileAndUpdateUser() throws Exception {

		// given
		Long userId = 1L;
		MockMultipartFile multipartFile = new MockMultipartFile(
			"multipartFile",                     // @RequestPart 와 이름이 일치해야 한다!!
			"test.png",                 // original filename
			MediaType.IMAGE_PNG_VALUE,  // content type
			"test image content".getBytes()
		);
		willDoNothing()
			.given(s3UploadService)
			.saveFile(any(MultipartFile.class), anyLong(), anyLong());

		// when & then
		mockMvc.perform(
				multipart("/api/user/{userId}/upload", userId)
					.file(multipartFile)
					.contentType(MediaType.MULTIPART_FORM_DATA))
			.andDo(print())
			.andExpect(status().isOk());
	}
}