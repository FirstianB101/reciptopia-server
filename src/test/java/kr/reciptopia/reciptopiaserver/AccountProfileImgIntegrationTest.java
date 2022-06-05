package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.UploadFileAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountProfileImgRepository;
import kr.reciptopia.reciptopiaserver.util.H2DbCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountProfileImgIntegrationTest {

	private static final String UUID_REGEX =
		"\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12}";
	private static final int UUID_LENGTH = 36;

	private static final String TEST_IMG_FILE_NAME = "testProfileImg.png";
	private static final String TEST_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testProfileImg.png";
	private static final String TEST_TXT_FILE_NAME = "testText.txt";
	private static final String TEST_TXT_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testText.txt";
	private static final String TEST_ORIGIN_STORE_FILE_NAME =
		"faf9da02-4762-461c-bc6f-48a05b561a8e.png";
	private static final String TEST_ORIGIN_STORE_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/uploaded/faf9da02-4762-461c-bc6f-48a05b561a8e.png";
	private static final String TEST_STORE_FILE_NAME = "634f963f-b61d-414b-b35b-57ab90533d36.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_NAME = "testProfileImg2.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testProfileImg2.png";
	private static final String TEST_STORE_FILE_NAME2 = "b88e22ad-ab50-44cb-8dd2-6da0f23ce9ef.png";

	private static final FieldDescriptor DOC_FIELD_ID =
		fieldWithPath("id").description("업로드한 이미지의 ID");
	private static final FieldDescriptor DOC_FIELD_UPLOADED_FILE_NAME =
		fieldWithPath("uploadFileName").description("업로드한 이미지 이름");
	private static final FieldDescriptor DOC_FIELD_STORED_FILE_NAME =
		fieldWithPath("storeFileName").description("업로드되어 서버에 저장된 이미지 이름");
	private static final FieldDescriptor DOC_FIELD_OWNER_ID =
		fieldWithPath("ownerId").description("이미지를 업로드한 계정의 ID");

	private static final ParameterDescriptor DOC_PARAMETER_OWNER_ID =
		parameterWithName("ownerId").description("사용자 ID").optional();

	private static final RequestPartDescriptor DOC_PART_OWNER_ID =
		partWithName("ownerId").description("이미지를 업로드할 계정의 ID");
	private static final RequestPartDescriptor DOC_PART_IMAGE_FILE =
		partWithName("imgFile").description("업로드할 이미지 파일");

	@Value("${file.upload.location}")
	private String fileDir;

	@Autowired
	PasswordEncoder passwordEncoder;

	private MockMvc mockMvc;

	@Autowired
	private JsonHelper jsonHelper;

	@Autowired
	private AccountProfileImgRepository repository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TransactionHelper trxHelper;

	@Autowired
	private EntityHelper entityHelper;

	@Autowired
	private UploadFileAuthHelper uploadFileAuthHelper;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext,
		RestDocumentationContextProvider restDocumentation) throws SQLException {
		H2DbCleaner.clean(dataSource);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(springSecurity())
			.apply(basicDocumentationConfiguration(restDocumentation))
			.build();
	}

	@AfterEach
	void deleteUploadImgFiles() {
		File dir = new File(fileDir);
		File[] files = dir.listFiles();

		if (files == null)
			return;

		for (File file : files) {
			String fileName = file.getName();
			if (fileName.equals(TEST_STORE_FILE_NAME))
				continue;

			String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".") + 1)
				.toLowerCase();
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1)
				.toLowerCase();

			if (fileNameWithoutExt.length() != UUID_LENGTH + 1)
				continue;

			fileNameWithoutExt = fileNameWithoutExt.substring(0, fileNameWithoutExt.length() - 1);

			Matcher uuidMatcher = createUUIDMatcher(fileNameWithoutExt);
			if (uuidMatcher.matches() && ext.equals("png")) {
				file.delete();
			}
		}
	}

	private Matcher createUUIDMatcher(String validationStr) {
		Pattern pattern = Pattern.compile(UUID_REGEX);
		return pattern.matcher(validationStr);
	}

	@Nested
	class PostAccountProfileImg {

		@Test
		void postAccountProfileImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = uploadFileAuthHelper.generateToken(account);

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(ownerIdMultipart)
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(
					jsonPath("$.uploadFileName").value(imgFileMultipart.getOriginalFilename()))
				.andExpect(jsonPath("$.storeFileName").value(stringContainsInOrder(
					".png"
				)));

			// Document
			actions.andDo(document("accountProfileImg-create-example",
				requestParts(
					DOC_PART_OWNER_ID,
					DOC_PART_IMAGE_FILE
				),
				responseFields(
					DOC_FIELD_ID,
					DOC_FIELD_UPLOADED_FILE_NAME,
					DOC_FIELD_STORED_FILE_NAME,
					DOC_FIELD_OWNER_ID
				)));
		}

		@Test
		void postAccountProfileImg_AccountProfileImgUnauthorized_UnauthorizedStatus()
			throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();

				return new Struct()
					.withValue("ownerId", account.getId());
			});
			Long ownerId = given.valueOf("ownerId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", MediaType.APPLICATION_JSON_VALUE,
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(imgFileMultipart)
					.file(ownerIdMultipart)
			);

			// Then
			actions
				.andExpect(status().isUnauthorized());
		}

		@Test
		void postAccountProfileImg_RequestPart의_ownerId_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = uploadFileAuthHelper.generateToken(account);

				return new Struct()
					.withValue("token", token);
			});
			String token = given.valueOf("token");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void postAccountProfileImg_RequestPart의_imgFile_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = uploadFileAuthHelper.generateToken(account);

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");

			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", MediaType.APPLICATION_JSON_VALUE,
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(ownerIdMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void postAccountProfileImg_이미지가_아닌_파일_업로드() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = uploadFileAuthHelper.generateToken(account);

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");

			FileInputStream fp = new FileInputStream(TEST_TXT_FILE_PATH);

			MockMultipartFile txtFileMultipart = new MockMultipartFile(
				"imgFile", TEST_TXT_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", MediaType.APPLICATION_JSON_VALUE,
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(txtFileMultipart)
					.file(ownerIdMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void postAccountProfileImg_이미지_파일_업로드_수정() throws Exception {
			// Given - 기존 프로필 이미지 존재 가정
			File file = new File(TEST_IMG_FILE_PATH);
			File originUploadedFile = new File(TEST_ORIGIN_STORE_FILE_PATH);
			Files.copy(file.toPath(), originUploadedFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				AccountProfileImg accountProfileImg = entityHelper
					.generateAccountProfileImg(it -> it
						.withOwner(account)
						.withUploadFileName(TEST_IMG_FILE_NAME)
						.withStoreFileName(TEST_ORIGIN_STORE_FILE_NAME)
					);
				String token = uploadFileAuthHelper.generateToken(accountProfileImg);

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");

			// Upload New File
			FileInputStream fp = new FileInputStream(TEST_NEW_UPLOAD_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_NEW_UPLOAD_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", MediaType.APPLICATION_JSON_VALUE,
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImages")
					.file(ownerIdMultipart)
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(
					jsonPath("$.uploadFileName").value(imgFileMultipart.getOriginalFilename()))
				.andExpect(jsonPath("$.storeFileName").value(stringContainsInOrder(
					".png"
				)));

			assertThat(repository.findByOwnerId(ownerId).stream().count()).isOne();
		}

	}

	@Nested
	class DownloadAccountProfileImg {

		@Test
		void downloadAccountProfileImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				AccountProfileImg accountProfileImg = entityHelper
					.generateAccountProfileImg(it -> it
						.withStoreFileName(TEST_STORE_FILE_NAME)
					);

				return new Struct()
					.withValue("id", accountProfileImg.getId());
			});
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				get("/account/profileImages/{id}/download", id));

			// Then
			actions
				.andExpect(status().isOk());

			// Document
			actions.andDo(document("accountProfileImg-download-example"));
		}

		@Test
		void downloadAccountProfileImg_AccountProfileImgNotFound_NotFoundStatus()
			throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				get("/account/profileImages/{id}/download", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class DownloadAccountProfileImgByOwnerId {

		@Test
		void downloadAccountProfileImgByOwnerId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();

				AccountProfileImg accountProfileImgA = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgB = entityHelper
					.generateAccountProfileImg(it -> it
						.withOwner(account)
						.withStoreFileName(TEST_STORE_FILE_NAME)
					);
				AccountProfileImg accountProfileImgC = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgD = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgE = entityHelper.generateAccountProfileImg();

				return new Struct()
					.withValue("ownerId", account.getId())
					.withValue("accountProfileImgBId", accountProfileImgB.getId());
			});
			Long ownerId = given.valueOf("ownerId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/account/profileImages/download")
					.param("ownerId", ownerId.toString()));

			// Then
			actions
				.andExpect(status().isOk());

			// Document
			actions.andDo(document("accountProfileImg-downloadByOwnerId-example",
				requestParameters(
					DOC_PARAMETER_OWNER_ID
				)));
		}

		@Test
		void downloadAccountProfileImgByOwnerId_프로필_이미지가_없는_경우() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();

				AccountProfileImg accountProfileImgA = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgB = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgC = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgD = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgE = entityHelper.generateAccountProfileImg();

				return new Struct()
					.withValue("ownerId", account.getId());
			});
			Long ownerId = given.valueOf("ownerId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/account/profileImages/download")
					.param("ownerId", ownerId.toString()));

			// Then
			actions
				.andExpect(status().isOk());
		}

	}

	@Nested
	class DeleteAccountProfileImg {

		@Test
		void deleteAccountProfileImg() throws Exception {
			// Given
			File originFile = new File(fileDir + TEST_STORE_FILE_NAME);
			File newFile = new File(fileDir + TEST_STORE_FILE_NAME2);
			Files.copy(originFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				AccountProfileImg accountProfileImg = entityHelper
					.generateAccountProfileImg(it -> it
						.withUploadFileName(TEST_IMG_FILE_NAME)
						.withStoreFileName(TEST_STORE_FILE_NAME2)
					);
				String token = uploadFileAuthHelper.generateToken(accountProfileImg);

				return new Struct()
					.withValue("token", token)
					.withValue("id", accountProfileImg.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				delete("/account/profileImages/{id}", id)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isNoContent())
				.andExpect(content().string(emptyString()));

			assertThat(repository.findById(id)).isEmpty();

			// Document
			actions.andDo(document("accountProfileImg-delete-example"));
		}

		@Test
		void deleteAccountProfileImg_AccountProfileImgNotFound_NotFound_Status()
			throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				delete("/account/profileImages/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

}
