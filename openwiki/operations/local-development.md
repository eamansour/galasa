---
type: operations-guide
title: Local Development Environment Setup
description: Complete guide to setting up a local Galasa development environment including GALASA_HOME structure, property files, local storage backends, and debugging configuration.
tags: [local-development, environment-setup, galasa-home, properties, storage-backends, debugging]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-74c80f790cd0bc057e9a4e99
    resource: repo://.vscode/settings.json
  - id: openwiki-source-029cd05e3bc8b747d91f21f8
    resource: repo://docs/content/docs/cli-command-reference/initialising-home-folder.md
  - id: openwiki-source-a670f861c6a03eadaccf0817
    resource: repo://docs/content/docs/cli-command-reference/runs-local-debug.md
  - id: openwiki-source-acfe7f006489f99fbd28d9ac
    resource: repo://modules/cli/pkg/cmd/localInit.go
  - id: openwiki-source-692f131ba8dc19912b77cea5
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/bootstrap.properties
  - id: openwiki-source-d915ad8d87c9189348a85468
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/cps.properties
  - id: openwiki-source-822b494eb6ca45c568a156b3
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/credentials.properties
  - id: openwiki-source-c612d6aa5730c6b5a31e6eb9
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/dss.properties
  - id: openwiki-source-924eebf0c78d79ffc6832cd8
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/galasactl.properties
  - id: openwiki-source-dc628654bb4616dbdcfea7db
    resource: repo://modules/cli/pkg/embedded/templates/galasahome/overrides.properties
  - id: openwiki-source-440e982e0bd602128189d73b
    resource: repo://modules/cli/pkg/utils/homeFolderCreator.go
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-e453bb7897333155608d7bb2
    resource: repo://modules/cli/temp/home/ras/L1/artifacts.json
  - id: openwiki-source-e5bcf710dbb3a90a111ddacc
    resource: repo://modules/cli/temp/home/ras/L1/run.log
  - id: openwiki-source-8e285b9482179bb3ff00560d
    resource: repo://modules/cli/temp/home/ras/L1/structure.json
  - id: openwiki-source-f888fdbbee1c3e79c1e1b105
    resource: repo://modules/framework/dev-instructions.md
  - id: openwiki-source-9904ee682e3d8cb87c6ba678
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/FrameworkInitialisation.java
  - id: openwiki-source-b05cea7fa882df5dcf906704
    resource: repo://modules/framework/galasa-parent/galasa-boot/src/main/java/dev/galasa/boot/Launcher.java
  - id: openwiki-source-2f0af65214aca74a6cff299a
    resource: repo://modules/framework/test-api-locally.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Local Development Environment Setup

This guide describes how to set up and configure a local Galasa development environment for running tests in a local JVM without a deployed ecosystem. It covers GALASA_HOME initialization, property file configuration, local storage backends, environment variables, and IDE integration.

## Overview

A local Galasa environment consists of:

- **GALASA_HOME** - Directory containing property files and local storage
- **Property files** - Configuration files controlling test execution and storage
- **Local storage backends** - File-based CPS, DSS, RAS, and credentials stores
- **Maven cache** - Local repository for Galasa bundles and dependencies
- **Environment variables** - Variables controlling framework and CLI behavior

Local development is suitable for:
- Test development and debugging
- Running tests without ecosystem infrastructure
- Offline test execution (after dependency preparation)
- Learning Galasa before deploying an ecosystem

**Limitations**: Local runs do not benefit from ecosystem features like centralized resource management, contention arbitration, or shared configuration.

## Initializing GALASA_HOME

### Using galasactl local init

The `galasactl local init` command creates the necessary directory structure and property files:

```bash
galasactl local init
```

This command:
1. Creates the GALASA_HOME directory (defaults to `${HOME}/.galasa`)
2. Generates property file templates
3. Creates the Maven settings file (`~/.m2/settings.xml`)
4. Installs the Galasa boot JAR for local test execution

**Development mode** - Use the `--development` flag to configure bleeding-edge Galasa repositories:

```bash
galasactl local init --development
```

