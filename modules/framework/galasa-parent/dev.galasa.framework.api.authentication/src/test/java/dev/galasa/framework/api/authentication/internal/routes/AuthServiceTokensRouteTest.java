/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockOidcProvider;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.spi.utils.GalasaGson;

public class AuthServiceTokensRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    // A minimal signed JWT with a "sub" claim for identity extraction
    private static final String TEST_JWT = JWT.create()
        .withSubject("user123")
        .withClaim("preferred_username", "testuser")
        .sign(Algorithm.none());

    private String buildRequestPayload(String connectorId, String subjectToken, String subjectTokenType) {
        JsonObject payload = new JsonObject();
        if (connectorId != null) payload.addProperty("connector_id", connectorId);
        if (subjectToken != null) payload.addProperty("subject_token", subjectToken);
        if (subjectTokenType != null) payload.addProperty("subject_token_type", subjectTokenType);
        return gson.toJson(payload);
    }

    private String buildDexTokenExchangeResponse(String accessToken) {
        JsonObject response = new JsonObject();
        if (accessToken != null) response.addProperty("access_token", accessToken);
        return gson.toJson(response);
    }

    @Test
    public void testPostServiceTokensReturns201WithJwtOnSuccess() throws Exception {
        // Given...
        String clientId = "new-client-id";
        String clientSecret = "new-client-secret";

        MockDexGrpcClient dexGrpcClient = new MockDexGrpcClient("https://my-issuer/dex", clientId, clientSecret, "http://callback");
        String dexResponse = buildDexTokenExchangeResponse(TEST_JWT);
        MockOidcProvider oidcProvider = new MockOidcProvider(new MockHttpResponse<String>(dexResponse, 200));

        MockAuthStoreService authStoreService = new MockAuthStoreService(new ArrayList<>());
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(oidcProvider, dexGrpcClient,
                new MockFramework(authStoreService));

        String requestBody = buildRequestPayload("github-actions", "upstream-token", "id_token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        JsonObject output = gson.fromJson(outStream.toString(), JsonObject.class);
        assertThat(output.has("jwt")).isTrue();
        assertThat(output.get("jwt").getAsString()).isEqualTo(TEST_JWT);
        // Dex client should be deleted immediately after the exchange
        assertThat(dexGrpcClient.getDexClients()).isEmpty();
    }

    @Test
    public void testPostServiceTokensMissingConnectorIdReturns400() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();
        String requestBody = buildRequestPayload(null, "upstream-token", "id_token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void testPostServiceTokensMissingSubjectTokenReturns400() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();
        String requestBody = buildRequestPayload("github-actions", null, "id_token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void testPostServiceTokensMissingSubjectTokenTypeReturns400() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();
        String requestBody = buildRequestPayload("github-actions", "upstream-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void testPostServiceTokensInvalidSubjectTokenTypeReturns400() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();
        String requestBody = buildRequestPayload("github-actions", "upstream-token", "not-a-valid-urn");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void testPostServiceTokensDexRejectionReturns401AndCleansUpClient() throws Exception {
        // Given...
        String clientId = "new-client-id";
        String clientSecret = "new-client-secret";

        MockDexGrpcClient dexGrpcClient = new MockDexGrpcClient("https://my-issuer/dex", clientId, clientSecret, "http://callback");
        // Dex returns 401
        MockOidcProvider oidcProvider = new MockOidcProvider(new MockHttpResponse<String>("{}", 401));

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(oidcProvider, dexGrpcClient);

        String requestBody = buildRequestPayload("github-actions", "expired-token", "id_token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(401);
        // The client created for the exchange should have been deleted
        assertThat(dexGrpcClient.getDexClients()).isEmpty();
    }

    @Test
    public void testPostServiceTokensMissingAccessTokenReturns500AndCleansUpClient() throws Exception {
        // Given...
        String clientId = "new-client-id";
        String clientSecret = "new-client-secret";

        MockDexGrpcClient dexGrpcClient = new MockDexGrpcClient("https://my-issuer/dex", clientId, clientSecret, "http://callback");
        // Dex returns 200 but no access_token
        String dexResponse = buildDexTokenExchangeResponse(null);
        MockOidcProvider oidcProvider = new MockOidcProvider(new MockHttpResponse<String>(dexResponse, 200));

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(oidcProvider, dexGrpcClient);

        String requestBody = buildRequestPayload("github-actions", "upstream-token", "id_token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/service-tokens", requestBody, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        // The client created for the exchange should have been deleted
        assertThat(dexGrpcClient.getDexClients()).isEmpty();
    }
}
