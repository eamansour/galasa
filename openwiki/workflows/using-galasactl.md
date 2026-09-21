---
type: workflow-guide
title: Using galasactl CLI
description: Comprehensive guide to common galasactl workflows including initialization, running tests locally and remotely, managing resources, properties, secrets, and streams, and authentication with tokens.
tags: [cli, galasactl, workflows, authentication, properties, resources, secrets, streams, local-testing, remote-testing]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-65c139ea5de51c58251c0eb1
    resource: repo://developer-docs/cli-architecture.md
  - id: openwiki-source-de7d59ecc33295d166186386
    resource: repo://modules/cli/docs/generated/galasactl_streams_delete.md
  - id: openwiki-source-b860a8826a3c31dd478da630
    resource: repo://modules/cli/docs/generated/galasactl_streams_get.md
  - id: openwiki-source-68a371f7b3e0e4d68c1e9534
    resource: repo://modules/cli/docs/generated/galasactl_streams_set.md
  - id: openwiki-source-907f28314de0e74185019325
    resource: repo://modules/cli/pkg/cmd/authLogin.go
  - id: openwiki-source-0bfdd643427e36b8817dd330
    resource: repo://modules/cli/pkg/cmd/authTokensDelete.go
  - id: openwiki-source-cd6d6848d0e42385ca0238f8
    resource: repo://modules/cli/pkg/cmd/authTokensGet.go
  - id: openwiki-source-acfe7f006489f99fbd28d9ac
    resource: repo://modules/cli/pkg/cmd/localInit.go
  - id: openwiki-source-10835eee1ee38fce8275cb6b
    resource: repo://modules/cli/pkg/cmd/monitorsGet.go
  - id: openwiki-source-fa8d6681f5a0e98b1511711c
    resource: repo://modules/cli/pkg/cmd/monitorsSet.go
  - id: openwiki-source-2c53382a531b29e2cca35a41
    resource: repo://modules/cli/pkg/cmd/projectCreate.go
  - id: openwiki-source-ac78fa78b4ea19b60389a438
    resource: repo://modules/cli/pkg/cmd/propertiesDelete.go
  - id: openwiki-source-571a89b955a49a149b3a1e5f
    resource: repo://modules/cli/pkg/cmd/propertiesGet.go
  - id: openwiki-source-fce6eaf4d71fe03fe305d880
    resource: repo://modules/cli/pkg/cmd/propertiesNamespaceGet.go
  - id: openwiki-source-175f9970c651e963637b5a04
    resource: repo://modules/cli/pkg/cmd/propertiesSet.go
  - id: openwiki-source-a708c25b390a4d3ac8756bab
    resource: repo://modules/cli/pkg/cmd/resourcesApply.go
  - id: openwiki-source-0e36bb07297d66dba6056e9f
    resource: repo://modules/cli/pkg/cmd/resourcesCreate.go
  - id: openwiki-source-ee50d7ac754cf438cac8497b
    resource: repo://modules/cli/pkg/cmd/resourcesDelete.go
  - id: openwiki-source-6fd67815b32694d6f0666856
    resource: repo://modules/cli/pkg/cmd/resourcesUpdate.go
  - id: openwiki-source-bffcb6207cda6ce3504b17b9
    resource: repo://modules/cli/pkg/cmd/runsCancel.go
  - id: openwiki-source-9f5d8a18443eeda92b6073d1
    resource: repo://modules/cli/pkg/cmd/runsCleanupLocal.go
  - id: openwiki-source-c1beda129ca8a3f7db611b9e
    resource: repo://modules/cli/pkg/cmd/runsDelete.go
  - id: openwiki-source-35d917dd5e75744683d64bfc
    resource: repo://modules/cli/pkg/cmd/runsDownload.go
  - id: openwiki-source-f7a66a21d2b2dbe54718b04d
    resource: repo://modules/cli/pkg/cmd/runsGet.go
  - id: openwiki-source-ab055d0ea79e6a8ff558db28
    resource: repo://modules/cli/pkg/cmd/runsPrepare.go
  - id: openwiki-source-68d9ee48a45606ffbb50d15a
    resource: repo://modules/cli/pkg/cmd/runsPrepareLocal.go
  - id: openwiki-source-b5997bdd9357431ca984bd0b
    resource: repo://modules/cli/pkg/cmd/runsReset.go
  - id: openwiki-source-7134f0a9d31280b3e82b9a4a
    resource: repo://modules/cli/pkg/cmd/runsSubmit.go
  - id: openwiki-source-2963b44feb101f45f1bfaeee
    resource: repo://modules/cli/pkg/cmd/runsSubmitLocal.go
  - id: openwiki-source-bdb2f154639f19cf57448707
    resource: repo://modules/cli/pkg/cmd/secretsDelete.go
  - id: openwiki-source-2d12af95b610105ba4f041c5
    resource: repo://modules/cli/pkg/cmd/secretsGet.go
  - id: openwiki-source-fc0656e9ac97f830cb4d9f81
    resource: repo://modules/cli/pkg/cmd/secretsSet.go
  - id: openwiki-source-c346f06906f8d5cfabc6c2d1
    resource: repo://modules/cli/pkg/cmd/streamsDelete.go
  - id: openwiki-source-ae9df0d03b602b848c6d73a5
    resource: repo://modules/cli/pkg/cmd/streamsGet.go
  - id: openwiki-source-cc9f7a27bdb07cd3debf2713
    resource: repo://modules/cli/pkg/cmd/streamsSet.go
  - id: openwiki-source-ac1a1aed2bb6aa875acf9114
    resource: repo://modules/cli/pkg/cmd/tagsDelete.go
  - id: openwiki-source-783cc92ff06e80c45fc2f772
    resource: repo://modules/cli/pkg/cmd/tagsGet.go
  - id: openwiki-source-ee63330b177ca997a5999fa6
    resource: repo://modules/cli/pkg/cmd/tagsSet.go
  - id: openwiki-source-0040f95187f5f5ed220d2efb
    resource: repo://modules/cli/pkg/cmd/usersSet.go
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Using galasactl CLI

