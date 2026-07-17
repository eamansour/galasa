/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.ServiceTokenRequest;
import dev.galasa.framework.api.beans.generated.ServiceTokenRequestSubjectTokenType;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ServiceTokenRequestValidatorTest {

    private static final GalasaGson gson = new GalasaGson();

    private final ServiceTokenRequestValidator validator = new ServiceTokenRequestValidator();

    private ServiceTokenRequest buildRequest(String connectorId, String subjectToken, String subjectTokenType) {
        JsonObject json = new JsonObject();
        if (connectorId != null) json.addProperty("connector_id", connectorId);
        if (subjectToken != null) json.addProperty("subject_token", subjectToken);
        if (subjectTokenType != null) json.addProperty("subject_token_type", subjectTokenType);
        return gson.fromJson(json.toString(), ServiceTokenRequest.class);
    }

    // -------------------------------------------------------------------------
    // Valid requests
    // -------------------------------------------------------------------------

    @Test
    public void testValidRequestWithIdTokenPassesValidation() throws Exception {
        ServiceTokenRequest request = buildRequest("github-actions", "eyJhbGci...", "id_token");
        validator.validate(request);
    }

    @Test
    public void testValidRequestWithAccessTokenPassesValidation() throws Exception {
        ServiceTokenRequest request = buildRequest("my-connector", "some-token", "access_token");
        validator.validate(request);
    }

    @Test
    public void testValidRequestWithJwtTokenTypePassesValidation() throws Exception {
        ServiceTokenRequest request = buildRequest("my-connector", "some-token", "jwt");
        validator.validate(request);
    }

    // -------------------------------------------------------------------------
    // toUrn mapping
    // -------------------------------------------------------------------------

    @Test
    public void testToUrnMapsIdTokenToFullUrn() {
        assertThat(ServiceTokenRequestValidator.toUrn(ServiceTokenRequestSubjectTokenType.ID_TOKEN))
            .isEqualTo("urn:ietf:params:oauth:token-type:id_token");
    }

    @Test
    public void testToUrnMapsAccessTokenToFullUrn() {
        assertThat(ServiceTokenRequestValidator.toUrn(ServiceTokenRequestSubjectTokenType.ACCESS_TOKEN))
            .isEqualTo("urn:ietf:params:oauth:token-type:access_token");
    }

    @Test
    public void testToUrnMapsJwtToFullUrn() {
        assertThat(ServiceTokenRequestValidator.toUrn(ServiceTokenRequestSubjectTokenType.jwt))
            .isEqualTo("urn:ietf:params:oauth:token-type:jwt");
    }

    // -------------------------------------------------------------------------
    // Invalid requests
    // -------------------------------------------------------------------------

    @Test
    public void testNullRequestThrowsBadRequest() {
        assertThatThrownBy(() -> validator.validate(null))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testMissingConnectorIdThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest(null, "eyJhbGci...", "id_token");
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testEmptyConnectorIdThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest("  ", "eyJhbGci...", "id_token");
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testMissingSubjectTokenThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest("github-actions", null, "id_token");
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testEmptySubjectTokenThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest("github-actions", "", "id_token");
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testMissingSubjectTokenTypeThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest("github-actions", "eyJhbGci...", null);
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }

    @Test
    public void testUnrecognisedSubjectTokenTypeThrowsBadRequest() {
        ServiceTokenRequest request = buildRequest("github-actions", "eyJhbGci...", "urn:ietf:params:oauth:token-type:id_token");
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(InternalServletException.class)
            .hasMessageContaining("GAL5463");
    }
}
