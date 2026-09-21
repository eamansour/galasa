---
type: testing-suite
title: Installation Verification Tests (IVTs)
description: Comprehensive explanation of the IVT test suite that validates Manager functionality, runs daily in CI, and is organized into test streams for automated regression testing
tags: [testing, ivts, managers, test-streams, ci-cd, regression-testing]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-f329c89444de4e0393d972a2
    resource: repo://.github/workflows/ivts.yaml
  - id: openwiki-source-61f30c4a777828a3d93b04f6
    resource: repo://modules/ivts/build-locally.sh
  - id: openwiki-source-3ef79b90abed9433b0dc01bb
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts.obr/build.gradle
  - id: openwiki-source-c9a5a03c9b2bc2f0586e04d3
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts/bnd.bnd
  - id: openwiki-source-6164cc1993e7ed0fd0e3c6b7
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts/build.gradle
  - id: openwiki-source-c15c1325fe6238471a3d8906
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts/dev.galasa.ivts.core/src/main/java/dev/galasa/ivts/core/CoreManagerIVT.java
  - id: openwiki-source-ca18bc15d8fdb704b6ef0777
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts/dev.galasa.ivts.framework/src/main/java/dev/galasa/ivts/framework/TestCredentialsStoreAccess.java
  - id: openwiki-source-0fc4c6a3f6810497d60a2465
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.ivts/dev.galasa.ivts.http/src/main/java/dev/galasa/ivts/http/HttpManagerIVT.java
  - id: openwiki-source-1f212a315e2c72de015ec992
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.ceci/src/main/java/dev/galasa/zos/ivts/ceci/CECIManagerIVT.java
  - id: openwiki-source-77ef9227cd596a3fe7372ca0
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.zos/src/main/java/dev/galasa/zos/ivts/zos/ZosManagerIVT.java
  - id: openwiki-source-588b07e897a9a744de4de085
    resource: repo://modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.zos3270/src/main/java/dev/galasa/zos/ivts/zos3270/Zos3270IVT.java
  - id: openwiki-source-fa4589c2b7b27e8dab9331d8
    resource: repo://modules/ivts/galasa-ivts-parent/settings.gradle
  - id: openwiki-source-4677f6fb5194da8fd9898ef0
    resource: repo://modules/ivts/README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Installation Verification Tests (IVTs)

Installation Verification Tests (IVTs) are a specialized suite of Galasa tests designed to validate the functionality of Galasa Managers. Each IVT is itself a Galasa test that exercises a specific Manager's capabilities to ensure correct behavior and detect regressions. IVTs are built into a test stream and run automatically in CI/CD pipelines to continuously verify that Managers work as expected.

## Purpose and Scope

IVTs serve several critical testing purposes:

### Manager Functionality Validation
Each IVT exercises a specific Manager by:
- Provisioning resources through the Manager's annotations
- Invoking the Manager's API methods
- Verifying expected behavior and return values
- Testing error handling and edge cases

For example, the `CoreManagerIVT` verifies that the Core Manager correctly injects logger instances, run names, and manager references into test fields.

### Regression Detection
IVTs run daily in automation to catch regressions introduced by:
- Framework changes that affect Manager behavior
- Manager implementation changes
- Dependency updates
- Infrastructure changes

### Installation Verification
IVTs validate that a Galasa installation is properly configured by ensuring:
- Managers can initialize correctly
- Resource provisioning works
- Required dependencies are available
- CPS properties are configured correctly

## IVT Module Structure

The IVTs are organized in the `/modules/ivts` directory with a clear bundle hierarchy that separates tests by resource requirements.

### Bundle Organization

IVTs are organized into two parent bundles based on their resource requirements:

#### dev.galasa.ivts
Contains IVTs that **do not require mainframe resources** to run. These tests can run in any environment with basic infrastructure:
- **CoreManagerIVT** - Tests Core Manager annotation injection (Logger, RunName, CoreManager)
- **ArtifactManagerIVT** - Tests artifact retrieval from bundles and Maven repositories
- **HttpManagerIVT** - Tests HTTP client functionality (GET, POST, PUT, DELETE, authentication, SSL)
- **DockerManagerIVT** - Tests Docker container provisioning, lifecycle, and volume management
- **JMeterManagerIVT** - Tests JMeter load testing integration
- **TestCredentialsStoreAccess** - Tests credentials retrieval from the credentials store

Each of these IVTs is organized as a subproject under the `dev.galasa.ivts` parent bundle, located in subdirectories like `dev.galasa.ivts.core`, `dev.galasa.ivts.http`, etc.

