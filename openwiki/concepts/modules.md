---
type: Repository Architecture
title: Repository Module Structure
description: Explanation of the modular organization of the Galasa repository and the purpose, build technology, and dependency relationships of each major module
tags: [architecture, modules, build, gradle, maven, go, dependencies, obr]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-4192f9ccb5aa76f05103d830
    resource: repo://modules/buildutils/go.mod
  - id: openwiki-source-925a57b76bc49106b0371afe
    resource: repo://modules/buildutils/README.md
  - id: openwiki-source-3fc387923be21a327453a018
    resource: repo://modules/cli/build-locally.sh
  - id: openwiki-source-b4e430d80bc36a4db1b95848
    resource: repo://modules/cli/go.mod
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-36e75e1dbc1befa486011f1d
    resource: repo://modules/extensions/README.md
  - id: openwiki-source-d81d69a930a39e392273844c
    resource: repo://modules/framework/galasa-parent/build.gradle
  - id: openwiki-source-19c289d51521684350e01d7c
    resource: repo://modules/framework/README.md
  - id: openwiki-source-d2a3fc5c55ba8b6a4ade5ca1
    resource: repo://modules/gradle/README.md
  - id: openwiki-source-4677f6fb5194da8fd9898ef0
    resource: repo://modules/ivts/README.md
  - id: openwiki-source-be25c848223b4bb8af75e8a0
    resource: repo://modules/managers/galasa-managers-parent/build.gradle
  - id: openwiki-source-83d1b64462dc4ee9ad9d27be
    resource: repo://modules/managers/README.md
  - id: openwiki-source-4b4fa74b099fc828fc2dcf8f
    resource: repo://modules/maven/README.md
  - id: openwiki-source-fef7b8dce1a14fc037c9fc23
    resource: repo://modules/obr/build-locally.sh
  - id: openwiki-source-ad10513a10e67a1d7411bad0
    resource: repo://modules/obr/README.md
  - id: openwiki-source-b8a1e6d46ff4407a505b13cb
    resource: repo://modules/platform/dev.galasa.platform/build.gradle
  - id: openwiki-source-94cb0b099ec3de99606f428a
    resource: repo://modules/platform/README.md
  - id: openwiki-source-36640b90097ff7613d304a43
    resource: repo://modules/wrapping/pom.xml
  - id: openwiki-source-65dec544d823b137b4381ee9
    resource: repo://modules/wrapping/README.md
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
  - id: openwiki-source-d9cc689740542a78d804536c
    resource: repo://tools/build-locally.sh
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Repository Module Structure

The Galasa repository is organized into a multi-module architecture with eleven distinct modules, each serving a specific purpose in the overall system. The modules are built using a combination of Gradle, Maven, and Go, and must be built in a specific dependency order to ensure proper artifact resolution.

## Module Overview

<!-- openwiki: mermaid parse failed and this diagram was converted to a text fence so it does not break rendering. Fix the diagram source and restore the mermaid fence. Parser error: Heuristic: an unescaped angle bracket inside a label breaks rendering; rephrase the label. -->
```text
graph TD
    platform[platform<br/>Gradle Platform BOM]
    buildutils[buildutils<br/>Go Build Tools]
    wrapping[wrapping<br/>Maven OSGi Wrappers]
    gradle[gradle<br/>Gradle Plugins]
    maven[maven<br/>Maven Plugin]
    framework[framework<br/>Java/Gradle Core]
    extensions[extensions<br/>Java/Gradle Extensions]
    managers[managers<br/>Java/Gradle Test Managers]
    obr[obr<br/>Maven OSGi Bundle Repository]
    ivts[ivts<br/>Gradle IVTs]
    cli[cli<br/>Go + Gradle CLI Tool]
    
    platform --> gradle
    platform --> wrapping
    platform --> framework
    platform --> extensions
    platform --> managers
    
    buildutils -.-> obr
    buildutils -.-> gradle
    
    wrapping --> framework
    gradle --> maven
    maven --> framework
    
    framework --> extensions
    framework --> managers
    
    extensions --> managers
    managers --> obr
    obr --> ivts
    ivts --> cli
    
    style platform fill:#e1f5ff
    style buildutils fill:#fff4e1
    style cli fill:#fff4e1
    style obr fill:#f0e1ff
```

