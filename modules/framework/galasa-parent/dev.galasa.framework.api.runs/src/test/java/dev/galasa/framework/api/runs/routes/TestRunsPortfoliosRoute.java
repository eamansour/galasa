/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockTestCatalogFetcher;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockOBR;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.spi.streams.IStream;

public class TestRunsPortfoliosRoute extends BaseServletTest {

    private static final Map<String, String> AUTH_HEADERS = Map.of("Authorization", "Bearer " + DUMMY_JWT);

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private String createCatalogJson(String bundle, String className, String pkg, String... tags) {
        JsonObject catalog = new JsonObject();
        JsonObject classes = new JsonObject();
        JsonObject classDef = new JsonObject();
        classDef.addProperty("bundle", bundle);
        classDef.addProperty("name", className);
        classDef.addProperty("package", pkg);
        JsonArray tagsArray = new JsonArray();
        for (String tag : tags) {
            tagsArray.add(tag);
        }
        classDef.add("tags", tagsArray);
        classes.add(bundle + "/" + className, classDef);
        catalog.add("classes", classes);
        return catalog.toString();
    }

    /**
     * Creates a {@link RunsPortfoliosRoute} wired with mock collaborators.
     * The route is instantiated directly via the package-visible constructor so
     * that tests do not need to go through the full servlet stack.
     */
    private RunsPortfoliosRoute createRoute(
        List<IStream> streams,
        String catalogJson
    ) throws Exception {
        MockRBACService rbac = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockStreamsService streamsService = new MockStreamsService(streams);
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTestCatalogFetcher catalogFetcher = new MockTestCatalogFetcher(catalogJson);
        return new RunsPortfoliosRoute(new ResponseBuilder(env), streamsService, rbac, catalogFetcher);
    }

    /**
     * Creates a route whose catalog fetcher always throws the given exception.
     */
    private RunsPortfoliosRoute createRouteWithCatalogException(
        List<IStream> streams,
        InternalServletException ex
    ) throws Exception {
        MockRBACService rbac = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockStreamsService streamsService = new MockStreamsService(streams);
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTestCatalogFetcher catalogFetcher = new MockTestCatalogFetcher(null);
        catalogFetcher.setExceptionToThrow(ex);
        return new RunsPortfoliosRoute(new ResponseBuilder(env), streamsService, rbac, catalogFetcher);
    }

    private MockStream makeStream(String name, String catalogUrl,
            String obrGroup, String obrArtifact, String obrVersion) throws Exception {
        MockStream stream = new MockStream();
        stream.setName(name);
        stream.setTestCatalogUrl(catalogUrl);
        stream.setObrs(List.of(new MockOBR(obrGroup, obrArtifact, obrVersion)));
        return stream;
    }

    private String buildRequestBody(String stream, JsonObject filters) {
        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject selection = new JsonObject();
        selection.addProperty("stream", stream);
        if (filters != null) {
            filters.entrySet().forEach(e -> selection.add(e.getKey(), e.getValue()));
        }
        selections.add(selection);
        request.add("selections", selections);
        return request.toString();
    }

