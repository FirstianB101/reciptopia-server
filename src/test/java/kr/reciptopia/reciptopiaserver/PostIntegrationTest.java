package kr.reciptopia.reciptopiaserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.helper.AuthHelper;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPostUpdateDto;
import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostIntegrationTest {

	private static final FieldDescriptor DOC_FIELD_ID =
			fieldWithPath("id").description("게시물 ID");
	private static final FieldDescriptor DOC_FIELD_TITLE =
			fieldWithPath("title").description("게시물 제목");
	private static final FieldDescriptor DOC_FIELD_CONTENT =
			fieldWithPath("content").description("게시물 내용");
	private static final FieldDescriptor DOC_FIELD_PICTURE_URLS =
			fieldWithPath("pictureUrls").description("게시물 사진 URL 목록");
	private static final FieldDescriptor DOC_FIELD_VIEWS =
			fieldWithPath("views").description("게시물 조회 수");
	private static final FieldDescriptor DOC_FIELD_OWNER_ID =
			fieldWithPath("ownerId").description("글쓴이 ID");
	private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
			fieldWithPath("recipeId").description("레시피 ID");

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PostRepository repository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TransactionHelper trxHelper;

	@Autowired
	private EntityHelper entityHelper;

	@Autowired
	private AuthHelper authHelper;

	@Autowired
	PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext,
			   RestDocumentationContextProvider restDocumentation) throws SQLException {
		H2DbCleaner.clean(dataSource);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.apply(springSecurity())
				.apply(basicDocumentationConfiguration(restDocumentation))
				.build();
	}

	private String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	private <T> T fromJson(String responseBody, Class<T> clazz) throws JsonProcessingException {
		return objectMapper.readValue(responseBody, clazz);
	}

	@Nested
	class PostPost {

		@Test
		void postPost() throws Exception {
			// Given - Account, Recipe
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();

				String token = authHelper.generateToken(account);
				return new Struct()
						.withValue("token", token)
						.withValue("id", account.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("id");
//			Recipe recipe = Recipe.builder().build();

			// When
			List<String> pictureUrls = new ArrayList<>();
			pictureUrls.add("C:\\Users\\eunsung\\Desktop\\temp\\picture");
			pictureUrls.add("C:\\Users\\tellang\\Desktop\\temp\\picture");

			Create dto = Create.builder()
					.ownerId(ownerId)
					.recipeId(1L)                    // 임시
					.title("매콤 가문어 볶음 만들기")
					.content("매콤매콤 맨들맨들 가문어 볶음")
					.pictureUrls(pictureUrls)
					.build();
			String body = toJson(dto);

			ResultActions actions = mockMvc.perform(post("/posts")
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token)
					.content(body));

			// Then
			MvcResult mvcResult = actions
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").isNumber())
					.andExpect(jsonPath("$.title").value("매콤 가문어 볶음 만들기"))
					.andExpect(jsonPath("$.content").value("매콤매콤 맨들맨들 가문어 볶음"))
					.andExpect(jsonPath("$.pictureUrls").value(hasSize(2)))
					.andExpect(jsonPath("$.pictureUrls").value(contains(
							pictureUrls.get(0),
							pictureUrls.get(1)
					)))
					.andExpect(jsonPath("$.ownerId").value(ownerId))
//				.andExpect(jsonPath("$.recipeId").value(1L))		// 임시
					.andReturn();

			// Document
			actions.andDo(document("post-create-example",
					requestFields(
							DOC_FIELD_OWNER_ID,
							DOC_FIELD_RECIPE_ID,
							DOC_FIELD_TITLE,
							DOC_FIELD_CONTENT,
							DOC_FIELD_PICTURE_URLS
					)));
		}
	}

	@Nested
	class GetPost {

		@Test
		void getPost() throws Exception {
			// Given
			Account owner = trxHelper.doInTransaction(() ->
					entityHelper.generateAccount()
			);

			List<String> pictureUrls = new ArrayList<>();
			pictureUrls.add("C:\\Users\\eunsung\\Desktop\\temp\\picture");
			pictureUrls.add("C:\\Users\\tellang\\Desktop\\temp\\picture");

			Long id = trxHelper.doInTransaction(() -> {
				Post post = entityHelper.generatePost(it ->
						it.withTitle("매콤 가문어 볶음 만들기")
								.withContent("매콤매콤 맨들맨들 가문어 볶음")
								.withPictureUrls(pictureUrls)
								.withOwner(owner)
				);
				return post.getId();
			});

			// When
			ResultActions actions = mockMvc
					.perform(get("/posts/{id}", id));

			// Then
			actions
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(id))
					.andExpect(jsonPath("$.title").value("매콤 가문어 볶음 만들기"))
					.andExpect(jsonPath("$.content").value("매콤매콤 맨들맨들 가문어 볶음"))
					.andExpect(jsonPath("$.pictureUrls").value(hasSize(2)))
					.andExpect(jsonPath("$.pictureUrls").value(contains(
							pictureUrls.get(0),
							pictureUrls.get(1)
					)))
					.andExpect(jsonPath("$.ownerId").value(owner.getId()))
//				.andExpect(jsonPath("$.recipeId").value(1L))		// 임시
					.andReturn();

			// Document
			actions.andDo(document("post-retrieve-example",
					responseFields(
							DOC_FIELD_ID,
							DOC_FIELD_OWNER_ID,
							DOC_FIELD_RECIPE_ID,
							DOC_FIELD_TITLE,
							DOC_FIELD_CONTENT,
							DOC_FIELD_PICTURE_URLS,
							DOC_FIELD_VIEWS
					)));
		}

		@Test
		void getPost_PostNotFound_NotFoundStatus() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(get("/posts/{id}", 0L));

			// Then
			actions
					.andExpect(status().isNotFound());
		}

	}

	@Nested
	class SearchPosts {

		@Test
		void listPosts() throws Exception {
			// Given
			Account owner = trxHelper.doInTransaction(() ->
					entityHelper.generateAccount()
			);

			Struct given = trxHelper.doInTransaction(() -> {
				Post postA = entityHelper.generatePost(it -> it.withOwner(owner));
				Post postB = entityHelper.generatePost(it -> it.withOwner(owner));

				return new Struct()
						.withValue("postAId", postA.getId())
						.withValue("postBId", postB.getId());
			});
			Long postAId = given.valueOf("postAId");
			Long postBId = given.valueOf("postBId");

			// When
			ResultActions actions = mockMvc.perform(get("/posts"));

			// Then
			actions
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").value(hasSize(2)))
					.andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
							postAId.intValue(),
							postBId.intValue()
					)));

			// Document
			actions.andDo(document("post-list-example",
					requestParameters(
							ApiDocumentation.DOC_PARAMETER_PAGE,
							ApiDocumentation.DOC_PARAMETER_SIZE,
							ApiDocumentation.DOC_PARAMETER_SORT
					)));
		}

		@Test
		void listPostsWithPaging() throws Exception {
			// Given
			Account owner = trxHelper.doInTransaction(() ->
					entityHelper.generateAccount()
			);

			Struct given = trxHelper.doInTransaction(() -> {
				Post postA = entityHelper.generatePost(it -> it.withOwner(owner));
				Post postB = entityHelper.generatePost(it -> it.withOwner(owner));

				return new Struct()
						.withValue("postAId", postA.getId())
						.withValue("postBId", postB.getId());
			});
			Long postAId = given.valueOf("postAId");
			Long postBId = given.valueOf("postBId");

			// When
			ResultActions actions = mockMvc.perform(get("/posts")
					.param("size", "2")
					.param("page", "0")
					.param("sort", "id,desc"));

			// Then
			actions
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").value(hasSize(2)))
					.andExpect(jsonPath("$.[*].id").value(contains(
							postBId.intValue(),
							postAId.intValue()
					)));

			// Document
			actions.andDo(document("post-list-with-paging-example"));
		}

	}

	@Nested
	class PatchPost {

		@Test
		void patchPost() throws Exception {
			// Given
			List<String> pictureUrls = new ArrayList<>();
			pictureUrls.add("C:\\Users\\eunsung\\Desktop\\temp\\picture");
			pictureUrls.add("C:\\Users\\tellang\\Desktop\\temp\\picture");

			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = authHelper.generateToken(account);
				Post post = entityHelper.generatePost(it ->
						it.withTitle("테스트 요리 만들기")
								.withContent("테스트 요리 컨텐츠")
								.withPictureUrls(pictureUrls)
								.withOwner(account)
				);

				return new Struct()
						.withValue("token", token)
						.withValue("id", post.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			pictureUrls.add("C:\\Users\\silverstar\\Desktop\\temp\\picture");

			Update dto = Update.builder()
					.title("새로운 요리 만들기")
					.content("새로운 요리 컨텐츠")
					.pictureUrls(pictureUrls)
					.build();
			String body = toJson(dto);

			ResultActions actions = mockMvc.perform(patch("/posts/{id}", id)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + token)
					.content(body));

			// Then
			MvcResult mvcResult = actions
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(id))
					.andExpect(jsonPath("$.title").value("새로운 요리 만들기"))
					.andExpect(jsonPath("$.content").value("새로운 요리 컨텐츠"))
					.andExpect(jsonPath("$.pictureUrls").value(hasSize(3)))
					.andExpect(jsonPath("$.pictureUrls").value(contains(
							pictureUrls.get(0),
							pictureUrls.get(1),
							pictureUrls.get(2)
					)))
					.andReturn();

			// Document
			actions.andDo(document("post-update-example",
					requestFields(
							DOC_FIELD_TITLE,
							DOC_FIELD_CONTENT,
							DOC_FIELD_PICTURE_URLS
					)));
		}

		@Test
		void patchPost_PostNotFound_NotFoundStatus() throws Exception {
			// When
			String body = toJson(aPostUpdateDto());

			ResultActions actions = mockMvc.perform(patch("/posts/{id}", 0)
					.contentType(MediaType.APPLICATION_JSON)
					.content(body));

			// Then
			actions
					.andExpect(status().isNotFound());
		}

	}

	@Nested
	class DeletePost {

		@Test
		void deletePost() throws Exception {
			// Given
			List<String> pictureUrls = new ArrayList<>();
			pictureUrls.add("C:\\Users\\eunsung\\Desktop\\temp\\picture");
			pictureUrls.add("C:\\Users\\tellang\\Desktop\\temp\\picture");

			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = authHelper.generateToken(account);
				Post post = entityHelper.generatePost(it ->
						it.withTitle("테스트 요리 만들기")
								.withContent("테스트 요리 컨텐츠")
								.withPictureUrls(pictureUrls)
								.withOwner(account)
				);

				return new Struct()
						.withValue("token", token)
						.withValue("id", post.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(delete("/posts/{id}", id)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
					.andExpect(status().isNoContent())
					.andExpect(content().string(emptyString()));

//			assertThat(repository.findByOwnerIdAndRecipeId()).isEmpty();

			// Document
			actions.andDo(document("post-delete-example"));
		}

		@Test
		void deletePost_PostNotFound_NotFound_Status() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(delete("/posts/{id}", 0));

			// Then
			actions
					.andExpect(status().isNotFound());
		}

	}

}