This adds development Maven repositories to `settings.xml` for accessing the latest Galasa builds.

### GALASA_HOME Structure

After initialization, the GALASA_HOME directory contains:

```
${HOME}/.galasa/
├── bootstrap.properties      # Framework boot configuration
├── cps.properties           # Configuration properties for managers
├── credentials.properties   # Test credentials storage
├── dss.properties          # Dynamic status store (resource tracking)
├── overrides.properties    # Test-specific property overrides
├── galasactl.properties    # CLI configuration (personal access token)
├── lib/                    # Galasa boot JAR storage
│   └── <version>/
│       └── galasa-boot-<version>.jar
├── cache/                  # Felix OSGi framework cache
└── ras/                    # Result Archive Store (test results)
    └── L<run-number>/      # Individual test run directories
        ├── artifacts.json  # Artifact metadata
        ├── structure.json  # Test structure
        ├── run.log        # Test execution log
        └── artifacts/     # Test artifacts and files
```

**Run naming**: Local test runs are named with an `L` prefix by default (e.g., `L1`, `L2`), controlled by the `framework.request.type.LOCAL.prefix` property.

## Environment Variables

### GALASA_HOME

Controls the location of the Galasa home directory.

**Default**: `${HOME}/.galasa` (or `%USERPROFILE%/.galasa` on Windows)

**Usage**:
```bash
export GALASA_HOME=/opt/galasa
```

**Override precedence**:
1. `--galasahome` command-line flag (highest priority)
2. `GALASA_HOME` environment variable
3. Default location (lowest priority)

**Changing GALASA_HOME**: If you change this to a new location, run `galasactl local init` to create the directory structure at the new location.

### GALASA_BOOTSTRAP

Specifies the bootstrap configuration file or URL.

**Usage**:
```bash
export GALASA_BOOTSTRAP=file://${HOME}/.galasa/bootstrap.properties
# or for remote ecosystem
export GALASA_BOOTSTRAP=https://ecosystem.example.com/bootstrap
```

**Override precedence**:
1. `--bootstrap` command-line flag
2. `GALASA_BOOTSTRAP` environment variable
3. Default: `file://${GALASA_HOME}/bootstrap.properties`

### GALASA_TOKEN

Personal access token for authenticating with a Galasa ecosystem.

**Usage**:
```bash
export GALASA_TOKEN=<your-personal-access-token>
```

**Alternatively**, store the token in `${GALASA_HOME}/galasactl.properties`:
```properties
GALASA_TOKEN=<your-personal-access-token>
```

### Store Configuration Variables

These environment variables override bootstrap properties for storage backend locations:

**GALASA_CONFIG_STORE** - Configuration Property Store location
```bash
export GALASA_CONFIG_STORE="etcd:http://localhost:2379"
```

**GALASA_DYNAMICSTATUS_STORE** - Dynamic Status Store location
```bash
export GALASA_DYNAMICSTATUS_STORE="etcd:http://localhost:2379"
```

**GALASA_RESULTARCHIVE_STORE** - Result Archive Store location
```bash
export GALASA_RESULTARCHIVE_STORE="couchdb:http://localhost:5984"
```

**GALASA_CREDENTIALS_STORE** - Credentials Store location
```bash
export GALASA_CREDENTIALS_STORE="etcd:http://localhost:2379"
```

**GALASA_AUTH_STORE** - Authentication Store location
```bash
export GALASA_AUTH_STORE="couchdb:http://localhost:5984"
```

### Framework Variables

**GALASA_RAS_TOKEN** - Authentication token for RAS access (when using CouchDB)
```bash
export GALASA_RAS_TOKEN=$(echo -n "admin:password" | base64)
```

**GALASA_AUTHSTORE_TOKEN** - Authentication token for auth store access
```bash
export GALASA_AUTHSTORE_TOKEN=$(echo -n "admin:password" | base64)
```

### API Server Variables

These variables are used when running the Galasa API server locally:

**GALASA_EXTERNAL_API_URL** - External URL for the API server
```bash
export GALASA_EXTERNAL_API_URL="http://localhost:8080"
```

