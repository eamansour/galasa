/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coreos.dex.api.DexOuterClass.Client;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.ServiceTokenRequestValidator;
import dev.galasa.framework.api.beans.generated.ServiceTokenRequest;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.JwtWrapper;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;

public class AuthServiceTokensRoute extends AbstractAuthRoute {

    private static final String PATH_PATTERN = "\\/service-tokens\\/?";

    private static final String RESPONSE_FIELD_ACCESS_TOKEN = "access_token";

    private static final IBeanValidator<ServiceTokenRequest> validator = new ServiceTokenRequestValidator();

    private final IOidcProvider oidcProvider;
    private final IDexGrpcClient dexGrpcClient;
    private final IAuthStoreService authStoreService;
    private final RBACService rbacService;
    private final ITimeService timeService;
    private final Environment env;

    private Log logger = LogFactory.getLog(this.getClass());

    public AuthServiceTokensRoute(
            ResponseBuilder responseBuilder,
            IOidcProvider oidcProvider,
            IAuthService authService,
            RBACService rbacService,
            ITimeService timeService,
            Environment env) {
        super(responseBuilder, PATH_PATTERN);
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = authService.getDexGrpcClient();
        this.authStoreService = authService.getAuthStoreService();
        this.rbacService = rbacService;
        this.timeService = timeService;
        this.env = env;
    }

    /**
     * POST /auth/service-tokens creates a new Galasa service token by performing an
     * OAuth2 Token Exchange with Dex using the caller's upstream OIDC token.
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("AuthServiceTokensRoute: handlePostRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        ServiceTokenRequest requestPayload = parseRequestBody(request, ServiceTokenRequest.class);
        validator.validate(requestPayload);

        // Create a new Dex client to use for this service token
        Client dexClient = dexGrpcClient.createClient(AuthCallbackRoute.getExternalAuthCallbackUrl());
        if (dexClient == null) {
            ServletError error = new ServletError(GAL5464_FAILED_TO_CREATE_DEX_CLIENT_FOR_SERVICE_TOKEN);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String newClientId = dexClient.getId();
        String newClientSecret = dexClient.getSecret();

        try {
            String idToken = performTokenExchange(requestPayload, newClientId, newClientSecret);

            // Validate the JWT before trusting it
            try {
                if (!oidcProvider.isJwtValid(idToken)) {
                    ServletError error = new ServletError(GAL5466_INVALID_JWT_FROM_TOKEN_EXCHANGE);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | InterruptedException e) {
                ServletError error = new ServletError(GAL5466_INVALID_JWT_FROM_TOKEN_EXCHANGE);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }

            // Create or update the user record
            JwtWrapper jwtWrapper = new JwtWrapper(idToken, env);
            recordUserLogin(jwtWrapper.getUsername());

            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("jwt", idToken);

            return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(),
                    gson.toJson(responseBody), HttpServletResponse.SC_CREATED);
        } finally {
            cleanUpDexClient(newClientId);
        }
    }

    private String performTokenExchange(ServiceTokenRequest requestPayload, String newClientId, String newClientSecret) throws IOException, InternalServletException {
        JsonObject tokenResponseJson = null;

        try {
            HttpResponse<String> tokenResponse = oidcProvider.sendTokenExchangePost(
                newClientId,
                newClientSecret,
                requestPayload.getSubjectToken(),
                ServiceTokenRequestValidator.toUrn(requestPayload.getSubjectTokenType()),
                requestPayload.getConnectorId()
            );

            if (tokenResponse == null || tokenResponse.statusCode() != HttpServletResponse.SC_OK) {
                ServletError error = new ServletError(GAL5467_FAILED_TO_EXCHANGE_TOKENS_WITH_ISSUER);
                throw new InternalServletException(error, HttpServletResponse.SC_UNAUTHORIZED);
            }

            tokenResponseJson = gson.fromJson(tokenResponse.body(), JsonObject.class);

        } catch (InterruptedException e) {
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        // Validate that Dex returned an ID token in the access_token field
        if (tokenResponseJson == null || !tokenResponseJson.has(RESPONSE_FIELD_ACCESS_TOKEN)) {
            ServletError error = new ServletError(GAL5465_TOKEN_EXCHANGE_MISSING_REQUESTED_TOKEN);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return tokenResponseJson.get(RESPONSE_FIELD_ACCESS_TOKEN).getAsString();
    }

    private void cleanUpDexClient(String clientId) {
        try {
            dexGrpcClient.deleteClient(clientId);
        } catch (InternalServletException e) {
            logger.warn("Failed to clean up Dex client '" + clientId + "' after service token creation failure", e);
        }
    }

    private void recordUserLogin(String loginId) {
        try {
            IUser user = authStoreService.getUserByLoginId(loginId);
            if (user == null) {
                String defaultRoleId = rbacService.getDefaultRoleId();
                authStoreService.createUser(loginId, REST_API_CLIENT, defaultRoleId);
            } else {
                IFrontEndClient client = user.getClient(REST_API_CLIENT);
                if (client == null) {
                    client = authStoreService.createClient(REST_API_CLIENT);
                    user.addClient(client);
                }
                client.setLastLogin(timeService.now());
                authStoreService.updateUser(user);
            }
        } catch (AuthStoreException | RBACException e) {
            logger.warn("Failed to update user record after service token creation", e);
        }
    }
}
