---
type: Workflow Guide
title: Creating and Building Test Projects
description: Guide to creating test projects using galasactl project create and building them with Maven or Gradle
tags: [cli, galasactl, maven, gradle, project-creation, build, test-catalog, obr, osgi]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-2c53382a531b29e2cca35a41
    resource: repo://modules/cli/pkg/cmd/projectCreate.go
  - id: openwiki-source-3057af51bffa0abf8534eadf
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/obr-project/build.gradle.template
  - id: openwiki-source-ba0561a0f7e29328198e0efa
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/obr-project/pom.xml
  - id: openwiki-source-76d87540cf5af406b11add1f
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/pom.xml
  - id: openwiki-source-3c11962e8781a24ba95d987d
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/settings.gradle.template
  - id: openwiki-source-2d14287c68737e69e9ceeba5
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/test-project/bnd.bnd
  - id: openwiki-source-78466b810a8e9181ca26759b
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/test-project/build.gradle.template
  - id: openwiki-source-9e939e448e4abfc2c99b457d
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/test-project/pom.xml
  - id: openwiki-source-43186f8352c88221c1dd45a5
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/test-project/src/main/java/TestSimple.java.template
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-d2a3fc5c55ba8b6a4ade5ca1
    resource: repo://modules/gradle/README.md
  - id: openwiki-source-4b4fa74b099fc828fc2dcf8f
    resource: repo://modules/maven/README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Creating and Building Test Projects

Galasa provides the `galasactl project create` command to scaffold new test projects with a proper structure, build configuration, and sample tests. This guide explains how to create projects, understand the generated structure, and build them using Maven or Gradle.

## Creating a Test Project

### Basic Project Creation

The simplest project creation requires only a package name:

```bash
galasactl project create --package dev.galasa.example.banking
```

This generates a Maven-based project structure with:
- Parent POM defining common dependencies and build configuration
- Test bundle (`dev.galasa.example.banking.feature1`) with sample tests
- Proper OSGi bundle metadata via `maven-bundle-plugin`

### Project Creation Options

#### Specifying Features

Create multiple test bundles for different application features:

```bash
galasactl project create --package dev.galasa.example.banking --features payee,account
```

This creates separate bundles for each feature:
- `dev.galasa.example.banking.payee` - Tests for payee functionality
- `dev.galasa.example.banking.account` - Tests for account functionality

Each feature bundle is an independent OSGi bundle containing its own test classes.

#### Creating an OBR Project

Add the `--obr` flag to generate an OSGi Bundle Repository project:

```bash
galasactl project create --package dev.galasa.example.banking --features payee,account --obr
```

The OBR project packages all test bundles together and generates:
- A `galasa.obr` XML index file cataloging bundle metadata
- A merged `testcatalog.json` file listing all test classes
- Maven/Gradle configuration to publish these artifacts

#### Choosing Build Tools

By default, `galasactl project create` generates Maven artifacts. You can explicitly choose build tools:

**Maven only (default):**
```bash
galasactl project create --package dev.galasa.example.banking --maven
```

**Gradle only:**
```bash
galasactl project create --package dev.galasa.example.banking --gradle
```

**Both Maven and Gradle:**
```bash
galasactl project create --package dev.galasa.example.banking --maven --gradle
```

Using both allows developers to choose their preferred build tool.

#### Using Development Versions

The `--development` flag configures the project to use bleeding-edge Galasa builds:

```bash
galasactl project create --package dev.galasa.example.banking --obr --development
```

This adds the development Maven repository (`https://development.galasa.dev/main/maven-repo/obr`) to the generated build files.

## Generated Project Structure

### Maven Project Layout

A Maven project with OBR looks like this:

```
dev.galasa.example.banking/
├── pom.xml                                    # Parent POM
├── .gitignore                                 # Git ignore patterns
├── dev.galasa.example.banking.payee/          # Test bundle
│   ├── pom.xml                                # Bundle POM
│   └── src/main/java/
│       └── dev/galasa/example/banking/payee/
│           ├── TestPayee.java                 # Sample test
│           └── TestPayeeExtended.java         # Extended sample
├── dev.galasa.example.banking.account/        # Test bundle
│   ├── pom.xml
│   └── src/main/java/
│       └── dev/galasa/example/banking/account/
│           ├── TestAccount.java
│           └── TestAccountExtended.java
└── dev.galasa.example.banking.obr/            # OBR project
    └── pom.xml                                # OBR POM
```

