package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.ReplyHelper.aReplyCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.ReplyHelper.aReplyUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.ReplyAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyRepository;
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
public class ReplyIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("답글 ID");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("답글 작성자 ID");
    private static final FieldDescriptor DOC_FIELD_COMMENT_ID =
        fieldWithPath("commentId").description("상위 댓글 ID");
    private static final FieldDescriptor DOC_FIELD_CONTENT =
        fieldWithPath("content").description("답글 내용");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private ReplyRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private ReplyAuthHelper replyAuthHelper;

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
    class PostReply {

        @Test
        void postReply() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                String token = replyAuthHelper.generateToken(account);
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
                .content("테스트 답글 내용")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/comment/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.content").value("테스트 답글 내용"))
                .andReturn();

            // Document
            actions.andDo(document("reply-create-example",
                requestFields(
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_COMMENT_ID,
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void postReply_ReplyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aReplyCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/comment/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class GetReply {

        @Test
        void getReply() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Reply reply = entityHelper.generateReply(it ->
                    it.withContent("테스트 답글 내용")
                );

                return new Struct()
                    .withValue("id", reply.getId())
                    .withValue("ownerId", reply.getOwner().getId())
                    .withValue("commentId", reply.getComment().getId());
            });
            Long id = given.valueOf("id");
            Long ownerId = given.valueOf("ownerId");
            Long commentId = given.valueOf("commentId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/comment/replies/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.content").value("테스트 답글 내용"));

            // Document
            actions.andDo(document("reply-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_COMMENT_ID,
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void getReply_ReplyNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/comment/replies/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchReplies {

        @Test
        void listReplies() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Reply replyA = entityHelper.generateReply();
                Reply replyB = entityHelper.generateReply();

                return new Struct()
                    .withValue("replyAId", replyA.getId())
                    .withValue("replyBId", replyB.getId());
            });
            Long replyAId = given.valueOf("replyAId");
            Long replyBId = given.valueOf("replyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/comment/replies"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replies").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.replies.[*].id").value(containsInAnyOrder(
                    replyAId.intValue(),
                    replyBId.intValue()
                )));

            // Document
            actions.andDo(document("reply-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listRepliesWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateReply();
                Reply replyA = entityHelper.generateReply();
                Reply replyB = entityHelper.generateReply();

                return new Struct()
                    .withValue("replyAId", replyA.getId())
                    .withValue("replyBId", replyB.getId());
            });
            Long replyAId = given.valueOf("replyAId");
            Long replyBId = given.valueOf("replyBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/comment/replies")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replies").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.replies.[*].id").value(contains(
                    replyCId.intValue(),
                    replyBId.intValue()
                )));

            // Document
            actions.andDo(document("reply-list-with-paging-example"));
        }

    }

    @Nested
    class PatchReply {

        @Test
        void patchReply() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Reply reply = entityHelper.generateReply(it ->
                    it.withContent("테스트 답글 내용")
                );
                String token = replyAuthHelper.generateToken(reply);

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", reply.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            Update dto = Update.builder()
                .content("새로운 답글 내용")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/comment/replies/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("새로운 답글 내용"))
                .andReturn();

            // Document
            actions.andDo(document("reply-update-example",
                requestFields(
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void patchReply_ReplyNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aReplyUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/post/comment/replies/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteReply {

        @Test
        void deleteReply() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Reply reply = entityHelper.generateReply();
                String token = replyAuthHelper.generateToken(reply);

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", reply.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/comment/replies/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("reply-delete-example"));
        }

        @Test
        void deleteReply_ReplyNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/comment/replies/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void ReplyLikeTag가_있는_Reply_삭제(
            @Autowired ReplyLikeTagRepository replyLikeTagRepository,
            @Autowired AccountRepository accountRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                ReplyLikeTag replyLikeTag = entityHelper.generateReplyLikeTag();
                String token = replyAuthHelper.generateToken(replyLikeTag.getReply());

                return new Struct()
                    .withValue("token", token)
                    .withValue("replyId", replyLikeTag.getReply().getId())
                    .withValue("replyLikeTagId", replyLikeTag.getId())
                    .withValue("replyLikeTagOwnerId", replyLikeTag.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long replyId = given.valueOf("replyId");
            Long replyLikeTagId = given.valueOf("replyLikeTagId");
            Long replyLikeTagOwnerId = given.valueOf("replyLikeTagOwnerId");

            // When
            ResultActions actions = mockMvc.perform(
                delete("/post/comment/replies/{id}", replyId)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(replyId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagId)).isFalse();
            assertThat(accountRepository.existsById(replyLikeTagOwnerId)).isTrue();
        }

        @Test
        void ReplyLikeTag들이_있는_Reply_삭제(
            @Autowired ReplyLikeTagRepository replyLikeTagRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Reply reply = entityHelper.generateReply();
                ReplyLikeTag replyLikeTagA = entityHelper.generateReplyLikeTag(it -> it
                    .withReply(reply));
                ReplyLikeTag replyLikeTagB = entityHelper.generateReplyLikeTag(it -> it
                    .withReply(reply));
                String token = replyAuthHelper.generateToken(reply);

                return new Struct()
                    .withValue("token", token)
                    .withValue("replyId", reply.getId())
                    .withValue("replyLikeTagAId", replyLikeTagA.getId())
                    .withValue("replyLikeTagBId", replyLikeTagB.getId());
            });
            String token = given.valueOf("token");
            Long replyId = given.valueOf("replyId");
            Long replyLikeTagAId = given.valueOf("replyLikeTagAId");
            Long replyLikeTagBId = given.valueOf("replyLikeTagBId");

            // When
            ResultActions actions = mockMvc.perform(
                delete("/post/comment/replies/{id}", replyId)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(replyId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagAId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagBId)).isFalse();
        }

    }
}
