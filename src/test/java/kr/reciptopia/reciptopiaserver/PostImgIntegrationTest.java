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
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
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
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.UploadFileAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostImgRepository;
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
public class PostImgIntegrationTest {

	private static final String UUID_REGEX =
		"\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12}";
	private static final int UUID_LENGTH = 36;

	private static final String TEST_IMG_FILE_NAME = "testPostImg.png";
	private static final String TEST_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testPostImg.png";
	private static final String TEST_IMG_FILE_NAME2 = "testPostImg2.png";
	private static final String TEST_IMG_FILE_PATH2 = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testPostImg2.png";
	private static final String TEST_TXT_FILE_NAME = "testText.txt";
	private static final String TEST_TXT_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testText.txt";
	private static final String TEST_ORIGIN_STORE_FILE_NAME =
		"faf9da02-4762-461c-bc6f-48a05b561a8e.png";
	private static final String TEST_ORIGIN_STORE_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/uploaded/faf9da02-4762-461c-bc6f-48a05b561a8e.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_NAME = "testPostImg3.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_PATH = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testPostImg3.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_NAME2 = "testPostImg4.png";
	private static final String TEST_NEW_UPLOAD_IMG_FILE_PATH2 = System.getProperty("user.dir")
		+ "/src/test/resources/testfiles/source/testPostImg4.png";
	private static final String TEST_STORE_FILE_NAME = "634f963f-b61d-414b-b35b-57ab90533d36.png";
	private static final String TEST_STORE_FILE_NAME2 = "b88e22ad-ab50-44cb-8dd2-6da0f23ce9ef.png";

	private static final FieldDescriptor DOC_FIELD_ID =
		fieldWithPath("id").description("업로드한 게시물 이미지 ID");
	private static final FieldDescriptor DOC_FIELD_UPLOADED_FILE_NAME =
		fieldWithPath("uploadFileName").description("업로드한 이미지 파일 이름");
	private static final FieldDescriptor DOC_FIELD_STORED_FILE_NAME =
		fieldWithPath("storeFileName").description("업로드되어 서버에 저장된 이미지 파일 이름");
	private static final FieldDescriptor DOC_FIELD_POST_ID =
		fieldWithPath("postId").description("업로드된 게시물 이미지가 속한 게시물 ID");
	private static final FieldDescriptor DOC_FIELD_PUT_BULK_POST_IMGS =
		subsectionWithPath("postImgs").type("Map<id, postImg>")
			.description("<게시물 이미지 ID, 게시물 이미지 업로드 정보>");

	private static final ParameterDescriptor DOC_PARAMETER_POST_ID =
		parameterWithName("postId").description("게시물 ID").optional();

	private static final RequestPartDescriptor DOC_PART_POST_ID =
		partWithName("postId").description("이미지를 업로드할 게시물 ID");
	private static final RequestPartDescriptor DOC_PART_IMAGE_FILE =
		partWithName("imgFile").description("업로드할 이미지 파일");
	private static final RequestPartDescriptor DOC_PART_IMAGE_FILES =
		partWithName("imgFiles").description("업로드할 이미지 파일들");

	@Autowired
	PasswordEncoder passwordEncoder;
	@Value("${file.upload.location}")
	private String fileDir;
	private MockMvc mockMvc;
	@Autowired
	private JsonHelper jsonHelper;
	@Autowired
	private PostImgRepository repository;
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
	class CreatePostImg {

		@Test
		void createPostImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);
			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/images")
					.file(postIdMultipart)
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(
					jsonPath("$.uploadFileName").value(imgFileMultipart.getOriginalFilename()))
				.andExpect(jsonPath("$.storeFileName").value(stringContainsInOrder(
					".png"
				)));

