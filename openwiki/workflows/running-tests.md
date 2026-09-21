---
type: workflow-guide
title: Running Tests
description: Complete guide to running Galasa tests in local JVM, local ecosystem, and remote ecosystem modes including configuration, viewing logs, and retrieving results.
tags: [testing, test-execution, galasactl, local-testing, remote-testing, cli, galasa-boot]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-24a14bb82674cecdc83b911a
    resource: repo://modules/cli/build/cli-docs/galasactl_runs_submit_local.md
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-f888fdbbee1c3e79c1e1b105
    resource: repo://modules/framework/dev-instructions.md
  - id: openwiki-source-19c289d51521684350e01d7c
    resource: repo://modules/framework/README.md
  - id: openwiki-source-2f0af65214aca74a6cff299a
    resource: repo://modules/framework/test-api-locally.md
  - id: openwiki-source-4677f6fb5194da8fd9898ef0
    resource: repo://modules/ivts/README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Running Tests

This guide covers the different modes for running Galasa tests, from local JVM development to remote ecosystem execution. Understanding these modes helps you choose the right approach for test development, debugging, and automated execution.

## Test Execution Modes

Galasa tests can be executed in three primary modes:

1. **Local JVM** - Tests run directly in a local Java process without ecosystem infrastructure
2. **Local Ecosystem** - Tests run against a locally deployed Galasa ecosystem (with etcd/CouchDB)
3. **Remote Ecosystem** - Tests submit to a deployed Galasa service for execution

Each mode has different use cases, benefits, and limitations.

## Running Tests in a Local JVM

Local JVM execution is the fastest way to run tests during development. Tests run directly in your local Java environment without requiring ecosystem infrastructure.

### Prerequisites

- Java 17 or later installed (`JAVA_HOME` configured)
- Galasa home initialized with `galasactl local init`
- Test OBR (OSGi Bundle Repository) available locally or in Maven repositories

### Basic Local Test Execution

Run a test using `galasactl runs submit local`:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log -
```

**Key parameters:**
- `--obr` - Maven coordinates of the OBR containing test bundles (format: `mvn:groupId/artifactId/version/obr`)
- `--class` - Test class to run (format: `osgi-bundle-id/fully-qualified-class-name`)
- `--log -` - Send log output to console (stderr); omit for no logging or specify a file path

### Running a Single Test Method

To run only specific test methods within a class, use the `--methods` flag:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --methods testCreateAccount \
  --log -
```

Multiple methods can be specified by repeating the `--methods` flag.

### Running Gherkin Tests

Galasa supports Gherkin feature files for behavior-driven testing:

```bash
galasactl runs submit local \
  --gherkin file:///path/to/test.feature \
  --log -
```

The `--gherkin` parameter accepts a file URL pointing to a `.feature` file.

### Specifying Bootstrap and Override Properties

Bootstrap properties configure the framework and storage backends:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --bootstrap file://${HOME}/.galasa/bootstrap.properties \
  --log -
```

Override properties allow you to change CPS (Configuration Property Store) values for a specific test run:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --override zos.default.lpar=MYLPAR \
  --override zos.default.cluster=MYCLUSTER \
  --log -
```

Alternatively, use an overrides file:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --overridefile /path/to/overrides.properties \
  --log -
```

**Override precedence** (highest to lowest):
1. `--override` command-line flags
2. `--overridefile` properties
3. `${GALASA_HOME}/overrides.properties` (default if exists)
4. CPS properties from bootstrap

### Maven Repository Configuration

By default, `galasactl` uses:
- **Local Maven**: `${HOME}/.m2/repository`
- **Remote Maven**: Maven Central

Override these with:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --localmaven file:///opt/maven/repository \
  --remotemaven https://development.galasa.dev/main/maven-repo/obr \
  --log -
```

### Offline Test Execution

To run tests without network access, first prepare dependencies:

```bash
galasactl runs prepare local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --log -
```

This downloads all required bundles to the local Maven cache. Then run tests offline:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --offline \
  --log -
```

The `--offline` flag prevents network access during bundle resolution.

### Parallel Execution

Control local test parallelism with the `--throttle` option:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.Test1 \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.Test2 \
  --throttle 1 \
  --log -
```

- `--throttle 1` - Run tests sequentially
- Higher values allow parallel execution

### Local Test Limitations

