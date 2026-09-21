---
type: architecture
title: CLI Architecture and Structure
description: Architecture of galasactl command-line tool, its Go implementation, command structure, and API communication patterns
tags: [cli, galasactl, golang, cobra, architecture, api-client, authentication]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-be0cdf9734b7674d36ba718e
    resource: repo://modules/cli/cmd/galasactl/main.go
  - id: openwiki-source-b4e430d80bc36a4db1b95848
    resource: repo://modules/cli/go.mod
  - id: openwiki-source-6fb22a14899eef193c1849ae
    resource: repo://modules/cli/pkg/api/apiCommsClient.go
  - id: openwiki-source-6b43b345b01140d9f4e59160
    resource: repo://modules/cli/pkg/api/bootstrap.go
  - id: openwiki-source-2fbef8b3f70ab62d481f446b
    resource: repo://modules/cli/pkg/auth/authenticator.go
  - id: openwiki-source-ac654aedaee8b180915f1383
    resource: repo://modules/cli/pkg/auth/authProperties.go
  - id: openwiki-source-673d58877b048c01d7d13d60
    resource: repo://modules/cli/pkg/auth/jwtCache.go
  - id: openwiki-source-97e7264e72fdf4c7ca83e522
    resource: repo://modules/cli/pkg/cmd/commandCollection.go
  - id: openwiki-source-88d345b826f5c19ffd15359e
    resource: repo://modules/cli/pkg/cmd/commsFlagSet.go
  - id: openwiki-source-3f4bb28c855eccee4006e489
    resource: repo://modules/cli/pkg/cmd/execute.go
  - id: openwiki-source-04ecca37cc58582b12913eb0
    resource: repo://modules/cli/pkg/cmd/factory.go
  - id: openwiki-source-2c53382a531b29e2cca35a41
    resource: repo://modules/cli/pkg/cmd/projectCreate.go
  - id: openwiki-source-8202414c9c18bb91436de7ab
    resource: repo://modules/cli/pkg/cmd/root.go
  - id: openwiki-source-1e212a4b09f20d40bbd80f1b
    resource: repo://modules/cli/pkg/galasaapi/client.go
  - id: openwiki-source-fb69cd2cf13235461a32e23c
    resource: repo://modules/cli/pkg/galasaapi/configuration.go
  - id: openwiki-source-392a0fa12e6ce65011aee1ca
    resource: repo://modules/cli/pkg/launcher/jvmLauncher.go
  - id: openwiki-source-3c62dd9454ec8aeea4e7aa21
    resource: repo://modules/cli/pkg/launcher/remoteLauncher.go
  - id: openwiki-source-2b878067ef4e72d8c3582e63
    resource: repo://modules/cli/pkg/runs/portfolio.go
  - id: openwiki-source-3a9251923c716abb11e39159
    resource: repo://modules/cli/pkg/runs/runsDownload.go
  - id: openwiki-source-2867089f4a96b0d1695c93d3
    resource: repo://modules/cli/pkg/runs/submitter.go
  - id: openwiki-source-9995c497515d2512e89253ac
    resource: repo://modules/cli/pkg/spi/factory.go
  - id: openwiki-source-3c0f6c2478ad9b531c5c85a5
    resource: repo://modules/cli/pkg/spi/galasaCommand.go
  - id: openwiki-source-92f971b955d5e8c3363ec845
    resource: repo://modules/cli/pkg/utils/galasaHome.go
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# CLI Architecture and Structure

The Galasa Command Line Interface (galasactl) is a comprehensive command-line tool written in Go that provides a user-friendly interface to interact with Galasa ecosystems and manage local test development. It enables users to run tests, manage ecosystem resources, query results, work with artifacts, and configure Galasa components.

## Purpose and Capabilities

The CLI serves as the primary interface for:

1. **Running tests** - Submit test runs to remote ecosystems or execute tests locally in a JVM
2. **Managing the Galasa ecosystem** - Configure properties, secrets, streams, roles, users, and monitors
3. **Querying test results** - Retrieve and download test artifacts and execution history
4. **Working with test artifacts** - Manage test runs, portfolios, and associated resources
5. **Local development** - Initialize local Galasa environments and run tests in development mode
6. **Authentication** - Log in to ecosystems and manage personal access tokens

## Environment Variables

The CLI behavior is controlled by three key environment variables:

### GALASA_HOME

Specifies the folder where Galasa reads and writes files and configuration settings. Defaults to `${HOME}/.galasa` if not specified. This directory contains:

- `bootstrap.properties` - Connection details for the Galasa ecosystem
- `galasactl.properties` - CLI configuration including the personal access token
- `bearer-token.json` - Cached JWT for authenticated API requests
- Test result artifacts for locally executed tests