**GALASA_USERNAME_CLAIMS** - JWT claims to extract username from
```bash
export GALASA_USERNAME_CLAIMS="preferred_username,name,sub"
```

**GALASA_ALLOWED_ORIGINS** - CORS allowed origins
```bash
export GALASA_ALLOWED_ORIGINS="*"
```

**GALASA_DEX_ISSUER** - Dex OpenID Connect issuer URL
```bash
export GALASA_DEX_ISSUER="http://127.0.0.1:5556/dex"
```

**GALASA_DEX_GRPC_HOSTNAME** - Dex gRPC server address
```bash
export GALASA_DEX_GRPC_HOSTNAME="127.0.0.1:5557"
```

## Property Files

### bootstrap.properties

Configures the framework boot sequence and storage backend locations.

**Location**: `${GALASA_HOME}/bootstrap.properties`

**Purpose**: 
- Specifies storage backend URIs
- Lists extra bundles to load for storage implementations
- Configures local JVM launch behavior
- Sets run name prefixes

**Key properties**:

```properties
# Configuration Property Store location
# Can be overridden by GALASA_CONFIG_STORE environment variable
framework.config.store=etcd:http://127.0.0.1:2379
# or for local file-based storage:
# framework.config.store=file:///${GALASA_HOME}/cps.properties

# Dynamic Status Store location
framework.dynamicstatus.store=etcd:http://127.0.0.1:2379

# Result Archive Store location
framework.resultarchive.store=couchdb:http://127.0.0.1:5984

# Credentials Store location
framework.credentials.store=etcd:http://127.0.0.1:2379

# Authentication Store location (for ecosystem)
framework.auth.store=couchdb:http://127.0.0.1:5984

# Extra bundles to load for storage backends
framework.extra.bundles=dev.galasa.ras.couchdb,dev.galasa.cps.etcd

# Local run name prefix (default: L)
framework.request.type.LOCAL.prefix=L

# JVM launch options for local test runs
galasactl.jvm.local.launch.options=-Xmx80m

# Debug configuration
galasactl.jvm.local.launch.debug.mode=listen
galasactl.jvm.local.launch.debug.port=2970
```

**Default fallback behavior**:
- If `framework.config.store` is not set, defaults to `file://${GALASA_HOME}/cps.properties`
- If `framework.dynamicstatus.store` is not set, defaults to `file://${GALASA_HOME}/dss.properties`
- If `framework.credentials.store` is not set, defaults to `file://${GALASA_HOME}/credentials.properties`
- If `framework.resultarchive.store` is not set, defaults to `file://${GALASA_HOME}/ras`

### cps.properties

Contains configuration properties used by Galasa managers and tests.

**Location**: `${GALASA_HOME}/cps.properties`

**Purpose**: Stores configuration for test environments, manager behavior, and resource definitions when running tests locally (not in an ecosystem).

**Format**: Standard Java properties file with namespace.prefix.suffix structure.

**Common properties**:

```properties
# Framework behavior
framework.continue.on.test.failure=false

# z/OS cluster and image configuration
zos.cluster.CLUSTERA.images=IMAGEA,IMAGEB

zos.image.IMAGEA.default.hostname=dev.galasa.system1
zos.image.IMAGEA.ipv4.hostname=dev.galasa.system1
zos.image.IMAGEA.telnet.port=992
zos.image.IMAGEA.telnet.tls=true
zos.image.IMAGEA.credentials=MY_CREDENTIALS_KEY
zos.image.IMAGEA.max.slots=4

# Docker engine configuration
docker.default.engines=LOCAL
docker.engine.LOCAL.hostname=localhost
docker.engine.LOCAL.port=2375

# Gherkin terminal configuration
zos3270.gherkin.terminal.rows=24
zos3270.gherkin.terminal.columns=80
```

**Naming conventions**:
- Lowercase letters are fixed parts of property names
- UPPERCASE letters represent variable placeholders (tags)
- Example: `zos.image.IMAGEA.hostname` - where `IMAGEA` is a user-defined tag

### overrides.properties

Provides temporary property overrides that take precedence over CPS values.