#### dev.galasa.zos.ivts
Contains IVTs that **require mainframe (z/OS) resources** to run. These tests validate Managers that interact with z/OS systems:
- **CECIManagerIVT** - Tests CICS CECI command execution
- **CedaManagerIVT** - Tests CICS CEDA resource definition management
- **CEMTManagerIVT** - Tests CICS CEMT transaction and resource control
- **CICSTSManagerIVT** - Tests CICS TS region provisioning and management
- **SdvManagerIVT** - Tests Service Development Verification functionality
- **ZosManagerIVT** - Tests z/OS image provisioning and credential management
- **ZosManagerBatchIVT** - Tests z/OS batch job submission and monitoring
- **ZosManagerFileDatasetIVT** - Tests z/OS dataset operations (create, read, write, delete)
- **ZosManagerFileIVT** - Tests z/OS file management operations
- **ZosManagerFileVSAMIVT** - Tests z/OS VSAM file operations
- **ZosManagerTSOCommandIVT** - Tests z/OS TSO command execution
- **Zos3270IVT** - Tests 3270 terminal emulation and interaction

These IVTs are organized as subprojects under the `dev.galasa.zos.ivts` parent bundle in subdirectories like `dev.galasa.zos.ivts.ceci`, `dev.galasa.zos.ivts.zos`, etc.

### Gradle Project Structure

The IVT module uses a multi-project Gradle build defined in `/modules/ivts/galasa-ivts-parent/settings.gradle`:

```gradle
include 'dev.galasa.ivts.obr'

include 'dev.galasa.ivts'
include 'dev.galasa.zos.ivts'

include 'dev.galasa.ivts:dev.galasa.ivts.artifact'
include 'dev.galasa.ivts:dev.galasa.ivts.core'
include 'dev.galasa.ivts:dev.galasa.ivts.http'
// ... more non-z/OS IVT subprojects

include 'dev.galasa.zos.ivts:dev.galasa.zos.ivts.ceci'
include 'dev.galasa.zos.ivts:dev.galasa.zos.ivts.zos'
// ... more z/OS IVT subprojects
```

The parent bundles (`dev.galasa.ivts` and `dev.galasa.zos.ivts`) aggregate their subprojects as dependencies, allowing all IVT classes to be exported from the parent bundle while keeping the test implementations in separate subproject bundles.

### OBR and Test Catalog

The `dev.galasa.ivts.obr` project creates the OSGi Bundle Repository and test catalog:
- **OBR** - Lists all IVT bundle dependencies for runtime resolution
- **Test Catalog** - JSON index of all available IVT test classes generated from source annotations

This OBR and test catalog represent the "ivts" test stream that can be referenced in ecosystem configuration.

## Anatomy of an IVT

IVTs follow a standard structure that exercises Manager functionality through Galasa's annotation-based provisioning system.

### Basic IVT Structure

A simple IVT like `CoreManagerIVT` demonstrates the fundamental pattern:

```java
@Test
@Summary("Ensure the basic functions are working in the Core Manager")
@Tags({"core","ivt"})
public class CoreManagerIVT {
    
    @Logger
    public Log logger;
    
    @RunName
    public String runName;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Test
    public void checkLogger() throws Exception {
        if (logger == null) {
            throw new Exception("Logger field is null");
        }
        logger.info("Logger field correctly initialised");
    }
    
    @Test
    public void checkRunName() throws Exception {
        if (runName == null || runName.trim().isEmpty()) {
            throw new Exception("Run Name field is null");
        }
        logger.info("Run Name field correctly initialised: " + runName);
    }
    
    @Test
    public void checkCoreManager() throws Exception {
        if (coreManager == null) {
            throw new Exception("Core Manager field is null");
        }
        logger.info("Core Manager field correctly initialised");
    }
}
```

This IVT validates that:
1. The `@Logger` annotation provides a logger instance
2. The `@RunName` annotation provides the test run name
3. The `@CoreManager` annotation provides the Core Manager interface

### Complex IVT Example

More complex IVTs like `HttpManagerIVT` test multiple Manager capabilities:

