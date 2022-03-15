package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccountUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.docs.ApiDocumentation;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import kr.reciptopia.reciptopiaserver.helper.AuthHelper;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
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
public class AccountIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_ID =
        fieldWithPath("id").description("사용자 ID");
    private static final FieldDescriptor DOC_FIELD_EMAIL =
        fieldWithPath("email").description("사용자 이메일");
    private static final FieldDescriptor DOC_FIELD_PASSWORD =
        fieldWithPath("password").description("비밀번호");
    private static final FieldDescriptor DOC_FIELD_NICKNAME =
        fieldWithPath("nickname").description("닉네임");
    private static final FieldDescriptor DOC_FIELD_ROLE =
        fieldWithPath("role").description("시스템 역할 (`USER`, `ADMIN`)");
    private static final FieldDescriptor DOC_FIELD_PICTURE_URL =
        fieldWithPath("profilePictureUrl").description("프로필 사진 URL");
    private static final FieldDescriptor DOC_FIELD_USERNAME_DUPLICATION =
        fieldWithPath("exists").description("사용자 ID 중복 여부");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String responseBody, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, clazz);
    }

    @Nested
    class PostAccount {

        @Test
        void postAccount(@Autowired PasswordEncoder passwordEncoder) throws Exception {
            // When
            Create dto = Create.builder()
                .email("test@email.com")
                .password("this!sPassw0rd")
                .nickname("pte1024")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.nickname").value("pte1024"))
                .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            Result resultDto = fromJson(responseBody, Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Account account = repository.findById(resultDto.getId()).orElseThrow();
                return account.getPassword();
            });
            assertThat(passwordEncoder.matches("this!sPassw0rd", encodedPassword)).isTrue();

            // Document
            actions.andDo(document("account-create-example",
                requestFields(
                    DOC_FIELD_EMAIL,
                    DOC_FIELD_PASSWORD,
                    DOC_FIELD_NICKNAME
                )));
        }
    }

    @Nested
    class GetAccount {

        @Test
        void getAccount() throws Exception {
            // Given
            Long id = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withEmail("test@email.com")
                        .withPassword("this!sPassw0rd")
                        .withNickname("pte1024")
                        .withProfilePictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                        .withRole(UserRole.USER)
                );
                return account.getId();
            });

            // When
            ResultActions actions = mockMvc
                .perform(get("/accounts/{id}", id));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.profilePictureUrl").value(
                    "C:\\Users\\tellang\\Desktop\\temp\\picture"))
                .andExpect(jsonPath("$.nickname").value("pte1024"));
        }

        @Test
        void getAccount_AccountNotFound_NotFoundStatus() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(get("/accounts/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class SearchAccounts {

        @Test
        void listAccounts() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account accountA = entityHelper.generateAccount();
                Account accountB = entityHelper.generateAccount();

                return new Struct()
                    .withValue("accountAId", accountA.getId())
                    .withValue("accountBId", accountB.getId());
            });
            Long accountAId = given.valueOf("accountAId");
            Long accountBId = given.valueOf("accountBId");

            // When
            ResultActions actions = mockMvc.perform(get("/accounts"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(containsInAnyOrder(
                    accountAId.intValue(),
                    accountBId.intValue()
                )));

            // Document
            actions.andDo(document("account-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                )));
        }

        @Test
        void listAccountsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateAccount();
                Account accountA = entityHelper.generateAccount();
                Account accountB = entityHelper.generateAccount();

                return new Struct()
                    .withValue("accountAId", accountA.getId())
                    .withValue("accountBId", accountB.getId());
            });
            Long accountAId = given.valueOf("accountAId");
            Long accountBId = given.valueOf("accountBId");

            // When
            ResultActions actions = mockMvc.perform(get("/accounts")
                .param("size", "2")
                .param("page", "0")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$.[*].id").value(contains(
                    accountBId.intValue(),
                    accountAId.intValue()
                )));

            // Document
            actions.andDo(document("account-list-with-paging-example"));
        }

    }

    @Nested
    class PatchAccount {

        @Test
        void patchAccount(@Autowired PasswordEncoder passwordEncoder) throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withEmail("test@email.com")
                        .withPassword("this!sPassw0rd")
                        .withNickname("pte1024")
                        .withProfilePictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                        .withRole(UserRole.USER)
                );

                String token = authHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            Update dto = Update.builder()
                .email("new@email.com")
                .password("newPassw0rd")
                .nickname("new1024")
                .build();
            String body = toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/accounts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.profilePictureUrl").value(
                    "C:\\Users\\tellang\\Desktop\\temp\\picture"))
                .andExpect(jsonPath("$.nickname").value("new1024"))
                .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            Result resultDto = fromJson(responseBody, Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Account account = repository.findById(resultDto.getId()).orElseThrow();
                return account.getPassword();
            });
            assertThat(passwordEncoder.matches("newPassw0rd", encodedPassword)).isTrue();
        }

        @Test
        void patchAccount_AccountNotFound_NotFoundStatus() throws Exception {
            // When
            String body = toJson(anAccountUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/accounts/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteAccount {

        @Test
        void deleteAccount() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account account = entityHelper.generateAccount();
                String token = authHelper.generateToken(account);
                return new Struct()
                    .withValue("token", token)
                    .withValue("id", account.getId());
            });
            String token = given.valueOf("token");
            Long id = given.valueOf("id");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", id)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        void deleteAccount_AccountNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

    }

    @Nested
    class CheckDuplicateEmail {

        @Test
        public void 중복되지_않는_email_중복확인조회() throws Exception {
            //When
            ResultActions actions = mockMvc.perform(get("/accounts/{email}/exists", "newUser"));

            //Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
        }

        @Test
        public void 중복되는_email_중복확인조회() throws Exception {
            // Given
            String email = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount();
                return account.getEmail();
            });

            // When
            ResultActions actions = mockMvc.perform(get("/accounts/{email}/exists",
                email));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
        }
    }

}
