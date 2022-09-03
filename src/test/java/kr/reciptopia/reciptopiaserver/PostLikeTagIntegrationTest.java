package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.PostLikeTagHelper.aPostLikeTagCreateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
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
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.LikeTagAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostLikeTagRepository;
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
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostLikeTagIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("게시물 좋아요 ID");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("게시물 좋아요 누른 사용자 ID");
    private static final FieldDescriptor DOC_FIELD_POST_ID =
        fieldWithPath("postId").description("게시물 좋아요가 달린 게시물 ID");

    private static final ParameterDescriptor DOC_PARAMETER_ID =
        parameterWithName("id").description("게시물 좋아요 ID").optional();
    private static final ParameterDescriptor DOC_PARAMETER_IDS =
        parameterWithName("ids").description("게시물 좋아요 ID 배열").optional();
    private static final ParameterDescriptor DOC_PARAMETER_OWNER_ID =
        parameterWithName("ownerId").description("게시물 좋아요 누른 사용자 ID").optional();
    private static final ParameterDescriptor DOC_PARAMETER_OWNER_IDS =
        parameterWithName("ownerIds").description("게시물 좋아요 누른 사용자 ID 배열").optional();

    private static final FieldDescriptor DOC_FIELD_BULK_POST_LIKE_TAG_GRUOP_BY_OWNER_ID =
        subsectionWithPath("postLikeTags").type("Map<ownerId, List<postLikeTag>>")
            .description("게시물 좋아요 누른 사용자 ID를 Key 로 갖고 게시물 좋아요 List를 Value 로 갖는 Map");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private PostLikeTagRepository repository;

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
    class PostPostLikeTag {

        @Test
        void postPostLikeTag() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                String token = likeTagAuthHelper.generateToken(account);
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
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/likeTags")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.postId").value(postId))
                .andReturn();

            // Document
            actions.andDo(document("postLikeTag-create-example",
                requestFields(
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void postPostLikeTag_PostLikeTagNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aPostLikeTagCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/likeTags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void 존재하지않는_owner_id로_post_like_tag_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                Post post = entityHelper.generatePost();

                String token = likeTagAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");

            // When
            Create dto = aPostLikeTagCreateDto(it -> it
                .withOwnerId(-1L)
                .withPostId(postId)
            );
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/likeTags")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void owner_id가_없는_post_like_tag_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                Post post = entityHelper.generatePost();

                String token = likeTagAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");

            // When
            Create dto = aPostLikeTagCreateDto(it -> it
                .withOwnerId(null)
                .withPostId(postId)
            );
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/likeTags")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void post_id가_없는_post_like_tag_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = likeTagAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");

            // When
            Create dto = aPostLikeTagCreateDto(it -> it
                .withOwnerId(ownerId)
                .withPostId(null)
            );
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/likeTags")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetPostLikeTag {

        @Test
        void getPostLikeTag() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                PostLikeTag postLikeTag = entityHelper.generatePostLikeTag();

                return new Struct()
                    .withValue("id", postLikeTag.getId())
                    .withValue("ownerId", postLikeTag.getOwner().getId())
                    .withValue("postId", postLikeTag.getPost().getId());
            });
            Long id = given.valueOf("id");
            Long ownerId = given.valueOf("ownerId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/likeTags/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andExpect(jsonPath("$.postId").value(postId));

            // Document
            actions.andDo(document("postLikeTag-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void getPostLikeTag_PostLikeTagNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/likeTags/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchPostLikeTags {

        @Test
        void listPostLikeTags() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag();

                return new Struct()
                    .withValue("postLikeTagAId", postLikeTagA.getId())
                    .withValue("postLikeTagBId", postLikeTagB.getId());
            });
            Long postLikeTagAId = given.valueOf("postLikeTagAId");
            Long postLikeTagBId = given.valueOf("postLikeTagBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/likeTags"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postLikeTags.[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.postLikeTags.[*].[*].id").value(containsInAnyOrder(
                    postLikeTagAId.intValue(),
                    postLikeTagBId.intValue()
                )));

            // Document
            actions.andDo(document("postLikeTag-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listPostLikeTagsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagC = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagD = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagE = entityHelper.generatePostLikeTag();

                return new Struct()
                    .withValue("postLikeTagBId", postLikeTagB.getId())
                    .withValue("postLikeTagCId", postLikeTagC.getId());
            });
            Long postLikeTagBId = given.valueOf("postLikeTagBId");
            Long postLikeTagCId = given.valueOf("postLikeTagCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/likeTags")
                .param("size", "2")
                .param("page", "1")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postLikeTags.[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.postLikeTags.[*].[*].id").value(containsInAnyOrder(
                    postLikeTagCId.intValue(),
                    postLikeTagBId.intValue()
                )));

            // Document
            actions.andDo(document("postLikeTag-list-with-paging-example"));
        }

        @Test
        void searchPostLikeTagsByIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagC = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagD = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagE = entityHelper.generatePostLikeTag();

                return new Struct()
                    .withValue("postLikeTagBId", postLikeTagB.getId())
                    .withValue("postLikeTagCId", postLikeTagC.getId())
                    .withValue("postLikeTagEId", postLikeTagE.getId());
            });
            Long postLikeTagBId = given.valueOf("postLikeTagBId");
            Long postLikeTagCId = given.valueOf("postLikeTagCId");
            Long postLikeTagEId = given.valueOf("postLikeTagEId");

            // When
            String idsParam = postLikeTagBId + ", " + postLikeTagCId + ", " + postLikeTagEId;
            ResultActions actions = mockMvc.perform(get("/post/likeTags")
                .param("ids", idsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postLikeTags").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.postLikeTags.[*].[*].id").value(
                    containsInAnyOrder(
                        postLikeTagCId.intValue(),
                        postLikeTagBId.intValue(),
                        postLikeTagEId.intValue()
                    )));

            // Document
            actions.andDo(document("postLikeTag-search-example",
                requestParameters(
                    DOC_PARAMETER_IDS,
                    DOC_PARAMETER_OWNER_IDS
                ))).andDo(document("postLikeTag-search-response-example",
                responseFields(
                    DOC_FIELD_BULK_POST_LIKE_TAG_GRUOP_BY_OWNER_ID
                )));
        }

        @Test
        void searchPostLikeTagsByOwnerIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account ownerB = entityHelper.generateAccount();

                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag(it -> it
                    .withOwner(ownerB));
                PostLikeTag postLikeTagC = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagD = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagE = entityHelper.generatePostLikeTag();
                PostLikeTag postLikeTagF = entityHelper.generatePostLikeTag(it -> it
                    .withOwner(ownerB));

                return new Struct()
                    .withValue("postLikeTagBId", postLikeTagB.getId())
                    .withValue("postLikeTagCId", postLikeTagC.getId())
                    .withValue("postLikeTagEId", postLikeTagE.getId())
                    .withValue("postLikeTagFId", postLikeTagF.getId())
                    .withValue("ownerBId", ownerB.getId())
                    .withValue("ownerCId", postLikeTagC.getOwner().getId())
                    .withValue("ownerEId", postLikeTagE.getOwner().getId());
            });
            Long postLikeTagBId = given.valueOf("postLikeTagBId");
            Long postLikeTagCId = given.valueOf("postLikeTagCId");
            Long postLikeTagEId = given.valueOf("postLikeTagEId");
            Long postLikeTagFId = given.valueOf("postLikeTagFId");
            Long ownerBId = given.valueOf("ownerBId");
            Long ownerCId = given.valueOf("ownerCId");
            Long ownerEId = given.valueOf("ownerEId");

            // When
            String ownerIdsParam = ownerBId + ", " + ownerCId + ", " + ownerEId;
            ResultActions actions = mockMvc.perform(get("/post/likeTags")
                .param("ownerIds", ownerIdsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postLikeTags").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.postLikeTags.[*].[*].id").value(
                    containsInAnyOrder(
                        postLikeTagCId.intValue(),
                        postLikeTagBId.intValue(),
                        postLikeTagEId.intValue(),
                        postLikeTagFId.intValue()
                    )));
        }
    }

    @Nested
    class DeletePostLikeTag {

        @Test
        void deletePostLikeTag() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                PostLikeTag postLikeTag = entityHelper.generatePostLikeTag();
                String token = likeTagAuthHelper.generateToken(postLikeTag);

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", postLikeTag.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/likeTags/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("postLikeTag-delete-example"));
        }

        @Test
        void deletePostLikeTag_PostLikeTagNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/likeTags/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

}
