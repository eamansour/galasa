---
type: Build System Architecture
title: Build System and Dependencies
description: Explanation of the build toolchain, dependency management, version control, and artifact publishing across Galasa modules
tags: [build, dependencies, gradle, maven, go, versioning, gpg, repositories]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-ee7e20f771635e8e385875e2
    resource: repo://.github/workflows/framework.yaml
  - id: openwiki-source-dca48af1473bd950da5310a3
    resource: repo://.github/workflows/pushes.yaml
  - id: openwiki-source-f317ee207e1653d2033c81a4
    resource: repo://CONTRIBUTING.md
  - id: openwiki-source-4192f9ccb5aa76f05103d830
    resource: repo://modules/buildutils/go.mod
  - id: openwiki-source-4be7b7be08bad0c06b3ce76c
    resource: repo://modules/buildutils/pkg/versioning/list.go
  - id: openwiki-source-26b8cb014aa9a83b4faae32d
    resource: repo://modules/buildutils/pkg/versioning/suffixSet.go
  - id: openwiki-source-925a57b76bc49106b0371afe
    resource: repo://modules/buildutils/README.md
  - id: openwiki-source-3fc387923be21a327453a018
    resource: repo://modules/cli/build-locally.sh
  - id: openwiki-source-e8d2eb9ec966368799a7121e
    resource: repo://modules/extensions/build-locally.sh
  - id: openwiki-source-d81d69a930a39e392273844c
    resource: repo://modules/framework/galasa-parent/build.gradle
  - id: openwiki-source-0c791ab0106ecb43b065c363
    resource: repo://modules/gradle/build.gradle
  - id: openwiki-source-e1979c6959dbd41fc63cc098
    resource: repo://modules/maven/galasa-maven-plugin/pom.xml
  - id: openwiki-source-b8a1e6d46ff4407a505b13cb
    resource: repo://modules/platform/dev.galasa.platform/build.gradle
  - id: openwiki-source-36640b90097ff7613d304a43
    resource: repo://modules/wrapping/pom.xml
  - id: openwiki-source-d9cc689740542a78d804536c
    resource: repo://tools/build-locally.sh
  - id: openwiki-source-2f8166d16f7247159a59b5d2
    resource: repo://tools/set-version.sh
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Build System and Dependencies

Galasa uses a multi-module build architecture with three primary build tools: Gradle, Maven, and Go. The build system manages dependency resolution across modules through a carefully orchestrated dependency chain, version synchronization, and artifact publishing to Maven repositories.

## Build Tool Responsibilities

### Gradle
Gradle is the primary build tool for most Galasa modules. The following modules use Gradle:
- **platform** - Defines the dependency platform (BOM) for version constraints
- **gradle** - Galasa Gradle plugins
- **framework** - Core framework components and APIs
- **extensions** - Framework extensions (CPS, RAS, authentication)
- **managers** - Test managers for various technologies
- **ivts** - Integration verification tests
- **cli** - Command-line interface (alongside Go)

Gradle modules use a common configuration pattern with:
- Platform constraints from `dev.galasa.platform`
- Repository resolution order: `mavenLocal()`, custom `sourceMaven`, `gradlePluginPortal()`, `mavenCentral()`
- GPG signing for artifact publishing
- Jacoco for test coverage

### Maven
Maven is used for specific packaging and plugin tasks:
- **wrapping** - Wraps non-OSGi JARs into OSGi bundles using maven-bundle-plugin
- **maven** - Galasa Maven plugin for OBR and test catalog generation
- **obr** - OSGi Bundle Repository assembly and Docker image preparation

Maven projects inherit dependency versions from the platform module's BOM and use maven-gpg-plugin for artifact signing.

### Go
Go is used for build utilities and CLI:
- **buildutils** - Contains the `galasabld` version management utility and `openapi2beans` code generator
- **cli** - galasactl command-line tool implementation

## Dependency Resolution

### Platform Module
The `platform` module (`dev.galasa.platform`) serves as the centralized dependency management platform. It defines version constraints for all external dependencies used across Galasa modules through Gradle's `java-platform` plugin.