The `galasactl` command-line tool is the primary interface for interacting with Galasa ecosystems and running tests. This guide covers common workflows including initial setup, running tests, authentication, and managing ecosystem resources.

## Initial Setup

### Installing galasactl

Download the appropriate binary for your platform from:

- **Stable releases**: https://github.com/galasa-dev/galasa/releases
- **Development builds**: https://development.galasa.dev/main/binary/cli/

Available binaries:
- `galasactl-darwin-x86_64` (macOS Intel)
- `galasactl-darwin-arm64` (macOS Apple Silicon)
- `galasactl-linux-x86_64` (Linux x86_64)
- `galasactl-linux-s390x` (Linux s390x)
- `galasactl-windows-x86_64.exe` (Windows)

Make the binary executable and add it to your system PATH.

Alternatively, use the Docker image:

```bash
docker pull ghcr.io/galasa-dev/galasactl-x86_64:main
docker run ghcr.io/galasa-dev/galasactl-x86_64:main galasactl --version
```

### Initializing GALASA_HOME

Before using `galasactl`, initialize your Galasa home directory:

```bash
galasactl local init
```

This command creates the `${HOME}/.galasa` directory (or the directory specified by `GALASA_HOME`) and sets up essential files:

- `bootstrap.properties` - Bootstrap configuration for framework initialization
- `galasactl.properties` - CLI configuration including personal access token
- `cps.properties` - Configuration Property Store properties for local tests
- `credentials.properties` - Local credentials store for test secrets
- `dss.properties` - Dynamic Status Store configuration
- `overrides.properties` - Property overrides for test execution

To use development/bleeding-edge Galasa versions, add the `--development` flag:

```bash
galasactl local init --development
```

This configures the CLI to use snapshot Maven repositories and creates `${HOME}/.m2/settings.xml` if it doesn't exist.