## Build Technology Stack

The modules use three primary build technologies:

### Java/Gradle Modules
Most modules use **Gradle** with Java for building OSGi bundles:
- `platform` - Dependency platform (BOM) definition
- `gradle` - Galasa Gradle plugins for OBR and test catalog generation
- `framework` - Core framework components and REST API
- `extensions` - Framework extensions (CPS, RAS, authentication)
- `managers` - Test managers for various technologies
- `ivts` - Installation Verification Tests

### Maven Modules
Specific modules use **Maven** for packaging and plugin functionality:
- `wrapping` - Wraps non-OSGi JARs into OSGi bundles using maven-bundle-plugin
- `maven` - Galasa Maven plugin for building OBRs and test catalogs
- `obr` - Assembles the uber OBR and prepares Docker images

### Go Modules
Two modules are written in **Go**:
- `buildutils` - Contains the `galasabld` version management utility and `openapi2beans` code generator
- `cli` - The `galasactl` command-line tool (also uses Gradle for dependency management)

## Module Descriptions

### platform
**Technology:** Gradle (java-platform plugin)  
**Purpose:** Centralized dependency management platform

The `platform` module defines version constraints for all external dependencies used across Galasa. It uses Gradle's `java-platform` plugin to create a Bill of Materials (BOM) that ensures consistent dependency versions throughout the entire codebase. Over 200 dependency version constraints are declared here, including libraries like Jackson, Kubernetes client, etcd, Selenium, and OSGi bundles.

Key characteristics:
- No executable code, only dependency declarations
- Referenced by other modules via `platform('dev.galasa:dev.galasa.platform:'+version)`
- Uses `api platform()` for POM-type dependencies and `constraints { api ... }` for JAR-type dependencies
- Must be built first as other modules depend on its version constraints

### buildutils
**Technology:** Go  
**Purpose:** Build tooling and code generation utilities

The `buildutils` module provides build-time utilities written in Go. The primary output is the `galasabld` command-line tool, which manages version numbers across Gradle modules in the repository. This tool can list module versions, set version suffixes (e.g., `-SNAPSHOT`, `-alpha`), and remove suffixes.

Key functionality:
- `galasabld versioning list` - Recursively scans for modules and lists their versions
- `galasabld versioning suffix set/remove` - Manages version suffixes across all modules
- `openapi2beans` - Generates Java beans from OpenAPI specifications
- Built as native binaries for multiple platforms (Darwin, Linux, Windows) and architectures (amd64, arm64)

The build produces binaries in `bin/` directory named like `galasabld-{os}-{architecture}`.

### wrapping
**Technology:** Maven  
**Purpose:** Wraps third-party dependencies into OSGi bundles

Many Java libraries are not packaged as OSGi bundles. The `wrapping` module takes these non-OSGi JARs and repackages them as OSGi bundles using the maven-bundle-plugin. This allows them to be loaded in Galasa's OSGi runtime environment.

Wrapped dependencies include:
- `dev.galasa.wrapping.com.auth0.jwt` - JWT authentication library
- `dev.galasa.wrapping.httpclient5` - Apache HTTP client
- `dev.galasa.wrapping.io.grpc.java` - gRPC Java libraries
- `dev.galasa.wrapping.io.kubernetes.client-java` - Kubernetes client
- `dev.galasa.wrapping.jetcd-core` - etcd client
- `dev.galasa.wrapping.kafka.clients` - Kafka client
- `dev.galasa.wrapping.selenium-java` - Selenium WebDriver
- `dev.galasa.wrapping.velocity-engine-core` - Apache Velocity templating

These wrapped bundles are published to Maven repositories and consumed by the framework and managers modules.

