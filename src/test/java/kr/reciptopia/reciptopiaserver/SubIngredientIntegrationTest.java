package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.Bulk.tripleSubIngredientsBulkCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.Bulk.tripleSubIngredientsBulkUpdateDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.aSubIngredientCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.aSubIngredientUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Bulk;
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
public class SubIngredientIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("부 재료 ID");
    private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
        fieldWithPath("recipeId").description("부 재료가 속한 레시피 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("부 재료 이름");
    private static final FieldDescriptor DOC_FIELD_DETAIL =
        fieldWithPath("detail").description("부 재료 세부사항");

    private static final ParameterDescriptor DOC_PARAMETER_RECIPE_ID =
        parameterWithName("recipeId").description("레시피 ID").optional();

    private static final FieldDescriptor DOC_FIELD_POST_BULK_SUB_INGREDIENTS =
        fieldWithPath("subIngredients").type("SubIngredient[]").description("부 재료 생성 필요필드 배열");
    private static final FieldDescriptor DOC_FIELD_POST_BULK_SUB_INGREDIENT =
        subsectionWithPath("subIngredients.[]").type("SubIngredient")
            .description("부 재료 생성 필요필드와 동일");

    private static final FieldDescriptor DOC_FIELD_PATCH_BULK_SUB_INGREDIENTS =
        subsectionWithPath("subIngredients").type("Map<id, subIngredient>")
            .description("부 재료 수정 필요필드 배열");

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
            Single dto = Single.builder()
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

        @Test
        void recipe_id가_없는_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token);
            });
            String token = given.valueOf("token");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(null);
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지않는_recipe_id로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token);
            });
            String token = given.valueOf("token");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(-1L);
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void token이_없는_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                return new Struct()
                    .withValue("recipeId", recipe.getId());
            });
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId);

            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isUnauthorized());
        }

        @Test
        void 권한이_없는_recipe_id로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipeA = entityHelper.generateRecipe();
                Recipe recipeB = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipeA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeAId", recipeA.getId())
                    .withValue("recipeBId", recipeB.getId());
            });
            String token = given.valueOf("token");
            Long recipeBId = given.valueOf("recipeBId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeBId);

            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isForbidden());
        }

        @Test
        void name이_없는_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withName(null);
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 공백으로_채워진_name으로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withName("      ");
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_name으로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withName("And_so_I_wake_in_the_morning_and_I_step_Outside_"
                    + "and_I_take_a_deep_breath_And_I_get_real_high_"
                    + "Then_I_scream_from_the_top_of_my_lungs_What's_goin_on");
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void detail이_없는_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withDetail(null);
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 공백으로_채워진_detail로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withDetail("      ");
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_detail로_sub_ingredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token)
                    .withValue("recipeId", recipe.getId());
            });
            String token = given.valueOf("token");
            Long recipeId = given.valueOf("recipeId");

            // When
            Single dto = aSubIngredientCreateDto()
                .withRecipeId(recipeId)
                .withDetail("And_so_I_wake_in_the_morning_and_I_step_Outside_"
                    + "and_I_take_a_deep_breath_And_I_get_real_high_"
                    + "Then_I_scream_from_the_top_of_my_lungs_What's_goin_on");
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/subIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
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

    @Nested
    class SearchSubIngredients {

        @Test
        void listSubIngredients() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SubIngredient subIngredientA = entityHelper.generateSubIngredient();
                SubIngredient subIngredientB = entityHelper.generateSubIngredient();

                return new Struct()
                    .withValue("subIngredientAId", subIngredientA.getId())
                    .withValue("subIngredientBId", subIngredientB.getId());
            });
            Long subIngredientAId = given.valueOf("subIngredientAId");
            Long subIngredientBId = given.valueOf("subIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/subIngredients"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subIngredients").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.subIngredients.[*].id").value(containsInAnyOrder(
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
                .andExpect(jsonPath("$.subIngredients").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.subIngredients.[*].id").value(contains(
                    subIngredientBId.intValue(),
                    subIngredientAId.intValue()
                )));

            // Document
            actions.andDo(document("sub-ingredient-list-with-paging-example"));
        }

        @Test
        void searchSubIngredientsByRecipeId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                SubIngredient subIngredientA = entityHelper.generateSubIngredient();
                SubIngredient subIngredientB = entityHelper.generateSubIngredient(it -> it
                    .withRecipe(recipe));
                SubIngredient subIngredientC = entityHelper.generateSubIngredient(it -> it
                    .withRecipe(recipe));
                SubIngredient subIngredientD = entityHelper.generateSubIngredient();
                SubIngredient subIngredientE = entityHelper.generateSubIngredient();

                return new Struct()
                    .withValue("recipeId", recipe.getId())
                    .withValue("subIngredientBId", subIngredientB.getId())
                    .withValue("subIngredientCId", subIngredientC.getId());
            });
            Long recipeId = given.valueOf("recipeId");
            Long subIngredientBId = given.valueOf("subIngredientBId");
            Long subIngredientCId = given.valueOf("subIngredientCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/subIngredients")
                .param("recipeId", recipeId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subIngredients").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.subIngredients.[*].id").value(containsInAnyOrder(
                    subIngredientBId.intValue(),
                    subIngredientCId.intValue()
                )));

            // Document
            actions.andDo(document("sub-ingredient-search-example",
                requestParameters(
                    DOC_PARAMETER_RECIPE_ID
                )));
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

        @Test
        void white_space_들로_채워진_name으로_subIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("         ")
                .detail("20g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_name으로_subIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name(
                    "압력밥솥에 물 2000ml정도와 한방팩, 대추, 마늘, 과 같이 뚜껑을 잘 닫은 뒤 "
                        + "센불에서 끓여 주시다가 추가 움직이기 시작하면 중불로 낮춰 10분간 삶아주고 "
                        + "바로 김을 빼준뒤 꺼낸 닭에 뿌릴 고추장")
                .detail("20g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void white_space_들로_채워진_detail로_subIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("불고기 고추장")
                .detail("         ")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_detail로_subIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("불고기 고추장")
                .detail(
                    "불고기 조각1/4컵, 고춧가루1/4컵, 겨자조금, 간장2T, 식초2T, "
                        + "설탕1/3T, 다진마늘1T 과 대파 약간을 잘 섞어서만든 고추장 소스 20g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/subIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
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

            assertThat(repository.existsById(id)).isFalse();

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

    @Nested
    class SubIngredientBulkTest {

        @Nested
        class BulkPostSubIngredient {

            @Test
            void bulkPostSubIngredient() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    String token = ingredientAuthHelper.generateToken(recipe);

                    return new Struct()
                        .withValue("recipeId", recipe.getId())
                        .withValue("token", token);
                });
                Long recipeId = given.valueOf("recipeId");
                String token = given.valueOf("token");

                // When
                Bulk.Create.Single dto = tripleSubIngredientsBulkCreateDto(
                    it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.subIngredients().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.subIngredients").value(aMapWithSize(dtoNumber)))
                    .andReturn();

                // Document
                actions.andDo(document("subIngredient-bulk-create-example",
                    requestFields(
                        DOC_FIELD_POST_BULK_SUB_INGREDIENTS,
                        DOC_FIELD_POST_BULK_SUB_INGREDIENT
                    )));
            }

            @Test
            void 존재하지_않는_Recipe에_SubIngredient들을_생성() throws Exception {
                String body = jsonHelper.toJson(tripleSubIngredientsBulkCreateDto());

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isNotFound())
                    .andReturn();
            }

            @Test
            void 부적절한_토큰의_Recipe에_SubIngredient들을_생성() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipeA = entityHelper.generateRecipe();
                    Recipe recipeB = entityHelper.generateRecipe();
                    String token = ingredientAuthHelper.generateToken(recipeB);

                    return new Struct()
                        .withValue("recipeId", recipeA.getId())
                        .withValue("token", token);
                });
                Long recipeId = given.valueOf("recipeId");
                String token = given.valueOf("token");

                // When
                Bulk.Create.Single dto = tripleSubIngredientsBulkCreateDto(
                    it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.subIngredients().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isForbidden())
                    .andReturn();
            }
        }

        @Nested
        class BulkPatchSubIngredient {

            @Test
            void bulkPatchSubIngredient() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    SubIngredient subIngredientA = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientA"));
                    SubIngredient subIngredientB = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientB"));
                    SubIngredient subIngredientC = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientC"));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("subIngredientAId", subIngredientA.getId())
                        .withValue("subIngredientBId", subIngredientB.getId())
                        .withValue("subIngredientCId", subIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long subIngredientAId = given.valueOf("subIngredientAId");
                Long subIngredientBId = given.valueOf("subIngredientBId");
                Long subIngredientCId = given.valueOf("subIngredientCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .subIngredient(subIngredientBId,
                        Update.builder().name("바뀐 SubIngredientB").build())
                    .subIngredient(subIngredientCId,
                        Update.builder().name("바뀐 SubIngredientC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subIngredients.[*].id").value(containsInAnyOrder(
                        subIngredientBId.intValue(),
                        subIngredientCId.intValue()
                    )))
                    .andExpect(
                        jsonPath("$.subIngredients." + subIngredientBId + ".name").value(
                            "바뀐 SubIngredientB"))
                    .andExpect(
                        jsonPath("$.subIngredients." + subIngredientCId + ".name").value(
                            "바뀐 SubIngredientC"))
                    .andReturn();

                // Document
                actions.andDo(document("subIngredient-bulk-update-example",
                    requestFields(
                        DOC_FIELD_PATCH_BULK_SUB_INGREDIENTS
                    )));
            }

            @Test
            void 존재하지_않는_SubIngredient이_일부있는_SubIngredient들을_수정() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    SubIngredient subIngredientA = entityHelper.generateSubIngredient(it -> it
                        .withName("바뀌기 전 SubIngredientA")
                        .withRecipe(recipe));
                    SubIngredient subIngredientB = entityHelper.generateSubIngredient(it -> it
                        .withName("바뀌기 전 SubIngredientB")
                        .withRecipe(recipe));
                    SubIngredient subIngredientC = entityHelper.generateSubIngredient(it -> it
                        .withName("바뀌기 전 SubIngredientC")
                        .withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("subIngredientAId", subIngredientA.getId())
                        .withValue("subIngredientBId", subIngredientB.getId())
                        .withValue("subIngredientCId", subIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long subIngredientAId = given.valueOf("subIngredientAId");
                Long subIngredientBId = given.valueOf("subIngredientBId");
                Long subIngredientCId = given.valueOf("subIngredientCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .subIngredient(subIngredientBId,
                        Update.builder().name("바뀐 SubIngredientB").build())
                    .subIngredient(2222L,
                        Update.builder().name("존재하지 않는 SubIngredient").build())
                    .subIngredient(subIngredientCId,
                        Update.builder().name("바뀐 SubIngredientC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.findById(subIngredientAId).get().getName()).isEqualTo(
                    "바뀌기 전 SubIngredientA");
                assertThat(repository.findById(subIngredientBId).get().getName()).isEqualTo(
                    "바뀌기 전 SubIngredientB");
                assertThat(repository.findById(subIngredientCId).get().getName()).isEqualTo(
                    "바뀌기 전 SubIngredientC");
            }


            @Test
            void subIngredients가_없는_subIngredient_다중_수정() throws Exception {

                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientA"));
                    entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientB"));
                    entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 SubIngredientC"));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token);
                });
                String token = given.valueOf("token");

                // When
                Bulk.Update dto = tripleSubIngredientsBulkUpdateDto()
                    .withSubIngredients(null);
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-subIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                actions
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        class BulkDeleteSubIngredient {

            @Test
            void bulkDeleteSubIngredient() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    SubIngredient subIngredientA = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));
                    SubIngredient subIngredientB = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));
                    SubIngredient subIngredientC = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("subIngredientAId", subIngredientA.getId())
                        .withValue("subIngredientBId", subIngredientB.getId())
                        .withValue("subIngredientCId", subIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long subIngredientAId = given.valueOf("subIngredientAId");
                Long subIngredientBId = given.valueOf("subIngredientBId");
                Long subIngredientCId = given.valueOf("subIngredientCId");

                // When
                String ids =
                    "" + subIngredientAId + "," + subIngredientBId + "," + subIngredientCId;
                ResultActions actions = mockMvc.perform(
                    delete("/post/recipe/bulk-subIngredient/{ids}", ids)
                        .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

                assertThat(repository.existsById(subIngredientAId)).isFalse();
                assertThat(repository.existsById(subIngredientBId)).isFalse();
                assertThat(repository.existsById(subIngredientCId)).isFalse();

                // Document
                actions.andDo(document("subIngredient-bulk-delete-example"));
            }

            @Test
            void 존재하지_않는_SubIngredient이_일부있는_SubIngredient들을_삭제() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    SubIngredient subIngredientA = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));
                    SubIngredient subIngredientB = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));
                    SubIngredient subIngredientC = entityHelper.generateSubIngredient(
                        it -> it.withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("subIngredientAId", subIngredientA.getId())
                        .withValue("subIngredientBId", subIngredientB.getId())
                        .withValue("subIngredientCId", subIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long subIngredientAId = given.valueOf("subIngredientAId");
                Long subIngredientBId = given.valueOf("subIngredientBId");
                Long subIngredientCId = given.valueOf("subIngredientCId");

                // When
                String ids = "" + subIngredientAId + "," + 2000L + "," + subIngredientCId;
                ResultActions actions = mockMvc.perform(
                    delete("/post/recipe/bulk-subIngredient/{ids}", ids)
                        .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.existsById(subIngredientAId)).isTrue();
                assertThat(repository.existsById(subIngredientBId)).isTrue();
                assertThat(repository.existsById(subIngredientCId)).isTrue();
            }
        }
    }

}
