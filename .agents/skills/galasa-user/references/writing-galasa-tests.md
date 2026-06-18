---
name: writing-galasa-tests
description: Provides information about how Galasa test classes are structured, how they are built, and how test environments are configured.
---

### Galasa Test Class Structure

Galasa test classes are Java files with this structure:

**Required elements:**
- Package: `{PACKAGE_NAME}.{FEATURE_NAME}`
- Imports: AssertJ (`assertThat`), Galasa annotations (`@Test`, `@Logger`)
- Class: `@Test` annotation + `@Summary("description")`
- Logger: `@Logger public Log logger;` (only if log messages are added to tests)
- Test methods: `@Test` annotation on each test method

**Optional lifecycle methods:**
- `@BeforeClass` / `@AfterClass`: One-time setup/cleanup
- `@Before` / `@After`: Per-test setup/cleanup

**Assertion rules:**
- Use AssertJ: `assertThat(value).as("Description").isEqualTo(expected)`
- Always include descriptive message with `.as()`
- Remove unused imports

**Example structure:**
```java
package dev.galasa.example.feature;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.commons.logging.Log;
import dev.galasa.*;
import dev.galasa.core.manager.Logger;

@Summary("Test suite description")
@Test
public class TestClass {
    @Logger
    public Log logger;
    
    @Test
    public void testSomething() throws Exception {
        assertThat(result).as("Should match expected").isEqualTo(expected);
    }
}
```