### gradle
**Technology:** Gradle  
**Purpose:** Galasa Gradle plugins for building test projects and OBRs

The `gradle` module provides custom Gradle plugins that enable users to build Galasa test projects using Gradle instead of Maven. These plugins handle OSGi Bundle Repository (OBR) generation and test catalog creation.

Key plugins:
- `dev.galasa.tests` - Applied to test case projects to build test catalogs
- `dev.galasa.obr` - Builds OSGi Bundle Repositories from test bundles
- `dev.galasa.testcatalog` - Generates test catalogs for deployment to Galasa ecosystems

The plugins are published to Maven repositories and the Gradle Plugin Portal for use in user projects.

### maven
**Technology:** Maven  
**Purpose:** Galasa Maven plugin for building OBRs and test catalogs

The `maven` module provides the `galasa-maven-plugin`, which extends Maven with goals for building Galasa projects. This is the traditional build tool for Galasa test projects and remains widely used.

Key Maven goals:
- `bundletestcat` - Builds a test catalog from test classes in a bundle
- `deploytestcat` - Deploys test catalogs to a Galasa ecosystem
- OBR generation - Assembles OSGi Bundle Repositories

The plugin reads properties like `galasa.bootstrap`, `galasa.token`, and `galasa.test.stream` to interact with Galasa ecosystems.

### framework
**Technology:** Gradle + Java  
**Purpose:** Core framework orchestration and REST API

The `framework` module contains Galasa's core framework code that orchestrates all component activities and coordinates with the test runner to execute tests. It handles framework initialization, Manager lifecycle, and provides the Galasa REST API server.

Major components:
- `dev.galasa.framework` - Core framework orchestration and test lifecycle
- `galasa-boot` - JAR file for launching Galasa in various modes (test runner, API server, K8s controller, resource management)
- `dev.galasa.framework.api.*` - REST API endpoints for runs, resources, authentication, CPS, RAS, users, secrets
- `dev.galasa.framework.k8s.controller` - Kubernetes-based engine controller service
- `dev.galasa.framework.resource.management` - Resource monitor service
- `dev.galasa` - Core annotations and interfaces for test authors

The framework uses Gradle's `biz.aQute.bnd.builder` plugin to generate OSGi bundle manifests. It downloads the Dex gRPC API proto file for authentication integration and generates Java code from protobuf definitions.

**Galasa Boot** is the primary entrypoint JAR that can launch Galasa in multiple modes using flags like `--api`, `--k8scontroller`, `--resourcemanagement`, `--test`, etc.

### extensions
**Technology:** Gradle + Java  
**Purpose:** Framework extensions for storage and authentication

The `extensions` module provides implementations of framework extension points, particularly for storing test configuration and results in various backends.

Key extensions:
- `dev.galasa.cps.etcd` - Configuration Property Store backed by etcd
- `dev.galasa.cps.rest` - CPS REST interface
- `dev.galasa.ras.couchdb` - Result Archive Store backed by CouchDB
- `dev.galasa.auth.couchdb` - Authentication backed by CouchDB
- `dev.galasa.creds.keychain` - Credentials provider using OS keychain
- `dev.galasa.creds.os` - OS-based credentials provider
- `dev.galasa.events.kafka` - Event streaming via Kafka
- `dev.galasa.extensions.common.*` - Common utilities for extensions

These extensions allow Galasa to integrate with different infrastructure backends for configuration, credentials, results storage, and authentication.

### managers
**Technology:** Gradle + Java  
**Purpose:** Test managers providing infrastructure and tooling

The `managers` module contains the test managers that form the core of Galasa's testing capability. Managers are reusable components that provide test infrastructure, provision resources, and inject capabilities into test classes.