Key characteristics:
- Declares over 200 dependency version constraints
- Uses `api platform()` for POM-type dependencies
- Uses `constraints { api ... }` for JAR-type dependencies
- Referenced by other modules via `platform('dev.galasa:dev.galasa.platform:'+version)`
- Ensures consistent dependency versions across all modules

### Repository Resolution Order
Builds resolve dependencies through a defined repository chain:

1. **mavenLocal()** - Local Maven cache (`~/.m2/repository`)
2. **sourceMaven** - Configurable development repository (defaults vary by module)
   - Framework/Extensions/Managers: `https://development.galasa.dev/main/maven-repo/obr/`
   - CLI: `file://` to local maven repository
3. **gradlePluginPortal()** - Gradle plugin repository
4. **mavenCentral()** - Maven Central repository

The `sourceMaven` property allows builds to pull from specific Maven repositories, supporting both local file paths and remote HTTPS URLs.

### Inter-Module Dependency Chain
Modules build in a strict dependency order:

```
platform ──┐
           ├──> wrapping ──┐
           │               ├──> maven ──> framework ──┬──> extensions
           └──> gradle ────┘                          └──> managers ──┐
                                                                      ├──> obr ──> ivts
buildutils ───────────────────────────────────────────────────────┘
```

Each module publishes artifacts to Maven repositories, which subsequent modules consume. The orchestration workflow enforces this order by making later jobs depend on earlier ones completing successfully.

## Version Management

### The galasabld Utility
The `galasabld` command-line utility, built from the `buildutils` module, provides version management for Gradle modules:

**List versions:**
```bash
galasabld versioning list --sourcefolderpath {source-folder}
```
Recursively scans for modules with:
- A `build.gradle` file containing `version = "x.y.z"`
- A `settings.gradle` file containing `rootProject.name = "..."`

**Set version suffix:**
```bash
galasabld versioning suffix set --sourcefolderpath {source-folder} --suffix "-alpha"
```
Strips any existing suffix and adds the new one. The suffix must start with `-` or `_`.

**Remove version suffix:**
```bash
galasabld versioning suffix remove --sourcefolderpath {source-folder}
```
Removes suffixes like `-SNAPSHOT` or `-alpha` from all module versions.

### Synchronized Version Setting
The repository-wide `set-version.sh` script orchestrates version changes across all modules:

1. Calls individual module `set-version.sh` scripts
2. Updates `build.properties` file with `GALASA_VERSION=x.y.z`
3. Updates `README.md` documentation
4. Updates documentation versions

This ensures all modules maintain synchronized version numbers throughout the repository.

## Artifact Publishing

### GPG Signing Requirements
All Galasa artifacts must be GPG-signed before publishing. Builds require three GPG-related secrets/properties:

**For Gradle builds:**
- `signingKeyId` - GPG key ID (short format)
- `signingKey` - Base64-encoded GPG private key
- `signingPassword` - GPG key passphrase

**For Maven builds:**
- `GPG_KEYID` - GPG key ID
- `GPG_KEY` - Base64-encoded GPG private key
- `GPG_PASSPHRASE` - GPG key passphrase

Gradle uses the `signing` plugin with in-memory PGP keys:
```gradle
signing {
    def signingKeyId = findProperty("signingKeyId")
    def signingKey = findProperty("signingKey")
    def signingPassword = findProperty("signingPassword")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign publishing.publications
}
```

Maven uses the `maven-gpg-plugin` with pinentry-mode loopback for non-interactive signing.

### Repository Publishing
Gradle modules publish to repositories defined by the `targetMaven` property:

```gradle
repositories {
    maven {
        url = "$targetMaven"
        if ("$targetMaven".startsWith('http')) {
            credentials {
                username System.getenv("GITHUB_ACTOR")
                password System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Maven modules use distribution management with separate repositories for releases and snapshots:

```xml
<distributionManagement>
    <repository>
        <id>galasa.release.repo</id>
        <url>${galasa.release.repo}</url>
    </repository>
    <snapshotRepository>
        <id>galasa.snapshot.repo</id>
        <url>${galasa.snapshot.repo}</url>
    </snapshotRepository>