### Environment Variables

The CLI uses three key environment variables:

**GALASA_HOME**: Directory for Galasa configuration and local test results

```bash
export GALASA_HOME="${HOME}/.galasa"
```

Can be overridden per-command with `--galasahome`:

```bash
galasactl --galasahome /custom/path local init
```

**GALASA_BOOTSTRAP**: Location of bootstrap configuration file or URL

```bash
export GALASA_BOOTSTRAP="http://galasa.example.com/api/bootstrap"
# or
export GALASA_BOOTSTRAP="file://${HOME}/.galasa/bootstrap.properties"
```

Can be overridden per-command with `--bootstrap`:

```bash
galasactl runs get --bootstrap http://ecosystem.example.com/api/bootstrap
```

**GALASA_TOKEN**: Personal access token for ecosystem authentication

```bash
export GALASA_TOKEN="your-personal-access-token"
```

Alternatively, store the token in `${GALASA_HOME}/galasactl.properties`:

```properties
GALASA_TOKEN=your-personal-access-token
```

## Authentication Workflows

### Logging In to an Ecosystem

Before interacting with a Galasa ecosystem, authenticate using your personal access token:

```bash
galasactl auth login
```

This command:
1. Reads your personal access token from `GALASA_TOKEN` or `galasactl.properties`
2. Authenticates with the ecosystem specified by `GALASA_BOOTSTRAP`
3. Creates `bearer-token.json` in `GALASA_HOME` containing a JWT bearer token

The bearer token is automatically used for subsequent commands. If the token expires, `galasactl` automatically re-authenticates.

### Creating a Personal Access Token

Personal access tokens are created through the Galasa web UI, not through the CLI. Steps:

1. Access your ecosystem's web interface
2. Navigate to the tokens/access management section
3. Create a new personal access token
4. Copy the token and store it in `GALASA_TOKEN` environment variable or `galasactl.properties`

### Managing Personal Access Tokens

List all tokens for the authenticated user:

```bash
galasactl auth tokens get
```

Example output:

```
tokenid                   created(YYYY-MM-DD) expiry(YYYY-MM-DD) user     description
098234980123-1283182389   2023-12-03          2024-03-03         mcobbett Laptop access
8218971d287s1-dhj32er2323 2024-03-03          2024-10-03         mcobbett CI/CD automation
87a6sd87ahq2-2y8hqwdjj273 2023-08-04          2023-12-04         savvas   VSCode access

Total:3
```

Revoke a compromised or lost token:

```bash
galasactl auth tokens delete --tokenid 098234980123-1283182389
```

### Logging Out

To invalidate the cached bearer token:

```bash
galasactl auth logout
```

After logging out, subsequent commands requiring authentication will automatically attempt to re-authenticate using your personal access token.

## Running Tests Locally

### Basic Local Test Execution

Run tests in a local JVM without ecosystem infrastructure:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log -
```

**Key parameters:**
- `--obr` - Maven coordinates of the OBR (OSGi Bundle Repository) containing test bundles
- `--class` - Test class in format `osgi-bundle-id/fully-qualified-class-name`
- `--log -` - Output logs to stderr; use `--log filename.txt` for file logging

### Running Specific Test Methods

To run only selected methods from a test class:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --methods testCreateAccount \
  --methods testTransfer \
  --log -
```

Method names must start with a letter, can contain letters, numbers, and underscores, and must not be Java reserved keywords.

### Running Gherkin Feature Files

Execute Gherkin-based behavior-driven tests:

```bash
galasactl runs submit local \
  --gherkin file:///path/to/test.feature \
  --log -
```

### Running Multiple Test Classes

Submit multiple test classes in a single command:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee \
  --throttle 5 \
  --log -
```

The `--throttle` parameter controls parallel execution (e.g., `--throttle 1` for sequential, higher values for parallel).

### Running Tests Offline

When network access to Maven repositories is unavailable, use offline mode:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --offline \
  --log -
```