### Gradle Project Layout

A Gradle project includes additional files:

```
dev.galasa.example.banking/
├── settings.gradle                            # Gradle settings
├── .gitignore
├── dev.galasa.example.banking.payee/
│   ├── build.gradle                           # Gradle build script
│   ├── bnd.bnd                                # OSGi bundle metadata
│   └── src/main/java/...
├── dev.galasa.example.banking.account/
│   ├── build.gradle
│   ├── bnd.bnd
│   └── src/main/java/...
└── dev.galasa.example.banking.obr/
    └── build.gradle                           # OBR build script
```

### Key Generated Files

#### Parent POM (`pom.xml`)

The parent POM defines:
- **Common dependencies**: Galasa framework, managers, test utilities
- **Dependency management**: Imports `galasa-bom` for version management
- **Plugin configuration**: Maven bundle plugin and Galasa Maven plugin
- **Test catalog goals**: `bundletestcat` (build) and `deploytestcat` (deploy)
- **Repository configuration**: Maven Central and optionally Galasa development repo

```xml
<properties>
    <galasa.skip.deploytestcatalog>true</galasa.skip.deploytestcatalog>
    <galasa.token>${GALASA_TOKEN}</galasa.token>
    <galasa.bootstrap>${GALASA_BOOTSTRAP}</galasa.bootstrap>
    <galasa.test.stream>${GALASA_STREAM}</galasa.test.stream>
</properties>
```

#### Test Bundle POM

Each test bundle POM:
- Inherits from parent POM
- Uses `<packaging>bundle</packaging>` for OSGi bundle generation
- Declares dependencies on Galasa framework and managers
- Includes manager bundle dependency if using a custom manager

#### Gradle Settings (`settings.gradle`)

Defines plugin repositories and includes all subprojects:

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

include 'dev.galasa.example.banking.payee'
include 'dev.galasa.example.banking.account'
include 'dev.galasa.example.banking.obr'
```

#### Test Bundle Gradle Build (`build.gradle`)

Applies plugins and defines dependencies:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
    id 'dev.galasa.tests' version '1.1.1'
    id 'biz.aQute.bnd.builder' version '7.3.0'
}

dependencies {
    implementation platform('dev.galasa:galasa-bom:1.1.1')
    implementation 'dev.galasa:dev.galasa'
    implementation 'dev.galasa:dev.galasa.framework'
    implementation 'dev.galasa:dev.galasa.core.manager'
    // More dependencies...
}

publishing {
    publications {
        maven(MavenPublication) {
            from components.java
        }
    }
}
```

The `dev.galasa.tests` plugin automatically generates a test catalog for each bundle during the build.

#### OBR Gradle Build

The OBR project build.gradle applies OBR and test catalog plugins:

```groovy
plugins {
    id 'base'
    id 'maven-publish'
    id 'dev.galasa.obr' version '1.1.1'
    id 'dev.galasa.testcatalog' version '1.1.1'
}

dependencies {
    bundle project(':dev.galasa.example.banking.payee')
    bundle project(':dev.galasa.example.banking.account')
}

def testcatalog = file('build/testcatalog.json')
def obrFile = file('build/galasa.obr')

publishing {
    publications {
        maven(MavenPublication) {
            artifact obrFile
            artifact (testcatalog) {
                classifier "testcatalog"
                extension "json"
            }
        }
    }
}
```

#### OSGi Bundle Metadata (`bnd.bnd`)

Gradle projects use BND to generate OSGi manifests:

```properties
Bundle-Version: 0.0.1-SNAPSHOT
Bundle-Name: dev.galasa.example.banking.payee
Import-Package: *
```

The `Import-Package: *` directive imports all packages referenced in the compiled code. Maven projects use `maven-bundle-plugin` which automatically generates similar metadata.

#### Sample Test Classes

Each test bundle contains sample test classes demonstrating Galasa annotations:

