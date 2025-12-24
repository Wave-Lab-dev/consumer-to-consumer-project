package com.example.yongeunmarket.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.yongeunmarket.entity.User;
import com.example.yongeunmarket.exception.AccessDeniedException;
import com.example.yongeunmarket.exception.upload.InvalidFileException;
import com.example.yongeunmarket.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3UploadService 단위 테스트")
class S3UploadServiceTest {

	@Mock
	private S3Client s3Client;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MultipartFile multipartFile;

	@InjectMocks
	private S3UploadService s3UploadService;

	private static final String TEST_BUCKET = "test-bucket";
	private static final Long USER_ID = 1L;
	private static final Long CURRENT_USER_ID = 1L;
	private static final String ORIGINAL_FILENAME = "test-image.jpg";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(s3UploadService, "bucket", TEST_BUCKET);

	}

	@Nested
	@DisplayName("saveFile 메서드는")
	class SaveFileTest {

		@Test
		@DisplayName("성공: 정상적으로 파일을 업로드하고 User 엔티티를 업데이트한다")
		void givenValidFile_whenSaveFile_thenUploadFileAndUpdateUser() throws IOException {

			// given
			User user = User.builder().email("test@naver.com").password("password").build();
			ReflectionTestUtils.setField(user, "id", 1L);
			given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
			given(multipartFile.isEmpty()).willReturn(false);
			given(multipartFile.getOriginalFilename()).willReturn(ORIGINAL_FILENAME);
			given(multipartFile.getSize()).willReturn(1024L);
			given(multipartFile.getContentType()).willReturn("image/jpeg");

			InputStream inputStream = new ByteArrayInputStream("test".getBytes());
			given(multipartFile.getInputStream()).willReturn(inputStream);
			given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
				.willReturn(PutObjectResponse.builder().build());

			// when
			s3UploadService.saveFile(multipartFile, USER_ID, CURRENT_USER_ID);

			// then
			verify(userRepository).findById(USER_ID);
			verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
			assertThat(user.getImageUrl()).isNotNull();
			assertThat(user.getImageUrl()).startsWith("images/");
			assertThat(user.getImageUrl()).endsWith(".jpg");
		}

		@Test
		@DisplayName("성공: 기존 이미지가 있으면 삭제 후 새 이미지를 업로드한다")
		void givenUserWithExistingImage_whenSaveFile_thenDeleteOldImageAndUploadNewImage() throws IOException {

			// given
			User user = User.builder().email("test@naver.com").password("password").build();
			ReflectionTestUtils.setField(user, "id", 1L);
			String oldImageUrl = "images/old-image.jpg";
			user.updateImageUrl(oldImageUrl); //이미지가 이미 존재함

			given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
			given(multipartFile.isEmpty()).willReturn(false);
			given(multipartFile.getOriginalFilename()).willReturn(ORIGINAL_FILENAME);
			given(multipartFile.getSize()).willReturn(1024L);
			given(multipartFile.getContentType()).willReturn("image/jpeg");
			InputStream inputStream = new ByteArrayInputStream("test".getBytes());
			given(multipartFile.getInputStream()).willReturn(inputStream);
			given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
				.willReturn(PutObjectResponse.builder().build());
			given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
				.willReturn(DeleteObjectResponse.builder().build());
			// when
			s3UploadService.saveFile(multipartFile, USER_ID, CURRENT_USER_ID);

			// then
			verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
			verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
			assertThat(user.getImageUrl()).isNotNull();
			assertThat(user.getImageUrl()).startsWith("images/");
			assertThat(user.getImageUrl()).endsWith(".jpg");

		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 ID로 요청")
		void givenNonExistentUserId_whenSaveFile_thenThrowEntityNotFoundException() {
			// given
			given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> s3UploadService.saveFile(multipartFile, USER_ID, CURRENT_USER_ID))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("user 가 존재하지 않음");

			verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		}

		@Test
		@DisplayName("실패: 사용자와 현재 사용자가 다름")
		void givenWrongUserId_whenSaveFile_thenIllegalStateException() {
			// given
			User user = User.builder().email("test@naver.com").password("password").build();
			ReflectionTestUtils.setField(user, "id", 1L);
			Long differentUserId = 2L;
			given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

			// when & then
			assertThatThrownBy(() -> s3UploadService.saveFile(multipartFile, USER_ID, differentUserId))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("해당 user 에 권한이 없습니다");

			verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		}

		@Test
		@DisplayName("실패: 파일이 비어있음")
		void givenEmptyFile_whenSaveFile_thenIllegalArgumentException() {
			// given
			User user = User.builder().email("test@naver.com").password("password").build();
			ReflectionTestUtils.setField(user, "id", 1L);
			given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
			given(multipartFile.isEmpty()).willReturn(true);

			// when & then
			assertThatThrownBy(() -> s3UploadService.saveFile(multipartFile, USER_ID, CURRENT_USER_ID))
				.isInstanceOf(InvalidFileException.class);
			verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		}

		@Test
		@DisplayName("실패: 확장자가 png, jpg, jpeg 가 아니다.")
		void saveWrongExtension_EmptyFile_ThrowsException() {
			// given
			User user = User.builder().email("test@naver.com").password("password").build();
			ReflectionTestUtils.setField(user, "id", 1L);
			String wrongExtensionFile = "images/old-image.pdf";
			given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
			given(multipartFile.isEmpty()).willReturn(false);
			given(multipartFile.getOriginalFilename()).willReturn(wrongExtensionFile);

			// when & then
			assertThatThrownBy(() -> s3UploadService.saveFile(multipartFile, USER_ID, CURRENT_USER_ID))
				.isInstanceOf(InvalidFileException.class);
			verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		}
	}
}