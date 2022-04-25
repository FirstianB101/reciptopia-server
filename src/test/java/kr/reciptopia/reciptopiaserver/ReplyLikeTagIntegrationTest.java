package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.ReplyLikeTagHelper.aReplyLikeTagCreateDto;
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
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.LikeTagAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyLikeTagRepository;
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
public class ReplyLikeTagIntegrationTest {

	private static final FieldDescriptor DOC_FIELD_ID =
		fieldWithPath("id").description("답글 좋아요 ID");
	private static final FieldDescriptor DOC_FIELD_OWNER_ID =
		fieldWithPath("ownerId").description("답글 좋아요 누른 사용자 ID");
	private static final FieldDescriptor DOC_FIELD_REPLY_ID =
		fieldWithPath("replyId").description("답글 좋아요가 달린 답글 ID");

	private MockMvc mockMvc;

	@Autowired
	private JsonHelper jsonHelper;

	@Autowired
	private ReplyLikeTagRepository repository;

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
	class PostReplyLikeTag {

		@Test
		void postReplyLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				Account account = entityHelper.generateAccount();
				String token = likeTagAuthHelper.generateToken(account);
				Reply reply = entityHelper.generateReply();

				return new Struct()
					.withValue("token", token)
					.withValue("ownerId", account.getId())
					.withValue("replyId", reply.getId());
			});
			String token = given.valueOf("token");
			Long ownerId = given.valueOf("ownerId");
			Long replyId = given.valueOf("replyId");

			// When
			Create dto = Create.builder()
				.ownerId(ownerId)
				.replyId(replyId)
				.build();
			String body = jsonHelper.toJson(dto);

			ResultActions actions = mockMvc.perform(post("/post/comment/reply/likeTags")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token)
				.content(body));

			// Then
			MvcResult mvcResult = actions
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(jsonPath("$.replyId").value(replyId))
				.andReturn();

			// Document
			actions.andDo(document("replyLikeTag-create-example",
				requestFields(
					DOC_FIELD_OWNER_ID,
					DOC_FIELD_REPLY_ID
				)));
		}

		@Test
		void postReplyLikeTag_ReplyLikeTagNotFound_NotFoundStatus() throws Exception {
			// When
			String body = jsonHelper.toJson(aReplyLikeTagCreateDto());

			ResultActions actions = mockMvc.perform(post("/post/comment/reply/likeTags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class GetReplyLikeTag {

		@Test
		void getReplyLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				ReplyLikeTag replyLikeTag = entityHelper.generateReplyLikeTag();

				return new Struct()
					.withValue("id", replyLikeTag.getId())
					.withValue("ownerId", replyLikeTag.getOwner().getId())
					.withValue("replyId", replyLikeTag.getReply().getId());
			});
			Long id = given.valueOf("id");
			Long ownerId = given.valueOf("ownerId");
			Long replyId = given.valueOf("replyId");

			// When
			ResultActions actions = mockMvc
				.perform(get("/post/comment/reply/likeTags/{id}", id));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.ownerId").value(ownerId))
				.andExpect(jsonPath("$.replyId").value(replyId));

			// Document
			actions.andDo(document("replyLikeTag-retrieve-example",
				responseFields(
					DOC_FIELD_ID,
					DOC_FIELD_OWNER_ID,
					DOC_FIELD_REPLY_ID
				)));
		}

		@Test
		void getReplyLikeTag_ReplyLikeTagNotFound_NotFoundStatus() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				get("/post/comment/reply/likeTags/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}

	@Nested
	class SearchReplyLikeTags {

		@Test
		void listReplyLikeTags() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				ReplyLikeTag replyLikeTagA = entityHelper.generateReplyLikeTag();
				ReplyLikeTag replyLikeTagB = entityHelper.generateReplyLikeTag();

				return new Struct()
					.withValue("replyLikeTagAId", replyLikeTagA.getId())
					.withValue("replyLikeTagBId", replyLikeTagB.getId());
			});
			Long replyLikeTagAId = given.valueOf("replyLikeTagAId");
			Long replyLikeTagBId = given.valueOf("replyLikeTagBId");

			// When
			ResultActions actions = mockMvc.perform(get("/post/comment/reply/likeTags"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(hasSize(2)))
				.andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
					replyLikeTagAId.intValue(),
					replyLikeTagBId.intValue()
				)));

			// Document
			actions.andDo(document("replyLikeTag-list-example",
				requestParameters(
					ApiDocumentation.DOC_PARAMETER_PAGE,
					ApiDocumentation.DOC_PARAMETER_SIZE,
					ApiDocumentation.DOC_PARAMETER_SORT
				)));
		}

		@Test
		void listReplyLikeTagsWithPaging() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				ReplyLikeTag replyLikeTagA = entityHelper.generateReplyLikeTag();
				ReplyLikeTag replyLikeTagB = entityHelper.generateReplyLikeTag();

				return new Struct()
					.withValue("replyLikeTagAId", replyLikeTagA.getId())
					.withValue("replyLikeTagBId", replyLikeTagB.getId());
			});
			Long replyLikeTagAId = given.valueOf("replyLikeTagAId");
			Long replyLikeTagBId = given.valueOf("replyLikeTagBId");

			// When
			ResultActions actions = mockMvc.perform(get("/post/comment/reply/likeTags")
				.param("size", "2")
				.param("page", "0")
				.param("sort", "id,desc"));

			// Then
			actions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(hasSize(2)))
				.andExpect(jsonPath("$.[*].id").value(contains(
					replyLikeTagBId.intValue(),
					replyLikeTagAId.intValue()
				)));

			// Document
			actions.andDo(document("replyLikeTag-list-with-paging-example"));
		}

	}

	@Nested
	class DeleteReplyLikeTag {

		@Test
		void deleteReplyLikeTag() throws Exception {
			// Given
			Struct given = trxHelper.doInTransaction(() -> {
				ReplyLikeTag replyLikeTag = entityHelper.generateReplyLikeTag();
				String token = likeTagAuthHelper.generateToken(replyLikeTag);

				return new Struct()
					.withValue("token", token)
					.withValue("id", replyLikeTag.getId());
			});
			String token = given.valueOf("token");
			Long id = given.valueOf("id");

			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/comment/reply/likeTags/{id}", id)
					.header("Authorization", "Bearer " + token));

			// Then
			actions
				.andExpect(status().isNoContent())
				.andExpect(content().string(emptyString()));

			assertThat(repository.existsById(id)).isFalse();

			// Document
			actions.andDo(document("replyLikeTag-delete-example"));
		}

		@Test
		void deleteReplyLikeTag_ReplyLikeTagNotFound_NotFound_Status() throws Exception {
			// When
			ResultActions actions = mockMvc.perform(
				delete("/post/comment/reply/likeTags/{id}", 0L));

			// Then
			actions
				.andExpect(status().isNotFound());
		}

	}
}