The `--galasahome` command-line flag can override the `GALASA_HOME` environment variable on a per-command basis.

### GALASA_BOOTSTRAP

Provides the location of the bootstrap file or URL, which contains essential configuration such as the API server URL. Can be:

- An HTTP/HTTPS URL (e.g., `http://example.com/bootstrap`)
- A file path (e.g., `file:///user/.galasa/bootstrap.properties`)
- Defaults to `${GALASA_HOME}/bootstrap.properties` if not specified

The `--bootstrap` command-line flag takes precedence over this environment variable.

### GALASA_TOKEN

Contains the personal access token for authenticating with a Galasa ecosystem. Can be set as an environment variable or stored in the `galasactl.properties` file within `GALASA_HOME`. Personal access tokens are created through the Galasa web user interface.

## Implementation Technology

The CLI is implemented in Go using established libraries:

- **Cobra** - Command-line interface framework providing hierarchical command structure and flag parsing
- **Go HTTP Client** - For REST API communication with Galasa ecosystems
- **YAML/JSON Libraries** - For configuration handling and data serialization

## Architecture Overview

```mermaid
flowchart TD
    User[User] --> CLI[galasactl CLI]
    CLI --> Factory[Factory/SPI]
    Factory --> Auth[Authenticator]
    Factory --> FS[FileSystem]
    Factory --> Env[Environment]
    Auth --> JWTCache[JWT Cache]
    CLI --> Commands[Command Collection]
    Commands --> APIComms[API Comms Client]
    APIComms --> Bootstrap[Bootstrap Loader]
    APIComms --> APIClient[Generated API Client]
    APIClient --> Ecosystem[Galasa Ecosystem API]
    CLI --> Launcher[Test Launcher]
    Launcher --> RemoteLauncher[Remote Launcher]
    Launcher --> JVMLauncher[Local JVM Launcher]
    RemoteLauncher --> Ecosystem
    JVMLauncher --> LocalJVM[Local Java Process]
```

The CLI follows a layered architecture with clear separation of concerns.

## Command Structure

The CLI uses a hierarchical command structure built on Cobra, with a root command and multiple subcommand groups:

```mermaid
flowchart TD
    Root[galasactl] --> Auth[auth]
    Root --> Local[local]
    Root --> Project[project]
    Root --> Runs[runs]
    Root --> Properties[properties]
    Root --> Resources[resources]
    Root --> Secrets[secrets]
    Root --> Monitors[monitors]
    Root --> Streams[streams]
    Root --> Users[users]
    Root --> Roles[roles]
    Root --> Tags[tags]
    
    Auth --> AuthLogin[login]
    Auth --> AuthLogout[logout]
    Auth --> AuthTokens[tokens]
    AuthTokens --> TokensGet[get]
    AuthTokens --> TokensDelete[delete]
    
    Local --> LocalInit[init]
    
    Project --> ProjectCreate[create]
    
    Runs --> RunsSubmit[submit]
    Runs --> RunsGet[get]
    Runs --> RunsPrepare[prepare]
    Runs --> RunsDownload[download]
    Runs --> RunsCancel[cancel]
    Runs --> RunsReset[reset]
    Runs --> RunsUpdate[update]
    Runs --> RunsDelete[delete]
    Runs --> RunsCleanup[cleanup]
    RunsSubmit --> RunsSubmitLocal[local]
    RunsPrepare --> RunsPrepareLocal[local]
    RunsCleanup --> RunsCleanupLocal[local]
    
    Properties --> PropGet[get]
    Properties --> PropSet[set]
    Properties --> PropDelete[delete]
    Properties --> PropNamespaces[namespaces]
    PropNamespaces --> NamespacesGet[get]
```

Each command group provides operations for managing specific aspects of the Galasa ecosystem.

## Package Structure

The CLI codebase is organized into focused packages under `pkg/`:

### Core Packages

