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
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
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
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountProfileImgIntegrationTest {

	private static final String TEST_DIR_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/";
	private static final String UUID_REGEX =
		"\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12}";
	private static final int UUID_LENGTH = 36;

	private static final String TEST_IMG_FILE_NAME = "testProfileImg.png";
	private static final String TEST_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testProfileImg.png";

	private static final String TEST_TXT_FILE_NAME = "testText.txt";
	private static final String TEST_TXT_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testText.txt";

	private static final String TEST_NEW_UPLOAD_IMG_FILE_NAME = "testProfileImg2.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testProfileImg2.png";

	private static final String TEST_STORE_FILE_NAME = "src/test/resources/testfiles/uploaded/"
		+ "634f963f-b61d-414b-b35b-57ab90533d36.png";
	private static final String TEST_STORE_FILE_NAME2 = "src/test/resources/testfiles/uploaded/"
		+ "b88e22ad-ab50-44cb-8dd2-6da0f23ce9ef.png";

	private static final FieldDescriptor DOC_FIELD_RESOURCE =
		fieldWithPath("resource").description("이미지 리소스");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_URI =
		fieldWithPath("resource.uri").description("이미지 리소스 URI");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_URL =
		fieldWithPath("resource.url").description("이미지 리소스 URL");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_INPUT_STREAM =
		fieldWithPath("resource.inputStream").description("이미지 리소스 Input Stream");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_FILE =
		fieldWithPath("resource.file").description("이미지 리소스 파일");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_FILENAME =
		fieldWithPath("resource.filename").description("이미지 리소스 파일 이름");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_DESCRIPTION =
		fieldWithPath("resource.description").description("이미지 리소스 설명");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_READABLE =
		fieldWithPath("resource.readable").description("이미지 리소스 읽기 가능 여부");
	private static final FieldDescriptor DOC_FIELD_RESOURCE_OPEN =
		fieldWithPath("resource.open").description("이미지 리소스 열기 가능 여부");

	private static final ParameterDescriptor DOC_PARAMETER_OWNER_ID =
		parameterWithName("ownerId").description("사용자 ID").optional();

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
		File dir = new File(TEST_DIR_PATH);
		File[] files = dir.listFiles();

		if (files == null)
			return;

		for (File file : files) {
			String fileName = file.getName();
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
				"imgFile", TEST_IMG_FILE_NAME, "image/png", fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
			actions.andDo(document("accountProfileImg-create-example"));
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
				"imgFile", TEST_IMG_FILE_NAME, "image/png", fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
				"imgFile", TEST_IMG_FILE_NAME, "image/png", fp
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
				"imgFile", TEST_TXT_FILE_NAME, "text/plain", fp
			);

			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
			File originUploadedFile = new File("faf9da02-4762-461c-bc6f-48a05b561a8e.png");
			Files.copy(file.toPath(), originUploadedFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				AccountProfileImg accountProfileImg = entityHelper
					.generateAccountProfileImg(it -> it
						.withOwner(account)
						.withUploadFileName(TEST_IMG_FILE_NAME)
						.withStoreFileName("faf9da02-4762-461c-bc6f-48a05b561a8e.png")
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
				"imgFile", TEST_NEW_UPLOAD_IMG_FILE_NAME, "image/png", fp
			);
			MockMultipartFile ownerIdMultipart = new MockMultipartFile(
				"ownerId", "ownerId", "application/json",
				ownerId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/account/profileImgs")
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
	class GetAccountProfileImg {

		@Test
		void getAccountProfileImg() throws Exception {
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
				get("/account/profileImgs/{id}", id));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resource").exists());

			// Document
			actions.andDo(document("accountProfileImg-retrieve-example",
				responseFields(
					DOC_FIELD_RESOURCE,
					DOC_FIELD_RESOURCE_URI,
					DOC_FIELD_RESOURCE_URL,
					DOC_FIELD_RESOURCE_INPUT_STREAM,
					DOC_FIELD_RESOURCE_FILE,
					DOC_FIELD_RESOURCE_FILENAME,
					DOC_FIELD_RESOURCE_DESCRIPTION,
					DOC_FIELD_RESOURCE_READABLE,
					DOC_FIELD_RESOURCE_OPEN
				)));
		}

		@Test
		void getAccountProfileImg_AccountProfileImgNotFound_NotFoundStatus()
			throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				get("/account/profileImgs/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class SearchAccountProfileImgs {

		@Test
		void listAccountProfileImgs() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				AccountProfileImg accountProfileImgA = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgB = entityHelper.generateAccountProfileImg();

				return new Struct()
					.withValue("accountProfileImgAId", accountProfileImgA.getId())
					.withValue("accountProfileImgBId", accountProfileImgB.getId());
			});
			Long accountProfileImgAId = given.valueOf("accountProfileImgAId");
			Long accountProfileImgBId = given.valueOf("accountProfileImgBId");

			// When
			ResultActions actions = mockMvc.perform(get("/account/profileImgs"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountProfileImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.accountProfileImgs.[*].id").value(containsInAnyOrder(
					accountProfileImgAId.intValue(),
					accountProfileImgBId.intValue()
				)));

			// Document
			actions.andDo(document("accountProfileImg-list-example",
				requestParameters(
					ApiDocumentation.DOC_PARAMETER_PAGE,
					ApiDocumentation.DOC_PARAMETER_SIZE,
					ApiDocumentation.DOC_PARAMETER_SORT
				)));
		}

		@Test
		void listAccountProfileImgsWithPaging() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				AccountProfileImg accountProfileImgA = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgB = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgC = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgD = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgE = entityHelper.generateAccountProfileImg();

				return new Struct()
					.withValue("accountProfileImgBId", accountProfileImgB.getId())
					.withValue("accountProfileImgCId", accountProfileImgC.getId());
			});
			Long accountProfileImgBId = given.valueOf("accountProfileImgBId");
			Long accountProfileImgCId = given.valueOf("accountProfileImgCId");

			// When
			ResultActions actions = mockMvc.perform(get("/account/profileImgs")
				.param("size", "2")
				.param("page", "1")
				.param("sort", "id,desc"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountProfileImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.accountProfileImgs.[*].id").value(contains(
					accountProfileImgCId.intValue(),
					accountProfileImgBId.intValue()
				)));

			// Document
			actions.andDo(document("accountProfileImg-list-with-paging-example"));
		}

		@Test
		void searchAccountProfileImgsByOwnerId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();

				AccountProfileImg accountProfileImgA = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgB = entityHelper.generateAccountProfileImg(
					it -> it.withOwner(account)
				);
				AccountProfileImg accountProfileImgC = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgD = entityHelper.generateAccountProfileImg();
				AccountProfileImg accountProfileImgE = entityHelper.generateAccountProfileImg();

				return new Struct()
					.withValue("ownerId", account.getId())
					.withValue("accountProfileImgBId", accountProfileImgB.getId());
			});
			Long ownerId = given.valueOf("ownerId");
			Long accountProfileImgBId = given.valueOf("accountProfileImgBId");

			// When
			ResultActions actions = mockMvc.perform(get("/account/profileImgs")
				.param("ownerId", ownerId.toString()));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountProfileImgs").value(aMapWithSize(1)))
				.andExpect(jsonPath("$.accountProfileImgs.[*].id").value(containsInAnyOrder(
					accountProfileImgBId.intValue()
				)));

			// Document
			actions.andDo(document("accountProfileImg-search-example",
				requestParameters(
					DOC_PARAMETER_OWNER_ID
				)));
		}

	}

	@Nested
	class DeleteAccountProfileImg {

		@Test
		void deleteAccountProfileImg() throws Exception {
			// Given
			File originFile = new File(TEST_STORE_FILE_NAME);
			File newFile = new File(TEST_STORE_FILE_NAME2);
			Files.copy(originFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				AccountProfileImg accountProfileImg = entityHelper
					.generateAccountProfileImg(it -> it
						.withUploadFileName("testProfileImg.png")
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
				delete("/account/profileImgs/{id}", id)
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
				delete("/account/profileImgs/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

}