```java
package dev.galasa.example.banking.payee;

import static org.assertj.core.api.Assertions.*;
import dev.galasa.core.manager.*;
import dev.galasa.Test;

@Test
public class TestPayee {

    @CoreManager
    public ICoreManager core;

    @Test
    public void simpleSampleTest() {
        assertThat(core).isNotNull();
    }
}
```

## Building Test Projects

### Building with Maven

#### Standard Build

Build all modules and generate test catalogs:

```bash
mvn clean install
```

This:
1. Compiles Java source files
2. Generates OSGi bundle manifests
3. Packages bundles as JARs
4. Builds test catalogs (one per bundle)
5. Installs artifacts to local Maven repository (`~/.m2/repository`)

If an OBR project exists, it also:
6. Generates the `galasa.obr` index file
7. Merges test catalogs from all bundles

#### Build Phases

Maven executes these phases:
- **`compile`** - Compiles Java sources
- **`package`** - Creates JAR files and runs `bundletestcat` goal to generate test catalogs
- **`install`** - Installs artifacts to local Maven repository

#### Skipping Test Catalog Generation

To skip building test catalogs:

```bash
mvn clean install -Dgalasa.skip.bundletestcatalog=true
```

Or add to parent POM:

```xml
<properties>
    <galasa.skip.bundletestcatalog>true</galasa.skip.bundletestcatalog>
</properties>
```

#### Deploying Test Catalogs to Ecosystem

To deploy test catalogs to a Galasa ecosystem, first set `galasa.skip.deploytestcatalog` to `false` in the POM, then run:

```bash
mvn deploy -DGALASA_TOKEN=${GALASA_TOKEN} \
           -DGALASA_BOOTSTRAP=${GALASA_BOOTSTRAP} \
           -DGALASA_STREAM=mystream
```

The `deploy` phase executes the `deploytestcat` goal, which uploads test catalogs to the specified ecosystem.

### Building with Gradle

#### Standard Build and Publish

Build and publish to local Maven repository:

```bash
gradle clean build publishToMavenLocal
```

This:
1. Compiles Java sources
2. Generates OSGi manifests from `bnd.bnd` files
3. Packages bundles as JARs
4. Builds test catalogs via `dev.galasa.tests` plugin
5. Publishes artifacts to local Maven repository (`~/.m2/repository`)

If an OBR project exists:
6. Runs `genobr` task to create `galasa.obr`
7. Runs `mergetestcat` task to merge test catalogs
8. Publishes OBR and merged test catalog

#### Build Tasks

Key Gradle tasks:
- **`build`** - Compiles and packages all bundles
- **`publishToMavenLocal`** - Publishes to local Maven repository
- **`genobr`** - Generates OBR index (OBR project only)
- **`mergetestcat`** - Merges test catalogs (OBR project only)

#### Deploying Test Catalogs (Deprecated)

The old method of deploying test catalogs directly from Gradle is deprecated as of v0.33.0:

```bash
# Deprecated - do not use
gradle deploytestcat \
    -DGALASA_BOOTSTRAP=$GALASA_BOOTSTRAP \
    -DGALASA_STREAM=$GALASA_STREAM \
    -DGALASA_TOKEN=$GALASA_TOKEN
```

**New method:** Publish test catalogs to a Maven repository, then use `galasactl properties set` to configure the test stream's `location` property to point to the catalog URL in the Maven repository.

## Understanding Test Catalogs

### What is a Test Catalog?

A test catalog is a JSON file listing all test classes in a bundle with their metadata:

```json
{
  "classes": [
    {
      "bundle": "dev.galasa.example.banking.payee",
      "name": "dev.galasa.example.banking.payee.TestPayee",
      "methods": [
        {
          "name": "simpleSampleTest",
          "type": "Test"
        }
      ]
    }
  ]
}
```

### How Test Catalogs are Built

#### Maven Process

The `galasa-maven-plugin` executes the `bundletestcat` goal during the `package` phase:

1. Scans compiled classes in `target/classes`
2. Identifies classes annotated with `@Test`
3. Identifies test methods annotated with `@Test`
4. Generates `target/testcatalog.json`
5. Attaches catalog as a Maven artifact

#### Gradle Process

The `dev.galasa.tests` plugin automatically:

1. Registers a `buildTestCatalog` task
2. Scans compiled classes for `@Test` annotations
3. Generates test catalog in `build/testcatalog.json`
4. Makes it available for merging by OBR projects