- **cmd/** - Command definitions and Cobra command setup
  - Implements all CLI commands using the Cobra framework
  - Each command has a dedicated file (e.g., `runsSubmit.go`, `authLogin.go`)
  - `commandCollection.go` assembles all commands into a hierarchy
  - `root.go` defines the root `galasactl` command
  - `execute.go` provides the main entry point

- **api/** - API client initialization and bootstrap loading
  - `apiCommsClient.go` - Smart client with retry and rate-limiting logic
  - `bootstrap.go` - Loads bootstrap configuration from files or URLs
  - Handles the `framework.api.server.url` property from bootstrap

- **auth/** - Authentication and token management
  - `authenticator.go` - Authenticates with API servers using personal access tokens
  - `jwtCache.go` - Caches JWT bearer tokens to `bearer-token.json`
  - `authProperties.go` - Reads `GALASA_TOKEN` from properties or environment

- **galasaapi/** - Generated OpenAPI client code
  - Auto-generated Go client for the Galasa REST API
  - Provides typed models and API methods for all ecosystem operations

- **spi/** - Service Provider Interfaces (abstractions)
  - `factory.go` - Factory pattern for creating real or mock dependencies
  - `authenticator.go` - Authentication interface
  - `fileSystem.go` - File system abstraction
  - `environment.go` - Environment variable access
  - `galasaCommand.go` - Command interface wrapping Cobra commands

### Domain Packages

- **runs/** - Test run submission, monitoring, and management
  - `submitter.go` - Orchestrates test submission with throttling and progress reporting
  - `runsGet.go` - Queries and filters test runs
  - `runsDownload.go` - Downloads test artifacts
  - `portfolio.go` - Parses test portfolios
  - `testSelection.go` - Test selection by stream, class, package, or bundle

- **launcher/** - Test execution launchers
  - `remoteLauncher.go` - Submits and monitors tests on remote ecosystems
  - `jvmLauncher.go` - Launches tests in a local Java process
  - `process.go` - Process management for local JVM execution

- **properties/** - Configuration property store operations
  - CRUD operations for Galasa configuration properties
  - Namespace management

- **secrets/** - Secrets management
  - Operations for creating, retrieving, and deleting secrets

- **resources/** - Resource management (YAML-based)
  - Apply, create, update, and delete operations for ecosystem resources

- **users/**, **roles/**, **streams/**, **monitors/**, **tags/** - Ecosystem entity management
  - Each provides operations for their respective entity types

### Utility Packages

- **utils/** - Common utilities
  - `galasaHome.go` - Resolves `GALASA_HOME` with precedence rules
  - `bearerTokenFile.go` - Manages the bearer token cache file
  - `console.go` - Console output abstraction
  - `timeService.go` - Time service for testing
  - `logRedirector.go` - Log output redirection

- **files/** - File system operations
  - Abstracts file I/O for testability

- **errors/** - Error definitions and handling
  - Structured error types with codes

- **embedded/** - Embedded resources
  - Version information and embedded assets

- **XXXformatter/** packages - Output formatting
  - Format entities for human-readable, JSON, or YAML output

## Authentication Flow

The CLI implements a sophisticated authentication system with automatic re-authentication:

```mermaid
sequenceDiagram
    participant User
    participant CLI
    participant Auth as Authenticator
    participant JWTCache as JWT Cache
    participant APIServer as API Server
    participant FileSystem
    
    User->>CLI: galasactl runs submit
    CLI->>Auth: GetAuthenticatedAPIClient()
    Auth->>JWTCache: Get(apiServerUrl, galasaToken)
    JWTCache->>FileSystem: Read bearer-token.json
    
    alt JWT exists and valid
        FileSystem-->>JWTCache: JWT token
        JWTCache-->>Auth: JWT token
        Auth-->>CLI: Authenticated API Client
    else No JWT or expired
        JWTCache-->>Auth: empty token
        Auth->>Auth: Login()
        Auth->>FileSystem: Read galasactl.properties
        FileSystem-->>Auth: GALASA_TOKEN
        Auth->>APIServer: POST /auth with token
        APIServer-->>Auth: JWT response
        Auth->>JWTCache: Put(apiServerUrl, galasaToken, jwt)
        JWTCache->>FileSystem: Write bearer-token.json
        Auth-->>CLI: Authenticated API Client
    end
    
    CLI->>APIServer: API Request with JWT
    APIServer-->>CLI: Response
```

Authentication is managed through several components working together.

## Configuration Precedence

Configuration values follow a clear precedence order (highest to lowest):

1. **Command-line flags** - Explicit flags like `--bootstrap`, `--galasahome`
2. **Environment variables** - `GALASA_BOOTSTRAP`, `GALASA_HOME`, `GALASA_TOKEN`
3. **Configuration files** - Properties in `${GALASA_HOME}/galasactl.properties`
4. **Default values** - Built-in defaults like `${HOME}/.galasa`

## API Communication

The CLI communicates with Galasa ecosystems through REST APIs:

```mermaid
sequenceDiagram
    participant CLI as galasactl Command
    participant CommsClient as API Comms Client
    participant Bootstrap as Bootstrap Loader
    participant Authenticator
    participant APIClient as Generated API Client
    participant API as Galasa REST API
    
    CLI->>CommsClient: NewAPICommsClient(bootstrap, ...)
    CommsClient->>Bootstrap: LoadBootstrap(path)
    Bootstrap->>Bootstrap: Read bootstrap file/URL
    Bootstrap-->>CommsClient: BootstrapData (API server URL)
    CommsClient->>Authenticator: GetAuthenticator(apiServerUrl)
    
    CLI->>CommsClient: RunAuthenticatedCommandWithRateLimitRetries(func)
    
    loop Retry with backoff on rate limit or auth failure
        CommsClient->>Authenticator: GetAuthenticatedAPIClient()
        Authenticator-->>CommsClient: API Client with JWT
        CommsClient->>API: Execute command function
        
        alt Success
            API-->>CommsClient: Success response
        else Rate Limited (429)
            API-->>CommsClient: 429 error
            CommsClient->>CommsClient: Sleep and retry
        else Unauthorized (401)
            API-->>CommsClient: 401 error
            CommsClient->>Authenticator: Re-authenticate
            CommsClient->>CommsClient: Retry with new token
        else Other error
            API-->>CommsClient: Error response
        end
    end
    
    CommsClient-->>CLI: Result or error
```

The API Comms Client provides intelligent retry logic with rate limiting and automatic re-authentication.

## Command Execution Flow

Commands follow a standardized execution pattern:

```mermaid
flowchart TD
    Start[User invokes command] --> Parse[Cobra parses args and flags]
    Parse --> Factory[Create Factory]
    Factory --> Collection[Build Command Collection]
    Collection --> Execute[Execute root command]
    Execute --> CobraExecute[Cobra routes to handler]
    CobraExecute --> RunE[Command RunE function]
    
    RunE --> CaptureLog[Capture logs]
    CaptureLog --> GalasaHome[Resolve GALASA_HOME]
    GalasaHome --> CreateComms[Create API Comms Client]
    CreateComms --> LoadBootstrap[Load bootstrap config]
    LoadBootstrap --> CreateAuth[Create authenticator]
    CreateAuth --> Validate[Validate parameters]
    
    Validate --> BizLogic[Execute business logic]
    BizLogic --> Format[Format output]
    Format --> FinalWord[Final word handler]
    FinalWord --> SetExitCode[Set exit code]
    SetExitCode --> End[Exit]
```

Each command follows this consistent pattern, ensuring predictable behavior.

## Factory Pattern

The CLI uses a factory pattern to create dependencies, enabling testability:

- **RealFactory** - Creates real implementations for production use
  - File system operations delegate to `os` package
  - Environment access delegates to `os.Getenv`
  - Time service uses real time
  
- **Mock factories** (in tests) - Create mock implementations
  - In-memory file systems
  - Stubbed environment variables
  - Controllable time for testing timeouts

This abstraction allows comprehensive unit testing without external dependencies.

## Local vs Remote Execution

The CLI supports two execution modes:

### Remote Execution

- Commands communicate with a Galasa ecosystem via REST API
- Tests run on ecosystem infrastructure with full resource management
- Requires authentication with personal access token
- Examples: `runs submit`, `runs get`, `properties set`

### Local Execution

- Tests run in a local JVM process spawned by the CLI
- Used for test development and debugging
- No authentication required
- Limited ecosystem features (no resource arbitration)
- Examples: `runs submit local`, `runs prepare local`, `runs cleanup local`

The launcher abstraction (`launcher.go`) defines the interface, with `remoteLauncher.go` and `jvmLauncher.go` providing concrete implementations.

## Error Handling

The CLI implements structured error handling:

1. **Typed errors** - All errors use types from the `errors/` package with error codes
2. **HTTP status codes** - API errors preserve HTTP status codes for proper diagnostics
3. **Retry logic** - Transient errors (rate limiting, network issues) trigger automatic retries with exponential backoff
4. **User-friendly messages** - Errors include actionable guidance
5. **Exit codes** - Non-zero exit codes signal failure for automation integration

## Testing Strategy

The CLI employs multiple testing approaches:

- **Unit tests** - Comprehensive tests for individual packages using mock factories
- **Integration tests** - Scripts like `test-galasactl-ecosystem.sh` and `test-galasactl-local.sh`
- **Mock implementations** - Most SPI interfaces have mock variants for testing
- **Testable abstractions** - File system, environment, time service, and HTTP client are all mockable

## Project Creation

The `project create` command generates complete project structures:

- **Test projects** - OSGi bundles containing Galasa tests
- **Manager projects** - Reusable infrastructure components with lifecycle hooks
- **Build systems** - Maven and/or Gradle build files
- **OBR projects** - OSGi Bundle Repository for artifact distribution

Generated projects follow Galasa best practices and are ready to build and run.

## Key Design Principles

1. **Separation of concerns** - Clear boundaries between commands, API client, authentication, and business logic
2. **Testability** - Factory pattern and SPI abstractions enable comprehensive testing
3. **Retry resilience** - Automatic retry with backoff for rate limiting and transient failures
4. **Configuration flexibility** - Multiple ways to configure with clear precedence
5. **Consistent UX** - All commands follow similar patterns for flags, output, and error handling
6. **Extensibility** - New commands can be added by following established patterns
