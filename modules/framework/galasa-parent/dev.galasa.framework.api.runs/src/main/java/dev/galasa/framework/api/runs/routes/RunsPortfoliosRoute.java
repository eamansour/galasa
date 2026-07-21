/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.beans.generated.RunsPortfolioClass;
import dev.galasa.framework.api.beans.generated.RunsPortfolioRequest;
import dev.galasa.framework.api.beans.generated.RunsPortfolioSelection;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.TestCatalogFetcher;
import dev.galasa.framework.api.runs.validators.RunsPortfolioRequestValidator;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

/**
 * Handles POST /runs/portfolios.
 *
 * Accepts test selection criteria for one or more named streams and returns the
 * resolved set of matching test classes. The response is stateless — nothing is
 * persisted on the server.
 */
public class RunsPortfoliosRoute extends ProtectedRoute {

    // Regex to match exactly /portfolios or /portfolios/
    protected static final String path = "\\/portfolios\\/?";

    private final IStreamsService streamsService;
    private final ITestCatalogFetcher catalogFetcher;
    private final RunsPortfolioRequestValidator validator;

    public RunsPortfoliosRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        ICredentialsService credentialsService,
        RBACService rbacService,
        HttpClient httpClient
    ) throws StreamsException {
        this(responseBuilder, streamsService, rbacService,
            new TestCatalogFetcher(httpClient, credentialsService));
    }

    RunsPortfoliosRoute(
        ResponseBuilder responseBuilder,
        IStreamsService streamsService,
        RBACService rbacService,
        ITestCatalogFetcher catalogFetcher
    ) throws StreamsException {
        super(responseBuilder, path, rbacService);
        this.streamsService = streamsService;
        this.catalogFetcher = catalogFetcher;
        this.validator = new RunsPortfolioRequestValidator();
    }

    @Override
    public HttpServletResponse handlePostRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws ServletException, IOException, FrameworkException {

        HttpServletRequest request = requestContext.getRequest();
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        RunsPortfolioRequest portfolioRequest = gson.fromJson(body, RunsPortfolioRequest.class);
        validator.validate(portfolioRequest);

        Map<String, String> overrides = parseOverrides(body);

        // Create deduplication keys in the form "stream/bundle/class" to avoid adding the same test again
        Set<String> seenClasses = new HashSet<>();
        List<RunsPortfolioClass> resolvedClasses = new ArrayList<>();

        for (RunsPortfolioSelection selection : portfolioRequest.getselections()) {
            String streamName = selection.getstream();

            IStream stream = getStreamByName(streamName);
            String obr = resolveObr(stream);
            boolean isRegexEnabled = selection.getregex();

            List<Pattern> bundlePatterns = buildPatterns(selection.getbundles(), isRegexEnabled);
            List<Pattern> packagePatterns = buildPatterns(selection.getpackages(), isRegexEnabled);
            List<Pattern> testPatterns = buildPatterns(selection.gettests(), isRegexEnabled);
            List<Pattern> tagPatterns = buildPatterns(selection.gettags(), isRegexEnabled);

            // Explicit classes — bypass catalog
            if (selection.getclasses() != null) {
                for (String classSelector : selection.getclasses()) {
                    int slash = classSelector.indexOf('/');
                    if (slash < 1 || slash == classSelector.length() - 1) {
                        ServletError error = new ServletError(GAL5466_RUNS_PORTFOLIO_INVALID_CLASS_FORMAT, classSelector);
                        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
                    }
                    addResolvedClass(resolvedClasses, seenClasses,
                        classSelector.substring(0, slash), classSelector.substring(slash + 1), streamName, obr);
                }
            }

            boolean hasTestCatalogFilter = !bundlePatterns.isEmpty() || !packagePatterns.isEmpty()
                || !testPatterns.isEmpty() || !tagPatterns.isEmpty();

            if (hasTestCatalogFilter) {
                String testCatalogJson = catalogFetcher.fetchTestCatalog(stream);

                if (testCatalogJson != null) {

                    JsonObject testCatalog = JsonParser.parseString(testCatalogJson).getAsJsonObject();
                    if (testCatalog.has("classes")) {
                        for (Map.Entry<String, JsonElement> entry : testCatalog.getAsJsonObject("classes").entrySet()) {
                            JsonObject classDef = entry.getValue().getAsJsonObject();
                            String bundle = classDef.has("bundle") ? classDef.get("bundle").getAsString() : "";
                            String className = classDef.has("name") ? classDef.get("name").getAsString() : "";
                            String pkg = classDef.has("package") ? classDef.get("package").getAsString() : "";

                            if (!bundlePatterns.isEmpty() && matchesAnyPattern(bundle, bundlePatterns)
                                || !packagePatterns.isEmpty() && matchesAnyPattern(pkg, packagePatterns)
                                || !testPatterns.isEmpty() && matchesAnyPattern(className, testPatterns)
                                || !tagPatterns.isEmpty() && matchesAnyTag(classDef, tagPatterns)
                            ) {
                                addResolvedClass(resolvedClasses, seenClasses, bundle, className, streamName, obr);
                            }
                        }
                    }
                }
            }
        }

        String responseBody = buildResponseJson(resolvedClasses, overrides);
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(),
            responseBody, HttpServletResponse.SC_OK);
    }

    private Map<String, String> parseOverrides(String body) {
        Map<String, String> overrides = new HashMap<>();
        JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();

        if (requestJson.has("overrides") && !requestJson.get("overrides").isJsonNull()) {

            JsonObject overridesJson = requestJson.getAsJsonObject("overrides");
            for (Map.Entry<String, JsonElement> entry : overridesJson.entrySet()) {
                overrides.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return overrides;
    }

    private String buildResponseJson(List<RunsPortfolioClass> resolvedClasses, Map<String, String> overrides) {
        JsonArray classesArray = gson.toJsonTree(resolvedClasses.toArray(new RunsPortfolioClass[0])).getAsJsonArray();
        JsonObject responseJson = new JsonObject();
        responseJson.add("classes", classesArray);

        if (!overrides.isEmpty()) {
            JsonObject overridesJson = new JsonObject();
            for (Map.Entry<String, String> entry : overrides.entrySet()) {
                overridesJson.addProperty(entry.getKey(), entry.getValue());
            }
            responseJson.add("overrides", overridesJson);
        }

        return gson.toJson(responseJson);
    }

    private IStream getStreamByName(String streamName) throws InternalServletException, FrameworkException {
        try {
            IStream stream = streamsService.getStreamByName(streamName);
            if (stream == null) {
                ServletError error = new ServletError(GAL5468_RUNS_PORTFOLIO_STREAM_NOT_FOUND, streamName);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }
            return stream;
        } catch (StreamsException e) {
            ServletError error = new ServletError(GAL5468_RUNS_PORTFOLIO_STREAM_NOT_FOUND, streamName);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

    private String resolveObr(IStream stream) {
        List<IOBR> obrs = stream.getObrs();
        if (obrs == null || obrs.isEmpty()) {
            return null;
        }
        IOBR obr = obrs.get(0);
        return obr.toString();
    }

    private List<Pattern> buildPatterns(String[] values, boolean isRegexEnabled) throws InternalServletException {
        List<Pattern> patterns = new ArrayList<>();

        if (values != null) {
            for (String value : values) {
                try {
                    patterns.add(isRegexEnabled ? Pattern.compile(value) : Pattern.compile(Pattern.quote(value)));
                } catch (PatternSyntaxException e) {
                    ServletError error = new ServletError(GAL5467_RUNS_PORTFOLIO_INVALID_REGEX, value);
                    throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST, e);
                }
            }
        }
        return patterns;
    }

    private boolean matchesAnyPattern(String value, List<Pattern> patterns) {
        boolean isMatchFound = false;
        for (Pattern p : patterns) {
            if (p.matcher(value).find()) {
                isMatchFound = true;
                break;
            }
        }
        return isMatchFound;
    }

    private boolean matchesAnyTag(JsonObject classDef, List<Pattern> tagPatterns) {
        if (classDef.has("tags") && classDef.get("tags").isJsonNull()) {
            return false;
        }
        
        boolean isMatchFound = false;
        for (JsonElement tagElem : classDef.getAsJsonArray("tags")) {
            if (matchesAnyPattern(tagElem.getAsString(), tagPatterns)) {
                isMatchFound = true;
                break;
            }
        }
        return isMatchFound;
    }

    private void addResolvedClass(
        List<RunsPortfolioClass> resolvedClasses,
        Set<String> seenClasses,
        String bundle,
        String className,
        String stream,
        String obr
    ) {
        String key = stream + "/" + bundle + "/" + className;
        if (!seenClasses.contains(key)) {
            seenClasses.add(key);
            RunsPortfolioClass classObj = new RunsPortfolioClass();
            classObj.setbundle(bundle);
            classObj.setClassValue(className);
            classObj.setstream(stream);
            classObj.setobr(obr);
            resolvedClasses.add(classObj);
        }
    }
}