The `--offline` flag prevents the JVM from contacting remote repositories. All required bundles must already exist in the local Maven cache (`${HOME}/.m2/repository`).

Before running offline, pre-fetch dependencies:

```bash
galasactl runs prepare local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --log -
```

### Configuring Maven Repositories

Specify custom local or remote Maven repositories:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --localMaven file:///custom/maven/repo \
  --remoteMaven https://custom-maven-repo.example.com/maven2 \
  --log -
```

**Default values:**
- `--localMaven`: `file://${HOME}/.m2/repository`
- `--remoteMaven`: `https://repo.maven.apache.org/maven2`

### Using Property Overrides

Override configuration properties for specific test runs:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --override zos.default.lpar=MYLPAR \
  --override zos.default.cluster=MYCLUSTER \
  --log -
```

Or provide overrides from a file:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --overridefile ${HOME}/.galasa/my-overrides.properties \
  --log -
```

### Debugging Local Tests

Enable debug mode to attach a Java debugger:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --debug \
  --debugMode listen \
  --debugPort 2970 \
  --log -
```

**Debug parameters:**
- `--debug` - Enable debug mode
- `--debugMode` - `listen` (JVM waits for debugger) or `attach` (JVM connects to debugger)
- `--debugPort` - Port for debugger connection (default: 2970)

Configure your IDE to connect to the specified port. See the CLI documentation for IDE-specific instructions (VSCode, IntelliJ, Eclipse).

## Submitting Tests to an Ecosystem

### Basic Remote Test Submission

Submit tests to a Galasa ecosystem for execution:

```bash
galasactl runs submit \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log -
```

The ecosystem uses its configured test catalog and OBRs to locate and execute the test.

### Submitting Multiple Tests

Submit multiple test classes:

```bash
galasactl runs submit \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --class dev.galasa.example.banking.payee/dev.galasa.example.banking.payee.TestPayee \
  --log -
```

### Using Test Portfolios

Prepare a test portfolio for batch execution:

```bash
galasactl runs prepare \
  --portfolio test-portfolio.yaml \
  --stream inttests \
  --package dev.galasa.example.banking \
  --override zos.default.lpar=MYLPAR
```

Submit the portfolio:

```bash
galasactl runs submit \
  --portfolio test-portfolio.yaml \
  --poll 5 \
  --progress 1 \
  --throttle 10 \
  --log -
```

**Portfolio parameters:**
- `--poll` - Seconds between status checks
- `--progress` - Interval for progress reporting
- `--throttle` - Maximum concurrent test runs

### Building Portfolios from Multiple Streams

Combine tests from different streams:

```bash
galasactl runs prepare \
  --portfolio test-portfolio.yaml \
  --stream inttests \
  --package dev.galasa.example.banking.account

galasactl runs prepare \
  --portfolio test-portfolio.yaml \
  --append \
  --stream regression \
  --package dev.galasa.example.banking.payee
```

### Associating Runs with a User

When submitting from automation tools, associate runs with the triggering user:

```bash
galasactl runs submit \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --user developer@example.com \
  --log -