For complete examples, see [Galasa IVT tests](https://github.com/galasa-dev/galasa/tree/main/modules/ivts).

## Building Galasa projects

- If you are using Gradle: Run `gradle clean build publishToMavenLocal` from the Galasa project's root directory to build the project.
- If you are using Maven: Run `mvn clean install` from the Galasa project's root directory to build the project.

**Note**: When building for the first time, Gradle/Maven will download all required dependencies. This may take several minutes. Subsequent builds will be faster.

**Tip**: To check which version of Galasa is being used, examine the `build.gradle` or `pom.xml` file for the Galasa version property.

## Configuring test environments

Galasa tests often need to connect to external systems. The details of these systems can be configured into the Configuration Property Store (CPS) file found at `~/.galasa/cps.properties` by default.

CPS properties are key-value pairs in the form:
```properties
NAMESPACE.PROPERTY_NAME=PROPERTY_VALUE
```

Where:
- `NAMESPACE`: The namespace of the manager that this property is associated with, typically the name of the manager itself. Example: The z/OS manager is `zos`, the Docker manager is `docker`, and the z/OS 3270 manager is `zos3270`.
- `PROPERTY_NAME`: The name of the CPS property to configure.
- `PROPERTY_VALUE`: The value to be associated with the CPS property.

**IMPORTANT**: Every manager has its own set of supported CPS property. **DO NOT** assume that managers share the same CPS properties. The supported CPS properties for each manager can be found in the managers' documentation pages https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/index.md. Example: the z/OS manager's supported CPS properties can be found at https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/zos-managers/zos-manager.md

### Configuring Credentials

Galasa tests often need to supply credentials to connect to protected systems. There are two ways to configure credentials:

#### Option 1: Plaintext Credentials File (Default)

Credentials can be configured into the Credentials Store file found at `~/.galasa/credentials.properties` by default.

Credentials properties are key-value pairs in the form:
```properties
secure.credentials.TAG.CREDENTIALS_TYPE=CREDENTIAL_VALUE
```

Where:
- `TAG`: A tag that identifies the credentials. This tag is used to reference the credentials from the CPS properties.
- `CREDENTIALS_TYPE`: The type of credentials. Supported types are: `username`, `password`, and `token`.
- `CREDENTIAL_VALUE`: The value of the credentials.

Example: To create a username `MYUSER` and password `PASSW0RD` associated with the tag `SYSTEM1`, you would set these properties:

```properties
secure.credentials.SYSTEM1.username=MYUSER
secure.credentials.SYSTEM1.password=PASSW0RD
```

Then, the CPS properties can use the credentials like this:
```properties
zos.image.MYZOSSYSTEM.credentials=SYSTEM1
```

#### Option 2: macOS Keychain Store (macOS only, more secure)

**If you are on macOS**, you can use the macOS Keychain to store credentials securely instead of storing them in plaintext in `credentials.properties`.

**Benefits:**
- Credentials are encrypted by macOS
- No plaintext passwords in configuration files
- Leverages macOS security features

**Setup:**

1. Enable the OS Credentials Store by adding this to `~/.galasa/bootstrap.properties`:
   ```properties
   framework.credentials.store=os:auto
   framework.extra.bundles=dev.galasa.creds.os
   ```

2. Add credentials to the macOS Keychain using the `security` command or Keychain Access.app. **ALWAYS** ask the user if they would like you to do this for them:
   ```bash
   # Example: Add username + password credentials
   security add-generic-password \
     -s "galasa.credentials.SYSTEM1" \
     -a "MYUSER" \
     -w "PASSW0RD" \
     -U
   ```

3. Reference credentials in CPS properties the same way:
   ```properties
   zos.image.MYZOSSYSTEM.credentials=SYSTEM1
   ```

**Supported credential types:**
- Username + Password
- Username only
- Token only
- Username + Token
- KeyStore (for SSL/TLS certificates)

**Note:** The first time Galasa accesses a credential, macOS will prompt you to allow access.

For complete documentation on using the macOS Keychain store, including all credential types and troubleshooting, see: https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/configuring-local-credentials/macos-keychain-store.md

## Controlling test execution flow

By default, if a `@Test` method fails, Galasa skips all subsequent `@Test` methods in the same class. Two class-level annotations let you change this behaviour.

**`@ContinueOnTestFailure`** — Place on the class declaration to keep executing subsequent `@Test` methods even after one fails. Useful when you want to collect as many failure signals as possible in a single run.

```java
@Test
@Summary("All login scenarios")
@ContinueOnTestFailure
public class LoginTests {
    // All @Test methods run even if an earlier one fails
}
```

**`@RunName`** — Inject the unique run identifier (e.g. `L12`, `U42`) assigned by Galasa for this execution. Useful for constructing unique resource or artifact names that will not clash with concurrent runs.

```java
@RunName
public String runName;   // populated by Core Manager before any @Test runs
```

Failures in `@BeforeClass` / `@AfterClass` are reported as `ENVIRONMENT_FAILURE`, indicating setup rather than test logic failed. Failures in `@Before` / `@After` are reported as `FAILED`.

Full docs: https://galasa.dev/docs/writing-own-tests/writing-test-classes/

## Inspecting test results programmatically

`ITestResultProvider` gives a test access to the overall test-class result and the result of each method that has already run. Inject it with `@TestResultProvider`:

```java
@TestResultProvider
public ITestResultProvider testResultProvider;
```

The Core Manager updates the provider after every lifecycle method (`@BeforeClass`, `@Before`, `@Test`, `@After`, `@AfterClass`).

**Typical pattern — conditional cleanup in `@AfterClass`:**

```java
@AfterClass
public void afterClass() throws FrameworkException {
    if (testResultProvider.getResult().isFailed()) {
        runDiagnostics(); // only incur cost on failure
    }
}
```

**Inspecting individual method results (useful with `@ContinueOnTestFailure`):**

```java
@After
public void afterEachTest() throws FrameworkException {
    List<ITestMethodResult> results = testResultProvider.getTestMethodResults();
    ITestMethodResult last = results.get(results.size() - 1);
    if (last.isFailed()) {
        logger.error("Method failed: " + last.getFailureReason());
    }
}
```

Full docs: https://galasa.dev/docs/writing-own-tests/test-result-provider/

## Binding tests to environments (image tags and clustering)

Hard-coding hostnames in test code means the test only works against one environment. Instead, use an `imageTag` in the annotation and store the actual connection details in CPS. Galasa resolves the tag at runtime.

**Single image — define CPS properties, reference by tag in the test:**

```properties
# cps.properties
zos.image.SYSA.ipv4.hostname=sysa.example.com
zos.image.SYSA.telnet.port=23
zos.image.SYSA.credentials=SYSA
```

```java
@ZosImage(imageTag="SYSA")
public IZosImage image;
```

**Cluster — define a named pool of images; Galasa picks an available one dynamically:**

```properties
# cps.properties
zos.cluster.CLUSTER1.images=IMAGEA,IMAGEB

zos.image.IMAGEA.ipv4.hostname=imagea.example.com
zos.image.IMAGEA.max.slots=1
zos.image.IMAGEA.clusterid=CLUSTER1
zos.tag.IMAGEA.clusterid=CLUSTER1

zos.image.IMAGEB.ipv4.hostname=imageb.example.com
zos.image.IMAGEB.max.slots=1
zos.image.IMAGEB.clusterid=CLUSTER1
zos.tag.IMAGEB.clusterid=CLUSTER1
```

```java
@ZosImage(imageTag="CLUSTER1")
public IZosImage image;  // Galasa selects IMAGEA or IMAGEB based on slot availability
```

Images can be added to the cluster by updating CPS without recompiling the test.

Full docs: https://galasa.dev/docs/writing-own-tests/binding-tests/

## Running modes: local, hybrid, and remote

Galasa supports three run modes:

| Mode | Where the JVM runs | Where config comes from | Command |
|---|---|---|---|
| **Local** | Local machine | Local `~/.galasa/` files | `runs submit local` |
| **Hybrid** | Local machine | Remote Ecosystem CPS | `runs submit local` |
| **Remote** | Ecosystem container | Remote Ecosystem | `runs submit` |

**Hybrid mode setup** — add the following to `~/.galasa/bootstrap.properties`, then authenticate:

```properties
framework.config.store=galasacps://my.ecosystem.url/api
framework.extra.bundles=dev.galasa.cps.rest
```

```bash
galasactl auth login
galasactl runs submit local --class dev.example/MyTest ...
```

In hybrid mode the test still runs in the local JVM and results are stored locally, but CPS properties are read from the remote Ecosystem. Credentials are still read from the local credentials file.

**Remote mode** — set `bootstrap` to the Ecosystem URL and use `runs submit` (see `galasa-ecosystem-management.md` in this skill for details).

Full docs: https://galasa.dev/docs/writing-own-tests/running-test-modes/

## Debugging tests locally

Pass `--debug` to `runs submit local` to suspend the JVM and wait for a Java debugger to connect on port `2970` (default):

```bash
galasactl runs submit local --debug --class dev.example/MyTest --bundle my-tests.jar
```

Override the port with `--debugPort <port>` or with `galasactl.jvm.local.launch.debug.port` in `bootstrap.properties`. Use `--debugMode attach` if you prefer the test to attach to an already-listening debugger rather than wait for one.

For full IDE setup instructions (VSCode `launch.json`, IntelliJ _Attach to Process_, Eclipse _Remote Java Application_), see `galasa-cli-tool.md` in this skill or https://galasa.dev/docs/cli-command-reference/runs-local-debug/