Local JVM testing does **not** provide:
- Centralized resource management
- Resource contention arbitration between concurrent tests
- Automatic retry on `ResourceUnavailableException`
- Centralized results in the ecosystem RAS
- Engine controller lifecycle management

Use local testing for development and debugging only. Production test execution should use an ecosystem.

## Running Tests with galasa-boot JAR

The `galasa-boot` JAR provides direct JVM-based test execution without the `galasactl` CLI wrapper.

### Locating galasa-boot JAR

After building the framework module:
```
galasa/modules/framework/galasa-parent/galasa-boot/build/libs/galasa-boot-{version}.jar
```

Or from a local Maven repository:
```
${HOME}/.m2/repository/dev/galasa/galasa-boot/{version}/galasa-boot-{version}.jar
```

### Running a Test with galasa-boot

```bash
java -jar ~/.m2/repository/dev/galasa/galasa-boot/0.36.0/galasa-boot-0.36.0.jar \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/0.36.0/obr \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --test dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --bootstrap file://${HOME}/.galasa/bootstrap.properties \
  --overrides file://${HOME}/.galasa/overrides.properties
```

**galasa-boot parameters:**
- `--obr` - OBR to load (can be specified multiple times)
- `--test` - Test class to run (format: `bundle/class`)
- `--bootstrap` - Bootstrap properties URL (`http://` or `file://`)
- `--overrides` - Override properties URL
- `--run` - Run name to associate with the test
- `--gherkin` - Gherkin feature file URL
- `--bundle` - Extra bundles to load
- `--localmaven` - Local Maven repository URL (default: `~/.m2/repository`)
- `--remotemaven` - Remote Maven repository URLs (default: Maven Central)
- `--trace` - Enable TRACE logging

### JVM Launch Options

Configure additional JVM options in `bootstrap.properties`:

```properties
framework.jvm.local.launch.options=-Xms20m -Xmx512m -Dmy.property=value
```

This is a space-separated list of JVM options applied when launching local test processes.

## Running Tests in a Remote Ecosystem

Remote ecosystem execution submits tests to a deployed Galasa service for scheduling and execution by the engine controller.

### Prerequisites

- Galasa ecosystem deployed and accessible
- `GALASA_BOOTSTRAP` environment variable or `--bootstrap` flag pointing to ecosystem
- Personal access token configured (via `galasactl auth login`)

### Submitting Tests to an Ecosystem

#### Submit by Test Class

```bash
galasactl runs submit \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log -
```

Multiple classes can be specified with repeated `--class` flags.

#### Submit from Test Stream

Test streams are named collections of tests configured in the ecosystem CPS:

```bash
galasactl runs submit \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --stream inttests \
  --package test.package.one \
  --log -
```

**Test selection parameters:**
- `--stream` - Test stream name (required for stream-based submission)
- `--package` - Filter by package name
- `--bundle` - Filter by bundle name
- `--tag` - Filter by test tag
- `--test` - Specific test name
- `--regex` - Interpret package/bundle filters as regular expressions

#### Submit with Portfolio

Build a test portfolio with specific overrides:

```bash
galasactl runs prepare \
  --portfolio test-portfolio.yaml \
  --stream inttests \
  --package test.package.one \
  --override zos.default.lpar=MYLPAR
```

Submit the portfolio:

```bash
galasactl runs submit \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --portfolio test-portfolio.yaml \
  --poll 5 \
  --progress 1 \
  --throttle 10 \
  --log -
```

**Submission parameters:**
- `--portfolio` - Portfolio file containing tests to run
- `--poll` - Poll interval in seconds for run status updates
- `--progress` - Progress update interval in minutes
- `--throttle` - Maximum concurrent test runs

### Providing Overrides for Ecosystem Runs

Override CPS properties for specific test runs:

```bash
galasactl runs submit \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --override zos.default.lpar=MYLPAR \
  --override zos.default.cluster=MYCLUSTER \
  --log -
```

**Note**: Overrides in the portfolio take precedence over command-line overrides.

### Associating Users with Runs

The `requestor` is automatically set to the personal access token owner. Specify a different user:

```bash
galasactl runs submit \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --user actualuser@company.com \
  --log -
```

This is useful when submitting runs from automation (e.g., CI/CD) where the token belongs to a bot account.

## Viewing Test Logs and Results

### Local Test Results

Local test results are stored in `${GALASA_HOME}/ras/` with run names prefixed by `L` (e.g., `L1`, `L2`):

