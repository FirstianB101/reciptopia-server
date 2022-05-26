package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Create;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.Bulk.tripleMainIngredientsBulkCreateWithRecipeDto;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPostCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.Bulk.tripleStepsBulkCreateWithRecipeDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.Bulk.tripleSubIngredientsBulkCreateWithRecipeDto;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.PostAuthHelper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RecipePostIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_POST =
        subsectionWithPath("post").type("Post").description("게시글 생성 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_MAIN_INGREDIENT =
        subsectionWithPath("mainIngredients").type("MainIngredient[]")
            .description("주 재료 다중 생성 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_SUB_INGREDIENT =
        subsectionWithPath("subIngredients").type("SubIngredient[]")
            .description("부 재료 다중 생성 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_STEP =
        subsectionWithPath("steps").type("Step[]").description("조리 단계 다중 생성 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_POST_RESPONSE =
        subsectionWithPath("post").type("Post").description("게시글 조회 결과 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_RECIPE_RESPONSE =
        subsectionWithPath("recipe").type("Recipe").description("레시피 조회 결과 필드와 동일");
    private static final FieldDescriptor DOC_FIELD_BULK_MAIN_INGREDIENTS_GRUOP_BY_POST_RESPONSE =
        subsectionWithPath("bulkMainIngredient.mainIngredients").type("Map<id, mainIngredient>")
            .description("주 재료의 Id를 Key 로 하고 주 재료 List를 Value 갖는 Map");
    private static final FieldDescriptor DOC_FIELD_BULK_SUB_INGREDIENTS_RESPONSE =
        subsectionWithPath("bulkSubIngredient.subIngredients").type("Map<id, subIngredient>")
            .description("부 재료의 Id를 Key 로 하고 부 재료를 Value로 갖는 Map");
    private static final FieldDescriptor DOC_FIELD_BULK_STEPS_RESPONSE =
        subsectionWithPath("bulkStep.steps").type("Map<id, step>")
            .description("조리 단계의 Id를 Key 로 하고 조리 단계를 Value로 갖는 Map");
    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    @Autowired
    private JsonHelper jsonHelper;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TransactionHelper trxHelper;
    @Autowired
    private EntityHelper entityHelper;
    @Autowired
    private PostAuthHelper postAuthHelper;

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
    class PostRecipePost {

        @Test
        void postRecipePost() throws Exception {
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
                .post(aPostCreateDto(it -> it.withOwnerId(ownerId)))
                .mainIngredients(tripleMainIngredientsBulkCreateWithRecipeDto())
                .subIngredients(tripleSubIngredientsBulkCreateWithRecipeDto())
                .steps(tripleStepsBulkCreateWithRecipeDto())
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(post("/recipePosts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.post.id").isNumber())
                .andExpect(jsonPath("$.recipe.id").isNumber())
                .andExpect(jsonPath("$.bulkMainIngredient.mainIngredients").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.bulkSubIngredient.subIngredients").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.bulkStep.steps").value(aMapWithSize(3)));

            // Document
            actions.andDo(document("recipe-post-create-example",
                requestFields(
                    DOC_FIELD_POST,
                    DOC_FIELD_MAIN_INGREDIENT,
                    DOC_FIELD_SUB_INGREDIENT,
                    DOC_FIELD_STEP
                ),
                responseFields(
                    DOC_FIELD_POST_RESPONSE,
                    DOC_FIELD_RECIPE_RESPONSE,
                    DOC_FIELD_BULK_MAIN_INGREDIENTS_GRUOP_BY_POST_RESPONSE,
                    DOC_FIELD_BULK_SUB_INGREDIENTS_RESPONSE,
                    DOC_FIELD_BULK_STEPS_RESPONSE
                )));
        }
    }
}
