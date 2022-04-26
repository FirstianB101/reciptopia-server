package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aCommentCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aCommentUpdateDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.CommentAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
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
public class CommentIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("댓글 ID");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("댓글 작성자 ID");
    private static final FieldDescriptor DOC_FIELD_POST_ID =
        fieldWithPath("postId").description("댓글 게시물 ID");
    private static final FieldDescriptor DOC_FIELD_CONTENT =
        fieldWithPath("content").description("댓글 내용");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private CommentRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private CommentAuthHelper commentAuthHelper;

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
    class PostComment {

        @Test
        void postComment() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                String token = commentAuthHelper.generateToken(account);
                Post post = entityHelper.generatePost();

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", account.getId())
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long postId = given.valueOf("postId");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId)
                .postId(postId)
                .content("테스트 댓글 내용")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("테스트 댓글 내용"))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.postId").value(postId))
                .andReturn();

            // Document
            actions.andDo(document("comment-create-example",
                requestFields(
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID,
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void postComment_CommentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aCommentCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class GetComment {

        @Test
        void getComment() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment comment = entityHelper.generateComment(it ->
                    it.withContent("테스트 댓글 내용")
                );
                return new Struct()
                    .withValue("id", comment.getId())
                    .withValue("ownerId", comment.getOwner().getId())
                    .withValue("postId", comment.getPost().getId());
            });
            Long id = given.valueOf("id");
            Long ownerId = given.valueOf("ownerId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/comments/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("테스트 댓글 내용"))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.postId").value(postId))
                .andReturn();

            // Document
            actions.andDo(document("comment-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID,
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void getComment_CommentNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/comments/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchComments {

        @Test
        void listComments() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment commentA = entityHelper.generateComment();
                Comment commentB = entityHelper.generateComment();

                return new Struct()
                    .withValue("commentAId", commentA.getId())
                    .withValue("commentBId", commentB.getId());
            });
            Long commentAId = given.valueOf("commentAId");
            Long commentBId = given.valueOf("commentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/comments"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    commentAId.intValue(),
                    commentBId.intValue()
                )));

            // Document
            actions.andDo(document("comment-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listCommentsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateComment();
                Comment commentA = entityHelper.generateComment();
                Comment commentB = entityHelper.generateComment();

                return new Struct()
                    .withValue("commentAId", commentA.getId())
                    .withValue("commentBId", commentB.getId());
            });
            Long commentAId = given.valueOf("commentAId");
            Long commentBId = given.valueOf("commentBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/comments")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    commentBId.intValue(),
                    commentAId.intValue()
                )));

            // Document
            actions.andDo(document("comment-list-with-paging-example"));
        }

    }

    @Nested
    class PatchComment {

        @Test
        void patchComment() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment comment = entityHelper.generateComment(it ->
                    it.withContent("테스트 댓글 내용")
                );

                String token = commentAuthHelper.generateToken(comment);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", comment.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            Update dto = Update.builder()
                .content("새로운 댓글 내용")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/comments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("새로운 댓글 내용"))
                .andReturn();

            // Document
            actions.andDo(document("comment-update-example",
                requestFields(
                    DOC_FIELD_CONTENT
                )));
        }

        @Test
        void patchComment_CommentNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aCommentUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/post/comments/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteComment {

        @Test
        void deleteComment() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment comment = entityHelper.generateComment();
                String token = commentAuthHelper.generateToken(comment);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", comment.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/comments/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(id)).isEmpty();

            // Document
            actions.andDo(document("comment-delete-example"));
        }

        @Test
        void deleteComment_CommentNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/comments/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void CommentLikeTag가_있는_Comment_삭제(
            @Autowired CommentLikeTagRepository commentLikeTagRepository,
            @Autowired AccountRepository accountRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                CommentLikeTag commentLikeTag = entityHelper.generateCommentLikeTag();
                String token = commentAuthHelper.generateToken(commentLikeTag.getComment());

                return new Struct()
                    .withValue("token", token)
                    .withValue("commentId", commentLikeTag.getComment().getId())
                    .withValue("commentLikeTagId", commentLikeTag.getId())
                    .withValue("commentLikeTagOwnerId", commentLikeTag.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long commentId = given.valueOf("commentId");
            Long commentLikeTagId = given.valueOf("commentLikeTagId");
            Long commentLikeTagOwnerId = given.valueOf("commentLikeTagOwnerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/comments/{id}", commentId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(commentId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagId)).isFalse();
            assertThat(accountRepository.existsById(commentLikeTagOwnerId)).isTrue();
        }

        @Test
        void CommentLikeTag들이_있는_Comment_삭제(
            @Autowired CommentLikeTagRepository commentLikeTagRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment comment = entityHelper.generateComment();
                CommentLikeTag commentLikeTagA = entityHelper.generateCommentLikeTag(it -> it
                    .withComment(comment));
                CommentLikeTag commentLikeTagB = entityHelper.generateCommentLikeTag(it -> it
                    .withComment(comment));
                String token = commentAuthHelper.generateToken(comment);

                return new Struct()
                    .withValue("token", token)
                    .withValue("commentId", comment.getId())
                    .withValue("commentLikeTagAId", commentLikeTagA.getId())
                    .withValue("commentLikeTagBId", commentLikeTagB.getId());
            });
            String token = given.valueOf("token");
            Long commentId = given.valueOf("commentId");
            Long commentLikeTagAId = given.valueOf("commentLikeTagAId");
            Long commentLikeTagBId = given.valueOf("commentLikeTagBId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/comments/{id}", commentId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(commentId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagAId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagBId)).isFalse();
        }

    }
}