```

The `requestor` field is set to the personal access token owner, while `user` is set to the specified value.

## Managing Test Runs

### Querying Test Runs

Retrieve information about a test run:

```bash
galasactl runs get --name C1234
```

Specify the output format:

```bash
galasactl runs get --name C1234 --format details
```

Supported formats: `summary`, `details`, `raw`

To see all available formatters:

```bash
galasactl runs get --name C1234 --format badFormatterName
```

### Downloading Test Artifacts

Download all artifacts for a test run:

```bash
galasactl runs download --name C1234
```

Artifacts are stored in a directory named after the run (e.g., `./C1234/`) in the current working directory.

Specify a custom destination:

```bash
galasactl runs download --name C1234 --destination /custom/path
```

Overwrite existing artifacts:

```bash
galasactl runs download --name C1234 --force
```

### Resetting a Stuck Test Run

If a test is stuck or looping, reset it to requeue:

```bash
galasactl runs reset --name C1234
```

The server accepts the reset request asynchronously; the command does not wait for completion.

### Canceling a Test Run

Cancel a running test:

```bash
galasactl runs cancel --name C1234
```

This stops test execution but preserves any RAS (Result Archive Store) data already recorded.

### Deleting a Test Run

Delete a test run and all associated artifacts from the RAS:

```bash
galasactl runs delete --name C1234
```

## Managing Configuration Properties

### Retrieving Properties

List all properties in a namespace:

```bash
galasactl properties get --namespace framework
```

Filter properties by prefix, infix, or suffix:

```bash
galasactl properties get --namespace framework --prefix test
galasactl properties get --namespace framework --infix galasa --suffix stream
galasactl properties get --namespace framework --prefix test --infix galasa --suffix stream
```

Get a specific property by name:

```bash
galasactl properties get --namespace framework --name propertyName
```

Output formats:

```bash
# Summary format (default)
galasactl properties get --namespace framework --format summary

# Raw format (pipe-delimited)
galasactl properties get --namespace framework --format raw

# YAML format
galasactl properties get --namespace framework --format yaml
```

Example YAML output:

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: propertyName
data:
    value: propertyValue
```

### Setting Properties

Create or update a property:

```bash
galasactl properties set --namespace framework --name propertyName --value propertyValue
```

If the property exists, its value is updated. If it doesn't exist, it is created.

### Deleting Properties

Remove a property from a namespace:

```bash
galasactl properties delete --namespace framework --name propertyName
```

### Listing Namespaces

View all CPS namespaces:

```bash
galasactl properties namespaces get
```

Output formats: `summary` (default), `raw`

## Managing Resources

Resources in Galasa are ecosystem objects like properties, secrets, and other configuration items that can be defined in YAML files.

### Applying Resources

Create or update resources from a YAML file:

```bash
galasactl resources apply -f my_resources.yaml
```

For each resource in the file, `apply` creates it if it doesn't exist or updates it if it does.

### Creating Resources

Create new resources (fails if they already exist):

```bash
galasactl resources create -f my_resources.yaml
```

### Updating Resources