### Merged Test Catalogs in OBR Projects

When an OBR project builds, it merges test catalogs from all bundle dependencies:

**Maven:** The `galasa-maven-plugin` with `<packaging>galasa-obr</packaging>` automatically merges catalogs from all `<dependency>` bundles.

**Gradle:** The `dev.galasa.testcatalog` plugin provides the `mergetestcat` task:

```groovy
tasks.withType(PublishToMavenLocal) { task ->
    task.dependsOn mergetestcat
}
```

The merged catalog is published as a Maven artifact with classifier `testcatalog` and extension `json`:

```
dev.galasa.example.banking:dev.galasa.example.banking.obr:0.0.1-SNAPSHOT:testcatalog:json
```

### Using Test Catalogs in Ecosystems

Test catalogs enable the Galasa ecosystem to:
- Discover available tests without loading bundle classes
- Filter tests by package, class name, or tags
- Schedule test runs using `galasactl runs prepare`
- Display test inventories in the Web UI

To make tests available in an ecosystem, configure the test stream's `location` property to point to the published test catalog URL.

## Creating Manager Projects

Managers are reusable components that provide test infrastructure and inject resources into test classes. Use the `--manager` flag to create a manager project instead of test projects.

### Creating a Manager

```bash
galasactl project create --package dev.galasa.example.docker --manager --managerName docker
```

If `--managerName` is omitted, the tool uses the last segment of the package name (e.g., `docker` from `dev.galasa.example.docker`).

### Generated Manager Structure

```
dev.galasa.example.docker/
├── pom.xml                                    # Parent POM
└── dev.galasa.example.docker.manager/         # Manager bundle
    ├── pom.xml
    ├── src/main/java/dev/galasa/example/docker/
    │   ├── DockerResource.java                # Annotation for injection
    │   ├── IDockerResource.java               # Resource interface
    │   ├── IDockerManager.java                # Manager interface
    │   ├── DockerManagerException.java        # Custom exception
    │   └── internal/
    │       ├── DockerManagerImpl.java         # Manager implementation
    │       ├── DockerResourceImpl.java        # Resource implementation
    │       ├── DockerManagerField.java        # Field injection handler
    │       ├── DockerResourceManagement.java  # Resource lifecycle
    │       └── properties/
    │           ├── DockerPropertiesSingleton.java
    │           └── DockerExampleProperty.java
    └── src/test/java/dev/galasa/example/docker/internal/
        └── DockerManagerImplTest.java         # Unit test
```

### Manager Components

The generated manager includes:

1. **Annotation (`@DockerResource`)** - Marks fields for resource injection
2. **Resource Interface (`IDockerResource`)** - Public API for injected resources
3. **Manager Interface (`IDockerManager`)** - Manager capabilities (optional for advanced use)
4. **Manager Implementation (`DockerManagerImpl`)** - Implements manager lifecycle:
   - `initialise()` - Called when manager loads
   - `youAreRequired()` - Called when a test needs this manager
   - `provisionGenerate()` - Provisions resources before test execution
   - `provisionDiscard()` - Cleans up resources after test execution
5. **Resource Implementation** - Implements resource interface
6. **Field Handler (`DockerManagerField`)** - Processes `@DockerResource` annotations
7. **Resource Management** - Manages resource lifecycle and state
8. **Properties** - CPS property definitions for configuration
9. **Exception** - Custom exception type for manager errors
10. **Unit Test** - Basic test for manager implementation

### Combining Manager and Test Projects

Create both a manager and test projects together:

```bash
galasactl project create --package dev.galasa.example \
    --manager --managerName example \
    --features banking,account --obr
```

This generates:
- Manager bundle (`dev.galasa.example.manager`)
- Test bundles (`dev.galasa.example.banking`, `dev.galasa.example.account`)
- OBR project including both manager and test bundles

Test bundles automatically include a dependency on the manager and demonstrate using the manager's resource injection.

### Building Manager Projects

Build manager projects the same way as test projects:

**Maven:**
```bash
mvn clean install
```

**Gradle:**
```bash
gradle clean build publishToMavenLocal
```

