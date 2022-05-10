package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipeCreateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
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
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.IngredientAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.RecipeAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.StepAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.MainIngredientRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.RecipeRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepRepository;
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
public class RecipeIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("레시피 ID");
    private static final FieldDescriptor DOC_FIELD_POST_ID =
        fieldWithPath("postId").description("레시피가 게시된 게시글 ID");
    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    @Autowired
    private JsonHelper jsonHelper;
    @Autowired
    private RecipeRepository repository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TransactionHelper trxHelper;
    @Autowired
    private EntityHelper entityHelper;
    @Autowired
    private RecipeAuthHelper recipeAuthHelper;

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
    class PostRecipe {

        @Test
        void postRecipe() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Post post = entityHelper.generatePost();
                String token = recipeAuthHelper.generateToken(post);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", post.getId());
            });
            Long postId = given.valueOf("postId");
            String token = given.valueOf("token");

            // When
            Create dto = Create.builder()
                .postId(postId)
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

            // Document
            actions.andDo(document("recipe-create-example",
                requestFields(
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void postRecipe_RecipeNotFound_NotFoundStatus() throws Exception {
            // When

            String body = jsonHelper.toJson(aRecipeCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetRecipe {

        @Test
        void getRecipe() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = recipeAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", recipe.getId());
            });
            Long id = given.valueOf("id");
            String token = given.valueOf("token");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/recipes/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

            // Document
            actions.andDo(document("recipe-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_POST_ID
                )));
        }

        @Test
        void getRecipe_RecipeNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/recipes/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Disabled
    @Nested
    class SearchRecipes {

        @Test
        void listRecipes() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipeA = entityHelper.generateRecipe();
                Recipe recipeB = entityHelper.generateRecipe();

                return new Struct()
                    .withValue("recipeAId", recipeA.getId())
                    .withValue("recipeBId", recipeB.getId());
            });
            Long recipeAId = given.valueOf("recipeAId");
            Long recipeBId = given.valueOf("recipeBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipes"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipes").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.recipes.[*].id").value(containsInAnyOrder(
                    recipeAId.intValue(),
                    recipeBId.intValue()
                )));

            // Document
            actions.andDo(document("recipe-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listRecipesWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateRecipe();
                Recipe recipeA = entityHelper.generateRecipe();
                Recipe recipeB = entityHelper.generateRecipe();

                return new Struct()
                    .withValue("recipeAId", recipeA.getId())
                    .withValue("recipeBId", recipeB.getId());
            });
            Long recipeAId = given.valueOf("recipeAId");
            Long recipeBId = given.valueOf("recipeBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipes")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipes").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.recipes.[*].id").value(contains(
                    recipeBId.intValue(),
                    recipeAId.intValue()
                )));

            // Document
            actions.andDo(document("recipe-list-with-paging-example"));
        }

    }

    @Nested
    class DeleteRecipe {

        @Test
        void deleteRecipe(@Autowired PostRepository postRepository) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = recipeAuthHelper.generateToken(recipe.getPost().getOwner());
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", recipe.getPost().getId())
                    .withValue("id", recipe.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();
            assertThat(postRepository.existsById(postId)).isTrue();

            // Document
            actions.andDo(document("recipe-delete-example"));
        }

        @Test
        void deleteRecipe_RecipeNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void Step이_있는_Recipe_삭제(
            @Autowired StepRepository stepRepository,
            @Autowired StepAuthHelper stepAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step step = entityHelper.generateStep();
                String token = stepAuthHelper.generateToken(step);
                return new Struct()
                    .withValue("recipeId", step.getRecipe().getId())
                    .withValue("token", token)
                    .withValue("stepId", step.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");
            Long stepId = given.valueOf("stepId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(stepRepository.existsById(stepId)).isFalse();
        }

        @Test
        void Step들이_있는_Recipe_삭제(
            @Autowired StepRepository stepRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Recipe recipe = entityHelper.generateRecipe();
                Step stepA = entityHelper.generateStep(it -> it
                    .withRecipe(recipe));
                Step stepB = entityHelper.generateStep(it -> it
                    .withRecipe(recipe));

                String token = recipeAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("stepAId", stepA.getId())
                    .withValue("stepBId", stepB.getId())
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long stepAId = given.valueOf("stepAId");
            Long stepBId = given.valueOf("stepBId");
            Long recipeId = given.valueOf("recipeId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(stepRepository.existsById(stepAId)).isFalse();
            assertThat(stepRepository.existsById(stepBId)).isFalse();
        }

        @Test
        void MainIngredient가_있는_Recipe_삭제(
            @Autowired
                MainIngredientRepository mainIngredientRepository,
            @Autowired
                IngredientAuthHelper ingredientAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                MainIngredient mainIngredient = entityHelper.generateMainIngredient();
                String token = ingredientAuthHelper.generateToken(mainIngredient);
                return new Struct()
                    .withValue("recipeId", mainIngredient.getRecipe().getId())
                    .withValue("token", token)
                    .withValue("mainIngredientId", mainIngredient.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");
            Long mainIngredientId = given.valueOf("mainIngredientId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(mainIngredientRepository.existsById(mainIngredientId)).isFalse();
        }

        @Test
        void MainIngredient들이_있는_Recipe_삭제(
            @Autowired MainIngredientRepository mainIngredientRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Recipe recipe = entityHelper.generateRecipe();
                MainIngredient mainIngredientA = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));

                String token = recipeAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("mainIngredientAId", mainIngredientA.getId())
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long mainIngredientAId = given.valueOf("mainIngredientAId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long recipeId = given.valueOf("recipeId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(mainIngredientRepository.existsById(mainIngredientAId)).isFalse();
            assertThat(mainIngredientRepository.existsById(mainIngredientBId)).isFalse();
        }

        @Test
        void SubIngredient가_있는_Recipe_삭제(
            @Autowired
                SubIngredientRepository subIngredientRepository,
            @Autowired
                IngredientAuthHelper ingredientAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SubIngredient subIngredient = entityHelper.generateSubIngredient();
                String token = ingredientAuthHelper.generateToken(subIngredient);
                return new Struct()
                    .withValue("recipeId", subIngredient.getRecipe().getId())
                    .withValue("token", token)
                    .withValue("subIngredientId", subIngredient.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");
            Long subIngredientId = given.valueOf("subIngredientId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(subIngredientRepository.existsById(subIngredientId)).isFalse();
        }

        @Test
        void SubIngredient들이_있는_Recipe_삭제(
            @Autowired SubIngredientRepository subIngredientRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Recipe recipe = entityHelper.generateRecipe();
                SubIngredient subIngredientA = entityHelper.generateSubIngredient(it -> it
                    .withRecipe(recipe));
                SubIngredient subIngredientB = entityHelper.generateSubIngredient(it -> it
                    .withRecipe(recipe));

                String token = recipeAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("subIngredientAId", subIngredientA.getId())
                    .withValue("subIngredientBId", subIngredientB.getId())
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long subIngredientAId = given.valueOf("subIngredientAId");
            Long subIngredientBId = given.valueOf("subIngredientBId");
            Long recipeId = given.valueOf("recipeId");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipes/{id}", recipeId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(recipeId)).isFalse();
            assertThat(subIngredientRepository.existsById(subIngredientAId)).isFalse();
            assertThat(subIngredientRepository.existsById(subIngredientBId)).isFalse();
        }
    }
}
