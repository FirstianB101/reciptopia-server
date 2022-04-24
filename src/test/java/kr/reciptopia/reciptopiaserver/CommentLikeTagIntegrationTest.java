package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.CommentLikeTagHelper.aCommentLikeTagCreateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.LikeTagAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentLikeTagRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CommentLikeTagIntegrationTest {

	private static final FieldDescriptor DOC_FIELD_ID =
		fieldWithPath("id").description("댓글 좋아요 ID");
	private static final FieldDescriptor DOC_FIELD_OWNER_ID =
		fieldWithPath("ownerId").description("댓글 좋아요 누른 사용자 ID");
	private static final FieldDescriptor DOC_FIELD_COMMENT_ID =
		fieldWithPath("commentId").description("댓글 좋아요가 달린 댓글 ID");

	private MockMvc mockMvc;

	@Autowired
	private JsonHelper jsonHelper;

	@Autowired
	private CommentLikeTagRepository repository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TransactionHelper trxHelper;

	@Autowired
	private EntityHelper entityHelper;

	@Autowired
	private LikeTagAuthHelper likeTagAuthHelper;

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext,
		RestDocumentationContextProvider restDocumentation) throws SQLException {
		H2DbCleaner.clean(dataSource);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(springSecurity())
			.apply(basicDocumentationConfiguration(restDocumentation))
			.build();
	}

	@Nested
	class PostCommentLikeTag {

		@Test
		void postCommentLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = likeTagAuthHelper.generateToken(account);
				Comment comment = entityHelper.generateComment();

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId())
					.withValue("commentId", comment.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");
			Long commentId = given.valueOf("commentId");

			// When
			Create dto = Create.builder()
				.ownerId(ownerId)
				.commentId(commentId)
				.build();
			String body = jsonHelper.toJson(dto);

			ResultActions actions = mockMvc.perform(post("/post/comment/likeTags")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token)
				.content(body));

			// Then
			MvcResult mvcResult = actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(jsonPath("$.commentId").value(commentId))
				.andReturn();

			// Document
			actions.andDo(document("commentLikeTag-create-example",
				requestFields(
					DOC_FIELD_OWNER_ID,
					DOC_FIELD_COMMENT_ID
				)));
		}

		@Test
		void postCommentLikeTag_CommentLikeTagNotFound_NotFoundStatus() throws Exception {
			// When
			String body = jsonHelper.toJson(aCommentLikeTagCreateDto());

			ResultActions actions = mockMvc.perform(post("/post/comment/likeTags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class GetCommentLikeTag {

		@Test
		void getCommentLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				CommentLikeTag commentLikeTag = entityHelper.generateCommentLikeTag();

				return new Struct()
					.withValue("id", commentLikeTag.getId())
					.withValue("ownerId", commentLikeTag.getOwner().getId())
					.withValue("commentId", commentLikeTag.getComment().getId());
			});
			Long id = given.valueOf("id");
			Long ownerId = given.valueOf("ownerId");
			Long commentId = given.valueOf("commentId");

			// When
			ResultActions actions = mockMvc
				.perform(get("/post/comment/likeTags/{id}", id));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(jsonPath("$.commentId").value(commentId));

			// Document
			actions.andDo(document("commentLikeTag-retrieve-example",
				responseFields(
					DOC_FIELD_ID,
					DOC_FIELD_OWNER_ID,
					DOC_FIELD_COMMENT_ID
				)));
		}

		@Test
		void getCommentLikeTag_CommentLikeTagNotFound_NotFoundStatus() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(get("/post/comment/likeTags/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class SearchCommentLikeTags {

		@Test
		void listCommentLikeTags() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				CommentLikeTag commentLikeTagA = entityHelper.generateCommentLikeTag();
				CommentLikeTag commentLikeTagB = entityHelper.generateCommentLikeTag();

				return new Struct()
					.withValue("commentLikeTagAId", commentLikeTagA.getId())
					.withValue("commentLikeTagBId", commentLikeTagB.getId());
			});
			Long commentLikeTagAId = given.valueOf("commentLikeTagAId");
			Long commentLikeTagBId = given.valueOf("commentLikeTagBId");

			// When
			ResultActions actions = mockMvc.perform(get("/post/comment/likeTags"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(hasSize(2)))
				.andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
					commentLikeTagAId.intValue(),
					commentLikeTagBId.intValue()
				)));

			// Document
			actions.andDo(document("commentLikeTag-list-example",
				requestParameters(
					ApiDocumentation.DOC_PARAMETER_PAGE,
					ApiDocumentation.DOC_PARAMETER_SIZE,
					ApiDocumentation.DOC_PARAMETER_SORT
				)));
		}

		@Test
		void listCommentLikeTagsWithPaging() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				CommentLikeTag commentLikeTagA = entityHelper.generateCommentLikeTag();
				CommentLikeTag commentLikeTagB = entityHelper.generateCommentLikeTag();

				return new Struct()
					.withValue("commentLikeTagAId", commentLikeTagA.getId())
					.withValue("commentLikeTagBId", commentLikeTagB.getId());
			});
			Long commentLikeTagAId = given.valueOf("commentLikeTagAId");
			Long commentLikeTagBId = given.valueOf("commentLikeTagBId");

			// When
			ResultActions actions = mockMvc.perform(get("/post/comment/likeTags")
				.param("size", "2")
				.param("page", "0")
				.param("sort", "id,desc"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(hasSize(2)))
				.andExpect(jsonPath("$.[*].id").value(contains(
					commentLikeTagBId.intValue(),
					commentLikeTagAId.intValue()
				)));

			// Document
			actions.andDo(document("commentLikeTag-list-with-paging-example"));
		}

	}

	@Nested
	class DeleteCommentLikeTag {

		@Test
		void deleteCommentLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				CommentLikeTag commentLikeTag = entityHelper.generateCommentLikeTag();
				String token = likeTagAuthHelper.generateToken(commentLikeTag);

				return new Struct()
					.withValue("token", token)
					.withValue("id", commentLikeTag.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(delete("/post/comment/likeTags/{id}", id)
				.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isNoContent())
				.andExpect(content().string(emptyString()));

			assertThat(repository.existsById(id)).isFalse();

			// Document
			actions.andDo(document("commentLikeTag-delete-example"));
		}

		@Test
		void deleteCommentLikeTag_CommentLikeTagNotFound_NotFound_Status() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/comment/likeTags/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}
	}
}