			// Document
			actions.andDo(document("postImg-create-example",
				requestParts(
					DOC_PART_POST_ID,
					DOC_PART_IMAGE_FILE
				),
				responseFields(
					DOC_FIELD_ID,
					DOC_FIELD_UPLOADED_FILE_NAME,
					DOC_FIELD_STORED_FILE_NAME,
					DOC_FIELD_POST_ID
				)));
		}

		@Test
		void createPostImg_PostImgUnauthorized_UnauthorizedStatus()
			throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();

				return new Struct()
					.withValue("postId", post.getId());
			});
			Long postId = given.valueOf("postId");

			FileInputStream fp = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile imgFileMultipart = new MockMultipartFile(
				"imgFile", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp
			);
			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/images")
					.file(postIdMultipart)
					.file(imgFileMultipart)
			);

			// Then
			actions
				.andExpect(status().isUnauthorized());
		}

		@Test
		void createPostImg_RequestPart의_postId_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

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
				multipart("/post/images")
					.file(imgFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void createPostImg_RequestPart의_imgFile_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/images")
					.file(postIdMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void createPostImg_이미지가_아닌_파일_업로드() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			FileInputStream fp = new FileInputStream(TEST_TXT_FILE_PATH);

			MockMultipartFile txtFileMultipart = new MockMultipartFile(
				"imgFile", TEST_TXT_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, fp
			);
			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/images")
					.file(postIdMultipart)
					.file(txtFileMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

	}

	@Nested
	class BulkPutPostImg {

		@Test
		void bulkPutPostImg_Post에_기존_PostImg가_없는_경우() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			FileInputStream fp1 = new FileInputStream(TEST_IMG_FILE_PATH);
			FileInputStream fp2 = new FileInputStream(TEST_IMG_FILE_PATH2);

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);
			MockMultipartFile imgFileMultipart1 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp1
			);
			MockMultipartFile imgFileMultipart2 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME2, MediaType.IMAGE_PNG_VALUE, fp2
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(postIdMultipart)
					.file(imgFileMultipart1)
					.file(imgFileMultipart2)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.postImgs.[*].id").exists())
				.andExpect(jsonPath("$.postImgs.[*].postId").value(contains(
					postId.intValue(), postId.intValue()
				)))
				.andExpect(jsonPath("$.postImgs.[*].uploadFileName").value(contains(
					TEST_IMG_FILE_NAME,
					TEST_IMG_FILE_NAME2
				)))
				.andExpect(jsonPath("$.postImgs.[*].storeFileName").exists());

			// Document
			actions.andDo(document("postImg-bulk-put-example",
				requestParts(
					DOC_PART_POST_ID,
					DOC_PART_IMAGE_FILES
				),
				responseFields(
					DOC_FIELD_PUT_BULK_POST_IMGS
				)));
		}

		@Test
		void bulkPutPostImg_Post에_기존_PostImg가_있는_경우() throws Exception {
			// Given - 기존 PostImg 1개 존재 가정
			File file = new File(TEST_IMG_FILE_PATH);
			File originUploadedFile = new File(TEST_ORIGIN_STORE_FILE_PATH);
			Files.copy(file.toPath(), originUploadedFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				PostImg postImg = entityHelper.generatePostImg(it -> it
					.withPost(post)
					.withUploadFileName(TEST_IMG_FILE_NAME)
					.withStoreFileName(TEST_ORIGIN_STORE_FILE_NAME)
				);
				String token = uploadFileAuthHelper.generateToken(postImg);

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			// Upload New File
			FileInputStream fp1 = new FileInputStream(TEST_NEW_UPLOAD_IMG_FILE_PATH);
			FileInputStream fp2 = new FileInputStream(TEST_NEW_UPLOAD_IMG_FILE_PATH2);

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);
			MockMultipartFile imgFileMultipart1 = new MockMultipartFile(
				"imgFiles", TEST_NEW_UPLOAD_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp1
			);
			MockMultipartFile imgFileMultipart2 = new MockMultipartFile(
				"imgFiles", TEST_NEW_UPLOAD_IMG_FILE_NAME2, MediaType.IMAGE_PNG_VALUE, fp2
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(postIdMultipart)
					.file(imgFileMultipart1)
					.file(imgFileMultipart2)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isCreated())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.postImgs.[*].id").exists())
				.andExpect(jsonPath("$.postImgs.[*].postId").value(contains(
					postId.intValue(), postId.intValue()
				)))
				.andExpect(jsonPath("$.postImgs.[*].uploadFileName").value(contains(
					TEST_NEW_UPLOAD_IMG_FILE_NAME,
					TEST_NEW_UPLOAD_IMG_FILE_NAME2
				)))
				.andExpect(jsonPath("$.postImgs.[*].storeFileName").exists());
		}

		@Test
		void bulkPutPostImg_PostImgUnauthorized_UnauthorizedStatus() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();

				return new Struct()
					.withValue("postId", post.getId());
			});
			Long postId = given.valueOf("postId");

			FileInputStream fp1 = new FileInputStream(TEST_IMG_FILE_PATH);
			FileInputStream fp2 = new FileInputStream(TEST_IMG_FILE_PATH2);

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);
			MockMultipartFile imgFileMultipart1 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp1
			);
			MockMultipartFile imgFileMultipart2 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME2, MediaType.IMAGE_PNG_VALUE, fp2
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(postIdMultipart)
					.file(imgFileMultipart1)
					.file(imgFileMultipart2)
			);

			// Then
			actions
				.andExpect(status().isUnauthorized());
		}

		@Test
		void bulkPutPostImg_RequestPart의_postId_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token);
			});
			String token = given.valueOf("token");

			FileInputStream fp1 = new FileInputStream(TEST_IMG_FILE_PATH);
			FileInputStream fp2 = new FileInputStream(TEST_IMG_FILE_PATH2);

			MockMultipartFile imgFileMultipart1 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME, MediaType.IMAGE_PNG_VALUE, fp1
			);
			MockMultipartFile imgFileMultipart2 = new MockMultipartFile(
				"imgFiles", TEST_IMG_FILE_NAME2, MediaType.IMAGE_PNG_VALUE, fp2
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(imgFileMultipart1)
					.file(imgFileMultipart2)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void bulkPutPostImg_RequestPart의_imgFiles_누락() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(postIdMultipart)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

		@Test
		void bulkPutPostImg_이미지가_아닌_파일_업로드() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();
				String token = uploadFileAuthHelper.generateToken(post.getOwner());

				return new Struct()
					.withValue("token", token)
					.withValue("postId", post.getId());
			});
			String token = given.valueOf("token");
			Long postId = given.valueOf("postId");

			FileInputStream fp1 = new FileInputStream(TEST_IMG_FILE_PATH);

			MockMultipartFile postIdMultipart = new MockMultipartFile(
				"postId", "postId", MediaType.APPLICATION_JSON_VALUE,
				postId.toString().getBytes(StandardCharsets.UTF_8)
			);
			MockMultipartFile imgFileMultipart1 = new MockMultipartFile(
				"imgFiles", TEST_TXT_FILE_NAME, MediaType.TEXT_PLAIN_VALUE, fp1
			);

			// When
			ResultActions actions = mockMvc.perform(
				multipart("/post/bulk-images")
					.file(postIdMultipart)
					.file(imgFileMultipart1)
					.header("Authorization", "Bearer " + token)
			);

			// Then
			actions
				.andExpect(status().isBadRequest());
		}

	}

	@Nested
	class DownloadPostImg {

		@Test
		void downloadPostImg() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				PostImg postImg = entityHelper.generatePostImg(it -> it
					.withStoreFileName(TEST_STORE_FILE_NAME)
				);

				return new Struct()
					.withValue("id", postImg.getId());
			});
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/images/{id}/download", id));

			// Then
			actions
				.andExpect(status().isOk());

			// Document
			actions.andDo(document("postImg-download-example"));
		}

		@Test
		void downloadPostImg_PostImgNotFound_NotFoundStatus() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				get("/post/images/{id}/download", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class SearchPostImgs {

		@Test
		void listPostImgs() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				PostImg postImgA = entityHelper.generatePostImg();
				PostImg postImgB = entityHelper.generatePostImg();

				return new Struct()
					.withValue("postImgAId", postImgA.getId())
					.withValue("postImgBId", postImgB.getId());
			});
			Long postImgAId = given.valueOf("postImgAId");
			Long postImgBId = given.valueOf("postImgBId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/images"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.postImgs.[*].id").value(containsInAnyOrder(
					postImgAId.intValue(),
					postImgBId.intValue()
				)));

			// Document
			actions.andDo(document("postImg-list-example",
				requestParameters(
					ApiDocumentation.DOC_PARAMETER_PAGE,
					ApiDocumentation.DOC_PARAMETER_SIZE,
					ApiDocumentation.DOC_PARAMETER_SORT
				)));
		}

		@Test
		void listPostImgsWithPaging() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				PostImg postImgA = entityHelper.generatePostImg();
				PostImg postImgB = entityHelper.generatePostImg();
				PostImg postImgC = entityHelper.generatePostImg();
				PostImg postImgD = entityHelper.generatePostImg();
				PostImg postImgE = entityHelper.generatePostImg();

				return new Struct()
					.withValue("postImgBId", postImgB.getId())
					.withValue("postImgCId", postImgC.getId());
			});
			Long postImgBId = given.valueOf("postImgBId");
			Long postImgCId = given.valueOf("postImgCId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/images")
					.param("size", "2")
					.param("page", "1")
					.param("sort", "id,desc"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.postImgs.[*].id").value(contains(
					postImgCId.intValue(),
					postImgBId.intValue()
				)));

			// Document
			actions.andDo(document("postImg-list-with-paging-example"));
		}

		@Test
		void searchPostImgsByPostId() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost();

				PostImg postImgA = entityHelper.generatePostImg();
				PostImg postImgB = entityHelper.generatePostImg(it -> it
					.withPost(post)
				);
				PostImg postImgC = entityHelper.generatePostImg(it -> it
					.withPost(post)
				);
				PostImg postImgD = entityHelper.generatePostImg();
				PostImg postImgE = entityHelper.generatePostImg();

				return new Struct()
					.withValue("postId", post.getId())
					.withValue("postImgBId", postImgB.getId())
					.withValue("postImgCId", postImgC.getId());
			});
			Long postId = given.valueOf("postId");
			Long postImgBId = given.valueOf("postImgBId");
			Long postImgCId = given.valueOf("postImgCId");

			// When
			ResultActions actions = mockMvc.perform(
				get("/post/images")
					.param("postId", postId.toString()));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postImgs").value(aMapWithSize(2)))
				.andExpect(jsonPath("$.postImgs.[*].id").value(containsInAnyOrder(
					postImgCId.intValue(),
					postImgBId.intValue()
				)));

			// Document
			actions.andDo(document("postImg-search-example",
				requestParameters(
					DOC_PARAMETER_POST_ID
				)));
		}

	}

	@Nested
	class DeletePostImg {

		@Test
		void deletePostImg() throws Exception {
			// Given
			File originFile = new File(fileDir + TEST_STORE_FILE_NAME);
			File newFile = new File(fileDir + TEST_STORE_FILE_NAME2);
			Files.copy(originFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			Struct given = trxHelper.doInTransaction(() -> {
				PostImg postImg = entityHelper.generatePostImg(it -> it
					.withUploadFileName(TEST_IMG_FILE_NAME)
					.withStoreFileName(TEST_STORE_FILE_NAME2)
				);
				String token = uploadFileAuthHelper.generateToken(postImg);

				return new Struct()
					.withValue("token", token)
					.withValue("id", postImg.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/images/{id}", id)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isNoContent())
				.andExpect(content().string(emptyString()));

			assertThat(repository.existsById(id)).isFalse();

			// Document
			actions.andDo(document("postImg-delete-example"));
		}

		@Test
		void deletePostImg_PostImgNotFound_NotFound_Status() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/images/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

}
