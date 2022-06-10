package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.Bulk.tripleMainIngredientsBulkCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.aMainIngredientCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.aMainIngredientUpdateDto;
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
public class MainIngredientIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("주 재료 ID");
    private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
        fieldWithPath("recipeId").description("주 재료가 속한 레시피 ID");
    private static final FieldDescriptor DOC_FIELD_NAME =
        fieldWithPath("name").description("주 재료 이름, 1 ~ 20자 이며 공백으로만 이루어 지지않아야 합니다");
    private static final FieldDescriptor DOC_FIELD_DETAIL =
        fieldWithPath("detail").description("주 재료 세부사항, 1 ~ 50자 이며 공백으로만 이루어 지지않아야 합니다");

    private static final ParameterDescriptor DOC_PARAMETER_RECIPE_ID =
        parameterWithName("recipeId").description("레시피 ID").optional();
    private static final ParameterDescriptor DOC_PARAMETER_RECIPE_IDS =
        parameterWithName("recipeIds").description("레시피 ID 배열").optional();
    private static final ParameterDescriptor DOC_PARAMETER_POST_ID =
        parameterWithName("postId").description("게시물 ID").optional();
    private static final ParameterDescriptor DOC_PARAMETER_POST_IDS =
        parameterWithName("postIds").description("게시물 ID 배열").optional();

    private static final FieldDescriptor DOC_FIELD_POST_BULK_MAIN_INGREDIENTS =
        fieldWithPath("mainIngredients").type("MainIngredient[]").description("주 재료 생성 필요필드 배열");
    private static final FieldDescriptor DOC_FIELD_POST_BULK_MAIN_INGREDIENT =
        subsectionWithPath("mainIngredients.[]").type("MainIngredient")
            .description("주 재료 생성 필요필드와 동일");

    private static final FieldDescriptor DOC_FIELD_PATCH_BULK_MAIN_INGREDIENTS =
        subsectionWithPath("mainIngredients").type("Map<id, mainIngredient>")
            .description("주 재료 수정 필요필드 배열");
    private static final FieldDescriptor DOC_FIELD_BULK_MAIN_INGREDIENTS_GRUOP_BY_POST =
        subsectionWithPath("mainIngredients").type("Map<postId, List<mainIngredient>>")
            .description("주 재료가 속한 Post의 Id를 Key 로 하고 주 재료 List를 Value 갖는 Map");

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
            Single dto = Single.builder()
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

        @Test
        void recipe_id가_없는_mainIngredient_생성() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = ingredientAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("token", token);
            });
            String token = given.valueOf("token");

            // When
            Single dto = Single.builder()
                .name("청경채")
                .detail("500g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void name이_없는_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .detail("500g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void white_space_들로_채워진_name으로_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .name("         ")
                .detail("500g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_name으로_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .name(
                    "압력밥솥에 물 2000ml정도와 한방팩, 대추, 마늘, 과 같이 뚜껑을 잘 닫은 뒤 센불에서 끓여 주시다가 추가 움직이기 시작하면 중불로 낮춰 10분간 삶아주고 바로 김을 빼준뒤 꺼낸 닭")
                .detail("500g")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void detail이_없는_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .name("청경채")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_detail로_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .name("삼계탕 소스")
                .detail(
                    "닭육수1/4컵, 고춧가루1/4컵, 겨자조금, 간장2T, 식초2T, 설탕1/3T, 다진마늘1T 과 대파 약간을 잘 섞어서만든 소스 1T")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void white_space_들로_채워진_detail로_mainIngredient_생성() throws Exception {
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
            Single dto = Single.builder()
                .recipeId(recipeId)
                .detail("         ")
                .name("청경채")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/mainIngredients")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
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
    class SearchMainIngredients {

        @Test
        void listMainIngredients() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("mainIngredientAId", mainIngredientA.getId())
                    .withValue("mainIngredientBId", mainIngredientB.getId());
            });
            Long mainIngredientAId = given.valueOf("mainIngredientAId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients.[*].[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(containsInAnyOrder(
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
                .andExpect(jsonPath("$.mainIngredients.[*].[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(contains(
                    mainIngredientBId.intValue(),
                    mainIngredientAId.intValue()
                )));

            // Document
            actions.andDo(document("main-ingredient-list-with-paging-example"));
        }

        @Test
        void searchMainIngredientsByRecipeId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));
                MainIngredient mainIngredientC = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));
                MainIngredient mainIngredientD = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientE = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("recipeId", recipe.getId())
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("mainIngredientCId", mainIngredientC.getId());
            });
            Long recipeId = given.valueOf("recipeId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long mainIngredientCId = given.valueOf("mainIngredientCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("recipeId", recipeId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients.[*].[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(containsInAnyOrder(
                    mainIngredientBId.intValue(),
                    mainIngredientCId.intValue()
                )));

            // Document
            actions.andDo(document("main-ingredient-search-example",
                requestParameters(
                    DOC_PARAMETER_RECIPE_ID,
                    DOC_PARAMETER_RECIPE_IDS,
                    DOC_PARAMETER_POST_ID,
                    DOC_PARAMETER_POST_IDS
                ))).andDo(document("main-ingredient-search-response-example",
                responseFields(
                    DOC_FIELD_BULK_MAIN_INGREDIENTS_GRUOP_BY_POST
                )));
        }

        @Test
        void searchMainIngredientsByPostId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));
                MainIngredient mainIngredientC = entityHelper.generateMainIngredient(it -> it
                    .withRecipe(recipe));
                MainIngredient mainIngredientD = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientE = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("postId", recipe.getPost().getId())
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("mainIngredientCId", mainIngredientC.getId());
            });
            Long postId = given.valueOf("postId");
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long mainIngredientCId = given.valueOf("mainIngredientCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("postId", postId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients.[*].[*]").value(hasSize(2)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(containsInAnyOrder(
                    mainIngredientBId.intValue(),
                    mainIngredientCId.intValue()
                )));
        }

        @Test
        void searchMainIngredientsByIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientC = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientD = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientE = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("mainIngredientCId", mainIngredientC.getId())
                    .withValue("mainIngredientEId", mainIngredientE.getId());
            });
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long mainIngredientCId = given.valueOf("mainIngredientCId");
            Long mainIngredientEId = given.valueOf("mainIngredientEId");

            // When
            String idsParam =
                mainIngredientBId + ", " + mainIngredientCId + ", " + mainIngredientEId;
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("ids", idsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients.[*].[*]").value(hasSize(3)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(
                    containsInAnyOrder(
                        mainIngredientCId.intValue(),
                        mainIngredientBId.intValue(),
                        mainIngredientEId.intValue()
                    )));
        }

        @Test
        void searchMainIngredientsByRecipeIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientC = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientD = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientE = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("mainIngredientCId", mainIngredientC.getId())
                    .withValue("mainIngredientEId", mainIngredientE.getId())
                    .withValue("recipeBId", mainIngredientB.getRecipe().getId())
                    .withValue("recipeCId", mainIngredientC.getRecipe().getId())
                    .withValue("recipeEId", mainIngredientE.getRecipe().getId());
            });
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long mainIngredientCId = given.valueOf("mainIngredientCId");
            Long mainIngredientEId = given.valueOf("mainIngredientEId");
            Long recipeBId = given.valueOf("recipeBId");
            Long recipeCId = given.valueOf("recipeCId");
            Long recipeEId = given.valueOf("recipeEId");

            // When
            String recipeIdsParam = recipeBId + ", " + recipeCId + ", " + recipeEId;
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("recipeIds", recipeIdsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(
                    containsInAnyOrder(
                        mainIngredientCId.intValue(),
                        mainIngredientBId.intValue(),
                        mainIngredientEId.intValue()
                    )));
        }

        @Test
        void searchMainIngredientsByPostIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                MainIngredient mainIngredientA = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientB = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientC = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientD = entityHelper.generateMainIngredient();
                MainIngredient mainIngredientE = entityHelper.generateMainIngredient();

                return new Struct()
                    .withValue("mainIngredientBId", mainIngredientB.getId())
                    .withValue("mainIngredientCId", mainIngredientC.getId())
                    .withValue("mainIngredientEId", mainIngredientE.getId())
                    .withValue("postBId", mainIngredientB.getRecipe().getPost().getId())
                    .withValue("postCId", mainIngredientC.getRecipe().getPost().getId())
                    .withValue("postEId", mainIngredientE.getRecipe().getPost().getId());
            });
            Long mainIngredientBId = given.valueOf("mainIngredientBId");
            Long mainIngredientCId = given.valueOf("mainIngredientCId");
            Long mainIngredientEId = given.valueOf("mainIngredientEId");
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");
            Long postEId = given.valueOf("postEId");

            // When
            String postIdsParam = postBId + ", " + postCId + ", " + postEId;
            ResultActions actions = mockMvc.perform(get("/post/recipe/mainIngredients")
                .param("postIds", postIdsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainIngredients").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.mainIngredients.[*].[*].id").value(
                    containsInAnyOrder(
                        mainIngredientCId.intValue(),
                        mainIngredientBId.intValue(),
                        mainIngredientEId.intValue()
                    )));
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

        @Test
        void white_space_들로_채워진_name으로_mainIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("         ")
                .detail("4개")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_name으로_mainIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name(
                    "압력밥솥에 물 2000ml정도와 한방팩, 대추, 마늘, 과 같이 뚜껑을 잘 닫은 뒤 센불에서 끓여 주시다가 추가 움직이기 시작하면 중불로 낮춰 10분간 삶아주고 바로 김을 빼준뒤 꺼낸 닭")
                .detail("4개")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void white_space_들로_채워진_detail로_mainIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("송이버섯")
                .detail("         ")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
        }

        @Test
        void 너무_긴_길이의_detail로_mainIngredient_수정() throws Exception {

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

            // When
            Update dto = Update.builder()
                .name("송이버섯")
                .detail(
                    "닭육수1/4컵, 고춧가루1/4컵, 겨자조금, 간장2T, 식초2T, 설탕1/3T, 다진마늘1T 과 대파 약간을 잘 섞어서만든 소스 1T")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/mainIngredients/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isBadRequest());
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

    @Nested
    class MainIngredientBulkTest {

        @Nested
        class BulkPostMainIngredient {

            @Test
            void bulkPostMainIngredient() throws Exception {
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
                Bulk.Create.Single dto = tripleMainIngredientsBulkCreateDto(
                    it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.mainIngredients().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-mainIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mainIngredients").value(aMapWithSize(dtoNumber)))
                    .andReturn();

                // Document
                actions.andDo(document("mainIngredient-bulk-create-example",
                    requestFields(
                        DOC_FIELD_POST_BULK_MAIN_INGREDIENTS,
                        DOC_FIELD_POST_BULK_MAIN_INGREDIENT
                    )));
            }

            @Test
            void 존재하지_않는_Recipe에_MainIngredient들을_생성() throws Exception {
                String body = jsonHelper.toJson(tripleMainIngredientsBulkCreateDto());

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-mainIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isNotFound())
                    .andReturn();
            }

            @Test
            void 부적절한_토큰의_Recipe에_MainIngredient들을_생성() throws Exception {
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
                Bulk.Create.Single dto = tripleMainIngredientsBulkCreateDto(
                    it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.mainIngredients().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-mainIngredient")
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
        class BulkPatchMainIngredient {

            @Test
            void bulkPatchMainIngredient() throws Exception {

                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    MainIngredient mainIngredientA = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientA"));
                    MainIngredient mainIngredientB = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientB"));
                    MainIngredient mainIngredientC = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientC"));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("mainIngredientAId", mainIngredientA.getId())
                        .withValue("mainIngredientBId", mainIngredientB.getId())
                        .withValue("mainIngredientCId", mainIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long mainIngredientAId = given.valueOf("mainIngredientAId");
                Long mainIngredientBId = given.valueOf("mainIngredientBId");
                Long mainIngredientCId = given.valueOf("mainIngredientCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .mainIngredient(mainIngredientBId,
                        Update.builder().name("바뀐 MainIngredientB").build())
                    .mainIngredient(mainIngredientCId,
                        Update.builder().name("바뀐 MainIngredientC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-mainIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mainIngredients.[*].id").value(containsInAnyOrder(
                        mainIngredientBId.intValue(),
                        mainIngredientCId.intValue()
                    )))
                    .andExpect(
                        jsonPath("$.mainIngredients." + mainIngredientBId + ".name").value(
                            "바뀐 MainIngredientB"))
                    .andExpect(
                        jsonPath("$.mainIngredients." + mainIngredientCId + ".name").value(
                            "바뀐 MainIngredientC"))
                    .andReturn();

                // Document
                actions.andDo(document("mainIngredient-bulk-update-example",
                    requestFields(
                        DOC_FIELD_PATCH_BULK_MAIN_INGREDIENTS
                    )));
            }

            @Test
            void 존재하지_않는_MainIngredient이_일부있는_MainIngredient들을_수정() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    MainIngredient mainIngredientA = entityHelper.generateMainIngredient(it -> it
                        .withName("바뀌기 전MainIngredientA")
                        .withRecipe(recipe));
                    MainIngredient mainIngredientB = entityHelper.generateMainIngredient(it -> it
                        .withName("바뀌기 전MainIngredientB")
                        .withRecipe(recipe));
                    MainIngredient mainIngredientC = entityHelper.generateMainIngredient(it -> it
                        .withName("바뀌기 전MainIngredientC")
                        .withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("mainIngredientAId", mainIngredientA.getId())
                        .withValue("mainIngredientBId", mainIngredientB.getId())
                        .withValue("mainIngredientCId", mainIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long mainIngredientAId = given.valueOf("mainIngredientAId");
                Long mainIngredientBId = given.valueOf("mainIngredientBId");
                Long mainIngredientCId = given.valueOf("mainIngredientCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .mainIngredient(mainIngredientBId,
                        Update.builder().name("바뀐 MainIngredientB").build())
                    .mainIngredient(2222L,
                        Update.builder().name("존재하지 않는 MainIngredient").build())
                    .mainIngredient(mainIngredientCId,
                        Update.builder().name("바뀐 MainIngredientC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-mainIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.findById(mainIngredientAId).get().getName()).isEqualTo(
                    "바뀌기 전MainIngredientA");
                assertThat(repository.findById(mainIngredientBId).get().getName()).isEqualTo(
                    "바뀌기 전MainIngredientB");
                assertThat(repository.findById(mainIngredientCId).get().getName()).isEqualTo(
                    "바뀌기 전MainIngredientC");
            }

            @Test
            void mainIngredients가_없는_mainIngredient_다중_수정() throws Exception {

                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientA"));
                    entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientB"));
                    entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe).withName("바꾸기 전 MainIngredientC"));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token);
                });
                String token = given.valueOf("token");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-mainIngredient")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                actions
                    .andExpect(status().isBadRequest());
            }
        }

        @Nested
        class BulkDeleteMainIngredient {

            @Test
            void bulkDeleteMainIngredient() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {

                    Recipe recipe = entityHelper.generateRecipe();
                    MainIngredient mainIngredientA = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));
                    MainIngredient mainIngredientB = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));
                    MainIngredient mainIngredientC = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("mainIngredientAId", mainIngredientA.getId())
                        .withValue("mainIngredientBId", mainIngredientB.getId())
                        .withValue("mainIngredientCId", mainIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long mainIngredientAId = given.valueOf("mainIngredientAId");
                Long mainIngredientBId = given.valueOf("mainIngredientBId");
                Long mainIngredientCId = given.valueOf("mainIngredientCId");

                // When
                String ids =
                    "" + mainIngredientAId + "," + mainIngredientBId + "," + mainIngredientCId;
                ResultActions actions = mockMvc.perform(
                    delete("/post/recipe/bulk-mainIngredient/{ids}", ids)
                        .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

                assertThat(repository.existsById(mainIngredientAId)).isFalse();
                assertThat(repository.existsById(mainIngredientBId)).isFalse();
                assertThat(repository.existsById(mainIngredientCId)).isFalse();

                // Document
                actions.andDo(document("mainIngredient-bulk-delete-example"));
            }

            @Test
            void 존재하지_않는_MainIngredient이_일부있는_MainIngredient들을_삭제() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    MainIngredient mainIngredientA = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));
                    MainIngredient mainIngredientB = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));
                    MainIngredient mainIngredientC = entityHelper.generateMainIngredient(
                        it -> it.withRecipe(recipe));

                    String token = ingredientAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("mainIngredientAId", mainIngredientA.getId())
                        .withValue("mainIngredientBId", mainIngredientB.getId())
                        .withValue("mainIngredientCId", mainIngredientC.getId());
                });
                String token = given.valueOf("token");
                Long mainIngredientAId = given.valueOf("mainIngredientAId");
                Long mainIngredientBId = given.valueOf("mainIngredientBId");
                Long mainIngredientCId = given.valueOf("mainIngredientCId");

                // When
                String ids = "" + mainIngredientAId + "," + 2000L + "," + mainIngredientCId;
                ResultActions actions = mockMvc.perform(
                    delete("/post/recipe/bulk-mainIngredient/{ids}", ids)
                        .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.existsById(mainIngredientAId)).isTrue();
                assertThat(repository.existsById(mainIngredientBId)).isTrue();
                assertThat(repository.existsById(mainIngredientCId)).isTrue();
            }
        }
    }
}
