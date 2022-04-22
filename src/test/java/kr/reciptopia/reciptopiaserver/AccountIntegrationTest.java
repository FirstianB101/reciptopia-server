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
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.AccountAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.CommentAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.PostAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.ReplyAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyRepository;
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
    private static final FieldDescriptor DOC_FIELD_EMAIL_DUPLICATION =
        fieldWithPath("exists").description("사용자 email 중복 여부");

    private MockMvc mockMvc;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private AccountRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionHelper trxHelper;

    @Autowired
    private EntityHelper entityHelper;

    @Autowired
    private AccountAuthHelper accountAuthHelper;

    @Autowired
    PasswordEncoder passwordEncoder;

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
    class PostAccount {

        @Test
        void postAccount() throws Exception {
            // When
            Create dto = Create.builder()
                .email("test@email.com")
                .password("this!sPassw0rd")
                .nickname("pte1024")
                .build();
            String body = jsonHelper.toJson(dto);

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
            Result resultDto = jsonHelper.fromJson(responseBody, Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Account account = repository.findById(resultDto.id()).orElseThrow();
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
                        .withPassword(passwordEncoder::encode, "this!sPassw0rd")
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

            // Document
            actions.andDo(document("account-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_EMAIL,
                    DOC_FIELD_NICKNAME,
                    DOC_FIELD_PICTURE_URL,
                    DOC_FIELD_ROLE
                )));
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
        void patchAccount() throws Exception {

            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account account = entityHelper.generateAccount(it ->
                    it.withEmail("test@email.com")
                        .withPassword(passwordEncoder::encode, "this!sPassw0rd")
                        .withNickname("pte1024")
                        .withProfilePictureUrl("C:\\Users\\tellang\\Desktop\\temp\\picture")
                        .withRole(UserRole.USER)
                );

                String token = accountAuthHelper.generateToken(account);
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
            String body = jsonHelper.toJson(dto);

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
            Result resultDto = jsonHelper.fromJson(responseBody, Result.class);

            String encodedPassword = trxHelper.doInTransaction(() -> {
                Account account = repository.findById(resultDto.id()).orElseThrow();
                return account.getPassword();
            });
            assertThat(passwordEncoder.matches("newPassw0rd", encodedPassword)).isTrue();

            // Document
            actions.andDo(document("account-update-example",
                requestFields(
                    DOC_FIELD_EMAIL,
                    DOC_FIELD_PASSWORD,
                    DOC_FIELD_NICKNAME,
                    DOC_FIELD_PICTURE_URL
                )));
        }

        @Test
        void patchAccount_AccountNotFound_NotFoundStatus() throws Exception {
            // When
            String body = jsonHelper.toJson(anAccountUpdateDto());

            ResultActions actions = mockMvc.perform(patch("/accounts/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void Post가_있는_Account_수정(
            @Autowired PostRepository postRepository
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post post = entityHelper.generatePost();
                String token = accountAuthHelper.generateToken(post.getOwner());
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", post.getId())
                    .withValue("ownerId", post.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");
            Long ownerId = given.valueOf("ownerId");

            // When
            Update dto = Update.builder()
                .email("new@email.com")
                .password("newPassw0rd")
                .nickname("new1024")
                .profilePictureUrl("C:\\Users\\tellang\\Desktop\\temp\\new-picture")
                .build();
            String body = jsonHelper.toJson(dto);

            ResultActions actions = mockMvc.perform(patch("/accounts/{id}", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body));

            // Then
            MvcResult mvcResult = actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerId))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.profilePictureUrl").value(
                    "C:\\Users\\tellang\\Desktop\\temp\\new-picture"))
                .andExpect(jsonPath("$.nickname").value("new1024"))
                .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            Result resultDto = jsonHelper.fromJson(responseBody, Result.class);

            Struct then = trxHelper.doInTransaction(() -> {
                Account account = repository.findById(resultDto.id()).orElseThrow();
                Long postOwnerId = postRepository.findById(postId).orElseThrow()
                    .getOwner().getId();
                return new Struct()
                    .withValue("encodedPassword", account.getPassword())
                    .withValue("postOwnerId", postOwnerId);
            });
            String encodedPassword = then.valueOf("encodedPassword");
            Long postOwnerId = then.valueOf("postOwnerId");
            assertThat(passwordEncoder.matches("newPassw0rd", encodedPassword)).isTrue();
            assertThat(postOwnerId).isEqualTo(ownerId);
        }
    }

    @Nested
    class DeleteAccount {

        @Test
        void deleteAccount() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account account = entityHelper.generateAccount();
                String token = accountAuthHelper.generateToken(account);
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

            assertThat(repository.existsById(id)).isFalse();

            // Document
            actions.andDo(document("account-delete-example"));
        }

        @Test
        void deleteAccount_AccountNotFound_NotFound_Status() throws Exception {
            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", 0L));

            // Then
            actions
                .andExpect(status().isNotFound());
        }

        @Test
        void Post가_있는_Account_삭제(
            @Autowired PostRepository postRepository,
            @Autowired PostAuthHelper postAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post post = entityHelper.generatePost();
                String token = postAuthHelper.generateToken(post);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postId", post.getId())
                    .withValue("ownerId", post.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long postId = given.valueOf("postId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(postRepository.findById(postId)).isEmpty();
        }

        @Test
        void Post들이_있는_Account_삭제(
            @Autowired PostRepository postRepository,
            @Autowired PostAuthHelper postAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                Post postA = entityHelper.generatePost(it -> it
                    .withOwner(owner));
                Post postB = entityHelper.generatePost(it -> it
                    .withOwner(owner));

                String token = postAuthHelper.generateToken(postA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postAId", postA.getId())
                    .withValue("postBId", postB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long postAId = given.valueOf("postAId");
            Long postBId = given.valueOf("postBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(postRepository.findById(postAId)).isEmpty();
            assertThat(postRepository.findById(postBId)).isEmpty();
        }

        @Test
        void Comment가_있는_Account_삭제(
            @Autowired CommentRepository commentRepository,
            @Autowired PostRepository postRepository,
            @Autowired CommentAuthHelper commentAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Comment comment = entityHelper.generateComment();
                String token = commentAuthHelper.generateToken(comment);
                return new Struct()
                    .withValue("token", token)
                    .withValue("commentId", comment.getId())
                    .withValue("postId", comment.getPost().getId())
                    .withValue("ownerId", comment.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long commentId = given.valueOf("commentId");
            Long ownerId = given.valueOf("ownerId");
            Long postId = given.valueOf("postId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(commentRepository.existsById(commentId)).isFalse();
            assertThat(postRepository.existsById(postId)).isTrue();
        }

        @Test
        void Comment들이_있는_Account_삭제(
            @Autowired CommentRepository commentRepository,
            @Autowired CommentAuthHelper commentAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                Comment commentA = entityHelper.generateComment(it -> it
                    .withOwner(owner));
                Comment commentB = entityHelper.generateComment(it -> it
                    .withOwner(owner));

                String token = commentAuthHelper.generateToken(commentA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("commentAId", commentA.getId())
                    .withValue("commentBId", commentB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long commentAId = given.valueOf("commentAId");
            Long commentBId = given.valueOf("commentBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(commentRepository.findById(commentAId)).isEmpty();
            assertThat(commentRepository.findById(commentBId)).isEmpty();
        }

        @Test
        void Reply가_있는_Account_삭제(
            @Autowired ReplyRepository replyRepository,
            @Autowired CommentRepository commentRepository,
            @Autowired ReplyAuthHelper replyAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Reply reply = entityHelper.generateReply();
                String token = replyAuthHelper.generateToken(reply);
                return new Struct()
                    .withValue("token", token)
                    .withValue("replyId", reply.getId())
                    .withValue("commentId", reply.getComment().getId())
                    .withValue("ownerId", reply.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long replyId = given.valueOf("replyId");
            Long ownerId = given.valueOf("ownerId");
            Long commentId = given.valueOf("commentId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(replyRepository.existsById(replyId)).isFalse();
            assertThat(commentRepository.existsById(commentId)).isTrue();
        }

        @Test
        void Reply들이_있는_Account_삭제(
            @Autowired ReplyRepository replyRepository,
            @Autowired ReplyAuthHelper replyAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                Reply replyA = entityHelper.generateReply(it -> it
                    .withOwner(owner));
                Reply replyB = entityHelper.generateReply(it -> it
                    .withOwner(owner));

                String token = replyAuthHelper.generateToken(replyA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("replyAId", replyA.getId())
                    .withValue("replyBId", replyB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long replyAId = given.valueOf("replyAId");
            Long replyBId = given.valueOf("replyBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(replyRepository.findById(replyAId)).isEmpty();
            assertThat(replyRepository.findById(replyBId)).isEmpty();
        }

    }

    @Nested
    class CheckDuplicateEmail {

        @Test
        void 중복되지_않는_email_중복확인조회() throws Exception {
            //When
            ResultActions actions = mockMvc.perform(get("/accounts/{email}/exists", "newUser"));

            //Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));

            // Document
            actions.andDo(document("account-check-duplicate-email-example",
                responseFields(
                    DOC_FIELD_EMAIL_DUPLICATION
                )));
        }

        @Test
        void 중복되는_email_중복확인조회() throws Exception {
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