</distributionManagement>
```

### GitHub Packages Integration
For remote publishing, builds authenticate to GitHub Container Registry and GitHub Packages using:
- `WRITE_GITHUB_PACKAGES_USERNAME` - GitHub username
- `WRITE_GITHUB_PACKAGES_TOKEN` - Personal access token with `write:packages` scope

These credentials enable pushing Docker images and Maven artifacts to GitHub Packages.

## Local Build Support

### Building Individual Modules
Each module provides a `build-locally.sh` script with common options:
- `--detectsecrets true|false` - Control secret scanning
- `--clean` - Clean build artifacts before building

Modules set environment variables for dependency sources:
- `SOURCE_MAVEN` - Where to pull dependencies from
- `TARGET_MAVEN` - Where to publish built artifacts (defaults to `~/.m2/repository`)

### Building the Entire Repository
The top-level `tools/build-locally.sh` orchestrates building multiple modules:

```bash
./tools/build-locally.sh [OPTIONS]
```

Options:
- `--module <name>` - Start building from a specific module
- `--chain true|false` - Enable/disable building dependent modules
- `--docker` - Build Docker images
- `--minikube` - Push images to minikube registry

The script:
1. Cleans local Maven cache (`~/.m2/repository/dev/galasa`)
2. Builds modules in dependency order
3. Publishes artifacts to local Maven repository

### Priming Fork Builds
For contributors working with forked repositories, the first build requires:
1. Configuring GitHub Actions secrets (GPG keys, GitHub tokens)
2. Enabling GitHub Actions on the fork
3. Manually triggering the "Main Build Orchestrator" workflow

This establishes a baseline of artifacts in the fork's GitHub Packages, enabling subsequent PR builds to download dependencies from previous successful runs.

## Build Orchestration

### Main Build Workflow
The GitHub Actions workflow (`pushes.yaml`) orchestrates the entire build:

1. **Check secrets** - Validates required GPG and GitHub credentials
2. **Build platform** - Publishes dependency platform
3. **Build buildutils** - Creates version management tools
4. **Build wrapping** - Creates OSGi bundle wrappers
5. **Build gradle** - Publishes Gradle plugins
6. **Build maven** - Publishes Maven plugin
7. **Build framework** - Core framework (depends on wrapping, maven, buildutils)
8. **Build extensions & managers** - Framework extensions (parallel)
9. **Build obr** - Assembles OSGi Bundle Repository
10. **Build ivts** - Integration tests (depends on obr for dependencies)
11. **Build CLI** - Command-line interface
12. **Build docs** - Documentation site

Each workflow job:
- Downloads artifacts from prerequisite jobs
- Configures GPG signing credentials
- Builds and tests the module
- Publishes signed artifacts
- Uploads artifacts for dependent jobs

### Artifact Download Strategy
Later workflow jobs download Maven artifacts from earlier jobs using GitHub Actions artifact storage. This avoids rebuilding dependencies and ensures consistency across the build pipeline.

For example, the framework build downloads:
- wrapping artifacts
- gradle artifacts  
- maven artifacts

The artifacts are extracted to a temporary location and provided to the build via the `sourceMaven` property.

## Dependency Management Tools

### Gradle Build Scan
Modules can generate dependency reports:
```bash
gradle allDeps
```
This creates reports showing all direct and transitive dependencies for troubleshooting version conflicts.

### Platform Constraints
The platform module serves as a single source of truth for dependency versions. To update a dependency version:
1. Modify the version constraint in `modules/platform/dev.galasa.platform/build.gradle`
2. Run builds to verify compatibility
3. Commit the updated platform

Some dependencies include comments noting they must be synchronized with other locations (e.g., in `galasa-boot` or `obr/release.yaml`).

### Wrapper Pattern for Non-OSGi Dependencies
The wrapping module converts non-OSGi JARs into OSGi bundles using Maven's bundle plugin. This allows Galasa's OSGi-based framework to consume modern Java libraries that don't provide native OSGi metadata.

Wrapped dependencies include:
- `com.auth0:java-jwt` - JWT authentication
- `io.kubernetes:client-java` - Kubernetes API client
- `io.etcd:jetcd-core` - etcd client
- `org.apache.kafka:kafka-clients` - Kafka client
- Selenium, gRPC, and Apache HttpClient 5

Each wrapper module defines Import-Package and Export-Package directives to control OSGi visibility and dependency wiring.
