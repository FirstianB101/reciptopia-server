package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.dto.AuthDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
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
public class AuthIntegrationTest {

    private static final FieldDescriptor DOC_FIELD_EMAIL =
        fieldWithPath("email").description("사용자 이메일");
    private static final FieldDescriptor DOC_FIELD_PASSWORD =
        fieldWithPath("password").description("비밀번호");
    private static final FieldDescriptor DOC_FIELD_TOKEN =
        fieldWithPath("token").description("엑세스 토큰");
    private static final FieldDescriptor DOC_FIELD_ACCOUNT = subsectionWithPath("account")
        .type("Account").description("사용자 계정");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .apply(basicDocumentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    void generateToken() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withEmail("test@email.com")
                    .withPassword(passwordEncoder::encode, "pAsSwOrD")
            );

            return new Struct()
                .withValue("id", account.getId());
        });
        Long id = given.valueOf("id");

        // When
        AuthDto.GenerateToken dto = AuthDto.GenerateToken.builder()
            .email("test@email.com")
            .password("pAsSwOrD")
            .build();
        String body = jsonHelper.toJson(dto);

        ResultActions actions = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        MvcResult result = actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.account.id").value(id))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthDto.GenerateTokenResult resultDto = jsonHelper.fromJson(responseBody,
            AuthDto.GenerateTokenResult.class);

        UserPrincipal principal = jwtService.extractSubject(resultDto.token());
        assertThat(principal.id()).isEqualTo(id);

        // Document
        actions.andDo(document("auth-generate-token-example",
            requestFields(
                DOC_FIELD_EMAIL,
                DOC_FIELD_PASSWORD
            ),
            responseFields(
                DOC_FIELD_TOKEN,
                DOC_FIELD_ACCOUNT
            )));
    }

    @Test
    void generateToken_InvalidPassword_UnauthorizedStatus() throws Exception {
        // Given
        trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withEmail("test@email.com")
                    .withPassword(passwordEncoder::encode, "pAsSwOrD")
            );
        });

        // When
        AuthDto.GenerateToken dto = AuthDto.GenerateToken.builder()
            .email("test@email.com")
            .password("wrongPassword")
            .build();
        String body = jsonHelper.toJson(dto);

        ResultActions actions = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // Then
        actions
            .andExpect(status().isUnauthorized())
            .andReturn();
    }

    @Test
    void getMe() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withEmail("test@email.com")
                    .withPassword(passwordEncoder::encode, "pAsSwOrD")
            );

            return new Struct()
                .withValue("token", jwtService.signJwt(account))
                .withValue("id", account.getId());
        });
        String token = given.valueOf("token");
        Long id = given.valueOf("id");

        // When
        ResultActions actions = mockMvc.perform(get("/auth/me")
            .header("Authorization", "Bearer " + token));

        // Then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account.id").value(id))
            .andReturn();

        // Document
        actions.andDo(document("auth-me-example",
            responseFields(
                DOC_FIELD_ACCOUNT
            )));
    }
}
