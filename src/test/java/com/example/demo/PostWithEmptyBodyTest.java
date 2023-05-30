package com.example.demo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import feign.Feign;
import feign.RequestLine;
import feign.codec.StringDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PostWithEmptyBodyTest {

    WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        this.wireMockServer = new WireMockServer(
                options()
                        .extensions(new ResponseTemplateTransformer(false))
                        .port(8081));
        this.wireMockServer.stubFor(post(urlEqualTo("/post"))
                .willReturn(aResponse()
                        .withBody("{{request.headers.Content-Type}}")
                        .withTransformers("response-template")));
        this.wireMockServer.addMockServiceRequestListener(PostWithEmptyBodyTest::logRequestReceived);
        this.wireMockServer.start();
    }

    @Test
    void postWithEmptyBodyTest() {
        var feignClient = Feign.builder()
                .decoder(new StringDecoder())
                .target(TestFeignClient.class, wireMockServer.baseUrl());

        var response = feignClient.testPost();

        // Feign < 12.0 sends a POST with no Content-Type header
        assertThat(response).isEmpty();

        // Feign >= 12.0 sends a POST with Content-Type: application/x-www-form-urlencoded
//        assertThat(response).isEqualTo("application/x-www-form-urlencoded");
    }

    static void logRequestReceived(Request request, Response response) {
        System.out.println("WireMock received request to " + request.getAbsoluteUrl() + " with the following headers: \n\n"
                + request.getHeaders().all().stream().map(MultiValue::toString).collect(Collectors.joining("\n"))
                + "\n\n");
    }

    public interface TestFeignClient {
        @RequestLine("POST /post")
        String testPost();
    }

}
