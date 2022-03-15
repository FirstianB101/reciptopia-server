package kr.reciptopia.reciptopiaserver.docs;

import java.net.URI;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.preprocess.UriModifyingOperationPreprocessor;
import org.springframework.web.util.UriComponentsBuilder;

public class BaseUriOperationPreprocessor extends UriModifyingOperationPreprocessor {

    private String basePath;

    public BaseUriOperationPreprocessor basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    @Override
    public BaseUriOperationPreprocessor scheme(String scheme) {
        super.scheme(scheme);
        return this;
    }

    @Override
    public BaseUriOperationPreprocessor host(String host) {
        super.host(host);
        return this;
    }

    @Override
    public BaseUriOperationPreprocessor port(int port) {
        super.port(port);
        return this;
    }

    @Override
    public BaseUriOperationPreprocessor removePort() {
        super.removePort();
        return this;
    }

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        URI oldUri = request.getUri();
        URI newUri = UriComponentsBuilder.fromUri(oldUri)
            .replacePath(basePath + oldUri.getPath())
            .build(true)
            .toUri();

        return super.preprocess(new OperationRequestFactory().create(
            newUri,
            request.getMethod(),
            request.getContent(),
            request.getHeaders(),
            request.getParameters(),
            request.getParts(),
            request.getCookies()));
    }
}
