package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.aMainIngredientCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.aMainIngredientUpdateDto;
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
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.IngredientAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.MainIngredientRepository;
import kr.reciptopia.reciptopiaserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
public class MainIngredientIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("주 재료 ID");
    private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
        fieldWithPath("recipeId").description("주 재료가 속한 레시피 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("주 재료 이름");
    private static final FieldDescriptor DOC_FIELD_DETAIL =
        fieldWithPath("detail").description("주 재료 세부사항");
    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    @Autowired
    private JsonHelper jsonHelper;
    @Autowired
    private MainIngredientRepository repository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TransactionHelper trxHelper;
    @Autowired
    private EntityHelper entityHelper;
    @Autowired
    private IngredientAuthHelper ingredientAuthHelper;

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
    class PostMainIngredient {

        @Test
        void postMainIngredient() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            Long recipeId = given.valueOf("recipeId");
            String token = given.valueOf("token");

            // When
            Create dto = Create.builder()
                .recipeId(recipeId)
                .name("청경채")
                .detail("500g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("청경채"))
                .andExpect(jsonPath("$.detail").value("500g"))
                .andReturn();

            // Document
            actions.andDo(document("main-ingredient-create-example",
                requestFields(
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void postMainIngredient_MainIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aMainIngredientCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetMainIngredient {

        @Test
        void getMainIngredient() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                MainIngredient mainIngredient = entityHelper.generateMainIngredient(it -> it
                    .withName("고사리")
                    .withDetail("20g")
                );
                return new Struct()
                    .withValue("id", mainIngredient.getId())
                    .withValue("recipeId", mainIngredient.getRecipe().getId());
            });
            Long recipeId = given.valueOf("recipeId");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/recipe/mainIngredients/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("고사리"))
                .andExpect(jsonPath("$.detail").value("20g"));

            // Document
            actions.andDo(document("main-ingredient-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void getMainIngredient_MainIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    @Disabled
    class SearchMainIngredients {

        @Test
        void listMainIngredients() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("MainIngredientAId", mainIngredientA.getId())
                    .withValue("MainIngredientBId", mainIngredientB.getId());
            });
            Long mainIngredientAId = given.valueOf("mainIngredientAId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    mainIngredientAId.intValue(),
                    mainIngredientBId.intValue()
                )));

            // Document
            actions.andDo(document("main-ingredient-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listMainIngredientsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateMainIngredient();
                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("mainIngredientAId", mainIngredientA.getId())
                    .withValue("mainIngredientBId", mainIngredientB.getId());
            });
            Long mainIngredientAId = given.valueOf("mainIngredientAId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    mainIngredientBId.intValue(),
                    mainIngredientAId.intValue()
                )));

            // Document
            actions.andDo(document("main-ingredient-list-with-paging-example"));
        }

    }

    @Nested
    class PatchMainIngredient {

        @Test
        void patchMainIngredient() throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                MainIngredient mainIngredient = entityHelper.generateMainIngredient();

                String token = ingredientAuthHelper.generateToken(mainIngredient);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", mainIngredient.getId())
                    .withValue("recipeId", mainIngredient.getRecipe().getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");
            Long recipeId = given.valueOf("recipeId");

            // When
            Update dto = Update.builder()
                .name("송이버섯")
                .detail("4개")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("송이버섯"))
                .andExpect(jsonPath("$.detail").value("4개"))
                .andReturn();

            // Document
            actions.andDo(document("main-ingredient-update-example",
                requestFields(
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void patchMainIngredient_MainIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aMainIngredientUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteMainIngredient {

        @Test
        void deleteMainIngredient() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                MainIngredient mainIngredient = entityHelper.generateMainIngredient();
                String token = ingredientAuthHelper.generateToken(mainIngredient);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", mainIngredient.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipe/mainIngredients/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("main-ingredient-delete-example"));
        }

        @Test
        void deleteMainIngredient_MainIngredientNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(
                delete("/post/recipe/mainIngredients/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }
}