**Location**: `${GALASA_HOME}/overrides.properties`

**Purpose**: Override configuration properties for specific test runs without modifying the main CPS.

**Common use cases**:
- Testing against different software versions
- Temporarily changing configuration during development
- Enabling/disabling features for specific test runs

**Example overrides**:

```properties
# Override framework behavior
framework.continue.on.test.failure=true

# Enable CPS REST caching
framework.cps.rest.cache.is.enabled=true

# Override terminal size for specific test
zos3270.gherkin.terminal.rows=30
zos3270.gherkin.terminal.columns=120

# Override target system
zos.image.PRIMARY.hostname=test.system.example.com
```

**Property lookup order**: Overrides → CPS → Defaults

### dss.properties

Tracks dynamic resource state and allocation.

**Location**: `${GALASA_HOME}/dss.properties`

**Purpose**: Stores runtime state information about resources in use by tests, including:
- Port allocations
- Resource locks
- Test run counters
- Temporary state data

**Format**: Key-value pairs written and read by managers.

**Important notes**:
- This file grows as tests execute
- If tests fail unexpectedly, resources may appear "in use" when actually free
- **Safe to delete when no tests are running** - resets all counters and resource state
- Deleting this file also resets the test run number counter

**Example content** (managed by framework, not manually edited):
```properties
dss.framework.run.name=L5
dss.resource.lock.port.3270.001=ALLOCATED
```

### credentials.properties

Stores test credentials for local execution.

**Location**: `${GALASA_HOME}/credentials.properties`

**Purpose**: Provides usernames, passwords, and tokens that tests retrieve using the Galasa credentials API.

**Format**: Java properties file with required prefixes and suffixes.

**Property structure**:
- All properties must start with `secure.credentials.`
- Must end with `.username`, `.password`, or `.token`

**Example credentials**:

```properties
# SIMBANK credentials
secure.credentials.SIMBANK.username=IBMUSER
secure.credentials.SIMBANK.password=SYS1

# API token
secure.credentials.MYAPI.token=abc123def456

# Database credentials
secure.credentials.DB2.username=galasa
secure.credentials.DB2.password=secret123
```

**Accessing in tests**:
```java
@Credentials(credentialsId = "SIMBANK")
public ICredentials simBankCreds;

// Use in test methods
String username = simBankCreds.getUsername();
String password = simBankCreds.getPassword();
```

### galasactl.properties

Stores CLI-specific configuration.

**Location**: `${GALASA_HOME}/galasactl.properties`

**Purpose**: Stores personal access token for ecosystem authentication.

**Content**:

```properties
# Personal access token for ecosystem authentication
GALASA_TOKEN=<your-personal-access-token>
```

**Token creation**: Obtain a token from the Galasa web UI by creating a personal access token.

**Alternative**: Set `GALASA_TOKEN` as an environment variable instead of storing in this file.

## Local Storage Backends

### File-Based CPS

The default CPS backend for local development uses a simple properties file.

**Configuration**: Automatically used when no explicit `framework.config.store` is set in bootstrap.properties.

**Characteristics**:
- Reads from `${GALASA_HOME}/cps.properties`
- Simple key-value storage
- No distribution or high availability
- Suitable for local test development

### File-Based DSS

The default DSS backend stores resource state in a local properties file.

**Configuration**: Automatically used when no explicit `framework.dynamicstatus.store` is set.

**Location**: `${GALASA_HOME}/dss.properties`

**Characteristics**:
- Stores resource allocation state
- Tracks port usage, locks, and counters
- No coordination across multiple test runners
- Can be deleted to reset state

### File-Based Credentials Store

The default credentials store uses the local credentials.properties file.

**Configuration**: Automatically used when no explicit `framework.credentials.store` is set.

**Security considerations**:
- Credentials are stored in plaintext
- File permissions should restrict access (e.g., `chmod 600 credentials.properties`)
- Not suitable for production use
- Consider OS credential store integration for better security

### Local RAS

The local Result Archive Store stores test results in a directory structure.

**Location**: `${GALASA_HOME}/ras/`