```
${HOME}/.galasa/ras/
└── L1/
    ├── artifacts.json      # Artifact metadata
    ├── structure.json      # Test structure
    ├── run.log            # Test execution log
    └── artifacts/         # Test artifacts and files
```

View the run log directly:

```bash
cat ${HOME}/.galasa/ras/L1/run.log
```

Or follow logs during execution:

```bash
tail -f ${HOME}/.galasa/ras/L1/run.log
```

### Ecosystem Test Results

#### Query Test Runs

Retrieve run information from the ecosystem:

```bash
galasactl runs get \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234 \
  --format details
```

**Output formats:**
- `summary` - Brief run summary
- `details` - Detailed run information including test methods
- `raw` - Raw JSON response

Query runs by age:

```bash
galasactl runs get \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --age 1d
```

#### Download Test Artifacts

Download all artifacts for a test run from the RAS:

```bash
galasactl runs download \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234
```

This creates a directory named `C1234` in the current working directory with all artifacts:

```
./C1234/
├── artifacts.json
├── structure.json
├── run.log
└── artifacts/
```

**Download options:**
- `--force` - Overwrite existing run directory
- `--destination /path/to/dir` - Specify download location (default: current directory)

Example with custom destination:

```bash
galasactl runs download \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234 \
  --destination /Users/me/test-results \
  --force
```

## Configuring CPS Properties for Managers

Galasa managers require Configuration Property Store (CPS) properties to function. These properties configure manager behavior, resource locations, and credentials.

### Local Environment CPS Configuration

For local test execution, configure CPS properties in `${GALASA_HOME}/cps.properties`:

```properties
# Docker Manager configuration
docker.default.engines=LOCAL
docker.engine.LOCAL.hostname=unix:///var/run/docker.sock

# HTTP Manager configuration
http.tls.truststore.path=/path/to/truststore.jks
http.tls.truststore.password=password

# Core Manager configuration
core.maven.repository.url=https://repo.maven.apache.org/maven2/
```

### Ecosystem CPS Configuration

For ecosystem-based testing, set properties using `galasactl properties set`:

```bash
galasactl properties set \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --namespace docker \
  --name default.engines \
  --value REMOTE1,REMOTE2
```

Query properties:

```bash
galasactl properties get \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --namespace docker
```

### Manager-Specific Property Requirements

Each manager has specific CPS property requirements. Consult the manager documentation for details:

- **Core Manager**: Test run configuration, resource management
- **Docker Manager**: Docker engine endpoints, image registries
- **HTTP Manager**: TLS configuration, timeouts, proxy settings
- **z/OS Managers**: LPAR configuration, credentials, TSO settings
- **CICS Managers**: CICS region configuration, transaction settings

**Example - Docker Manager setup for local testing:**

```properties
# ${GALASA_HOME}/cps.properties
docker.default.engines=LOCAL
docker.engine.LOCAL.hostname=unix:///var/run/docker.sock
docker.engine.LOCAL.max.slots=5
```

**Example - HTTP Manager TLS setup:**

```properties
# ${GALASA_HOME}/cps.properties
http.tls.truststore.path=/Users/me/.galasa/truststore.jks
http.tls.truststore.password=changeit
http.request.timeout=30000
```

### Credentials Configuration

Manager credentials are stored in `${GALASA_HOME}/credentials.properties` for local testing:

```properties
secure.credentials.ZOS01.username=USERID
secure.credentials.ZOS01.password=PASSWORD
```

For ecosystem testing, use the ecosystem's credentials store configured by administrators.

## Debugging Local Tests

### Enable Debug Mode

Launch tests in debug mode to attach a Java debugger:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --debug \
  --debugPort 2970 \
  --debugMode listen \
  --log -