```java
@Test
public class HttpManagerIVT {
    @Logger
    public Log logger;

    @HttpClient
    public IHttpClient client;
    
    @Test
    public void getTests() throws Exception {
        client.setURI(new URI("https://httpbin.org"));
        String sResponse = client.getText("/get").getContent();
        assertThat(sResponse).contains("\"url\": \"https://httpbin.org/get\"");
        
        JsonObject jResponse = client.getJson("/get").getContent();
        assertThat(jResponse.get("url").getAsString())
            .isEqualTo("https://httpbin.org/get");
    }
    
    @Test
    public void authTest() throws Exception {
        String user = "hobbit";
        String pword = "passw0rd";
        
        client.setAuthorisation(user, pword);
        HttpClientResponse<JsonObject> response = 
            client.getJson("/basic-auth/" + user + "/" + pword);
        
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getContent().get("authenticated").getAsBoolean())
            .isTrue();
    }
}
```

This IVT validates HTTP Manager functionality including:
- HTTP client provisioning via `@HttpClient`
- GET requests for text and JSON responses
- Authentication and credential handling
- Response parsing and validation

### z/OS IVT Example

z/OS IVTs test Managers that require mainframe resources:

```java
@Test
public class ZosManagerIVT {
    @Logger
    public Log logger;
    
    @ZosImage
    public IZosImage imagePrimary;
    
    @ZosIpHost
    public IIpHost hostPrimary;
    
    @Test
    public void checkPrimaryImage() throws Exception {
        assertThat(imagePrimary).isNotNull();
        assertThat(hostPrimary).isNotNull();
        logger.info("Primary Image field correctly initialised");
    }
    
    @Test
    public void checkDefaultCredentials() throws Exception {
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        logger.info("Primary Credentials are being returned");
    }
}
```

This IVT validates:
- z/OS image provisioning via `@ZosImage`
- IP host provisioning via `@ZosIpHost`
- Credential retrieval from the z/OS Manager

## Building IVTs

IVTs can be built locally for development or in CI for deployment.

### Building Locally

To build IVTs locally, use the provided build script:

```bash
cd modules/ivts
./build-locally.sh
```

This script:
1. Builds all IVT test bundles
2. Generates the OBR (OSGi Bundle Repository)
3. Creates the test catalog
4. Publishes artifacts to your local Maven repository (`~/.m2/repository`)

The local build references development artifacts from the source Maven location (defaults to `https://development.galasa.dev/main/maven-repo/obr/`).

### Building in CI

The GitHub Actions workflow `.github/workflows/ivts.yaml` builds IVTs as part of the main build pipeline:

1. **Downloads dependencies** - Retrieves artifacts from earlier workflow jobs (platform, framework, managers, obr)
2. **Builds IVT bundles** - Compiles test code and generates OSGi bundles using Gradle
3. **Generates OBR and test catalog** - Creates the test stream artifacts
4. **Publishes to Maven repository** - Uploads artifacts for consumption by test execution systems
5. **Builds Docker images** - Creates Maven registry images:
   - `ivts-maven-artefacts` - Standard IVT Maven repository
   - `ivts-auth-maven-artefacts` - Authenticated Maven repository for restricted tests
6. **Deploys to ArgoCD** - Recycles IVT applications in Kubernetes for continuous deployment

The workflow uses Gradle with the `galasa.obr` and `galasa.testcatalog` plugins to generate the test stream artifacts.

## Running IVTs

IVTs can be run locally for development or in an ecosystem for automation.

### Running IVTs Locally

To run an IVT locally, use `galasactl runs submit local`:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa/dev.galasa.ivts.obr/1.1.1/obr \
  --class dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
  --log -
```

Command breakdown:
- `--obr` - Maven coordinates of the IVT OBR
- `--class` - Test class in format `bundle-symbolic-name/test-class-name`
- `--log -` - Stream test output to stdout

### Running z/OS IVTs Locally

z/OS IVTs require additional CPS properties to specify mainframe resources:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa/dev.galasa.ivts.obr/1.1.1/obr \
  --class dev.galasa.zos.ivts/dev.galasa.zos.ivts.zos.ZosManagerIVT \
  --log -
```

Before running, configure CPS properties in your local `~/.galasa/cps.properties`:

```properties
# z/OS Image Configuration
zos.dse.tag.PRIMARY.imageid=MVSA
zos.image.MVSA.ipv4.hostname=192.168.1.100
zos.image.MVSA.credentials=MVSA

# Credentials (stored in credentials.properties)
secure.credentials.MVSA.username=USERID
secure.credentials.MVSA.password=PASSWORD
```

