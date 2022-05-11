package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Create;
import static kr.reciptopia.reciptopiaserver.helper.SearchHistoryHelper.aSearchHistoryCreateDto;
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
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.SearchHistoryAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.SearchHistoryRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SearchHistoryIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID = fieldWithPath("id").description("검색 기록 ID");
    private static final FieldDescriptor DOC_FIELD_OWNER_ID = fieldWithPath("ownerId").description(
        "검색기록 소유자 ID");
    private static final FieldDescriptor DOC_FIELD_INGREDIENT_NAMES = fieldWithPath(
        "ingredientNames").description("검색 재료 이름들");
    private static final FieldDescriptor DOC_FIELD_CREATE_DATE = fieldWithPath(
        "createdDate").description("검색 기록 생성 시간");
    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;
    @Autowired
    private JsonHelper jsonHelper;
    @Autowired
    private SearchHistoryRepository repository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TransactionHelper trxHelper;
    @Autowired
    private EntityHelper entityHelper;
    @Autowired
    private SearchHistoryAuthHelper searchHistoryAuthHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity()).apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Nested
    class PostSearchHistory {

        @Test
        void postSearchHistory() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();

                String token = searchHistoryAuthHelper.generateToken(account);
                return new Struct().withValue("token", token).withValue("ownerId", account.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");

            // When
            Create dto = Create.builder().ownerId(ownerId).ingredientName("고추장")
                .ingredientName("삼겹살").ingredientName("감자").build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(
                post("/account/searchHistories").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token).content(body));

            // Then
            MvcResult mvcResult = actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(ownerId)).andExpect(
                    jsonPath("$.ingredientNames").value(containsInAnyOrder("고추장", "삼겹살", "감자")))
                .andReturn();

            // Document
            actions.andDo(document("search-history-create-example",
                requestFields(DOC_FIELD_OWNER_ID, DOC_FIELD_INGREDIENT_NAMES)));
        }

        @Test
        void postSearchHistory_SearchHistoryNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(aSearchHistoryCreateDto());

            ResultActions actions = mockMvc.perform(post("/account/searchHistories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetSearchHistory {

        @Test
        void getSearchHistory() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                SearchHistory searchHistory = entityHelper.generateSearchHistory(
                    it -> it.withIngredientName("고추장")
                        .withIngredientName("삼겹살")
                        .withIngredientName("감자"));

                String token = searchHistoryAuthHelper.generateToken(searchHistory);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", searchHistory.getId())
                    .withValue("ownerId", searchHistory.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(get("/account/searchHistories/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ownerId").value(ownerId)).andExpect(
                    jsonPath("$.ingredientNames").value(containsInAnyOrder("고추장", "삼겹살", "감자")))
                .andReturn();

            // Document
            actions.andDo(document("search-history-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_OWNER_ID,
                    DOC_FIELD_INGREDIENT_NAMES,
                    DOC_FIELD_CREATE_DATE)));
        }

        @Test
        void getSearchHistory_SearchHistoryNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/account/searchHistories/{id}", 0L));

            // Then
            actions.andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchSearchHistorys {

        @Test
        void listSearchHistorys() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                SearchHistory searchHistoryA = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                SearchHistory searchHistoryB = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                String token = searchHistoryAuthHelper.generateToken(owner);

                return new Struct()
                    .withValue("ownerId", owner.getId())
                    .withValue("token", token)
                    .withValue("searchHistoryAId", searchHistoryA.getId())
                    .withValue("searchHistoryBId", searchHistoryB.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long searchHistoryAId = given.valueOf("searchHistoryAId");
            Long searchHistoryBId = given.valueOf("searchHistoryBId");

            // When
            ResultActions actions = mockMvc.perform(
                get("/account/{ownerId}/searchHistories", ownerId)
                    .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchHistories").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.searchHistories.[*].id").value(containsInAnyOrder(
                    searchHistoryAId.intValue(),
                    searchHistoryBId.intValue()
                )));

            // Document
            actions.andDo(document("search-history-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listSearchHistorysWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                SearchHistory searchHistoryA = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                SearchHistory searchHistoryB = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                SearchHistory searchHistoryC = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                SearchHistory searchHistoryD = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                SearchHistory searchHistoryE = entityHelper.generateSearchHistory(
                    it -> it.withOwner(owner));
                String token = searchHistoryAuthHelper.generateToken(owner);

                return new Struct()
                    .withValue("ownerId", owner.getId())
                    .withValue("token", token)
                    .withValue("searchHistoryBId", searchHistoryB.getId())
                    .withValue("searchHistoryCId", searchHistoryC.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long searchHistoryBId = given.valueOf("searchHistoryBId");
            Long searchHistoryCId = given.valueOf("searchHistoryCId");

            // When
            ResultActions actions = mockMvc.perform(
                get("/account/{ownerId}/searchHistories", ownerId)
                    .header("Authorization", "Bearer " + token)
                    .param("size", "2")
                    .param("page", "1")
                    .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchHistories").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.searchHistories.[*].id").value(contains(
                    searchHistoryCId.intValue(),
                    searchHistoryBId.intValue()
                )));

            // Document
            actions.andDo(document("search-history-list-with-paging-example"));
        }

    }

    @Nested
    class DeleteSearchHistory {

        @Test
        void deleteSearchHistory() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                SearchHistory searchHistory = entityHelper.generateSearchHistory();
                String token = searchHistoryAuthHelper.generateToken(searchHistory);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", searchHistory.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/account/searchHistories/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(id)).isEmpty();

            // Document
            actions.andDo(document("search-history-delete-example"));
        }

        @Test
        void deleteSearchHistory_SearchHistoryNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/account/searchHistories/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }
    }
}