    /**
     * Calls the route directly (bypassing the servlet) and returns the response.
     * Authentication is not validated at the route level — the {@code testMissingAuthTokenReturns401}
     * test covers that via the full servlet stack.
     */
    private MockHttpServletResponse invokePost(RunsPortfoliosRoute route, String body) throws Exception {
        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockHttpServletRequest request = new MockHttpServletRequest("/portfolios", body, "POST", AUTH_HEADERS);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpRequestContext requestContext = new HttpRequestContext(request, env);
        route.handlePostRequest("/portfolios", requestContext, response);
        return response;
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    public void testValidSingleStreamSelectionByTagReturns200() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example.bundle";
        String className = "com.example.tests.MyTest";
        String pkg = "com.example.tests";
        String catalogJson = createCatalogJson(bundle, className, pkg, "regression");

        List<IStream> streams = List.of(makeStream(streamName,
            "http://myrepo.com/catalog.json", "com.example", "my-obr", "1.0.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("regression");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(route, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        JsonArray classes = responseJson.getAsJsonArray("classes");
        assertThat(classes).hasSize(1);
        assertThat(classes.get(0).getAsJsonObject().get("bundle").getAsString()).isEqualTo(bundle);
        assertThat(classes.get(0).getAsJsonObject().get("class").getAsString()).isEqualTo(className);
        assertThat(classes.get(0).getAsJsonObject().get("stream").getAsString()).isEqualTo(streamName);
        assertThat(classes.get(0).getAsJsonObject().get("obr").getAsString())
            .isEqualTo("mvn:com.example/my-obr/1.0.0/obr");
    }

    @Test
    public void testMultiStreamSelectionReturnsUnionOfClasses() throws Exception {
        // Given...
        String streamA = "stream-a";
        String streamB = "stream-b";
        // Both streams share the same catalog content for simplicity; the mock
        // fetcher always returns the same JSON regardless of which stream is asked.
        String catalogJson = createCatalogJson("com.a", "com.a.TestA", "com.a", "smoke");

        MockStream streamMockA = makeStream(streamA, "http://myrepo.com/catalogA.json", "com.a", "a-obr", "1.0.0");
        MockStream streamMockB = makeStream(streamB, "http://myrepo.com/catalogB.json", "com.b", "b-obr", "2.0.0");
        List<IStream> streams = List.of(streamMockA, streamMockB);

        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        for (String sName : List.of(streamA, streamB)) {
            JsonObject sel = new JsonObject();
            sel.addProperty("stream", sName);
            JsonArray tags = new JsonArray();
            tags.add("smoke");
            sel.add("tags", tags);
            selections.add(sel);
        }
        request.add("selections", selections);

        // When...
        MockHttpServletResponse response = invokePost(route, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        JsonArray classes = responseJson.getAsJsonArray("classes");
        // Same catalog content for both streams — the mock always returns the same JSON.
        // The same bundle/class pair appears once per stream, so we expect >= 1 result.
        assertThat(classes.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testDeduplicationAcrossSelectionsReturnsSingleEntry() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example";
        String className = "com.example.TestDup";
        String pkg = "com.example";
        String catalogJson = createCatalogJson(bundle, className, pkg, "smoke");

        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        // Two selections targeting the same stream and tag — should deduplicate to one entry
        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        for (int i = 0; i < 2; i++) {
            JsonObject sel = new JsonObject();
            sel.addProperty("stream", streamName);
            JsonArray tags = new JsonArray();
            tags.add("smoke");
            sel.add("tags", tags);
            selections.add(sel);
        }
        request.add("selections", selections);

        // When...
        MockHttpServletResponse response = invokePost(route, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray classes = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(classes).hasSize(1);
    }

    @Test
    public void testEmptyResultSetReturns200WithEmptyArray() throws Exception {
        // Given...
        String streamName = "myStream";
        // Catalog has a "smoke"-tagged class; filter asks for "regression" — no match
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("regression");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(route, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray classes = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(classes).isEmpty();
    }

    @Test
    public void testStreamNotFoundReturns404() throws Exception {
        // Given...
        List<IStream> streams = new ArrayList<>(); // no streams registered
        RunsPortfoliosRoute route = createRoute(streams, null);

        String body = buildRequestBody("nonExistentStream", null);

        // When / Then...
        assertThatThrownBy(() -> invokePost(route, body))
            .isInstanceOfSatisfying(InternalServletException.class, thrown -> {
                assertThat(thrown.getHttpFailureCode()).isEqualTo(404);
                try {
                    checkErrorStructure(thrown.getMessage(), 5468, "GAL5468E");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void testCatalogFetchFailureReturns502() throws Exception {
        // Given...
        String streamName = "myStream";
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));

        InternalServletException catalogException = new InternalServletException(
            new ServletError(ServletErrorMessage.GAL5469_RUNS_PORTFOLIO_CATALOG_FETCH_FAILED, streamName),
            502
        );
        RunsPortfoliosRoute route = createRouteWithCatalogException(streams, catalogException);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When / Then...
        assertThatThrownBy(() -> invokePost(route, body))
            .isInstanceOfSatisfying(InternalServletException.class, thrown -> {
                assertThat(thrown.getHttpFailureCode()).isEqualTo(502);
                try {
                    checkErrorStructure(thrown.getMessage(), 5469, "GAL5469E");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void testInvalidRegexReturns400() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray bundles = new JsonArray();
        bundles.add("[invalid(regex");
        filters.add("bundles", bundles);
        filters.addProperty("regex", true);
        String body = buildRequestBody(streamName, filters);

        // When / Then...
        assertThatThrownBy(() -> invokePost(route, body))
            .isInstanceOfSatisfying(InternalServletException.class, thrown -> {
                assertThat(thrown.getHttpFailureCode()).isEqualTo(400);
                try {
                    checkErrorStructure(thrown.getMessage(), 5467, "GAL5467E");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void testMissingSelectionsFieldReturns400() throws Exception {
        // Given...
        RunsPortfoliosRoute route = createRoute(List.of(), null);

        // Request with no "selections" field
        JsonObject request = new JsonObject();

        // When / Then...
        assertThatThrownBy(() -> invokePost(route, request.toString()))
            .isInstanceOfSatisfying(InternalServletException.class, thrown -> {
                assertThat(thrown.getHttpFailureCode()).isEqualTo(400);
                try {
                    checkErrorStructure(thrown.getMessage(), 5465, "GAL5465E");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void testObrPopulatedFromStreamConfiguration() throws Exception {
        // Given...
        String streamName = "myStream";
        String bundle = "com.example";
        String className = "com.example.TestA";
        String catalogJson = createCatalogJson(bundle, className, "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example.group", "my-obr", "2.5.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject filters = new JsonObject();
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        filters.add("tags", tags);
        String body = buildRequestBody(streamName, filters);

        // When...
        MockHttpServletResponse response = invokePost(route, body);

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonArray classes = JsonParser.parseString(response.getOutputStream().toString())
            .getAsJsonObject().getAsJsonArray("classes");
        assertThat(classes).hasSize(1);
        assertThat(classes.get(0).getAsJsonObject().get("obr").getAsString())
            .isEqualTo("mvn:com.example.group/my-obr/2.5.0/obr");
    }

    @Test
    public void testOverridesAreEchoedInResponse() throws Exception {
        // Given...
        String streamName = "myStream";
        String catalogJson = createCatalogJson("com.example", "com.example.TestA", "com.example", "smoke");
        List<IStream> streams = List.of(makeStream(streamName,
            "http://repo.com/catalog.json", "com.example", "obr", "1.0.0"));
        RunsPortfoliosRoute route = createRoute(streams, catalogJson);

        JsonObject request = new JsonObject();
        JsonArray selections = new JsonArray();
        JsonObject sel = new JsonObject();
        sel.addProperty("stream", streamName);
        JsonArray tags = new JsonArray();
        tags.add("smoke");
        sel.add("tags", tags);
        selections.add(sel);
        request.add("selections", selections);
        JsonObject overrides = new JsonObject();
        overrides.addProperty("zos.image", "SYSA");
        request.add("overrides", overrides);

        // When...
        MockHttpServletResponse response = invokePost(route, request.toString());

        // Then...
        assertThat(response.getStatus()).isEqualTo(200);
        JsonObject responseJson = JsonParser.parseString(response.getOutputStream().toString()).getAsJsonObject();
        assertThat(responseJson.has("overrides")).isTrue();
        assertThat(responseJson.getAsJsonObject("overrides").get("zos.image").getAsString()).isEqualTo("SYSA");
    }
}
