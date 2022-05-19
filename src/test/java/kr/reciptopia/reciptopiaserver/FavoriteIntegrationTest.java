package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Create;
import static kr.reciptopia.reciptopiaserver.util.H2DbCleaner.clean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.FavoriteAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.FavoriteRepository;
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

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class FavoriteIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("즐겨찾기 ID");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID =
        fieldWithPath("ownerId").description("즐겨찾기 소유자 ID");
    private static final FieldDescriptor DOC_FIELD_POST_ID =
        fieldWithPath("postId").description("즐겨찾기에 추가된 게시글 ID");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private FavoriteRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private FavoriteAuthHelper favoriteAuthHelper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class PostFavorite {

        @Test
        void postFavorite() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                Post post = entityHelper.generatePost();
                String token = favoriteAuthHelper.generateToken(owner);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", owner.getId())
                    .withValue("postId", post.getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");
            Long ownerId = given.valueOf("ownerId");

            // When
            Create dto = Create.builder()
                .postId(postId)
                .ownerId(ownerId)
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/account/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.ownerId").value(ownerId))
                .andReturn();

            // Document
            actions.andDo(document("favorite-create-example",
                requestFields(
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void 존재하지않는_post를_즐겨찾기_추가() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                String token = favoriteAuthHelper.generateToken(owner);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");

            // When
            Create dto = Create.builder()
                .postId(5L)
                .ownerId(ownerId)
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/account/favorites/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void 존재하지않는_account로_즐겨찾기_추가() throws Exception {
            /// Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost();

                return new Struct()
                    .withValue("postId", post.getId());
            });
            Long postId = given.valueOf("postId");

            // When
            Create dto = Create.builder()
                .postId(postId)
                .ownerId(5L)
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/account/favorites/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetFavorite {

        @Test
        void getFavorite() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Favorite favorite = entityHelper.generateFavorite();

                return new Struct()
                    .withValue("id", favorite.getId())
                    .withValue("ownerId", favorite.getOwner().getId())
                    .withValue("postId", favorite.getPost().getId());
            });
            Long id = given.valueOf("id");
            Long postId = given.valueOf("postId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/account/favorites/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.ownerId").value(ownerId));

            // Document
            actions.andDo(document("favorite-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void getFavorite_FavoriteNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/account/favorites/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchFavorites {

        @Test
        void listFavorites() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Favorite favoriteA = entityHelper.generateFavorite();
                Favorite favoriteB = entityHelper.generateFavorite();

                return new Struct()
                    .withValue("favoriteAId", favoriteA.getId())
                    .withValue("favoriteBId", favoriteB.getId());
            });
            Long favoriteAId = given.valueOf("favoriteAId");
            Long favoriteBId = given.valueOf("favoriteBId");

            // When
            ResultActions actions = mockMvc.perform(get("/account/favorites"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorites").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.favorites.[*].id").value(containsInAnyOrder(
                    favoriteAId.intValue(),
                    favoriteBId.intValue()
                )));

            // Document
            actions.andDo(document("favorite-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listFavoritesWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateFavorite();
                Favorite favoriteA = entityHelper.generateFavorite();
                Favorite favoriteB = entityHelper.generateFavorite();

                return new Struct()
                    .withValue("favoriteAId", favoriteA.getId())
                    .withValue("favoriteBId", favoriteB.getId());
            });
            Long favoriteAId = given.valueOf("favoriteAId");
            Long favoriteBId = given.valueOf("favoriteBId");

            // When
            ResultActions actions = mockMvc.perform(get("/account/favorites")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorites").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.favorites.[*].id").value(contains(
                    favoriteCId.intValue(),
                    favoriteBId.intValue()
                )));

            // Document
            actions.andDo(document("favorite-list-with-paging-example"));
        }

    }

    @Nested
    class DeleteFavorite {

        @Test
        void deleteFavorite() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Favorite favorite = entityHelper.generateFavorite();
                String token = favoriteAuthHelper.generateToken(favorite);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", favorite.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/account/favorites/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("favorite-delete-example"));
        }

        @Test
        void deleteFavorite_FavoriteNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/account/favorites/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }
}