Refer to the [Galasa Manager documentation](https://galasa.dev/docs/managers) for specific CPS properties required by each Manager.

### Running IVTs in the Ecosystem

IVTs run automatically in the Galasa ecosystem via the "ivts" test stream. The test stream is configured with:
- **OBR URL** - Points to the IVT OBR in the Maven repository
- **Test Catalog URL** - Points to the IVT test catalog JSON file
- **Repository URL** - Base URL of the Maven repository hosting the artifacts

Test runs are submitted using `galasactl runs submit`:

```bash
galasactl runs submit \
  --bootstrap https://galasa.example.com/api/bootstrap \
  --stream ivts \
  --class dev.galasa.ivts.core.CoreManagerIVT
```

The ecosystem resolves the test class from the "ivts" stream and executes it on available test engines.

### Daily Automated Runs

While the IVT build runs as part of the main CI pipeline, the actual test execution happens through:
1. **Deployment to Development Environment** - IVT Maven repositories are deployed as Docker images to the development Galasa ecosystem via ArgoCD
2. **Scheduled Test Execution** - Tests from the "ivts" stream are run on a schedule against the deployed ecosystem
3. **Regression Detection** - Failures indicate Manager regressions or environment issues

The workflow creates and maintains:
- `ghcr.io/<namespace>/ivts-maven-artefacts:<branch>` - IVT artifacts for the specified branch
- ArgoCD applications that deploy these artifacts to Kubernetes clusters
- Health checks that verify successful deployment

## CPS Properties Required for IVTs

Different IVTs require different Configuration Property Store (CPS) properties depending on the Managers they test.

### Core Manager IVTs
Core Manager IVTs (CoreManagerIVT, ArtifactManagerIVT) typically require no additional CPS properties as they test fundamental framework functionality.

### HTTP Manager IVTs
HTTP Manager IVTs may require proxy configuration if running in restricted networks:

```properties
http.proxy.host=proxy.example.com
http.proxy.port=8080
```

### Docker Manager IVTs
Docker Manager IVTs require Docker Engine configuration:

```properties
# Use local Docker engine
docker.default.engines=LOCAL

# Or use remote Docker engine
docker.default.engines=REMOTE
docker.engine.REMOTE.hostname=docker.example.com
docker.engine.REMOTE.port=2375
```

### z/OS Manager IVTs
z/OS IVTs require comprehensive z/OS system configuration:

```properties
# Image pool definition
zos.dse.tag.PRIMARY.imageid=MVSA|MVSB|MVSC

# Image connection details
zos.image.MVSA.ipv4.hostname=192.168.1.100
zos.image.MVSA.telnet.port=23
zos.image.MVSA.telnet.tls=false
zos.image.MVSA.credentials=MVSA

# Credentials
secure.credentials.MVSA.username=TESTUSER
secure.credentials.MVSA.password=TESTPASS

# z/OS File Manager properties
zos.image.MVSA.dataset.hlq=GALASA
```

### CICS Manager IVTs
CICS IVTs require CICS region configuration:

```properties
# CICS region association
cicsts.provision.type=DSE
cicsts.dse.tag.A.applid=CICSRGN1

# CICS region details
cicsts.region.CICSRGN1.host=MVSA
```

### 3270 Terminal IVTs
3270 IVTs require terminal emulation configuration:

```properties
# Terminal allocation
zos3270.image.MVSA.terminal.pool=2-10
zos3270.terminal.autoconnect=true
```

Consult the [Galasa CPS Properties documentation](https://galasa.dev/docs/managers) for complete property lists for each Manager.

## IVT Migration Status

IVTs are being migrated from the Managers module to the dedicated IVTs module to simplify testing organization. Previously, each IVT was stored in its own bundle within the same subdirectory as the Manager it tested. The new structure consolidates IVTs into the two parent bundles described above.

### Migrated IVTs
The following IVTs have been migrated to the `/modules/ivts` module:

**Non-z/OS IVTs (dev.galasa.ivts):**
- CoreManagerIVT
- ArtifactManagerIVT
- HttpManagerIVT
- DockerManagerIVT
- JMeterManagerIVT (if applicable)
- TestCredentialsStoreAccess (Framework IVT)

**z/OS IVTs (dev.galasa.zos.ivts):**
- CECIManagerIVT
- CedaManagerIVT
- CEMTManagerIVT
- CICSTSManagerIVT
- SdvManagerIVT
- ZosManagerIVT
- ZosManagerBatchIVT
- ZosManagerFileDatasetIVT
- ZosManagerFileIVT
- ZosManagerFileVSAMIVT
- ZosManagerTSOCommandIVT
- Zos3270IVT

### IVTs Remaining in Managers Module
Some IVTs may still reside in the Managers module if they have not been migrated yet. Check the `/modules/managers` directory for legacy IVT bundles.

## Test Stream Integration

IVTs are packaged as a test stream named "ivts" that integrates with Galasa's ecosystem test execution infrastructure.

### Test Stream Components

The IVT test stream consists of:
1. **OBR (OSGi Bundle Repository)** - Maven artifact at `mvn:dev.galasa/dev.galasa.ivts.obr/<version>/obr` listing all IVT bundles and dependencies
2. **Test Catalog** - JSON file listing all IVT test classes with their metadata (package, class, methods, annotations)
3. **Maven Repository** - Hosts the actual IVT JAR files and their dependencies

### Test Catalog Generation

The test catalog is generated using the `galasa.testcatalog` Gradle plugin during the build process. It scans IVT classes for:
- `@Test` class annotations
- `@Summary` descriptions
- `@Tags` metadata
- Test method definitions

The resulting `testcatalog.json` file enables test selection and filtering in the ecosystem.

### Ecosystem Configuration

To use the IVT test stream in a Galasa ecosystem, configure it using `galasactl streams set`:

```bash
galasactl streams set \
  --name ivts \
  --description "Installation Verification Tests for Galasa Managers" \
  --obr mvn:dev.galasa/dev.galasa.ivts.obr/1.1.1/obr \
  --location https://development.galasa.dev/main/maven-repo/ivts/testcatalog.json \
  --repo https://development.galasa.dev/main/maven-repo/ivts
```

This makes the IVT test stream available for test submission in the ecosystem.

## Relationship to Manager Development

IVTs play a crucial role in the Manager development lifecycle:

### Development Phase
- Developers write IVTs while developing or enhancing Managers
- IVTs validate that new Manager features work as designed
- Local IVT runs provide rapid feedback during development

### Testing Phase
- IVTs must pass before a Manager can be promoted from Alpha to Beta readiness
- IVTs validate that Manager TPI (Test Program Interface) works correctly
- IVTs ensure resource provisioning and cleanup work properly

### Maintenance Phase
- Daily IVT runs detect regressions introduced by framework or dependency changes
- Failed IVTs trigger alerts to the development team
- IVT history tracks Manager stability over time

### Documentation Phase
- IVTs serve as executable examples of Manager usage
- IVT code demonstrates proper annotation usage and API calls
- IVTs validate that documented behavior matches actual behavior

See the [Managers architecture documentation](/openwiki/architecture/managers.md) for more information on Manager readiness levels and testing requirements.

## Troubleshooting IVT Failures

When an IVT fails, use this troubleshooting guide to diagnose the issue.

### Check Test Output
Review the test execution log for error messages:

```bash
galasactl runs get --name <run-id> --log
```

Look for:
- Exception stack traces
- Assertion failures
- Resource provisioning errors
- Timeout messages

### Verify CPS Properties
Ensure required CPS properties are configured:

```bash
galasactl properties get --namespace <namespace>
```

Common issues:
- Missing image definitions
- Incorrect hostnames or ports
- Invalid credentials
- Missing dataset prefixes

### Check Resource Availability
Verify that required resources are available:
- z/OS systems are online and accessible
- Docker engines are running
- Network connectivity is working
- Credentials are valid

### Validate Dependencies
Ensure all Manager dependencies are available:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa/dev.galasa.ivts.obr/1.1.1/obr \
  --class dev.galasa.ivts.core.CoreManagerIVT \
  --log -
```

If the OBR cannot be resolved, check Maven repository accessibility.

### Review Manager Logs
Manager-specific logs may provide additional context:
- Check DSS (Dynamic Status Store) for resource allocation failures
- Review engine logs for framework errors
- Examine Manager debug output if enabled

## Related Documentation

- [Managers Architecture](/openwiki/architecture/managers.md) - Comprehensive guide to Manager design and lifecycle
- [Test Execution Lifecycle](/openwiki/concepts/test-execution-lifecycle.md) - How tests move through states from submission to completion
- [Modules Overview](/openwiki/concepts/modules.md) - Overview of the IVTs module structure
- [GitHub Workflows](/openwiki/operations/github-workflows.md) - CI/CD pipeline that builds and deploys IVTs

## Further Reading

External documentation:
- [Test Streams Documentation](https://galasa.dev/docs/manage-ecosystem/test-streams) - Official documentation on test streams
- [galasactl CLI Reference](https://github.com/galasa-dev/galasa/blob/main/modules/cli/README.md) - Command-line interface for running tests
- [Manager Documentation](https://galasa.dev/docs/managers) - Individual Manager documentation with CPS property reference
- [Initializing Local Environment](https://galasa.dev/docs/cli-command-reference/initialising-home-folder) - Setting up local Galasa environment