**Structure**:
```
ras/
├── L1/                    # First local run
│   ├── run.log           # Test execution log
│   ├── structure.json    # Test structure metadata
│   ├── artifacts.json    # Artifact index
│   └── artifacts/        # Test artifacts
│       └── framework/    # Framework artifacts
│           └── overrides.properties
├── L2/                    # Second local run
│   └── ...
└── L3/                    # Third local run
    └── ...
```

**Key files in each run**:
- `run.log` - Complete test execution log
- `structure.json` - Test class and method structure
- `artifacts.json` - Index of stored artifacts
- `artifacts/` - Directory containing test-generated files

**Viewing results**: Use `galasactl runs get` to query local run results:
```bash
galasactl runs get --age 1d
```

## Connecting to External Storage Backends

For testing API server functionality or ecosystem-like behavior locally, you can run external storage services in Docker.

### etcd for CPS/DSS

**Setup**:
```bash
docker pull bitnami/etcd:3.3.27-debian-11-r100
docker network create app-tier --driver bridge

docker run -d --name etcd \
    --network app-tier \
    --publish 2379:2379 \
    --publish 2380:2380 \
    --env ALLOW_NONE_AUTHENTICATION=yes \
    --env ETCD_ADVERTISE_CLIENT_URLS=http://etcd:2379 \
    bitnami/etcd:3.3.27-debian-11-r100
```

**Configuration**:
```properties
# In bootstrap.properties
framework.config.store=etcd:http://127.0.0.1:2379
framework.dynamicstatus.store=etcd:http://127.0.0.1:2379
framework.extra.bundles=dev.galasa.cps.etcd
```

**Health check**:
```bash
curl http://localhost:2379/version
```

### CouchDB for RAS/Auth Store

**Setup**:
```bash
docker pull couchdb:3.3.3

export COUCHDB_USER="admin"
export COUCHDB_PASSWORD=$(openssl rand -base64 10)
echo "CouchDB password: $COUCHDB_PASSWORD"

docker run -p 5984:5984 -d \
    -e COUCHDB_USER=${COUCHDB_USER} \
    -e COUCHDB_PASSWORD=${COUCHDB_PASSWORD} \
    --name couchdb \
    couchdb:3.3.3
```

**First-time setup**: Open http://localhost:5984/_utils and configure a single-node setup.

**Configuration**:
```properties
# In bootstrap.properties
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
framework.auth.store=couchdb:http://127.0.0.1:5984
framework.extra.bundles=dev.galasa.ras.couchdb,dev.galasa.auth.couchdb
```

**Environment variables for authentication**:
```bash
export COUCHDB_TOKEN=$(echo -n "${COUCHDB_USER}:${COUCHDB_PASSWORD}" | base64)
export GALASA_RAS_TOKEN=${COUCHDB_TOKEN}
export GALASA_AUTHSTORE_TOKEN=${COUCHDB_TOKEN}
```

**Health check**:
```bash
curl -H "Authorization: Basic $COUCHDB_TOKEN" http://localhost:5984/_all_dbs
```

### Dex for Authentication

Required when running the Galasa API server locally with authentication.

**Setup**:
```bash
docker pull ghcr.io/dexidp/dex:v2.38.0

# Generate admin password
export DEX_ADMIN_PASSWORD=$(echo password | htpasswd -BinC 10 admin | cut -d: -f2)

# Create configuration
mkdir -p ~/.dex
cat << EOF > ~/.dex/config-dev.yaml
issuer: http://127.0.0.1:5556/dex

storage:
  type: sqlite3
  config:
    file: var/dex/dex.db

web:
  http: 0.0.0.0:5556

grpc:
  addr: 0.0.0.0:5557
  reflection: true

staticClients:
- id: galasa-webui
  redirectURIs:
  - 'http://localhost:8080/auth/callback'
  name: 'Galasa Web UI'
  secret: example-webui-client-secret

staticPasswords:
- email: "admin@example.com"
  hash: "${DEX_ADMIN_PASSWORD}"
  username: "admin"
  userID: "08a8684b-db88-4b73-90a9-3cd1661f5466"
EOF

# Launch Dex
docker run -d \
    -v ~/.dex/config-dev.yaml:/etc/dex/config.docker.yaml \
    -p 5556:5556 -p 5558:5558 -p 5557:5557 \
    --name dex \
    ghcr.io/dexidp/dex:v2.38.0
```