The manager bundle is packaged as a standard OSGi bundle with proper `Export-Package` and `Import-Package` declarations.

## Maven vs Gradle

### When to Use Maven

Maven is the traditional Galasa build tool and offers:
- **Stability**: Well-established integration with Galasa framework
- **Convention**: Less configuration needed for standard projects
- **IDE Support**: Excellent integration with Eclipse, IntelliJ IDEA
- **Documentation**: Most Galasa examples use Maven

### When to Use Gradle

Gradle provides:
- **Flexibility**: Groovy/Kotlin DSL for complex build logic
- **Performance**: Incremental builds and build caching
- **Modern Tooling**: Better CI/CD integration
- **Multi-language**: Easier to combine Java, Groovy, Kotlin

### Key Differences

| Aspect | Maven | Gradle |
|--------|-------|--------|
| Build files | `pom.xml` | `build.gradle`, `settings.gradle` |
| OSGi metadata | Auto-generated by `maven-bundle-plugin` | Defined in `bnd.bnd`, processed by BND plugin |
| Test catalog | `bundletestcat` goal | `dev.galasa.tests` plugin |
| OBR generation | `galasa-obr` packaging | `dev.galasa.obr` plugin |
| Local publish | `mvn install` | `gradle publishToMavenLocal` |
| Configuration | XML | Groovy DSL |

Both tools produce identical artifacts (JAR bundles, OBRs, test catalogs) and can be used interchangeably.

## Best Practices

### Project Organization

- **Feature-based bundles**: Create separate bundles for distinct application features
- **Manager separation**: Keep manager code separate from test code
- **OBR per project**: Each project should have its own OBR for independent deployment

### Dependency Management

- **Use galasa-bom**: Import the Galasa BOM for consistent versions
- **Scope correctly**: Mark Galasa dependencies as `provided` or `compileOnly`
- **Minimize manager dependencies**: Only depend on managers you actually use

### Build Configuration

- **Local first**: Always test builds locally before CI/CD
- **Version snapshots**: Use `-SNAPSHOT` versions during development
- **Skip catalog deploy**: Keep `galasa.skip.deploytestcatalog=true` until ready to deploy

### Test Catalog Management

- **Build catalogs regularly**: Include catalog generation in standard builds
- **Merge in OBR**: Use OBR projects to merge catalogs from multiple bundles
- **Publish to Maven**: Store test catalogs in Maven repositories for easy access

## Troubleshooting

### Common Issues

#### "Bundle symbolicname not found"

**Cause:** OSGi manifest not generated properly.

**Maven solution:**
```xml
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <extensions>true</extensions>
</plugin>
```

**Gradle solution:** Ensure `bnd.bnd` file exists in bundle root.

#### "Cannot resolve package imports"

**Cause:** Missing dependencies or incorrect Import-Package directives.

**Solution:** Add required dependencies to POM/build.gradle and verify `Import-Package` in manifest.

#### "Test catalog empty"

**Cause:** Test classes not annotated with `@Test` or not compiled.

**Solution:** Ensure test classes have `@Test` annotation and run full build before catalog generation.

#### "Failed to deploy test catalog"

**Cause:** Invalid credentials or incorrect ecosystem URL.

**Solution:** Verify `GALASA_TOKEN`, `GALASA_BOOTSTRAP`, and `GALASA_STREAM` environment variables.

### Debugging Builds

#### Maven Debug

```bash
mvn clean install -X  # Debug output
mvn clean install -e  # Show full exception stack traces
```

#### Gradle Debug

```bash
gradle clean build --debug     # Debug output
gradle clean build --info      # Info output
gradle clean build --stacktrace # Show stack traces
```

### Verifying Artifacts

After building, verify artifacts are in the local Maven repository:

```bash
ls -la ~/.m2/repository/dev/galasa/example/banking/
```

You should see:
- Bundle JAR files
- OBR XML files
- Test catalog JSON files
- POM files

## Related Documentation

- [OSGi Bundle Architecture and OBRs](/openwiki/concepts/osgi-bundles.md) - Deep dive into OSGi bundles and OBR structure
- [Building Locally](/openwiki/workflows/building-locally.md) - Building the entire Galasa framework locally
- [CLI Overview](/openwiki/architecture/cli.md) - Complete galasactl command reference
