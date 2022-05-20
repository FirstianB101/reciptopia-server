package kr.reciptopia.reciptopiaserver.docs;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ApiDocumentation {

    public static final ParameterDescriptor DOC_PARAMETER_PAGE =
        parameterWithName("page").description("페이지 인덱스 (0부터 시작)").optional();
    public static final ParameterDescriptor DOC_PARAMETER_SIZE =
        parameterWithName("size").description("페이지 단위 (한 페이지에 보여줄 리소스 수)").optional();
    public static final ParameterDescriptor DOC_PARAMETER_SORT =
        parameterWithName("sort").description("정렬 기준 (`property,{ASC|DESC}` 형식)").optional();


    private MockMvc mockMvc;

    public static MockMvcRestDocumentationConfigurer basicDocumentationConfiguration(
        RestDocumentationContextProvider restDocumentation) {
        var baseUriOperationPreprocessor = new BaseUriOperationPreprocessor()
            .scheme("https")
            .host("reciptopia.firstian.kr")
            .removePort()
            .basePath("/api/alpha");
        return documentationConfiguration(restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(baseUriOperationPreprocessor)
            .and();
    }

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    void errorExample() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/accounts")
            .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Account id was not found"));

        // Then
        actions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("error").value("Not Found"))
            .andExpect(jsonPath("timestamp").value(notNullValue()))
            .andExpect(jsonPath("status").value(404))
            .andExpect(jsonPath("path").value(notNullValue()));

        // Document
        actions
            .andDo(document("error-example",
                responseFields(
                    fieldWithPath("error").description("발생한 HTTP 에러 (e.g. `Not Found`)"),
                    fieldWithPath("message").description("에러의 원인 설명"),
                    fieldWithPath("path").description("요청을 보낸 경로"),
                    fieldWithPath("status").description("HTTP 상태 코드 (e.g. `404`)"),
                    fieldWithPath("timestamp").description("에러가 발생한 시각 (단위: 밀리세컨드)")
                )));
    }

}
