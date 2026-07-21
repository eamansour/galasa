/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import java.net.http.HttpClient;
import java.time.Duration;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.runs.routes.GroupRunsRoute;
import dev.galasa.framework.api.runs.routes.RunsPortfoliosRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.streams.IStreamsService;

/*
* Proxy servlet for /runs/* endpoints
*/
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/runs/*" }, name = "Galasa Schedule Runs microservice")
public class RunsServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    private static final long serialVersionUID = 1L;
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(60);

    private Log logger = LogFactory.getLog(this.getClass());

    private final HttpClient httpClientOverride;

    public RunsServlet() {
        this(new SystemEnvironment(), null);
    }

    public RunsServlet(Environment env) {
        this(env, null);
    }

    public RunsServlet(Environment env, HttpClient httpClient) {
        super(env);
        this.httpClientOverride = httpClient;
    }

    @Override
    public void init() throws ServletException {
        logger.info("Schedule Runs Servlet initialising");

        super.init();
        try {
            HttpClient httpClient = (httpClientOverride != null) ? httpClientOverride
                : HttpClient.newBuilder()
                    .connectTimeout(CONNECTION_TIMEOUT)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            IStreamsService streamsService = framework.getStreamsService();
            ICredentialsService credentialsService = framework.getCredentialsService();

            addRoute(new GroupRunsRoute(getResponseBuilder(), framework, env));
            addRoute(new RunsPortfoliosRoute(getResponseBuilder(), streamsService, credentialsService,
                framework.getRBACService(), httpClient));
        } catch (RBACException e) {
            throw new ServletException("Failed to initialise schedule runs servlet");
        } catch (Exception e) {
            throw new ServletException("Failed to initialise schedule runs servlet", e);
        }
        logger.info("Schedule Runs Servlet initialised");
    }
}