Managers are organized by category:
- **Core managers** (`galasa-managers-core-parent`) - Artifact Manager, Core Manager
- **CICS managers** (`galasa-managers-cicsts-parent`) - CECI, CEDA, CEMT, CICS TS
- **Cloud managers** (`galasa-managers-cloud-parent`) - Docker, Kubernetes, OpenStack
- **Communication managers** (`galasa-managers-comms-parent`) - HTTP, IP Network, MQ
- **z/OS managers** (`galasa-managers-zos-parent`) - z/OS, z/OS Batch, z/OS File, z/OS 3270, z/OS Console, z/OS TSO, z/OS Unix, z/OS MF, RSE API
- **Testing tools** (`galasa-managers-testingtools-parent`) - JMeter, Selenium
- **Database managers** (`galasa-managers-database-parent`) - DB2
- **Unix managers** (`galasa-managers-unix-parent`) - Linux
- **Windows managers** (`galasa-managers-windows-parent`) - Windows
- **Language managers** (`galasa-managers-languages-parent`) - Java
- **Workflow managers** (`galasa-managers-workflow-parent`) - GitHub Issue
- **Logging managers** (`galasa-managers-logging-parent`) - Elastic Log
- **Other managers** (`galasa-managers-other-parent`) - Galasa Ecosystem

Each manager provides annotations, interfaces, and implementations that test authors use to provision and interact with systems under test. For example, the HTTP Manager provides `@HttpClient` annotations to inject HTTP clients, while the Docker Manager provides `@DockerContainer` to provision containers.

The managers module produces a `release.yaml` metadata file that is published to Maven and consumed by the OBR build.

### obr
**Technology:** Maven  
**Purpose:** Assembles the uber OSGi Bundle Repository

The `obr` module creates the "uber OBR" - a comprehensive OSGi Bundle Repository that packages together all Galasa bundles and their dependencies. The OBR is the distribution mechanism for Galasa, allowing the OSGi framework to load bundles on demand.

Key components:
- `dev.galasa.uber.obr` - The uber OBR assembly project
- `galasa-bom` - Bill of Materials for Galasa artifacts
- `javadocs` - Aggregated Javadoc generation
- `codecoveragetemplates` - Code coverage reporting templates
- Docker image preparation for embedding the OBR in container images

The OBR build process:
1. Downloads dependency information from the framework, extensions, and managers modules
2. Uses the `galasabld` tool to merge `release.yaml` files
3. Generates a Maven POM with all required bundle dependencies
4. Uses Maven to resolve and package all bundles into an OBR file
5. Optionally builds Docker images with the OBR embedded

The OBR is published as `dev.galasa:dev.galasa.uber.obr:{version}:obr` to Maven repositories.

### ivts
**Technology:** Gradle + Java  
**Purpose:** Installation Verification Tests for managers

The `ivts` module contains Installation Verification Tests (IVTs) that validate the functionality of Galasa managers. Each IVT is itself a Galasa test that uses a manager to perform operations and verifies the expected behavior.

IVTs are organized into two parent bundles:
- `dev.galasa.ivts` - IVTs that do not require mainframe resources (Core, Artifact, HTTP, Docker, CICS non-z/OS tests)
- `dev.galasa.zos.ivts` - IVTs that require mainframe resources (z/OS Manager, z/OS Batch, z/OS File, z/OS 3270, z/OS TSO)

Example IVTs:
- `CoreManagerIVT` - Tests the Core Manager
- `HttpManagerIVT` - Tests HTTP client functionality
- `DockerManagerIVT` - Tests Docker container provisioning
- `ZosManagerIVT` - Tests z/OS connectivity and operations

IVTs are built as test bundles with an OBR and test catalog, representing a test stream called 'ivts'. These tests run daily in automation to detect manager regressions.

### cli
**Technology:** Go + Gradle  
**Purpose:** galasactl command-line interface

The `cli` module provides the `galasactl` command-line tool for interacting with Galasa ecosystems and running tests locally. It combines Go for the application logic with Gradle for dependency management.

Key functionality:
- **Ecosystem interaction** - Submit runs, query resources, manage properties, configure authentication
- **Local testing** - Initialize local environment, run tests locally using local JVMs
- **Project generation** - Create template test projects and manager projects
- **Resource management** - Manage runs, properties, secrets, users, roles, test streams
- **Authentication** - Token-based authentication with Galasa services

