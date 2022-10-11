package kr.reciptopia.reciptopiaserver.helper.integrationtest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class MockHttpServletRequestBuilderHelper {

    public static MockHttpServletRequestBuilder post(String url, String body) {
        return MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    }
}