Update existing resources (fails if they don't exist):

```bash
galasactl resources update -f my_resources.yaml
```

### Deleting Resources

Remove resources defined in a YAML file:

```bash
galasactl resources delete -f my_resources.yaml
```

A compiled list of errors is returned if any operation fails for any resource.

## Managing Secrets

Secrets are stored in the Galasa ecosystem's credentials store and can be referenced by tests for authentication and secure operations.

### Retrieving Secrets

List all secrets:

```bash
galasactl secrets get
```

Get a specific secret:

```bash
galasactl secrets get --name SYSTEM1
```

Display secrets in YAML format:

```bash
galasactl secrets get --name SYSTEM1 --format yaml
```

Supported formats: `summary` (default), `yaml`

### Creating Username/Password Secrets

Create a UsernamePassword secret:

```bash
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password"
```

Create a UsernameToken secret:

```bash
galasactl secrets set --name SYSTEM1 --username "my-username" --token "my-token"
```

Create a Token-only secret:

```bash
galasactl secrets set --name SYSTEM1 --token "my-token"
```

Create a Username-only secret:

```bash
galasactl secrets set --name SYSTEM1 --username "my-username"
```

### Using Base64-Encoded Credentials

Provide base64-encoded credentials:

```bash
galasactl secrets set --name SYSTEM1 --base64-username "bXktdXNlcm5hbWU=" --base64-password "bXktcGFzc3dvcmQ="
```

Mix encoded and plain-text credentials:

```bash
galasactl secrets set --name SYSTEM1 --username "my-username" --base64-token "bXktdG9rZW4="
```

### Creating Keystore Secrets

Store Java keystores (JKS or PKCS12) for use in tests:

```bash
galasactl secrets set --name MYKEYSTORE \
  --keystore-file /path/to/keystore.jks \
  --password "keystore-password" \
  --keystore-type JKS
```

For PKCS12 keystores:

```bash
galasactl secrets set --name MYKEYSTORE \
  --keystore-file /path/to/keystore.p12 \
  --password "keystore-password" \
  --keystore-type PKCS12
```

The keystore file is base64-encoded before storage. Supported types: `JKS`, `PKCS12`

Create an unprotected keystore (no integrity-check password):

```bash
galasactl secrets set --name MYKEYSTORE \
  --keystore-file /path/to/keystore.p12 \
  --password "" \
  --keystore-type PKCS12
```

Provide base64-encoded keystore data directly:

```bash
galasactl secrets set --name MYKEYSTORE \
  --base64-keystore-encoded "dGVzdC1rZXlzdG9yZS1kYXRh" \
  --password "keystore-password" \
  --keystore-type JKS
```

Update only the keystore type:

```bash
galasactl secrets set --name MYKEYSTORE --keystore-type PKCS12
```

### Creating Opaque Secrets

Opaque secrets store arbitrary binary data like license JAR files:

```bash
galasactl secrets set --name LICENSE_JAR --secret-file /path/to/my_license.jar
```

The file contents are base64-encoded before storage. Maximum raw file size: **760 KB** (expands to ~1,013 KB when base64-encoded).

Provide base64-encoded data directly:

```bash
galasactl secrets set --name LICENSE_JAR --base64-secret "UEsDBAAAAAA..."
```

Update an opaque secret:

```bash
galasactl secrets set --name LICENSE_JAR --secret-file /path/to/new_license.jar
```

### Changing Secret Types

Change an existing secret's type:

```bash
# Create a UsernamePassword secret
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password"

# Change to a Token secret
galasactl secrets set --name SYSTEM1 --token "my-token" --type Token
```

All credentials for the new type must be provided when changing types.

### Deleting Secrets

Remove a secret from the credentials store:

```bash
galasactl secrets delete --name SYSTEM1
```

## Managing Streams

Test streams organize tests into logical groups for portfolio execution.

### Listing Streams

Get all streams:

```bash
galasactl streams get
```

Get a specific stream:

```bash
galasactl streams get --name inttests
```

Display streams in YAML format:

```bash
galasactl streams get --format yaml
```

Supported formats: `summary` (default), `yaml`

### Creating or Updating Streams

Create or update a stream:

```bash
galasactl streams set --name inttests --description "Integration test suite" --obr mvn:dev.galasa.example/dev.galasa.example.obr/0.0.1/obr
```

If the stream exists, it is updated; otherwise, it is created.

### Deleting Streams

Remove a stream:

```bash
galasactl streams delete --name inttests
```

## Managing Tags

Tags annotate test runs with metadata for filtering and reporting.

### Listing Tags

Get all tags:

```bash
galasactl tags get
```

Get a specific tag:

```bash
galasactl tags get --name core-regression
```

Display tags in YAML format:

```bash
galasactl tags get --format yaml
```

Supported formats: `summary` (default), `yaml`

### Creating or Updating Tags

Create or update a tag:

```bash
galasactl tags set --name core-regression --description "Core regression tests" --priority 10
```

Higher priority values indicate greater importance.

### Deleting Tags

Remove a tag:

```bash
galasactl tags delete --name core-regression
```

## Managing Monitors

Monitors perform resource cleanup and maintenance tasks in the ecosystem.

### Listing Monitors

Get all monitors:

```bash
galasactl monitors get
```

Get a specific monitor:

```bash
galasactl monitors get --name myCustomMonitor
```

Display monitors in different formats:

```bash
galasactl monitors get --format summary
```

Supported formats: `summary` (default), and other formats shown via `galasactl monitors get --help`

### Enabling or Disabling Monitors

Enable a monitor:

```bash
galasactl monitors set --name myCustomMonitor --is-enabled true
```

Disable a monitor:

```bash
galasactl monitors set --name myCustomMonitor --is-enabled false
```

## Resource Cleanup

### Cleaning Up Local Resources

Run resource management providers to clean up resources left by local tests:

```bash
galasactl runs cleanup local \
  --obr my.company.group/my.company.group.obr/0.0.1/obr \
  --remoteMaven https://my-company/maven-repo \
  --includes-pattern "my.company.*" \
  --excludes-pattern "*MyUnwantedCleanupProviderClass" \
  --log -
```

**Cleanup parameters:**
- `--obr` - OBRs containing resource management provider services (can be specified multiple times)
- `--remoteMaven` - Remote Maven repositories (can be specified multiple times)
- `--includes-pattern` - Glob pattern to match provider class names (can be specified multiple times)
- `--excludes-pattern` - Glob pattern to exclude provider class names (can be specified multiple times)

**Glob pattern syntax:**
- `*` - Matches zero or more characters
- `?` - Matches exactly one character

Examples:
- `dev.galasa*` matches `dev.galasa.core.CoreResourceMonitorClass`
- `*MyResourceMonitorClass` matches `my.company.monitors.MyResourceMonitorClass`

Cleanup also supports debug flags (`--debug`, `--debugMode`, `--debugPort`) similar to `runs submit local`.

## Managing Users and Roles

### Listing Users

Get all users in the ecosystem:

```bash
galasactl users get
```

Example output:

```
login-id               role   web-last-login(UTC) rest-api-last-login(UTC)
user.one@mydomain.com  tester 2025-01-13 15:33
Jade@mydomain.com      admin  2025-01-13 15:33    2025-01-16 10:47
mikec@mydomain.com     admin  2025-01-13 15:33    2025-01-16 16:20

Total:3
```

Get a specific user:

```bash
galasactl users get --login-id mikec@mydomain.com
```

### Updating User Roles

Change a user's role (requires admin privileges):

```bash
galasactl users set --login-id user.one@mydomain.com --role tester
```

Change a user's priority:

```bash
galasactl users set --login-id user.one@mydomain.com --priority 100
```

### Listing Roles

View available roles in the ecosystem:

```bash
galasactl roles get
```

Example output:

```
name        description
admin       Administrator access
deactivated User has no access
tester      Test developer and runner

Total:3
```

Get details about a specific role in YAML format:

```bash
galasactl roles get --name admin --format yaml
```

Example output:

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaRole
metadata:
    id: "2"
    name: admin
    description: Administrator access
    url: http://galasa-ecosystem.example.com/rbac/roles/2
data:
    actions:
        - GENERAL_API_ACCESS
        - SECRETS_GET
        - USER_ROLE_UPDATE_ANY
```

**Note**: Roles are currently read-only and cannot be modified through the CLI.

## Creating Test Projects

`galasactl` can generate skeleton test projects to bootstrap development.

### Creating a Basic Test Project

Create an OSGi bundle structure without an OBR:

```bash
galasactl project create --package dev.galasa.example.banking
```

Create with an OBR:

```bash
galasactl project create --package dev.galasa.example.banking --obr
```

### Creating Multi-Feature Projects

Create a project with multiple feature bundles:

```bash
galasactl project create \
  --package dev.galasa.example.banking \
  --features payee,account \
  --obr \
  --log -
```

This creates separate bundles for each feature (`dev.galasa.example.banking.payee`, `dev.galasa.example.banking.account`) and a single OBR.

### Creating Manager Projects

Create a Galasa manager project:

```bash
galasactl project create --package dev.galasa.example.docker --manager --managerName docker
```

If `--managerName` is omitted, the last part of the package name is used as the manager name.

Combine manager and test projects:

```bash
galasactl project create \
  --package dev.galasa.example \
  --manager \
  --managerName example \
  --features banking,account \
  --obr
```

This creates:
- Manager bundle (`dev.galasa.example.manager`)
- Test bundles for each feature (`dev.galasa.example.banking`, `dev.galasa.example.account`)
- Single OBR containing all bundles

### Choosing Build Systems

Generate Maven build files (default):

```bash
galasactl project create --package dev.galasa.example.banking --maven
```

Generate Gradle build files:

```bash
galasactl project create --package dev.galasa.example.banking --gradle
```

Generate both:

```bash
galasactl project create --package dev.galasa.example.banking --maven --gradle --obr
```

Build commands:
- Maven: `mvn clean install`
- Gradle: `gradle build publishToMavenLocal`

Use development dependencies:

```bash
galasactl project create --package dev.galasa.example.banking --development --obr
```

## Using Shared Remote Configuration

Run local tests while using shared configuration from a remote ecosystem's CPS:

Add to `${GALASA_HOME}/bootstrap.properties`:

```properties
# Use REST API to get configuration from remote server
framework.config.store=galasacps://galasa-ecosystem.example.com/api
framework.extra.bundles=dev.galasa.cps.rest
```

Authenticate before running local tests:

```bash
galasactl auth login
```

Now local tests can access the remote CPS:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log -
```

## Common Flags

Many commands support these common flags:

**Logging:**
- `--log -` - Send logs to stderr (console)
- `--log filename.txt` - Send logs to a file
- Omit `--log` for no logging

**Galasa Home:**
- `--galasahome /custom/path` - Override `GALASA_HOME` for this command

**Bootstrap:**
- `--bootstrap http://ecosystem.example.com/api/bootstrap` - Override `GALASA_BOOTSTRAP` for this command

**Output Format:**
- `--format summary` - Human-readable output (default for most commands)
- `--format yaml` - YAML output
- `--format raw` - Raw/pipe-delimited output
- `--format details` - Detailed output (runs get)

**Version:**

```bash
galasactl --version
```

Example output:

```
galasactl version 0.20.0-alpha-2305ba574524af5cd0fba59de18411582f470de5
```

## Error Handling

The CLI provides clear error messages and exit codes for automation:

- **Exit code 0**: Command succeeded
- **Non-zero exit codes**: Command failed

For detailed error information, enable logging:

```bash
galasactl runs submit local \
  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
  --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
  --log /tmp/galasactl.log
```

See the [error reference](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/generated/errors-list.md) for a complete list of CLI error codes.

## Integration with CI/CD

The CLI is designed for automation:

**Jenkins example:**

```groovy
stage('Run Galasa Tests') {
    steps {
        sh '''
            galasactl auth login
            galasactl runs submit \
              --class com.example.TestClass \
              --user ${BUILD_USER_ID} \
              --log - || exit 1
        '''
    }
}
```

**GitLab CI example:**

```yaml
test:
  script:
    - export GALASA_TOKEN=${GALASA_PAT}
    - galasactl auth login
    - galasactl runs submit --class com.example.TestClass --log -
```

**Key automation features:**
- Standard exit codes
- Machine-readable output (JSON/YAML)
- Non-interactive execution
- Environment variable configuration

## Known Limitations

### DNS Resolution Issues

Go programs can struggle with DNS resolution over VPNs. If `galasactl` cannot resolve bootstrap URLs that work in browsers or `curl`:

Add the host to `/etc/hosts` to bypass DNS:

```
192.168.1.100  galasa-ecosystem.example.com
```

This avoids DNS resolution and allows the CLI to connect.

## Reference Documentation

For complete command syntax and parameters:

- [Full CLI syntax reference](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/generated/galasactl.md)
- [Error code reference](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/generated/errors-list.md)
- [Debugging in VSCode](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/vscode/debug_in_vscode.md)
- [Debugging in IntelliJ](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/intellij/debug_in_intellij.md)
- [Debugging in Eclipse](https://github.com/galasa-dev/galasa/blob/main/modules/cli/docs/eclipse/debug_in_eclipse.md)
- [Gherkin support](https://github.com/galasa-dev/galasa/blob/main/modules/cli/gherkin-docs.md)
