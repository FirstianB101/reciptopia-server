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

    public static MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(url);
    }

    public static MockHttpServletRequestBuilder get(String url, Long id) {
        return MockMvcRequestBuilders.get(url, id);
    }

    public static MockHttpServletRequestBuilder patch(String url, Long id, String token,
        String body) {
        return patch(url, id, body)
            .header("Authorization", "Bearer " + token);
    }

    public static MockHttpServletRequestBuilder patch(String url, Long id, String body) {
        return MockMvcRequestBuilders.patch(url, id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    }
}
