/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.ServiceTokenRequest;
import dev.galasa.framework.api.beans.generated.ServiceTokenRequestSubjectTokenType;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class ServiceTokenRequestValidator implements IBeanValidator<ServiceTokenRequest> {

    private static final Map<String, String> SUBJECT_TOKEN_TYPE_URN_MAP;
    static {
        SUBJECT_TOKEN_TYPE_URN_MAP = new HashMap<>();
        SUBJECT_TOKEN_TYPE_URN_MAP.put("id_token",     "urn:ietf:params:oauth:token-type:id_token");
        SUBJECT_TOKEN_TYPE_URN_MAP.put("access_token", "urn:ietf:params:oauth:token-type:access_token");
        SUBJECT_TOKEN_TYPE_URN_MAP.put("jwt",          "urn:ietf:params:oauth:token-type:jwt");
    }

    /**
     * Maps a simplified subject_token_type value (as accepted by the API) to the
     * full RFC 8693 URN, or null if it does not match any known type.
     */
    public static String toUrn(ServiceTokenRequestSubjectTokenType subjectTokenType) {
        return SUBJECT_TOKEN_TYPE_URN_MAP.getOrDefault(subjectTokenType.toString(), null);
    }

    @Override
    public void validate(ServiceTokenRequest request) throws InternalServletException {
        boolean isValid = false;
        if (request != null) {
            String connectorId = request.getConnectorId();
            String subjectToken = request.getSubjectToken();
            ServiceTokenRequestSubjectTokenType subjectTokenType = request.getSubjectTokenType();

            isValid = connectorId != null && !connectorId.trim().isEmpty()
                    && subjectToken != null && !subjectToken.trim().isEmpty()
                    && subjectTokenType != null;
        }

        if (!isValid) {
            ServletError error = new ServletError(GAL5463_INVALID_SERVICE_TOKEN_REQUEST_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
