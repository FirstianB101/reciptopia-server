package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.Bulk.tripleStepsBulkCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.aStepCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.aStepUpdateDto;
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
import kr.reciptopia.reciptopiaserver.domain.dto.StepDto;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.StepAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepRepository;
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
public class StepIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("조리 단계 ID");
    private static final FieldDescriptor DOC_FIELD_RECIPE_ID =
        fieldWithPath("recipeId").description("레시피 ID");
    private static final FieldDescriptor DOC_FIELD_DESCRIPTION =
        fieldWithPath("description").description("단계별 조리 방법 설명");
    private static final FieldDescriptor DOC_FIELD_PICTURE_URL =
        fieldWithPath("pictureUrl").description("참고 이미지 URL");
    private static final ParameterDescriptor DOC_PARAMETER_RECIPE_ID =
        parameterWithName("recipeId").description("레시피 ID").optional();

    private static final FieldDescriptor DOC_FIELD_POST_BULK_STEPS =
        fieldWithPath("steps").type("Step[]").description("조리 단계 생성 필요필드 배열");
    private static final FieldDescriptor DOC_FIELD_POST_BULK_STEP =
        subsectionWithPath("steps.[]").type("Step").description("조리 단계 생성 필요필드와 동일");

    private static final FieldDescriptor DOC_FIELD_PATCH_BULK_STEPS =
        subsectionWithPath("steps").type("Map<id, step>").description("조리 단계 수정 필요필드 배열");

    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    @Autowired
    private JsonHelper jsonHelper;
    @Autowired
    private StepRepository repository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TransactionHelper trxHelper;
    @Autowired
    private EntityHelper entityHelper;
    @Autowired
    private StepAuthHelper stepAuthHelper;

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
    class PostStep {

        @Test
        void postStep() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();
                String token = stepAuthHelper.generateToken(recipe);
                return new Struct()
                    .withValue("recipeId", recipe.getId())
                    .withValue("token", token);
            });
            Long recipeId = given.valueOf("recipeId");
            String token = given.valueOf("token");

            // When
            Single dto = StepDto.Create.Single.builder()
                .recipeId(recipeId)
                .description("고춧가루 2수저 가득, 청정원 고추장 1수저 가득, "
                    + "청정원 양조간장 3수저, 맛술 1수저, 설탕 2수저, "
                    + "청정원 올리고당 1수저, 청정원 다진 마늘 1수저 넣고 양념장을 만든다.")
                .pictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/post/recipe/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("고춧가루 2수저 가득, 청정원 고추장 1수저 가득, "
                    + "청정원 양조간장 3수저, 맛술 1수저, 설탕 2수저, "
                    + "청정원 올리고당 1수저, 청정원 다진 마늘 1수저 넣고 양념장을 만든다."))
                .andExpect(
                    jsonPath("$.pictureUrl").value("C:\\Users\\eunsung\\Desktop\\temp\\picture"))
                .andReturn();

            // Document
            actions.andDo(document("step-create-example",
                requestFields(
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_DESCRIPTION,
                    DOC_FIELD_PICTURE_URL
                )));
        }

        @Test
        void postStep_RecipeNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aStepCreateDto());

            ResultActions actions = mockMvc.perform(post("/post/recipe/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isNotFound())
                .andReturn();
        }

    }

    @Nested
    class GetStep {

        @Test
        void getStep() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step step = entityHelper.generateStep(it ->
                    it.withDescription("고춧가루 2수저 가득, 고추장 1수저 가득, "
                            + "양조간장 3수저, 맛술 1수저, 설탕 2수저, "
                            + "올리고당 1수저, 다진 마늘 1수저 넣고 양념장을 만든다.")
                        .withPictureUrl("C:\\Users\\eunsung\\Desktop\\temp\\picture")
                );
                return new Struct()
                    .withValue("id", step.getId())
                    .withValue("recipeId", step.getRecipe().getId());
            });
            Long id = given.valueOf("id");
            Long recipeId = given.valueOf("recipeId");

            // When
            ResultActions actions = mockMvc
                .perform(get("/post/recipe/steps/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.recipeId").value(recipeId))
                .andExpect(jsonPath("$.description").value(
                    "고춧가루 2수저 가득, 고추장 1수저 가득, "
                        + "양조간장 3수저, 맛술 1수저, 설탕 2수저, "
                        + "올리고당 1수저, 다진 마늘 1수저 넣고 양념장을 만든다."))
                .andExpect(jsonPath("$.pictureUrl")
                    .value("C:\\Users\\eunsung\\Desktop\\temp\\picture"));

            // Document
            actions.andDo(document("step-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_RECIPE_ID,
                    DOC_FIELD_DESCRIPTION,
                    DOC_FIELD_PICTURE_URL
                )));
        }

        @Test
        void getStep_StepNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/steps/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchSteps {

        @Test
        void listSteps() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step stepA = entityHelper.generateStep();
                Step stepB = entityHelper.generateStep();

                return new Struct()
                    .withValue("stepAId", stepA.getId())
                    .withValue("stepBId", stepB.getId());
            });
            Long stepAId = given.valueOf("stepAId");
            Long stepBId = given.valueOf("stepBId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/steps"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.steps.[*].id").value(containsInAnyOrder(
                    stepAId.intValue(),
                    stepBId.intValue()
                )));

            // Document
            actions.andDo(document("step-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listStepsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step stepA = entityHelper.generateStep();
                Step stepB = entityHelper.generateStep();
                Step stepC = entityHelper.generateStep();
                Step stepD = entityHelper.generateStep();
                Step stepE = entityHelper.generateStep();

                return new Struct()
                    .withValue("stepBId", stepB.getId())
                    .withValue("stepCId", stepC.getId());
            });
            Long stepBId = given.valueOf("stepBId");
            Long stepCId = given.valueOf("stepCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/steps")
                .param("size", "2")
                .param("page", "1")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.steps.[*].id").value(contains(
                    stepCId.intValue(),
                    stepBId.intValue()
                )));

            // Document
            actions.andDo(document("step-list-with-paging-example"));
        }

        @Test
        void searchStepsByRecipeId() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Recipe recipe = entityHelper.generateRecipe();

                Step stepA = entityHelper.generateStep();
                Step stepB = entityHelper.generateStep(it -> it.withRecipe(recipe));
                Step stepC = entityHelper.generateStep(it -> it.withRecipe(recipe));
                Step stepD = entityHelper.generateStep();
                Step stepE = entityHelper.generateStep();

                return new Struct()
                    .withValue("recipeId", recipe.getId())
                    .withValue("stepBId", stepB.getId())
                    .withValue("stepCId", stepC.getId());
            });
            Long recipeId = given.valueOf("recipeId");
            Long stepBId = given.valueOf("stepBId");
            Long stepCId = given.valueOf("stepCId");

            // When
            ResultActions actions = mockMvc.perform(get("/post/recipe/steps")
                .param("recipeId", recipeId.toString()));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.steps.[*].id").value(containsInAnyOrder(
                    stepBId.intValue(),
                    stepCId.intValue()
                )));

            // Document
            actions.andDo(document("step-search-example",
                requestParameters(
                    DOC_PARAMETER_RECIPE_ID
                )));
        }

    }

    @Nested
    class PatchStep {

        @Test
        void patchStep() throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step step = entityHelper.generateStep();
                String token = stepAuthHelper.generateToken(step);

                return new Struct()
                    .withValue("token", token)
                    .withValue("id", step.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            Update dto = Update.builder()
                .description("새로운 조리 방법!")
                .pictureUrl("완전 새로운 사진!")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/post/recipe/steps/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("새로운 조리 방법!"))
                .andExpect(jsonPath("$.pictureUrl").value("완전 새로운 사진!"))
                .andReturn();

            // Document
            actions.andDo(document("step-update-example",
                requestFields(
                    DOC_FIELD_DESCRIPTION,
                    DOC_FIELD_PICTURE_URL
                )));
        }

        @Test
        void patchStep_StepNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aStepUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/post/recipe/steps/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteStep {

        @Test
        void deleteStep() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Step step = entityHelper.generateStep();
                String token = stepAuthHelper.generateToken(step);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", step.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipe/steps/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("step-delete-example"));
        }

        @Test
        void deleteStep_StepNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/post/recipe/steps/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class StepBulkTest {

        @Nested
        class BulkPostStep {

            @Test
            void bulkPostStep() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    String token = stepAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("recipeId", recipe.getId())
                        .withValue("token", token);
                });
                Long recipeId = given.valueOf("recipeId");
                String token = given.valueOf("token");

                // When
                Bulk.Create.Single dto = tripleStepsBulkCreateDto(it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.steps().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-step")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.steps").value(aMapWithSize(dtoNumber)))
                    .andReturn();

                assertThat(repository.count()).isEqualTo(dtoNumber);

                // Document
                actions.andDo(document("step-bulk-create-example",
                    requestFields(
                        DOC_FIELD_POST_BULK_STEPS,
                        DOC_FIELD_POST_BULK_STEP
                    )));
            }

            @Test
            void 존재하지_않는_Recipe에_Step들을_생성() throws Exception {
                String body = jsonHelper.toJson(tripleStepsBulkCreateDto());

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-step")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isNotFound())
                    .andReturn();
            }

            @Test
            void 부적절한_토큰의_Recipe에_Step들을_생성() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipeA = entityHelper.generateRecipe();
                    Recipe recipeB = entityHelper.generateRecipe();
                    String token = stepAuthHelper.generateToken(recipeB);
                    return new Struct()
                        .withValue("recipeId", recipeA.getId())
                        .withValue("token", token);
                });
                Long recipeId = given.valueOf("recipeId");
                String token = given.valueOf("token");

                // When
                Bulk.Create.Single dto = tripleStepsBulkCreateDto(it -> it.withRecipeId(recipeId));
                int dtoNumber = dto.steps().size();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(post("/post/recipe/bulk-step")
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
        class BulkPatchStep {

            @Test
            void bulkPatchStep() throws Exception {

                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    Step stepA = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepB = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepC = entityHelper.generateStep(it -> it.withRecipe(recipe));

                    String token = stepAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("stepAId", stepA.getId())
                        .withValue("stepBId", stepB.getId())
                        .withValue("stepCId", stepC.getId());
                });
                String token = given.valueOf("token");
                Long stepAId = given.valueOf("stepAId");
                Long stepBId = given.valueOf("stepBId");
                Long stepCId = given.valueOf("stepCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .step(stepBId, Update.builder().description("바뀐 StepB").build())
                    .step(stepCId, Update.builder().description("바뀐 StepC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-step")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                MvcResult mvcResult = actions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.steps.[*].id").value(containsInAnyOrder(
                        stepBId.intValue(),
                        stepCId.intValue()
                    )))
                    .andExpect(jsonPath("$.steps." + stepBId + ".description").value("바뀐 StepB"))
                    .andExpect(jsonPath("$.steps." + stepCId + ".description").value("바뀐 StepC"))
                    .andReturn();

                // Document
                actions.andDo(document("step-bulk-update-example",
                    requestFields(
                        DOC_FIELD_PATCH_BULK_STEPS
                    )));
            }

            @Test
            void 존재하지_않는_Step이_일부있는_Step들을_수정() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    Step stepA = entityHelper.generateStep(it -> it
                        .withDescription("바뀌기 전StepA")
                        .withRecipe(recipe));
                    Step stepB = entityHelper.generateStep(it -> it
                        .withDescription("바뀌기 전StepB")
                        .withRecipe(recipe));
                    Step stepC = entityHelper.generateStep(it -> it
                        .withDescription("바뀌기 전StepC")
                        .withRecipe(recipe));

                    String token = stepAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("stepAId", stepA.getId())
                        .withValue("stepBId", stepB.getId())
                        .withValue("stepCId", stepC.getId());
                });
                String token = given.valueOf("token");
                Long stepAId = given.valueOf("stepAId");
                Long stepBId = given.valueOf("stepBId");
                Long stepCId = given.valueOf("stepCId");

                // When
                Bulk.Update dto = Bulk.Update.builder()
                    .step(stepBId, Update.builder().description("바뀐 StepB").build())
                    .step(2222L, Update.builder().description("존재하지 않는 Step").build())
                    .step(stepCId, Update.builder().description("바뀐 StepC").build())
                    .build();
                String body = jsonHelper.toJson(dto);

                ResultActions actions = mockMvc.perform(patch("/post/recipe/bulk-step")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content(body));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.findById(stepAId).get().getDescription()).isEqualTo(
                    "바뀌기 전StepA");
                assertThat(repository.findById(stepBId).get().getDescription()).isEqualTo(
                    "바뀌기 전StepB");
                assertThat(repository.findById(stepCId).get().getDescription()).isEqualTo(
                    "바뀌기 전StepC");
            }
        }

        @Nested
        class BulkDeleteStep {

            @Test
            void bulkDeleteStep() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {

                    Recipe recipe = entityHelper.generateRecipe();
                    Step stepA = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepB = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepC = entityHelper.generateStep(it -> it.withRecipe(recipe));

                    String token = stepAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("stepAId", stepA.getId())
                        .withValue("stepBId", stepB.getId())
                        .withValue("stepCId", stepC.getId());
                });
                String token = given.valueOf("token");
                Long stepAId = given.valueOf("stepAId");
                Long stepBId = given.valueOf("stepBId");
                Long stepCId = given.valueOf("stepCId");

                // When
                String ids = "" + stepAId + "," + stepBId + "," + stepCId;
                ResultActions actions = mockMvc.perform(delete("/post/recipe/bulk-step/{ids}", ids)
                    .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(emptyString()));

                assertThat(repository.existsById(stepAId)).isFalse();
                assertThat(repository.existsById(stepBId)).isFalse();
                assertThat(repository.existsById(stepCId)).isFalse();

                // Document
                actions.andDo(document("step-bulk-delete-example"));
            }

            @Test
            void 존재하지_않는_Step이_일부있는_Step들을_삭제() throws Exception {
                // Given
                Struct given = trxHelper.doInTransaction(() -> {
                    Recipe recipe = entityHelper.generateRecipe();
                    Step stepA = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepB = entityHelper.generateStep(it -> it.withRecipe(recipe));
                    Step stepC = entityHelper.generateStep(it -> it.withRecipe(recipe));

                    String token = stepAuthHelper.generateToken(recipe);
                    return new Struct()
                        .withValue("token", token)
                        .withValue("stepAId", stepA.getId())
                        .withValue("stepBId", stepB.getId())
                        .withValue("stepCId", stepC.getId());
                });
                String token = given.valueOf("token");
                Long stepAId = given.valueOf("stepAId");
                Long stepBId = given.valueOf("stepBId");
                Long stepCId = given.valueOf("stepCId");

                // When
                String ids = "" + stepAId + "," + 2000L + "," + stepCId;
                ResultActions actions = mockMvc.perform(delete("/post/recipe/bulk-step/{ids}", ids)
                    .header("Authorization", "Bearer " + token));

                // Then
                actions
                    .andExpect(status().isNotFound());

                assertThat(repository.existsById(stepAId)).isTrue();
                assertThat(repository.existsById(stepBId)).isTrue();
                assertThat(repository.existsById(stepCId)).isTrue();
            }
        }
    }
}