```

**Debug parameters:**
- `--debug` - Enable debug mode
- `--debugPort` - Port for debugger connection (default: 2970)
- `--debugMode` - Connection mode: `listen` or `attach`

### Debug Modes

**Listen mode** (default):
- Test JVM opens a port and waits for debugger to connect
- Start the test first, then attach your IDE debugger

**Attach mode**:
- Test JVM attempts to connect to an already-running debugger
- Start your IDE debugger first, then launch the test

### Configure Debug Defaults in Bootstrap

Set default debug configuration in `bootstrap.properties`:

```properties
galasactl.jvm.local.launch.debug.port=2971
galasactl.jvm.local.launch.debug.mode=attach
```

Command-line flags override these defaults.

### IDE Integration

Refer to the CLI documentation for IDE-specific debug configuration:
- VS Code: `/modules/cli/docs/vscode/debug_in_vscode.md`
- IntelliJ: `/modules/cli/docs/intellij/debug_in_intellij.md`
- Eclipse: `/modules/cli/docs/eclipse/debug_in_eclipse.md`

## Managing Running Tests

### Reset a Stuck Test

If a test is stuck in timeout or looping:

```bash
galasactl runs reset \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234
```

This requeues the test for execution. The command returns immediately after the server accepts the request.

### Cancel a Test

Cancel a running test that cannot complete successfully:

```bash
galasactl runs cancel \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234
```

This stops test execution but preserves any RAS data already stored. The command returns immediately after the server accepts the request.

### Delete Test Results

Remove a test run and all associated artifacts from the RAS:

```bash
galasactl runs delete \
  --bootstrap https://ecosystem.example.com/api/bootstrap \
  --name C1234
```

This permanently deletes the test run data.

## Resource Cleanup

### Local Resource Cleanup

If local test JVM processes exit before cleanup completes, resources may remain provisioned. Clean them up manually:

```bash
galasactl runs cleanup local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --includes-pattern "dev.galasa.*" \
  --log -
```

**Cleanup parameters:**
- `--obr` - OBRs containing resource management providers (can be repeated)
- `--includes-pattern` - Glob pattern to match providers to run (can be repeated)
- `--excludes-pattern` - Glob pattern to exclude providers (can be repeated)
- `--remotemaven` - Remote Maven repositories for OBR downloads

**Glob pattern syntax:**
- `*` - Matches zero or more characters
- `?` - Matches exactly one character

**Examples:**
- `dev.galasa*` - Matches any class starting with `dev.galasa`
- `*MyResourceMonitorClass` - Matches any class ending with `MyResourceMonitorClass`

## Test Execution Best Practices

### Development Phase
- Use `galasactl runs submit local` for rapid iteration
- Enable `--log -` to see immediate feedback
- Use `--methods` to run specific test methods
- Configure debug mode for troubleshooting

### Integration Testing Phase
- Submit tests to ecosystem using `--stream` for organized test selection
- Use `--portfolio` for complex test configurations
- Apply `--override` for environment-specific configuration
- Monitor with `--poll` and `--progress` flags

### Production Phase
- Execute tests via ecosystem only (never local JVM)
- Configure appropriate `--throttle` to balance throughput and resource usage
- Use `--user` flag in CI/CD to track actual requestors
- Regularly query and archive test results with `runs download`

### Configuration Management
- Store common overrides in `overrides.properties`
- Document required CPS properties for each test suite
- Use test streams to organize tests by feature or component
- Version control portfolio files for repeatable test execution

## Common Issues and Solutions

### Issue: "Unable to locate test stream"
**Cause**: Test stream not configured in ecosystem CPS or incorrect stream name.

**Solution**: Verify stream exists with `galasactl streams get` and check CPS configuration.

### Issue: "ResourceUnavailableException"
**Cause**: Required resources are not available (local JVM does not retry automatically).

**Solution**: For ecosystem testing, the framework will automatically retry. For local testing, ensure resources are available or mock them.

### Issue: Test hangs in "generating" state
**Cause**: Manager cannot determine resource availability.

**Solution**: Check CPS properties for the manager. Verify resource connectivity (e.g., Docker daemon, z/OS LPAR).

### Issue: Local test logs not appearing
**Cause**: `--log` flag not specified or incorrect path.

**Solution**: Use `--log -` for console output or `--log /path/to/file.log` for file output.

### Issue: Bundle resolution failures
**Cause**: Missing dependencies in local Maven cache or network issues.

**Solution**: Use `galasactl runs prepare local` to pre-fetch dependencies, then run with `--offline`.

### Issue: "Bootstrap not found" over VPN
**Cause**: DNS resolution issues in Go programs over VPN.

**Solution**: Add IP address mapping to `bootstrap.properties`:
```properties
https://myhost/api/bootstrap=1.2.3.4
```

## Summary

Galasa provides flexible test execution options:

- **Local JVM**: Fast development iteration with `galasactl runs submit local`
- **galasa-boot JAR**: Direct JVM execution for advanced use cases
- **Remote Ecosystem**: Production test execution with `galasactl runs submit`

Choose the appropriate mode based on your development phase and infrastructure availability. Always use ecosystem execution for production testing to benefit from resource management, retry logic, and centralized results storage.
