package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.aSubIngredientCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.aSubIngredientUpdateDto;
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
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.IngredientAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.SubIngredientRepository;
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
public class SubIngredientIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("부 재료 ID");
    private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
        fieldWithPath("recipeId").description("부 재료가 속한 레시피 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("부 재료 이름");
    private static final FieldDescriptor DOC_FIELD_DETAIL =
        fieldWithPath("detail").description("부 재료 세부사항");

    @Autowired
    PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private SubIngredientRepository repository;

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
    class PostSubIngredient {

        @Test
        void postSubIngredient() throws Exception {
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
                .name("간장")
                .detail("한 큰술")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("간장"))
                .andExpect(jsonPath("$.detail").value("한 큰술"))
                .andReturn();

            // Document
            actions.andDo(document("sub-ingredient-create-example",
                requestFields(
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void postSubIngredient_SubIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aSubIngredientCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetSubIngredient {

        @Test
        void getSubIngredient() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SubIngredient subIngredient = entityHelper.generateSubIngredient(it -> it
                    .withName("소금")
                    .withDetail("10g")
                );
                return new Struct()
                    .withValue("id", subIngredient.getId())
                    .withValue("recipeId", subIngredient.getRecipe().getId());
            });
            Long recipeId = given.valueOf("recipeId");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/recipe/subIngredients/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("소금"))
                .andExpect(jsonPath("$.detail").value("10g"));

            // Document
            actions.andDo(document("sub-ingredient-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void getSubIngredient_SubIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/subIngredients/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Disabled
    @Nested
    class SearchSubIngredients {

        @Test
        void listSubIngredients() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SubIngredient subIngredientA = entityHelper.generateSubIngredient();
                SubIngredient subIngredientB = entityHelper.generateSubIngredient();

                return new Struct()
                    .withValue("SubIngredientAId", subIngredientA.getId())
                    .withValue("SubIngredientBId", subIngredientB.getId());
            });
            Long subIngredientAId = given.valueOf("subIngredientAId");
            Long subIngredientBId = given.valueOf("subIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/subIngredients"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    subIngredientAId.intValue(),
                    subIngredientBId.intValue()
                )));

            // Document
            actions.andDo(document("sub-ingredient-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listSubIngredientsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateSubIngredient();
                SubIngredient subIngredientA = entityHelper.generateSubIngredient();
                SubIngredient subIngredientB = entityHelper.generateSubIngredient();

                return new Struct()
                    .withValue("subIngredientAId", subIngredientA.getId())
                    .withValue("subIngredientBId", subIngredientB.getId());
            });
            Long subIngredientAId = given.valueOf("subIngredientAId");
            Long subIngredientBId = given.valueOf("subIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/subIngredients")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    subIngredientBId.intValue(),
                    subIngredientAId.intValue()
                )));

            // Document
            actions.andDo(document("sub-ingredient-list-with-paging-example"));
        }

    }

    @Nested
    class PatchSubIngredient {

        @Test
        void patchSubIngredient() throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SubIngredient subIngredient = entityHelper.generateSubIngredient();

                String token = ingredientAuthHelper.generateToken(subIngredient);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", subIngredient.getId())
                    .withValue("recipeId", subIngredient.getRecipe().getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");
            Long recipeId = given.valueOf("recipeId");

            // When
            Update dto = Update.builder()
                .name("고추장")
                .detail("12g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.name").value("고추장"))
                .andExpect(jsonPath("$.detail").value("12g"))
                .andReturn();

            // Document
            actions.andDo(document("sub-ingredient-update-example",
                requestFields(
                    DOC_FIELD_NAME,
                    DOC_FIELD_DETAIL
                )));
        }

        @Test
        void patchSubIngredient_SubIngredientNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aSubIngredientUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteSubIngredient {

        @Test
        void deleteSubIngredient() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                SubIngredient subIngredient = entityHelper.generateSubIngredient();
                String token = ingredientAuthHelper.generateToken(subIngredient);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", subIngredient.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipe/subIngredients/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(id)).isEmpty();

            // Document
            actions.andDo(document("sub-ingredient-delete-example"));
        }

        @Test
        void deleteSubIngredient_SubIngredientNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipe/subIngredients/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }
}