**Environment variables**:
```bash
export GALASA_DEX_ISSUER="http://127.0.0.1:5556/dex"
export GALASA_DEX_GRPC_HOSTNAME="127.0.0.1:5557"
```

## Running Tests Locally

### Basic Local Test Execution

```bash
galasactl runs submit local \
    --log - \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

**Required flags**:
- `--obr` - Maven coordinates of the OSGi Bundle Repository containing test bundles
- `--class` - Test class to run (format: `<bundle-id>/<fully-qualified-class-name>`)

**Optional flags**:
- `--log -` - Send debug output to console
- `--methods <method-name>` - Run specific test method(s)
- `--override <property>=<value>` - Override specific properties
- `--overridefile <path>` - Load overrides from a file
- `--bootstrap <url>` - Specify bootstrap file location
- `--galasahome <path>` - Override GALASA_HOME location

### Running Specific Test Methods

```bash
galasactl runs submit local \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount \
    --methods testCreateAccount,testAccountBalance
```

### Running Gherkin Tests

```bash
galasactl runs submit local \
    --log - \
    --gherkin file:///path/to/test.feature
```

### Offline Test Execution

Prepare dependencies while online:
```bash
galasactl runs prepare local \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr
```

Run tests offline:
```bash
galasactl runs submit local \
    --offline \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

## Debugging Local Tests

### Debug Mode Configuration

Tests can be run in debug mode to attach a Java debugger.

**Launch test in debug mode**:
```bash
galasactl runs submit local \
    --debug \
    --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr \
    --class dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount
```

**Debug configuration**:
- **Default port**: 2970
- **Default mode**: listen (test JVM waits for debugger to connect)

**Override debug port**:
```bash
galasactl runs submit local --debug --debugPort 2971 ...
```

**Override debug mode**:
```bash
galasactl runs submit local --debug --debugMode attach ...
```

**Bootstrap property configuration**:
```properties
# In bootstrap.properties
galasactl.jvm.local.launch.debug.port=2971
galasactl.jvm.local.launch.debug.mode=listen
```

### Connection Modes

**Listen mode** (default):
- Test JVM opens debug port and waits
- IDE debugger connects to the port
- Launch test first, then attach debugger

**Attach mode**:
- IDE debugger opens port and waits
- Test JVM connects to the debugger
- Launch debugger first, then run test

**Important**: The IDE must use the opposite mode from the test JVM. If the test uses `listen`, the IDE must `attach`.

## VSCode Settings

### Recommended VSCode Extensions

For Galasa development, install:
- **Language Support for Java** by Red Hat
- **Debugger for Java** by Microsoft
- **Test Runner for Java** by Microsoft

### Java Debug Configuration

