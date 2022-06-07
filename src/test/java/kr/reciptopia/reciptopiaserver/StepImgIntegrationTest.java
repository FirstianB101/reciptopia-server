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
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.UploadFileAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepImgRepository;
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
public class StepImgIntegrationTest {

	private static final String UUID_REGEX =
		"\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12}";
	private static final int UUID_LENGTH = 36;

	private static final String TEST_IMG_FILE_NAME = "testStepImg.jpeg";
	private static final String TEST_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testStepImg.jpeg";
	private static final String TEST_TXT_FILE_NAME = "testText.txt";
	private static final String TEST_TXT_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testText.txt";
	private static final String TEST_ORIGIN_STORE_FILE_NAME =
		"faf9da02-4762-461c-bc6f-48a05b561a8e.jpeg";
	private static final String TEST_ORIGIN_STORE_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/uploaded/faf9da02-4762-461c-bc6f-48a05b561a8e.jpeg";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_NAME = "testStepImg2.jpeg";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testStepImg2.jpeg";
	private static final String TEST_STORE_FILE_NAME = "689f963f-b61d-414b-b35b-57ab90533d36.jpeg";
	private static final String TEST_STORE_FILE_NAME2 = "b88e22ad-ab50-44cb-8dd2-6da0f23ce9ef.jpeg";

	private static final FieldDescriptor DOC_FIELD_ID =
		fieldWithPath("id").description("업로드한 조리 단계 이미지 ID");
	private static final FieldDescriptor DOC_FIELD_UPLOADED_FILE_NAME =
		fieldWithPath("uploadFileName").description("업로드한 이미지 파일 이름");
	private static final FieldDescriptor DOC_FIELD_STORED_FILE_NAME =
		fieldWithPath("storeFileName").description("업로드되어 서버에 저장된 이미지 파일 이름");
	private static final FieldDescriptor DOC_FIELD_STEP_ID =
		fieldWithPath("stepId").description("업로드된 이미지가 속한 조리 단계 ID");

	private static final ParameterDescriptor DOC_PARAMETER_STEP_ID =
		parameterWithName("stepId").description("조리 단계 ID").optional();
	private static final ParameterDescriptor DOC_PARAMETER_RECIPE_ID =
		parameterWithName("recipeId").description("레시피 ID").optional();

