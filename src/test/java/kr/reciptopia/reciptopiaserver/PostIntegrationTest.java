package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPostUpdateDto;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
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
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.PostAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.RecipeAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.FavoriteRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.RecipeRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("????????? ID");
    private static final FieldDescriptor DOC_FIELD_TITLE =
        fieldWithPath("title").description("????????? ??????, 1 ~ 30??? ?????? ??????????????? ????????? ??????????????? ?????????");
    private static final FieldDescriptor DOC_FIELD_CONTENT =
        fieldWithPath("content").description("????????? ??????");
    private static final FieldDescriptor DOC_FIELD_PICTURE_URLS =
        fieldWithPath("pictureUrls").description("????????? ?????? URL ??????");
    private static final FieldDescriptor DOC_FIELD_VIEWS =
        fieldWithPath("views").description("????????? ?????? ???");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("????????? ID");

    private static final ParameterDescriptor DOC_PARAMETER_OWNER_ID =
        parameterWithName("ownerId").description("????????? ID").optional();
    private static final ParameterDescriptor DOC_PARAMETER_TITLE_LIKE =
        parameterWithName("titleLike").description("????????? ????????? ??????").optional();
    private static final ParameterDescriptor DOC_PARAMETER_IDS =
        parameterWithName("ids").description("????????? ID ??????").optional();
    private static final ParameterDescriptor DOC_PARAMETER_MAIN_INGREDIENT_NAMES =
        parameterWithName("mainIngredientNames").description("??? ?????? ??????").optional();
    private static final ParameterDescriptor DOC_PARAMETER_SUB_INGREDIENT_NAMES =
        parameterWithName("subIngredientNames").description("??? ?????? ??????").optional();


    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private PostRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private PostAuthHelper postAuthHelper;

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

    @Nested
    class PostPost {

        @Test
        void postPost() throws Exception {
            // Given - Account, Recipe
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("id");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId)
                .title("?????? ????????? ?????? ?????????")
                .content("???????????? ???????????? ????????? ??????")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("?????? ????????? ?????? ?????????"))
                .andExpect(jsonPath("$.content").value("???????????? ???????????? ????????? ??????"))
                .andExpect(jsonPath("$.pictureUrls").value(hasSize(2)))
                .andExpect(jsonPath("$.pictureUrls").value(contains(
                    "C:\\Users\\eunsung\\Desktop\\temp\\picture",
                    "C:\\Users\\tellang\\Desktop\\temp\\picture"
                )))
                .andExpect(jsonPath("$.views").isNumber())
                .andExpect(jsonPath("$.views").value(0))
                .andExpect(jsonPath("$.ownerId").value(ownerId))    // ??????
                .andReturn();

            // Document
            actions.andDo(document("post-create-example",
                requestFields(
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_TITLE,
                    DOC_FIELD_CONTENT,
                    DOC_FIELD_PICTURE_URLS
                )));
        }

        @Test
        void ??????????????????_owner_id???_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("id");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId + 100L)
                .title("???????????? ???????????? ?????????????????? ?????? ????????? ????????? ???")
                .content("???????????? ??????, ????????????, ??????, ???????????????(????????????)??? ?????? ????????????????????? ????????? ???????????? ???????????? ?????? ???????????? ????????????!")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void owner_id???_??????_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token);
            });
            String token = given.valueOf("token");

            // When
            Create dto = Create.builder()
                .title("???????????? ???????????? ?????????????????? ?????? ????????? ????????? ???")
                .content("???????????? ??????, ????????????, ??????, ???????????????(????????????)??? ?????? ????????????????????? ????????? ???????????? ???????????? ?????? ???????????? ????????????!")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void title???_??????_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("id");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId)
                .content("???????????? ??????, ????????????, ??????, ???????????????(????????????)??? ?????? ????????????????????? ????????? ???????????? ???????????? ?????? ???????????? ????????????!")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void white_space_??????_?????????_title???_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("id");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId)
                .title("      ")
                .content("???????????? ??????, ????????????, ??????, ???????????????(????????????)??? ?????? ????????????????????? ????????? ???????????? ???????????? ?????? ???????????? ????????????!")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void ??????_???_?????????_title???_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = postAuthHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("id");

            // When
            Create dto = Create.builder()
                .ownerId(ownerId)
                .title(
                    "????????? ?????? 2????????? ?????? ?????????????????? 30?????? ?????? ???????????? ?????? ????????? ?????? 1.5 ???????????? ?????? ?????? ?????? 2??????, ????????? ?????? 2????????? ?????? ??????????????? ???????????? ?????? ????????? ????????? ????????? ???????????? ????????????????????? ?????? ???????????? ????????? ?????????????????? ?????? 180????????? 10?????? ????????? ?????? ?????? ?????????")
                .content("???????????? ??????, ????????????, ??????, ???????????????(????????????)??? ?????? ????????????????????? ????????? ???????????? ???????????? ?????? ???????????? ????????????!")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetPost {

        @Test
        void getPost() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost(it ->
                    it.withTitle("?????? ????????? ?????? ?????????")
                        .withContent("???????????? ???????????? ????????? ??????")
                        .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                        .withPictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                        .withViews(10L)
                );

                return new Struct()
                    .withValue("ownerId", post.getOwner().getId())
                    .withValue("id", post.getId());
            });
            Long id = given.valueOf("id");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/posts/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("?????? ????????? ?????? ?????????"))
                .andExpect(jsonPath("$.content").value("???????????? ???????????? ????????? ??????"))
                .andExpect(jsonPath("$.pictureUrls").value(hasSize(2)))
                .andExpect(jsonPath("$.pictureUrls").value(contains(
                    "C:\\Users\\eunsung\\Desktop\\temp\\picture",
                    "C:\\Users\\tellang\\Desktop\\temp\\picture"
                )))
                .andExpect(jsonPath("$.views").isNumber())
                .andExpect(jsonPath("$.views").value(11))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andReturn();

            // Document
            actions.andDo(document("post-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
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
            Struct given = trxHelper.doInTransaction(() -> {
                Post postA = entityHelper.generatePost();
                entityHelper.generateComment(it -> it.withPost(postA));
                entityHelper.generateComment(it -> it.withPost(postA));
                entityHelper.generateComment(it -> it.withPost(postA));
                entityHelper.generatePostLikeTag(it -> it.withPost(postA));
                entityHelper.generatePostLikeTag(it -> it.withPost(postA));
                entityHelper.generatePostLikeTag(it -> it.withPost(postA));
                entityHelper.generatePostLikeTag(it -> it.withPost(postA));

                Post postB = entityHelper.generatePost();
                entityHelper.generateComment(it -> it.withPost(postB));
                entityHelper.generateComment(it -> it.withPost(postB));
                entityHelper.generateComment(it -> it.withPost(postB));
                entityHelper.generateComment(it -> it.withPost(postB));
                entityHelper.generateComment(it -> it.withPost(postB));
                entityHelper.generatePostLikeTag(it -> it.withPost(postB));
                entityHelper.generatePostLikeTag(it -> it.withPost(postB));

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
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(
                    containsInAnyOrder(
                        postAId.intValue(),
                        postBId.intValue()
                    )))
                .andExpect(jsonPath(
                    "$.postWithCommentAndLikeTagCounts." + postAId + ".commentCount").value(3))
                .andExpect(jsonPath(
                    "$.postWithCommentAndLikeTagCounts." + postAId + ".likeTagCount").value(4))
                .andExpect(jsonPath(
                    "$.postWithCommentAndLikeTagCounts." + postBId + ".commentCount").value(5))
                .andExpect(jsonPath(
                    "$.postWithCommentAndLikeTagCounts." + postBId + ".likeTagCount").value(2));

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
            Struct given = trxHelper.doInTransaction(() -> {
                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost();
                Post postC = entityHelper.generatePost();
                Post postD = entityHelper.generatePost();
                Post postE = entityHelper.generatePost();

                return new Struct()
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId());
            });
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");

            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("size", "2")
                .param("page", "1")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(contains(
                    postCId.intValue(),
                    postBId.intValue()
                )));

            // Document
            actions.andDo(document("post-list-with-paging-example"));
        }

        @Test
        void searchPostsByOwnerId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();

                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost(it -> it.withOwner(owner));
                Post postC = entityHelper.generatePost(it -> it.withOwner(owner));
                Post postD = entityHelper.generatePost();
                Post postE = entityHelper.generatePost();

                return new Struct()
                    .withValue("ownerId", owner.getId())
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId());
            });
            Long ownerId = given.valueOf("ownerId");
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");

            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("ownerId", ownerId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(
                    containsInAnyOrder(
                        postCId.intValue(),
                        postBId.intValue()
                    )));

            // Document
            actions.andDo(document("post-search-example",
                requestParameters(
                    DOC_PARAMETER_OWNER_ID,
                    DOC_PARAMETER_TITLE_LIKE,
                    DOC_PARAMETER_IDS,
                    DOC_PARAMETER_MAIN_INGREDIENT_NAMES,
                    DOC_PARAMETER_SUB_INGREDIENT_NAMES
                )));
        }

        @Test
        void searchPostsByTitleLike() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost(it -> it
                    .withTitle("????????? ????????? ?????? ?????????"));
                Post postC = entityHelper.generatePost(it -> it
                    .withTitle("????????? ????????? ?????? ?????????"));
                Post postD = entityHelper.generatePost();
                Post postE = entityHelper.generatePost();

                return new Struct()
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId());
            });
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");

            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("titleLike", "????????? ?????????"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(
                    containsInAnyOrder(
                        postCId.intValue(),
                        postBId.intValue()
                    )));
        }

        @Test
        void searchPostsByOwnerIdAndTitleLike() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account ownerA = entityHelper.generateAccount();
                Account ownerB = entityHelper.generateAccount();

                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost(it -> it
                    .withOwner(ownerA)
                    .withTitle("?????? ????????? ?????????")
                );
                Post postC = entityHelper.generatePost(it -> it
                    .withOwner(ownerA)
                    .withTitle("?????? ?????? ?????????")
                );
                Post postD = entityHelper.generatePost(it -> it
                    .withOwner(ownerA)
                    .withTitle("????????? ?????????")
                );
                Post postE = entityHelper.generatePost(it -> it
                    .withOwner(ownerB)
                    .withTitle("????????? ?????????")
                );
                Post postF = entityHelper.generatePost();
                Post postG = entityHelper.generatePost();

                return new Struct()
                    .withValue("ownerAId", ownerA.getId())
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId());
            });
            Long ownerAId = given.valueOf("ownerAId");
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");

            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("ownerId", ownerAId.toString())
                .param("titleLike", "??????"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(
                    containsInAnyOrder(
                        postCId.intValue(),
                        postBId.intValue()
                    )));
        }

        @Test
        void searchPostsByIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost();
                Post postC = entityHelper.generatePost();
                Post postD = entityHelper.generatePost();
                Post postE = entityHelper.generatePost();

                return new Struct()
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId())
                    .withValue("postEId", postE.getId());
            });
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");
            Long postEId = given.valueOf("postEId");

            // When
            String idsParam = postBId + ", " + postCId + ", " + postEId;
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("ids", idsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(
                    containsInAnyOrder(
                        postCId.intValue(),
                        postBId.intValue(),
                        postEId.intValue()
                    )));
        }

        @Test
        void mainIngredients???_post??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipeA = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeA));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeA));

                Recipe recipeB = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeB));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeB));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeB));

                Recipe recipeC = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeC));

                Recipe recipeD = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeD));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeD));

                Recipe recipeE = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeE));
                return new Struct()
                    .withValue("postBId", recipeB.getPost().getId())
                    .withValue("postCId", recipeC.getPost().getId());
            });
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");

            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("mainIngredientNames", "?????????, ??????, ?????????")
            );

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(contains(
                    postBId.intValue(),
                    postCId.intValue()
                )));
        }

        @Test
        void id??????_mainIngredients???_post??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipeA = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeA));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeA));

                Recipe recipeB = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeB));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeB));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeB));

                Recipe recipeC = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeC));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeC));

                Recipe recipeD = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("??????")
                        .withRecipe(recipeD));
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeD));

                Recipe recipeE = entityHelper.generateRecipe();
                entityHelper.generateMainIngredient(
                    ig -> ig.withName("?????????")
                        .withRecipe(recipeE));
                return new Struct()
                    .withValue("postAId", recipeA.getPost().getId())
                    .withValue("postBId", recipeB.getPost().getId())
                    .withValue("postCId", recipeC.getPost().getId())
                    .withValue("postDId", recipeD.getPost().getId());
            });
            Long postAId = given.valueOf("postAId");
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");
            Long postDId = given.valueOf("postDId");

            String idsParam = postAId + ", " + postBId + ", " + postCId + ", " + postDId;
            // When
            ResultActions actions = mockMvc.perform(get("/posts")
                .param("mainIngredientNames", "?????????, ??????, ?????????")
                .param("ids", idsParam)
            );

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.postWithCommentAndLikeTagCounts.[*].post.id").value(contains(
                    postBId.intValue(),
                    postCId.intValue()
                )));
        }
    }

    @Nested
    class PatchPost {

        @Test
        void patchPost() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost(it -> it
                    .withTitle("????????? ?????? ?????????")
                    .withContent("????????? ?????? ?????????")
                    .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                    .withPictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                );
                String token = postAuthHelper.generateToken(post.getOwner());

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", post.getId());
            });
            Long id = given.valueOf("id");
            String token = given.valueOf("token");

            // When
            Update dto = Update.builder()
                .title("????????? ?????? ?????????")
                .content("????????? ?????? ?????????")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\silverstar\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("????????? ?????? ?????????"))
                .andExpect(jsonPath("$.content").value("????????? ?????? ?????????"))
                .andExpect(jsonPath("$.pictureUrls").value(hasSize(3)))
                .andExpect(jsonPath("$.pictureUrls").value(contains(
                    "C:\\Users\\eunsung\\Desktop\\temp\\picture",
                    "C:\\Users\\tellang\\Desktop\\temp\\picture",
                    "C:\\Users\\silverstar\\Desktop\\temp\\picture"
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
            String body = jsonHelper.toJson(aPostUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/posts/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void white_space_??????_?????????_title???_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost(it -> it
                    .withTitle("????????? ?????? ?????????")
                    .withContent("????????? ?????? ?????????")
                    .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                    .withPictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                );
                String token = postAuthHelper.generateToken(post.getOwner());

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", post.getId());
            });
            Long id = given.valueOf("id");
            String token = given.valueOf("token");

            // When
            Update dto = Update.builder()
                .title("         ")
                .content("????????? ?????? ?????????")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\silverstar\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void ??????_???_?????????_title???_post_??????() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost(it -> it
                    .withTitle("????????? ?????? ?????????")
                    .withContent("????????? ?????? ?????????")
                    .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                    .withPictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                );
                String token = postAuthHelper.generateToken(post.getOwner());

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", post.getId());
            });
            Long id = given.valueOf("id");
            String token = given.valueOf("token");

            // When
            Update dto = Update.builder()
                .title(
                    "????????? ?????? 2????????? ?????? ?????????????????? 30?????? ?????? ???????????? ?????? ????????? ?????? 1.5 ???????????? ?????? ?????? ?????? 2??????, ????????? ?????? 2????????? ?????? ??????????????? ???????????? ?????? ????????? ????????? ????????? ???????????? ????????????????????? ?????? ???????????? ????????? ?????????????????? ?????? 180????????? 10?????? ????????? ?????? ?????? ?????????")
                .content("????????? ?????? ?????????")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                .pictureUrl("C:\\Users\\silverstar\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeletePost {

        @Test
        void deletePost(@Autowired AccountRepository accountRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost(it -> it
                    .withTitle("????????? ?????? ?????????")
                    .withContent("????????? ?????? ?????????")
                    .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                    .withPictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                );
                String token = postAuthHelper.generateToken(post.getOwner());

                return new Struct()
                    .withValue("ownerId", post.getOwner().getId())
                    .withValue("token", token)
                    .withValue("id", post.getId());
            });
            Long id = given.valueOf("id");
            Long ownerId = given.valueOf("ownerId");
            String token = given.valueOf("token");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();
            assertThat(accountRepository.existsById(ownerId)).isTrue();

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

        @Test
        void Comment???_??????_Post_??????(
            @Autowired CommentRepository commentRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Comment comment = entityHelper.generateComment();
                String token = postAuthHelper.generateToken(comment.getPost());

                return new Struct()
                    .withValue("token", token)
                    .withValue("commentId", comment.getId())
                    .withValue("postId", comment.getPost().getId());
            });
            Long postId = given.valueOf("postId");
            Long commentId = given.valueOf("commentId");
            String token = given.valueOf("token");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(commentRepository.existsById(commentId)).isFalse();
            assertThat(repository.existsById(postId)).isFalse();
        }

        @Test
        void Comment??????_??????_Post_??????(
            @Autowired CommentRepository commentRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post post = entityHelper.generatePost();
                Comment commentA = entityHelper.generateComment(it -> it
                    .withPost(post));
                Comment commentB = entityHelper.generateComment(it -> it
                    .withPost(post));

                String token = postAuthHelper.generateToken(post);
                return new Struct()
                    .withValue("token", token)
                    .withValue("commentAId", commentA.getId())
                    .withValue("commentBId", commentB.getId())
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long commentAId = given.valueOf("commentAId");
            Long commentBId = given.valueOf("commentBId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(postId)).isFalse();
            assertThat(commentRepository.existsById(commentAId)).isFalse();
            assertThat(commentRepository.existsById(commentBId)).isFalse();
        }

        @Test
        void Recipe???_??????_Post_??????(
            @Autowired RecipeAuthHelper recipeAuthHelper,
            @Autowired RecipeRepository recipeRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = recipeAuthHelper.generateToken(recipe);

                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId())
                    .withValue("postId", recipe.getPost().getId());
            });
            Long postId = given.valueOf("postId");
            Long recipeId = given.valueOf("recipeId");
            String token = given.valueOf("token");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(recipeRepository.existsById(recipeId)).isFalse();
            assertThat(repository.existsById(postId)).isFalse();
        }

        @Test
        void PostLikeTag???_??????_Post_??????(
            @Autowired PostLikeTagRepository postLikeTagRepository,
            @Autowired AccountRepository accountRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                PostLikeTag postLikeTag = entityHelper.generatePostLikeTag();
                String token = postAuthHelper.generateToken(postLikeTag.getPost());

                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", postLikeTag.getPost().getId())
                    .withValue("postLikeTagId", postLikeTag.getId())
                    .withValue("postLikeTagOwnerId", postLikeTag.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");
            Long postLikeTagId = given.valueOf("postLikeTagId");
            Long postLikeTagOwnerId = given.valueOf("postLikeTagOwnerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(postId)).isFalse();
            assertThat(postLikeTagRepository.existsById(postLikeTagId)).isFalse();
            assertThat(accountRepository.existsById(postLikeTagOwnerId)).isTrue();
        }

        @Test
        void PostLikeTag??????_??????_Post_??????(
            @Autowired PostLikeTagRepository postLikeTagRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post post = entityHelper.generatePost();
                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag(it -> it
                    .withPost(post));
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag(it -> it
                    .withPost(post));

                String token = postAuthHelper.generateToken(post);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postLikeTagAId", postLikeTagA.getId())
                    .withValue("postLikeTagBId", postLikeTagB.getId())
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long postLikeTagAId = given.valueOf("postLikeTagAId");
            Long postLikeTagBId = given.valueOf("postLikeTagBId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(postId)).isFalse();
            assertThat(postLikeTagRepository.existsById(postLikeTagAId)).isFalse();
            assertThat(postLikeTagRepository.existsById(postLikeTagBId)).isFalse();
        }

        @Test
        void Favorite???_????????????_??????_Post_??????(
            @Autowired FavoriteRepository favoriteRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Favorite favorite = entityHelper.generateFavorite();
                String token = postAuthHelper.generateToken(favorite.getPost());

                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", favorite.getPost().getId())
                    .withValue("favoriteId", favorite.getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");
            Long favoriteId = given.valueOf("favoriteId");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(favoriteRepository.existsById(favoriteId)).isFalse();
            assertThat(repository.existsById(postId)).isFalse();
        }

        @Test
        void Favorite??????_????????????_??????_Post_??????(
            @Autowired FavoriteRepository favoriteRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post post = entityHelper.generatePost();
                Favorite favoriteA = entityHelper.generateFavorite(it -> it
                    .withPost(post));
                Favorite favoriteB = entityHelper.generateFavorite(it -> it
                    .withPost(post));

                String token = postAuthHelper.generateToken(post);
                return new Struct()
                    .withValue("token", token)
                    .withValue("favoriteAId", favoriteA.getId())
                    .withValue("favoriteBId", favoriteB.getId())
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long favoriteAId = given.valueOf("favoriteAId");
            Long favoriteBId = given.valueOf("favoriteBId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc.perform(delete("/posts/{id}", postId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(postId)).isFalse();
            assertThat(favoriteRepository.existsById(favoriteAId)).isFalse();
            assertThat(favoriteRepository.existsById(favoriteBId)).isFalse();
        }
    }

}