The CLI is built as native binaries for multiple platforms using Go's cross-compilation capabilities. It embeds the `galasa-boot` JAR and required dependencies for local test execution.

Build process:
1. Gradle downloads dependencies (galasa-boot JAR, OpenAPI generator)
2. OpenAPI specs are converted to Go client code for the Galasa REST API
3. Go builds the `galasactl` binary for the target platform
4. Binaries are packaged for distribution

The CLI supports environment variables like `GALASA_HOME`, `GALASA_BOOTSTRAP`, and `GALASA_TOKEN` for configuration.

## Build Order and Dependencies

Modules must be built in a strict dependency order to ensure artifact availability:

1. **platform** - Must be first; defines dependency versions for all other modules
2. **buildutils** - Provides the `galasabld` tool used by later builds
3. **wrapping** - Produces OSGi-wrapped dependencies needed by framework
4. **gradle** - Gradle plugins needed for building other modules
5. **maven** - Maven plugin needed for OBR assembly
6. **framework** - Core framework bundles consumed by extensions and managers
7. **extensions** - Framework extensions that may be used by managers
8. **managers** - Test managers that are packaged in the OBR
9. **obr** - Assembles all previous artifacts into the uber OBR
10. **ivts** - Tests built against the OBR
11. **cli** - CLI tool that can consume the OBR for local testing

This ordering is enforced by the `build-locally.sh` script in the `tools/` directory:

```bash
platform -> buildutils -> wrapping -> gradle -> maven -> framework -> extensions -> managers -> obr -> ivts -> cli -> docs
```

The `get_next_module()` function in the build script defines this chain explicitly.

## Dependency Resolution

Each module resolves dependencies through a defined repository chain:

1. **mavenLocal()** - Local Maven cache (`~/.m2/repository`)
2. **sourceMaven** - Configurable development repository (can be file:// or https://)
3. **gradlePluginPortal()** - For Gradle plugins
4. **mavenCentral()** - Maven Central for third-party dependencies

The `sourceMaven` property allows flexible artifact resolution:
- For local builds: defaults to `file://<local-maven-repo>`
- For CI builds: can be set to `https://development.galasa.dev/main/maven-repo/obr/`

## Version Management

All modules share a single version number maintained across the repository. The version is set in each module's build file:
- Gradle modules: `version = "x.y.z"` in `build.gradle`
- Maven modules: `<version>x.y.z</version>` in `pom.xml`
- Go modules: `VERSION` file or version constant

The `galasabld` utility from the `buildutils` module provides centralized version management, allowing developers to list versions, set suffixes (e.g., `-SNAPSHOT`), and remove suffixes across all modules simultaneously.

The repository-wide `set-version.sh` scripts coordinate version updates across all modules.

## Build Artifacts

Each module publishes artifacts to Maven repositories:

- **Platform:** `dev.galasa:dev.galasa.platform:{version}:pom`
- **Wrapping:** OSGi bundles like `dev.galasa:dev.galasa.wrapping.httpclient5:{version}:jar`
- **Gradle:** Plugin artifacts like `dev.galasa:dev.galasa.obr:{version}:jar`
- **Maven:** `dev.galasa:galasa-maven-plugin:{version}:jar`
- **Framework:** Core bundles like `dev.galasa:dev.galasa.framework:{version}:jar` and `dev.galasa:galasa-boot:{version}:jar`
- **Extensions:** Extension bundles like `dev.galasa:dev.galasa.cps.etcd:{version}:jar`
- **Managers:** Manager bundles like `dev.galasa:dev.galasa.http.manager:{version}:jar`
- **OBR:** `dev.galasa:dev.galasa.uber.obr:{version}:obr`
- **IVTs:** Test bundles and test catalog
- **CLI:** Native binaries `galasactl-{os}-{architecture}`

Artifacts are signed with GPG for Maven Central publication and include source and Javadoc JARs for release builds.