	private static final RequestPartDescriptor DOC_PART_STEP_ID =
		partWithName("stepId").description("이미지를 업로드할 조리 단계 ID");
	private static final RequestPartDescriptor DOC_PART_IMAGE_FILE =
		partWithName("imgFile").description("업로드할 이미지 파일");
	@Autowired
	PasswordEncoder passwordEncoder;
	@Value("${file.upload.location}")
	private String fileDir;
	private MockMvc mockMvc;
	@Autowired
	private JsonHelper jsonHelper;
	@Autowired
	private StepImgRepository repository;
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
			if (uuidMatcher.matches() && ext.equals("jpeg")) {
				file.delete();
			}
		}
	}

	private Matcher createUUIDMatcher(String validationStr) {
		Pattern pattern = Pattern.compile(UUID_REGEX);
		return pattern.matcher(validationStr);
	}

	@Nested
	class PutStepImg {

		@Test
		void putStepImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();
				String token = uploadFileAuthHelper.generateToken(
					step.getRecipe().getPost().getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("stepId", step.getId());
			});
			String token = given.valueOf("token");
			Long stepId = given.valueOf("stepId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_JPEG_VALUE, fp
			);
			MockMultipartFile stepIdMultipart = new MockMultipartFile(
				"stepId", "stepId", MediaType.APPLICATION_JSON_VALUE,
				stepId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(stepIdMultipart)
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.uploadFileName").value(
					imgFileMultipart.getOriginalFilename()))
				.andExpect(jsonPath("$.storeFileName").value(stringContainsInOrder(
					".jpeg"
				)));

			// Document
			actions.andDo(document("stepImg-create-example",
				requestParts(
					DOC_PART_STEP_ID,
					DOC_PART_IMAGE_FILE
				),
				responseFields(
					DOC_FIELD_ID,
					DOC_FIELD_UPLOADED_FILE_NAME,
					DOC_FIELD_STORED_FILE_NAME,
					DOC_FIELD_STEP_ID
				)));
		}

		@Test
		void putStepImg_StepImgUnauthorized_UnauthorizedStatus() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();

				return new Struct()
					.withValue("stepId", step.getId());
			});
			Long stepId = given.valueOf("stepId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_JPEG_VALUE, fp
			);
			MockMultipartFile stepIdMultipart = new MockMultipartFile(
				"stepId", "stepId", MediaType.APPLICATION_JSON_VALUE,
				stepId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(stepIdMultipart)
					.file(imgFileMultipart)
			);

			// Then
			actions
				.andExpect(status().isUnauthorized());
		}

		@Test
		void putStepImg_RequestPart의_stepId_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();
				String token = uploadFileAuthHelper.generateToken(
					step.getRecipe().getPost().getOwner());

				return new Struct()
					.withValue("token", token);
			});
			String token = given.valueOf("token");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_JPEG_VALUE, fp
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void putStepImg_RequestPart의_imgFile_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();
				String token = uploadFileAuthHelper.generateToken(
					step.getRecipe().getPost().getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("stepId", step.getId());
			});
			String token = given.valueOf("token");
			Long stepId = given.valueOf("stepId");

			MockMultipartFile stepIdMultipart = new MockMultipartFile(
				"stepId", "stepId", MediaType.APPLICATION_JSON_VALUE,
				stepId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(stepIdMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void putStepImg_이미지가_아닌_파일_업로드() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();
				String token = uploadFileAuthHelper.generateToken(
					step.getRecipe().getPost().getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("stepId", step.getId());
			});
			String token = given.valueOf("token");
			Long stepId = given.valueOf("stepId");

			FileInputStream fp = new FileInputStream(TEST_TXT_FILE_PATH);

			MockMultipartFile txtFileMultipart = new MockMultipartFile(
				"imgFile", TEST_TXT_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, fp
			);
			MockMultipartFile stepIdMultipart = new MockMultipartFile(
				"stepId", "stepId", MediaType.APPLICATION_JSON_VALUE,
				stepId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(stepIdMultipart)
					.file(txtFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void putStepImg_이미지_파일_업로드_수정() throws Exception {
			// Given - 기존 StepImg 존재 가정
			File file = new File(TEST_IMG_FILE_PATH);
			File originUploadedFile = new File(TEST_ORIGIN_STORE_FILE_PATH);
			Files.copy(file.toPath(), originUploadedFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();
				StepImg stepImg = entityHelper.generateStepImg(it -> it
					.withStep(step)
					.withUploadFileName(TEST_IMG_FILE_NAME)
					.withStoreFileName(TEST_ORIGIN_STORE_FILE_NAME)
				);
				String token = uploadFileAuthHelper.generateToken(stepImg);

				return new Struct()
					.withValue("token", token)
					.withValue("stepId", step.getId())
					.withValue("stepImgId", stepImg.getId());
			});
			String token = given.valueOf("token");
			Long stepId = given.valueOf("stepId");
			Long stepImgId = given.valueOf("stepImgId");

			// Upload New File
			FileInputStream fp = new FileInputStream(TEST_NEW_UPLOAD_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_NEW_UPLOAD_IMG_FILE_NAME, MediaType.IMAGE_JPEG_VALUE, fp
			);
			MockMultipartFile stepIdMultipart = new MockMultipartFile(
				"stepId", "stepId", MediaType.APPLICATION_JSON_VALUE,
				stepId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/recipe/step/images")
					.file(stepIdMultipart)
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(stepImgId))
				.andExpect(jsonPath("$.stepId").value(stepId))
				.andExpect(jsonPath("$.uploadFileName").value(
					imgFileMultipart.getOriginalFilename()))
				.andExpect(jsonPath("$.storeFileName").value(stringContainsInOrder(
					".jpeg"
				)));

			assertThat(repository.findByStepId(stepId).stream().count()).isOne();
		}

	}

	@Nested
	class DownloadStepImg {

		@Test
		void downloadStepImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				StepImg stepImg = entityHelper.generateStepImg(it -> it
					.withStoreFileName(TEST_STORE_FILE_NAME)
				);

				return new Struct()
					.withValue("id", stepImg.getId());
			});
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images/{id}/download", id));

			// Then
			actions
				.andExpect(status().isOk());

			// Document
			actions.andDo(document("stepImg-download-example"));
		}

		@Test
		void downloadStepImg_StepImgNotFound_NotFoundStatus() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images/{id}/download", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class DownloadStepImgByStepId {

		@Test
		void downloadStepImgByStepId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();

				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg(it -> it
					.withStep(step)
					.withStoreFileName(TEST_STORE_FILE_NAME)
				);
				StepImg stepImgC = entityHelper.generateStepImg();
				StepImg stepImgD = entityHelper.generateStepImg();
				StepImg stepImgE = entityHelper.generateStepImg();

				return new Struct()
					.withValue("stepId", step.getId());
			});
			Long stepId = given.valueOf("stepId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images/download")
					.param("stepId", stepId.toString()));

			// Then
			actions
				.andExpect(status().isOk());

			// Document
			actions.andDo(document("stepImg-downloadByStepId-example",
				requestParameters(
					DOC_PARAMETER_STEP_ID
				)));
		}

		@Test
		void downloadStepImgByStepId_조리과정_이미지가_없는_경우() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();

				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg();
				StepImg stepImgC = entityHelper.generateStepImg();
				StepImg stepImgD = entityHelper.generateStepImg();
				StepImg stepImgE = entityHelper.generateStepImg();

				return new Struct()
					.withValue("stepId", step.getId());
			});
			Long stepId = given.valueOf("stepId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images/download")
					.param("stepId", stepId.toString()));

			// Then
			actions
				.andExpect(status().isOk());
		}

	}

	@Nested
	class SearchStepImgs {

		@Test
		void listStepImgs() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg();

				return new Struct()
					.withValue("stepImgAId", stepImgA.getId())
					.withValue("stepImgBId", stepImgB.getId());
			});
			Long stepImgAId = given.valueOf("stepImgAId");
			Long stepImgBId = given.valueOf("stepImgBId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stepImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.stepImgs.[*].id").value(containsInAnyOrder(
					stepImgAId.intValue(),
					stepImgBId.intValue()
				)));

			// Document
			actions.andDo(document("stepImg-list-example",
				requestParameters(
					ApiDocumentation.DOC_PARAMETER_PAGE,
					ApiDocumentation.DOC_PARAMETER_SIZE,
					ApiDocumentation.DOC_PARAMETER_SORT
				)));
		}

		@Test
		void listStepImgsWithPaging() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg();
				StepImg stepImgC = entityHelper.generateStepImg();
				StepImg stepImgD = entityHelper.generateStepImg();
				StepImg stepImgE = entityHelper.generateStepImg();

				return new Struct()
					.withValue("stepImgBId", stepImgB.getId())
					.withValue("stepImgCId", stepImgC.getId());
			});
			Long stepImgBId = given.valueOf("stepImgBId");
			Long stepImgCId = given.valueOf("stepImgCId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images")
					.param("size", "2")
					.param("page", "1")
					.param("sort", "id,desc"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stepImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.stepImgs.[*].id").value(contains(
					stepImgCId.intValue(),
					stepImgBId.intValue()
				)));

			// Document
			actions.andDo(document("stepImg-list-with-paging-example"));
		}

		@Test
		void searchStepImgByStepId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Step step = entityHelper.generateStep();

				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg(it -> it
					.withStep(step));
				StepImg stepImgC = entityHelper.generateStepImg();
				StepImg stepImgD = entityHelper.generateStepImg();
				StepImg stepImgE = entityHelper.generateStepImg();

				return new Struct()
					.withValue("stepId", step.getId())
					.withValue("stepImgBId", stepImgB.getId());
			});
			Long stepId = given.valueOf("stepId");
			Long stepImgBId = given.valueOf("stepImgBId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images")
					.param("stepId", stepId.toString()));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stepImgs").value(aMapWithSize(1)))
				.andExpect(jsonPath("$.stepImgs.[*].id").value(contains(
					stepImgBId.intValue()
				)));
		}

		@Test
		void searchStepImgsByRecipeId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Recipe recipe = entityHelper.generateRecipe();

				Step step1 = entityHelper.generateStep(it -> it
					.withRecipe(recipe));
				Step step2 = entityHelper.generateStep(it -> it
					.withRecipe(recipe));
				Step step3 = entityHelper.generateStep(it -> it
					.withRecipe(recipe));

				StepImg stepImgA = entityHelper.generateStepImg();
				StepImg stepImgB = entityHelper.generateStepImg(it -> it
					.withStep(step1));
				StepImg stepImgC = entityHelper.generateStepImg();
				StepImg stepImgD = entityHelper.generateStepImg(it -> it
					.withStep(step2));
				StepImg stepImgE = entityHelper.generateStepImg(it -> it
					.withStep(step3));

				return new Struct()
					.withValue("recipeId", recipe.getId())
					.withValue("stepImgBId", stepImgB.getId())
					.withValue("stepImgDId", stepImgD.getId())
					.withValue("stepImgEId", stepImgE.getId());
			});
			Long recipeId = given.valueOf("recipeId");
			Long stepImgBId = given.valueOf("stepImgBId");
			Long stepImgDId = given.valueOf("stepImgDId");
			Long stepImgEId = given.valueOf("stepImgEId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/recipe/step/images")
					.param("recipeId", recipeId.toString()));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stepImgs").value(aMapWithSize(3)))
				.andExpect(jsonPath("$.stepImgs.[*].id").value(containsInAnyOrder(
					stepImgBId.intValue(),
					stepImgDId.intValue(),
					stepImgEId.intValue()
				)));

			// Document
			actions.andDo(document("stepImg-search-example",
				requestParameters(
					DOC_PARAMETER_STEP_ID,
					DOC_PARAMETER_RECIPE_ID
				)));
		}

	}

	@Nested
	class DeleteStepImg {

		@Test
		void deleteStepImg() throws Exception {
			// Given
			File originFile = new File(fileDir + TEST_STORE_FILE_NAME);
			File newFile = new File(fileDir + TEST_STORE_FILE_NAME2);
			Files.copy(originFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				StepImg stepImg = entityHelper.generateStepImg(it -> it
					.withUploadFileName(TEST_IMG_FILE_NAME)
					.withStoreFileName(TEST_STORE_FILE_NAME2)
				);
				String token = uploadFileAuthHelper.generateToken(stepImg);

				return new Struct()
					.withValue("token", token)
					.withValue("id", stepImg.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/recipe/step/images/{id}", id)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isNoContent())
				.andExpect(content().string(emptyString()));

			assertThat(repository.findById(id)).isEmpty();

			// Document
			actions.andDo(document("stepImg-delete-example"));
		}

		@Test
		void deleteStepImg_StepImgNotFound_NotFound_Status() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/recipe/step/images/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

}