Create or edit `.vscode/launch.json` in your workspace:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Debug (Attach to Galasa test on 2970)",
            "projectName": "myproject",
            "request": "attach",
            "hostName": "localhost",
            "port": 2970
        }
    ]
}
```

**Launch process**:
1. Set breakpoints in test code
2. Run `galasactl runs submit local --debug` (test waits for debugger)
3. In VSCode, select "Run and Debug" from side menu
4. Choose "Debug (Attach to Galasa test on 2970)"
5. Click "Run" - debugger connects and stops at breakpoint

### VSCode Workspace Settings

Recommended settings for Galasa development in `.vscode/settings.json`:

```json
{
    "java.compile.nullAnalysis.mode": "disabled",
    "java.configuration.updateBuildConfiguration": "disabled",
    "java.debug.settings.onBuildFailureProceed": true,
    "java.jdt.ls.vmargs": "-XX:+UseParallelGC -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -Dsun.zip.disableMemoryMapping=true -Xmx8G -Xms100m"
}
```

**Settings explanation**:
- `java.compile.nullAnalysis.mode: disabled` - Reduces IDE warnings
- `java.configuration.updateBuildConfiguration: disabled` - Prevents automatic pom.xml updates
- `java.debug.settings.onBuildFailureProceed: true` - Allows debugging even with build issues
- `java.jdt.ls.vmargs` - Optimizes Java Language Server performance

## IntelliJ IDEA Configuration

### Debug Configuration

1. Load test code into IntelliJ workspace
2. Set breakpoint in test code
3. Run `galasactl runs submit local --debug`
4. In IntelliJ: Run → Attach to Process
5. Select the Java process matching your test name
6. Debugger launches at your breakpoint

**Alternative configuration**:
1. Create Remote JVM Debug configuration
2. Set connection type to "Attach to remote JVM"
3. Set host to `localhost`
4. Set port to `2970` (or your configured port)
5. Launch this configuration after starting the test

## Eclipse Configuration

### Debug Configuration

1. Import test projects into workspace
2. Set breakpoint in test code
3. Run `galasactl runs submit local --debug`
4. Create Remote Java Application debug configuration
5. Set connection type to "Standard (Socket Attach)"
6. Set port to `2970`
7. Launch debug configuration
8. Debugger connects and stops at breakpoint

## Common Troubleshooting

### GALASA_HOME Initialization Issues

**Problem**: `galasactl local init` fails or creates incomplete structure

**Solution**:
- Verify GALASA_HOME is writable
- Check disk space availability
- Ensure no file permission issues
- Try with explicit path: `galasactl local init --galasahome /path/to/galasa`

### Test Cannot Find Properties

**Problem**: Test fails with "property not found" errors

**Solution**:
- Verify property exists in `cps.properties`
- Check property name spelling
- Ensure bootstrap.properties points to correct CPS location
- Use `--override` flag to provide missing properties

### Credentials Not Found

**Problem**: Test fails with credentials errors

**Solution**:
- Check credentials exist in `credentials.properties`
- Verify property format: `secure.credentials.ID.username=value`
- Ensure credentials ID matches what test expects
- Check file permissions allow reading

### Debug Connection Fails

**Problem**: IDE cannot connect to test debugger

**Solution**:
- Verify port number matches in both test and IDE
- Check connection mode is opposite (listen vs attach)
- Ensure no firewall blocking the port
- Try different port with `--debugPort`

### Resource State Issues

**Problem**: Tests fail with "resource in use" errors when resources should be free

**Solution**:
- Stop all running tests
- Delete `${GALASA_HOME}/dss.properties`
- Run tests again

### Maven Dependency Issues

**Problem**: Tests fail with "bundle not found" errors

**Solution**:
- Run `mvn clean install` in test project
- Verify OBR coordinates are correct
- Check `~/.m2/repository` contains the bundles
- Try offline preparation: `galasactl runs prepare local --obr ...`

## Best Practices

### Property File Management

- **Keep credentials.properties secure** - Use restrictive file permissions
- **Version control CPS and overrides** - Track test configuration changes
- **Don't commit credentials** - Use `.gitignore` for credentials.properties
- **Document custom properties** - Add comments explaining property purposes

### Local Storage Maintenance

- **Clean RAS periodically** - Remove old test run directories to save space
- **Reset DSS when needed** - Delete dss.properties if resource state becomes inconsistent
- **Backup configuration** - Save working property files before making changes

### Development Workflow

- **Use overrides for experiments** - Keep CPS stable, use overrides for temporary changes
- **Test locally before ecosystem** - Validate tests work locally before deploying
- **Prepare offline dependencies** - Use `runs prepare local` for consistent offline testing
- **Monitor resource usage** - Check DSS for resource leaks during development

## Related Documentation

- [Configuration Property System (CPS)](/openwiki/concepts/configuration-properties.md) - Detailed CPS architecture and usage
- [Storage Backend Implementations](/openwiki/integrations/storage-backends.md) - External storage backend setup
- [Galasa Boot JAR](/openwiki/operations/galasa-boot.md) - Boot JAR parameters and operation
- [CLI Architecture](/openwiki/architecture/cli.md) - CLI command structure and implementation
