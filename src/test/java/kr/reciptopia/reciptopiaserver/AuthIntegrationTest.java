package kr.reciptopia.reciptopiaserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import javax.sql.DataSource;
import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.dto.AuthDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.util.H2DbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void setUp(WebApplicationContext webApplicationContext) throws SQLException {
        H2DbCleaner.clean(dataSource);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    void generateToken() throws Exception {
        // Given
        Struct given = trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withEmail("test@email.com")
                    .withPassword(encodedPassword("pAsSwOrD"))
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
        String body = toJson(dto);

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
        AuthDto.GenerateTokenResult resultDto = fromJson(responseBody,
            AuthDto.GenerateTokenResult.class);

        UserPrincipal principal = jwtService.extractSubject(resultDto.getToken());
        assertThat(principal.getId()).isEqualTo(id);
    }

    @Test
    void generateToken_InvalidPassword_UnauthorizedStatus() throws Exception {
        // Given
        trxHelper.doInTransaction(() -> {
            Account account = entityHelper.generateAccount(it ->
                it.withEmail("test@email.com")
                    .withPassword(encodedPassword("pAsSwOrD"))
            );
        });

        // When
        AuthDto.GenerateToken dto = AuthDto.GenerateToken.builder()
            .email("test@email.com")
            .password("wrongPassword")
            .build();
        String body = toJson(dto);

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
                    .withPassword(encodedPassword("pAsSwOrD"))
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

    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    private String encodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
