package kr.reciptopia.reciptopiaserver;

import static kr.reciptopia.reciptopiaserver.docs.ApiDocumentation.basicDocumentationConfiguration;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccountUpdateDto;
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
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import kr.reciptopia.reciptopiaserver.helper.EntityHelper;
import kr.reciptopia.reciptopiaserver.helper.JsonHelper;
import kr.reciptopia.reciptopiaserver.helper.Struct;
import kr.reciptopia.reciptopiaserver.helper.TransactionHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.AccountAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.CommentAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.FavoriteAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.LikeTagAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.PostAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.ReplyAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.SearchHistoryAuthHelper;
import kr.reciptopia.reciptopiaserver.helper.auth.UploadFileAuthHelper;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountProfileImgRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.FavoriteRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyRepository;
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
import org.springframework.restdocs.request.ParameterDescriptor;
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
    private static final FieldDescriptor DOC_FIELD_EMAIL_DUPLICATION =
        fieldWithPath("exists").description("사용자 email 중복 여부");
    private static final ParameterDescriptor DOC_PARAMETER_POST_IDS =
        parameterWithName("postIds").description("게시물 ID 배열").optional();
    private static final FieldDescriptor DOC_FIELD_BULK_MAP_BY_ID =
        subsectionWithPath("accounts").type("Map<id, step>")
            .description("계정의 Id를 Key 로 하고 계정을 Value로 갖는 Map");
    private static final FieldDescriptor DOC_FIELD_BULK_MAP_BY_POST_ID =
        subsectionWithPath("accounts").type("Map<postId, step>")
            .description("해당 게시글의 Id를 Key 로 하고 해당 게시글을 소유자의 계정을 Value로 갖는 Map");

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
                .andExpect(jsonPath("$.nickname").value("pte1024"));

            // Document
            actions.andDo(document("account-retrieve-example",
                responseFields(
                    DOC_FIELD_ID,
                    DOC_FIELD_EMAIL,
                    DOC_FIELD_NICKNAME,
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
                .andExpect(jsonPath("$.accounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.accounts.[*].id").value(containsInAnyOrder(
                    accountAId.intValue(),
                    accountBId.intValue()
                )));

            // Document
            actions.andDo(document("account-list-example",
                requestParameters(
                    ApiDocumentation.DOC_PARAMETER_PAGE,
                    ApiDocumentation.DOC_PARAMETER_SIZE,
                    ApiDocumentation.DOC_PARAMETER_SORT
                ),
                responseFields(
                    DOC_FIELD_BULK_MAP_BY_ID
                )));
        }

        @Test
        void listAccountsWithPaging() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                entityHelper.generateAccount();
                Account accountA = entityHelper.generateAccount();
                Account accountB = entityHelper.generateAccount();
                Account accountC = entityHelper.generateAccount();
                Account accountD = entityHelper.generateAccount();
                Account accountE = entityHelper.generateAccount();

                return new Struct()
                    .withValue("accountBId", accountB.getId())
                    .withValue("accountCId", accountC.getId());
            });
            Long accountBId = given.valueOf("accountBId");
            Long accountCId = given.valueOf("accountCId");

            // When
            ResultActions actions = mockMvc.perform(get("/accounts")
                .param("size", "2")
                .param("page", "1")
                .param("sort", "id,desc"));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").value(aMapWithSize(2)))
                .andExpect(jsonPath("$.accounts.[*].id").value(contains(
                    accountCId.intValue(),
                    accountBId.intValue()
                )));

            // Document
            actions.andDo(document("account-list-with-paging-example"));
        }

        @Test
        void searchAccountsByPostIds() throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Post postA = entityHelper.generatePost();
                Post postB = entityHelper.generatePost();
                Post postC = entityHelper.generatePost();
                Post postD = entityHelper.generatePost();
                Post postE = entityHelper.generatePost();

                return new Struct()
                    .withValue("ownerBId", postB.getOwner().getId())
                    .withValue("ownerCId", postC.getOwner().getId())
                    .withValue("ownerEId", postE.getOwner().getId())
                    .withValue("postBId", postB.getId())
                    .withValue("postCId", postC.getId())
                    .withValue("postEId", postE.getId());
            });
            Long ownerBId = given.valueOf("ownerBId");
            Long ownerCId = given.valueOf("ownerCId");
            Long ownerEId = given.valueOf("ownerEId");
            Long postBId = given.valueOf("postBId");
            Long postCId = given.valueOf("postCId");
            Long postEId = given.valueOf("postEId");

            // When
            String postIdsParam = postBId + ", " + postCId + ", " + postEId;
            ResultActions actions = mockMvc.perform(get("/accounts")
                .param("postIds", postIdsParam));

            // Then
            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").value(aMapWithSize(3)))
                .andExpect(jsonPath("$.accounts.[*].id").value(
                    containsInAnyOrder(
                        ownerCId.intValue(),
                        ownerBId.intValue(),
                        ownerEId.intValue()
                    )));

            // Document
            actions.andDo(document("account-search-example",
                requestParameters(
                    DOC_PARAMETER_POST_IDS
                ),
                responseFields(
                    DOC_FIELD_BULK_MAP_BY_POST_ID
                )));
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
                    DOC_FIELD_NICKNAME
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

        @Test
        void Favorite가_있는_Account_삭제(
            @Autowired FavoriteRepository favoriteRepository,
            @Autowired PostRepository postRepository,
            @Autowired FavoriteAuthHelper favoriteAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Favorite favorite = entityHelper.generateFavorite();
                String token = favoriteAuthHelper.generateToken(favorite);
                return new Struct()
                    .withValue("token", token)
                    .withValue("favoriteId", favorite.getId())
                    .withValue("postId", favorite.getPost().getId())
                    .withValue("ownerId", favorite.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long favoriteId = given.valueOf("favoriteId");
            Long postId = given.valueOf("postId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(favoriteRepository.existsById(favoriteId)).isFalse();
            assertThat(postRepository.existsById(postId)).isTrue();
        }

        @Test
        void Favorite들이_있는_Account_삭제(
            @Autowired FavoriteRepository favoriteRepository,
            @Autowired FavoriteAuthHelper favoriteAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                Favorite favoriteA = entityHelper.generateFavorite(it -> it
                    .withOwner(owner));
                Favorite favoriteB = entityHelper.generateFavorite(it -> it
                    .withOwner(owner));

                String token = favoriteAuthHelper.generateToken(favoriteA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("favoriteAId", favoriteA.getId())
                    .withValue("favoriteBId", favoriteB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long favoriteAId = given.valueOf("favoriteAId");
            Long favoriteBId = given.valueOf("favoriteBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(favoriteRepository.findById(favoriteAId)).isEmpty();
            assertThat(favoriteRepository.findById(favoriteBId)).isEmpty();
        }

        @Test
        void SearchHistory가_있는_Account_삭제(
            @Autowired SearchHistoryRepository searchHistoryRepository,
            @Autowired SearchHistoryAuthHelper searchHistoryAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                SearchHistory searchHistory = entityHelper.generateSearchHistory();
                String token = searchHistoryAuthHelper.generateToken(searchHistory);
                return new Struct()
                    .withValue("token", token)
                    .withValue("searchHistoryId", searchHistory.getId())
                    .withValue("ownerId", searchHistory.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long searchHistoryId = given.valueOf("searchHistoryId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(searchHistoryRepository.findById(searchHistoryId)).isEmpty();
        }

        @Test
        void SearchHistory들이_있는_Account_삭제(
            @Autowired SearchHistoryRepository searchHistoryRepository,
            @Autowired SearchHistoryAuthHelper searchHistoryAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                SearchHistory searchHistoryA = entityHelper.generateSearchHistory(it -> it
                    .withOwner(owner));
                SearchHistory searchHistoryB = entityHelper.generateSearchHistory(it -> it
                    .withOwner(owner));

                String token = searchHistoryAuthHelper.generateToken(searchHistoryA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("searchHistoryAId", searchHistoryA.getId())
                    .withValue("searchHistoryBId", searchHistoryB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long searchHistoryAId = given.valueOf("searchHistoryAId");
            Long searchHistoryBId = given.valueOf("searchHistoryBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(searchHistoryRepository.findById(searchHistoryAId)).isEmpty();
            assertThat(searchHistoryRepository.findById(searchHistoryBId)).isEmpty();
        }

        @Test
        void PostLikeTag가_있는_Account_삭제(
            @Autowired PostLikeTagRepository postLikeTagRepository,
            @Autowired PostRepository postRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                PostLikeTag postLikeTag = entityHelper.generatePostLikeTag();
                String token = likeTagAuthHelper.generateToken(postLikeTag);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postLikeTagId", postLikeTag.getId())
                    .withValue("postId", postLikeTag.getPost().getId())
                    .withValue("ownerId", postLikeTag.getOwner().getId());
            });
            String token = given.valueOf("token");
            Long postLikeTagId = given.valueOf("postLikeTagId");
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
            assertThat(postLikeTagRepository.existsById(postLikeTagId)).isFalse();
            assertThat(postRepository.existsById(postId)).isTrue();
        }

        @Test
        void PostLikeTag들이_있는_Account_삭제(
            @Autowired PostLikeTagRepository postLikeTagRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {

                Account owner = entityHelper.generateAccount();
                PostLikeTag postLikeTagA = entityHelper.generatePostLikeTag(it -> it
                    .withOwner(owner));
                PostLikeTag postLikeTagB = entityHelper.generatePostLikeTag(it -> it
                    .withOwner(owner));

                String token = likeTagAuthHelper.generateToken(postLikeTagA);
                return new Struct()
                    .withValue("token", token)
                    .withValue("postLikeTagAId", postLikeTagA.getId())
                    .withValue("postLikeTagBId", postLikeTagB.getId())
                    .withValue("ownerId", owner.getId());
            });
            String token = given.valueOf("token");
            Long postLikeTagAId = given.valueOf("postLikeTagAId");
            Long postLikeTagBId = given.valueOf("postLikeTagBId");
            Long ownerId = given.valueOf("ownerId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.findById(ownerId)).isEmpty();
            assertThat(postLikeTagRepository.findById(postLikeTagAId)).isEmpty();
            assertThat(postLikeTagRepository.findById(postLikeTagBId)).isEmpty();
        }

        @Test
        void CommentLikeTag가_있는_Account_삭제(
            @Autowired CommentLikeTagRepository commentLikeTagRepository,
            @Autowired CommentRepository commentRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                CommentLikeTag commentLikeTag = entityHelper.generateCommentLikeTag();
                String token = likeTagAuthHelper.generateToken(commentLikeTag);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", commentLikeTag.getOwner().getId())
                    .withValue("commentId", commentLikeTag.getComment().getId())
                    .withValue("commentLikeTagId", commentLikeTag.getId());
            });

            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long commentId = given.valueOf("commentId");
            Long commentLikeTagId = given.valueOf("commentLikeTagId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagId)).isFalse();
            assertThat(commentRepository.existsById(commentId)).isTrue();
        }

        @Test
        void CommentLikeTag들이_있는_Account_삭제(
            @Autowired CommentLikeTagRepository commentLikeTagRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                CommentLikeTag commentLikeTagA = entityHelper.generateCommentLikeTag(it -> it
                    .withOwner(owner));
                CommentLikeTag commentLikeTagB = entityHelper.generateCommentLikeTag(it -> it
                    .withOwner(owner));
                String token = likeTagAuthHelper.generateToken(commentLikeTagA);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", owner.getId())
                    .withValue("commentLikeTagAId", commentLikeTagA.getId())
                    .withValue("commentLikeTagBId", commentLikeTagB.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long commentLikeTagAId = given.valueOf("commentLikeTagAId");
            Long commentLikeTagBId = given.valueOf("commentLikeTagBId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagAId)).isFalse();
            assertThat(commentLikeTagRepository.existsById(commentLikeTagBId)).isFalse();
        }

        @Test
        void ReplyLikeTag가_있는_Account_삭제(
            @Autowired ReplyLikeTagRepository replyLikeTagRepository,
            @Autowired ReplyRepository replyRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                ReplyLikeTag replyLikeTag = entityHelper.generateReplyLikeTag();
                String token = likeTagAuthHelper.generateToken(replyLikeTag);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", replyLikeTag.getOwner().getId())
                    .withValue("replyId", replyLikeTag.getReply().getId())
                    .withValue("replyLikeTagId", replyLikeTag.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long replyId = given.valueOf("replyId");
            Long replyLikeTagId = given.valueOf("replyLikeTagId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagId)).isFalse();
            assertThat(replyRepository.existsById(replyId)).isTrue();
        }

        @Test
        void ReplyLikeTag들이_있는_Account_삭제(
            @Autowired ReplyLikeTagRepository replyLikeTagRepository,
            @Autowired LikeTagAuthHelper likeTagAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                Account owner = entityHelper.generateAccount();
                ReplyLikeTag replyLikeTagA = entityHelper.generateReplyLikeTag(it -> it
                    .withOwner(owner));
                ReplyLikeTag replyLikeTagB = entityHelper.generateReplyLikeTag(it -> it
                    .withOwner(owner));
                String token = likeTagAuthHelper.generateToken(replyLikeTagA);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", owner.getId())
                    .withValue("replyLikeTagAId", replyLikeTagA.getId())
                    .withValue("replyLikeTagBId", replyLikeTagB.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long replyLikeTagAId = given.valueOf("replyLikeTagAId");
            Long replyLikeTagBId = given.valueOf("replyLikeTagBId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagAId)).isFalse();
            assertThat(replyLikeTagRepository.existsById(replyLikeTagBId)).isFalse();
        }

        @Test
        void AccountProfileImg가_있는_Account_삭제(
            @Autowired AccountProfileImgRepository accountProfileImgRepository,
            @Autowired UploadFileAuthHelper uploadFileAuthHelper
        ) throws Exception {
            // Given
            Struct given = trxHelper.doInTransaction(() -> {
                AccountProfileImg accountProfileImg = entityHelper.generateAccountProfileImg();
                String token = uploadFileAuthHelper.generateToken(accountProfileImg);

                return new Struct()
                    .withValue("token", token)
                    .withValue("ownerId", accountProfileImg.getOwner().getId())
                    .withValue("accountProfileImgId", accountProfileImg.getId());
            });
            String token = given.valueOf("token");
            Long ownerId = given.valueOf("ownerId");
            Long accountProfileImgId = given.valueOf("accountProfileImgId");

            // When
            ResultActions actions = mockMvc.perform(delete("/accounts/{id}", ownerId)
                .header("Authorization", "Bearer " + token));

            // Then
            actions
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

            assertThat(repository.existsById(ownerId)).isFalse();
            assertThat(accountProfileImgRepository.existsById(accountProfileImgId)).isFalse();
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
