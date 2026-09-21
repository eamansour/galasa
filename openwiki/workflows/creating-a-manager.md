---
type: Development Workflow
title: Creating a Custom Galasa Manager
description: Complete guide to implementing a custom Galasa Manager including scaffolding generation, interface implementation, lifecycle methods, annotation handling, and OSGi configuration.
tags: [manager, development, workflow, lifecycle, annotations, osgi, dependency-injection]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-2c53382a531b29e2cca35a41
    resource: repo://modules/cli/pkg/cmd/projectCreate.go
  - id: openwiki-source-f0226f4e45c050c7b402602c
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/manager-project/bnd.bnd
  - id: openwiki-source-b34491ce0391d8c6928d387c
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/manager-project/internal/ManagerField.java
  - id: openwiki-source-af76c07c16e4acdb8ebe2b94
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/manager-project/internal/ManagerImpl.java
  - id: openwiki-source-d5a81adf65d5a1e1946d61bf
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/manager-project/ManagerAnnotation.java
  - id: openwiki-source-2e97bf2e5152c6a56d3cbe4b
    resource: repo://modules/cli/pkg/embedded/templates/projectCreate/parent-project/manager-project/pom.xml
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-901ac9331df78d7c4e0f651e
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/AbstractManager.java
  - id: openwiki-source-1a4c1dafb642d7c25fae4118
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/IManager.java
  - id: openwiki-source-9833f70e1005d58ee8b1214c
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.docker.manager/src/main/java/dev/galasa/docker/internal/DockerManagerImpl.java
  - id: openwiki-source-01640e443453816b0dc08053
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.http.manager/bnd.bnd
  - id: openwiki-source-14f4785980844ebc833b8a59
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.http.manager/src/main/java/dev/galasa/http/HttpClient.java
  - id: openwiki-source-97538d179a6aad3e76371829
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.http.manager/src/main/java/dev/galasa/http/internal/HttpManagerField.java
  - id: openwiki-source-99a2f95b2a80bf064687a53c
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.http.manager/src/main/java/dev/galasa/http/internal/HttpManagerImpl.java
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Creating a Custom Galasa Manager

This guide walks through the complete process of creating a custom Galasa Manager, from scaffolding generation to implementing resource provisioning, lifecycle methods, and annotation-based field injection.

## Overview

A Galasa Manager is a reusable component that provides test infrastructure and resource management capabilities. Managers:

- **Provision and manage resources** (containers, databases, connections, etc.)
- **Inject resources into test classes** via annotations
- **Follow a defined lifecycle** with initialization, provisioning, and cleanup phases
- **Declare dependencies on other managers** for layered functionality
- **Register as OSGi services** for automatic discovery by the framework

## Prerequisites

Before creating a Manager, ensure you have:

- Galasa CLI (`galasactl`) installed and initialized
- Java 17 or later
- Maven 3.6+ or Gradle 7+ (depending on your build preference)
- Understanding of the [Manager lifecycle](/openwiki/concepts/manager-lifecycle.md) and [architecture](/openwiki/architecture/managers.md)

## Step 1: Generate Manager Skeleton

The `galasactl project create` command generates a complete Manager project structure with best-practice implementations.

### Basic Manager Creation

Create a standalone Manager project:

```bash
galasactl project create --package dev.galasa.example.docker --manager
```

This generates a manager named "docker" (derived from the last package segment). To specify a different name:

```bash
galasactl project create --package dev.galasa.example --manager --managerName mymanager
```

### Manager with Tests

Create both a Manager and test projects (similar to SimBank structure):

```bash
galasactl project create --package dev.galasa.example \
    --manager --managerName example \
    --features banking,account \
    --obr
```

This creates:
- `dev.galasa.example.manager` - The Manager bundle
- `dev.galasa.example.banking` - Test project using the Manager
- `dev.galasa.example.account` - Another test project
- `dev.galasa.example.obr` - OSGi Bundle Repository combining all bundles

### Build Tool Selection

By default, Maven build files are generated. Choose build tools explicitly:

```bash
# Maven only
galasactl project create --package dev.galasa.example --manager --maven

# Gradle only
galasactl project create --package dev.galasa.example --manager --gradle

# Both Maven and Gradle
galasactl project create --package dev.galasa.example --manager --maven --gradle
```

### Development Versions

To use bleeding-edge Galasa versions and repositories:

```bash
galasactl project create --package dev.galasa.example --manager --development
```

## Step 2: Understanding Generated Files

The `galasactl project create --manager` command generates a complete Manager implementation with the following structure:

```
dev.galasa.example.manager/
├── pom.xml (or build.gradle)      # Build configuration
├── bnd.bnd                         # OSGi bundle manifest
└── src/
    ├── main/java/dev/galasa/example/
    │   ├── ExampleResource.java              # Public annotation
    │   ├── IExampleManager.java              # Manager interface for dependencies
    │   ├── IExampleResource.java             # Resource interface (TPI)
    │   ├── ExampleManagerException.java      # Custom exception
    │   └── internal/
    │       ├── ExampleManagerField.java      # Internal marker annotation
    │       ├── ExampleManagerImpl.java       # Manager implementation
    │       ├── ExampleResourceImpl.java      # Resource implementation
    │       ├── ExampleResourceManagement.java # Resource pooling/tracking
    │       └── properties/
    │           ├── ExamplePropertiesSingleton.java  # CPS access
    │           └── ExampleExampleProperty.java      # Example property
    └── test/java/dev/galasa/example/internal/
        └── ExampleManagerImplTest.java       # Unit test
```

### Key Generated Files

#### 1. Public Annotation (`ExampleResource.java`)

The annotation that test classes use to request resources:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IExampleResource.class })
@ExampleManagerField
public @interface ExampleResource {
    String resourceTag() default "PRIMARY";
}
```

Tests use this annotation like:

```java
@ExampleResource(resourceTag = "PRIMARY")
public IExampleResource resource;
```

#### 2. Resource Interface (`IExampleResource.java`)

The Test Programming Interface (TPI) that tests interact with:

```java
public interface IExampleResource {
    String getTag();
    // Add methods tests will use
}
```

This is the public API tests use to interact with provisioned resources. Add methods for resource operations.

#### 3. Manager Interface (`IExampleManager.java`)

The interface for manager-to-manager dependencies:

```java
public interface IExampleManager {
    // Add methods for dependent managers
}
```

Keep this minimal. Most functionality should be in `IExampleResource`. This interface is for other Managers that depend on your Manager.

#### 4. Manager Implementation (`ExampleManagerImpl.java`)

The core implementation extending `AbstractManager`:

```java
@Component(service = { IManager.class })
public class ExampleManagerImpl extends AbstractManager implements IExampleManager {
    
    protected static final String NAMESPACE = "example";
    
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        // Initialize CPS
        try {
            ExamplePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (Exception e) {
            throw new ManagerException("Unable to initialise CPS for Example Manager", e);
        }
        
        // Check if manager is needed
        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(ExampleManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }
    
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
            @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);
    }
    
    @GenerateAnnotatedField(annotation = ExampleResource.class)
    public IExampleResource generateExampleResource(Field field, List<Annotation> annotations)
            throws ExampleManagerException {
        ExampleResource annotation = field.getAnnotation(ExampleResource.class);
        return new ExampleResourceImpl(annotation.resourceTag());
    }
    
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ExampleManagerField.class);
    }
    
    @Override
    public void provisionDiscard() {
        // Clean up resources
    }
}
```

#### 5. Resource Implementation (`ExampleResourceImpl.java`)

The concrete implementation of the resource interface:

```java
public class ExampleResourceImpl implements IExampleResource {
    private final String tag;
    
    public ExampleResourceImpl(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag cannot be null");
    }
    
    @Override
    public String getTag() {
        return this.tag;
    }
}
```

#### 6. Field Handler (`ExampleManagerField.java`)

Internal marker annotation linking public annotations to the Manager:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ExampleManagerField {
    // Marker annotation
}
```

This annotation is applied to the public `@ExampleResource` annotation to indicate which Manager handles it.

#### 7. Custom Exception (`ExampleManagerException.java`)

Manager-specific exception for error handling:

```java
public class ExampleManagerException extends ManagerException {
    public ExampleManagerException(String message) {
        super(message);
    }
    
    public ExampleManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Step 3: Implementing the IManager Interface

### Core Lifecycle Methods

The `IManager` interface defines the complete Manager lifecycle. `AbstractManager` provides default implementations, so you only override methods where you need custom behavior.

#### initialise()

Called during framework startup to determine if the Manager is needed:

```java
@Override
public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
        @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
    super.initialise(framework, allManagers, activeManagers, galasaTest);
    
    // Store framework reference for later use
    this.framework = framework;
    
    // Initialize Configuration Property Store
    try {
        ExamplePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
    } catch (Exception e) {
        throw new ManagerException("Unable to initialise CPS", e);
    }
    
    // Check if test uses our annotations
    if (galasaTest.isJava()) {
        List<AnnotatedField> ourFields = findAnnotatedFields(ExampleManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
    }
}
```

#### youAreRequired()

Activates the Manager and declares dependencies:

```java
@Override
public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
        @NotNull GalasaTest galasaTest) throws ManagerException {
    // Prevent duplicate activation
    if (activeManagers.contains(this)) {
        return;
    }
    
    activeManagers.add(this);
    
    // Declare dependencies on other managers
    httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
    if (httpManager == null) {
        throw new ExampleManagerException("HTTP Manager is required but not available");
    }
}
```

#### areYouProvisionalDependentOn()

Declares provisioning-phase dependencies to establish manager ordering:

```java
@Override
public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    // Ensure HTTP Manager provisions before us
    if (otherManager instanceof IHttpManager) {
        return true;
    }
    return false;
}
```

### Resource Provisioning Methods

The provisioning lifecycle has three phases, executed in dependency order:

#### provisionGenerate()

Allocate resource names and pool entries without creating actual resources:

```java
@Override
public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    // Generate annotated field instances
    generateAnnotatedFields(ExampleManagerField.class);
    
    // Check resource availability
    if (!isResourceAvailable()) {
        throw new ResourceUnavailableException("Example resource pool exhausted");
    }
    
    // Allocate resource names/identifiers
    allocateResourceIdentifiers();
}
```

If resources are unavailable, throw `ResourceUnavailableException` to put the test run into waiting state (in automated environments).

#### provisionBuild()

Create and configure resources:

```java
@Override
public void provisionBuild() throws ManagerException, ResourceUnavailableException {
    // Create actual resources
    for (IExampleResource resource : resources) {
        createResource(resource);
        configureResource(resource);
    }
}
```

#### provisionStart()

Start resources and establish connections:

```java
@Override
public void provisionStart() throws ManagerException, ResourceUnavailableException {
    // Start resources
    for (IExampleResource resource : resources) {
        startResource(resource);
        validateResourceReady(resource);
    }
}
```

### Cleanup Methods

#### provisionStop()

Gracefully stop resources:

```java
@Override
public void provisionStop() {
    for (IExampleResource resource : resources) {
        try {
            stopResource(resource);
        } catch (Exception e) {
            logger.warn("Failed to stop resource " + resource.getTag(), e);
        }
    }
}
```

#### provisionDiscard()

Delete resources and free allocations:

```java
@Override
public void provisionDiscard() {
    for (IExampleResource resource : resources) {
        try {
            deleteResource(resource);
            returnToPool(resource);
        } catch (Exception e) {
            logger.warn("Failed to discard resource " + resource.getTag(), e);
        }
    }
}
```

#### shutdown()

Final cleanup called by the framework:

```java
@Override
public void shutdown() {
    // Close connections, release system resources
    closeConnections();
    releaseSystemResources();
}
```

Cleanup methods should **not** throw exceptions. Log errors and continue cleanup.

## Step 4: Annotation Handling and Field Injection

Managers use annotations to inject resources into test class fields. The framework automatically discovers and invokes generation methods.

### Defining the Annotation

Your public annotation is marked with `@ExampleManagerField` to link it to your Manager:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IExampleResource.class })
@ExampleManagerField
public @interface ExampleResource {
    String resourceTag() default "PRIMARY";
    boolean autoStart() default true;
    String configFile() default "";
}
```

### Implementing the Generator Method

Use `@GenerateAnnotatedField` to mark methods that create resource instances:

```java
@GenerateAnnotatedField(annotation = ExampleResource.class)
public IExampleResource generateExampleResource(Field field, List<Annotation> annotations)
        throws ExampleManagerException {
    
    // Extract annotation parameters
    ExampleResource annotation = field.getAnnotation(ExampleResource.class);
    String tag = annotation.resourceTag();
    boolean autoStart = annotation.autoStart();
    String configFile = annotation.configFile();
    
    // Create resource instance
    ExampleResourceImpl resource = new ExampleResourceImpl(tag);
    resource.setAutoStart(autoStart);
    
    if (!configFile.isEmpty()) {
        resource.loadConfiguration(configFile);
    }
    
    // Track for later lifecycle operations
    resources.add(resource);
    
    return resource;
}
```

The method must:
- Be `public`
- Return the interface type (`IExampleResource`)
- Accept `Field` and `List<Annotation>` parameters
- Be annotated with `@GenerateAnnotatedField`

### Triggering Field Generation

Call `generateAnnotatedFields()` in `provisionGenerate()`:

```java
@Override
public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    // Framework invokes all @GenerateAnnotatedField methods
    generateAnnotatedFields(ExampleManagerField.class);
    
    // Now all resources are created and in the resources list
}
```

### Multiple Annotation Support

Support multiple annotations for different resource types:

```java
@GenerateAnnotatedField(annotation = ExampleContainer.class)
public IExampleContainer generateContainer(Field field, List<Annotation> annotations) {
    // Generate container resource
}

@GenerateAnnotatedField(annotation = ExampleVolume.class)
public IExampleVolume generateVolume(Field field, List<Annotation> annotations) {
    // Generate volume resource
}

@Override
public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    generateAnnotatedFields(ExampleManagerField.class);
    // Invokes both generator methods
}
```

## Step 5: Depending on Other Managers

Managers frequently build on functionality provided by other Managers. The framework provides utilities for declaring and accessing dependencies.

### Declaring Dependencies in youAreRequired()

Use `addDependentManager()` to locate and activate required Managers:

```java
@Override
public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
        @NotNull GalasaTest galasaTest) throws ManagerException {
    if (activeManagers.contains(this)) {
        return;
    }
    activeManagers.add(this);
    
    // Require HTTP Manager (via SPI interface)
    httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
    if (httpManager == null) {
        throw new ExampleManagerException("HTTP Manager is required but not available");
    }
    
    // Require Artifact Manager
    artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
    if (artifactManager == null) {
        throw new ExampleManagerException("Artifact Manager is required but not available");
    }
}
```

Store Manager references as instance fields:

```java
private IHttpManagerSpi httpManager;
private IArtifactManager artifactManager;
```

### Declaring Provisioning Order

Use `areYouProvisionalDependentOn()` to ensure dependencies provision first:

```java
@Override
public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    // Ensure HTTP Manager provisions before us
    if (otherManager instanceof IHttpManager) {
        return true;
    }
    // Ensure Platform Manager provisions before us
    if (otherManager instanceof IPlatformManager) {
        return true;
    }
    return false;
}
```

This ensures the framework calls `provisionGenerate()`, `provisionBuild()`, and `provisionStart()` on dependencies before calling your Manager's methods.

### Using Dependent Managers

Once declared, use dependent Managers in your provisioning and resource methods:

```java
@Override
public void provisionBuild() throws ManagerException {
    // Use HTTP Manager to create client
    IHttpClient client = httpManager.newHttpClient();
    
    // Use Artifact Manager to retrieve configuration
    Path config = artifactManager.getBundleResource(getClass(), "config.json");
    
    // Create resources using dependent manager capabilities
    for (IExampleResource resource : resources) {
        createResourceViaHttp(resource, client, config);
    }
}
```

### Optional Dependencies

For optional dependencies, check for `null` and adjust behavior:

```java
@Override
public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
        @NotNull GalasaTest galasaTest) throws ManagerException {
    if (activeManagers.contains(this)) {
        return;
    }
    activeManagers.add(this);
    
    // Optional dependency
    artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
    // Don't throw if null - adjust functionality instead
}
```

## Step 6: OSGi Bundle Configuration

Managers are packaged as OSGi bundles and must properly export public APIs and import dependencies.

### bnd.bnd Configuration

The `bnd.bnd` file controls OSGi manifest generation:

```properties
-snapshot: ${tstamp}
Bundle-Name: Example Manager
Export-Package: !dev.galasa.example.internal*;dev.galasa.example*
Import-Package: !javax.validation.constraints,\
                !org.osgi.service.component.annotations,\
                *
```

**Key directives:**

- **Export-Package**: Export public APIs, exclude internal packages
  - `!dev.galasa.example.internal*` - Exclude internal packages
  - `dev.galasa.example*` - Export all other packages
- **Import-Package**: Control dependency imports
  - `!javax.validation.constraints` - Optional dependency, don't fail if missing
  - `!org.osgi.service.component.annotations` - Build-time only, don't import
  - `*` - Import everything else automatically

### Export-Package Patterns

Follow these patterns for proper API visibility:

**Export public API packages:**
```properties
Export-Package: dev.galasa.example,dev.galasa.example.spi
```

**Exclude internal packages:**
```properties
Export-Package: !dev.galasa.example.internal*;dev.galasa.example*
```

**Export with version:**
```properties
Export-Package: dev.galasa.example;version="1.0.0"
```

### Maven Configuration

The Maven POM uses the `maven-bundle-plugin` to generate the OSGi manifest:

```xml
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <version>5.1.9</version>
    <extensions>true</extensions>
    <configuration>
        <instructions>
            <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
            <Export-Package>dev.galasa.example,dev.galasa.example.spi</Export-Package>
            <Import-Package>
                !javax.validation.constraints,
                !org.osgi.service.component.annotations,
                *
            </Import-Package>
        </instructions>
    </configuration>
</plugin>
```

### Gradle Configuration

For Gradle builds, use the `biz.aQute.bnd` plugin:

```gradle
plugins {
    id 'biz.aQute.bnd.builder' version '6.4.0'
}

jar {
    manifest {
        attributes(
            'Bundle-Name': 'Example Manager',
            'Export-Package': 'dev.galasa.example,dev.galasa.example.spi',
            'Import-Package': '!javax.validation.constraints,!org.osgi.service.component.annotations,*'
        )
    }
}
```

### Registering as an OSGi Service

The `@Component` annotation registers the Manager as an OSGi service:

```java
import org.osgi.service.component.annotations.Component;
import dev.galasa.framework.spi.IManager;

@Component(service = { IManager.class })
public class ExampleManagerImpl extends AbstractManager implements IExampleManager {
    // Implementation
}
```

The framework discovers all `IManager` services at runtime and includes them in the Manager lifecycle.

## Step 7: Configuration Properties

Managers use the Configuration Property Store (CPS) to read test environment configuration.

### Defining Properties

Create a properties class for each configuration property:

```java
package dev.galasa.example.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ExampleHostname extends CpsProperties {
    
    public static String get(String tag) throws ConfigurationPropertyStoreException {
        return getStringNulled(ExamplePropertiesSingleton.cps(), 
            "hostname", "default", tag);
    }
}
```

### Properties Singleton

The singleton provides access to the CPS:

```java
package dev.galasa.example.internal.properties;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class ExamplePropertiesSingleton {
    
    public static IConfigurationPropertyStoreService cps;
    
    public static void setCps(IConfigurationPropertyStoreService cps) {
        ExamplePropertiesSingleton.cps = cps;
    }
}
```

Initialize the singleton in your Manager's `initialise()` method:

```java
@Override
public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
        @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
    super.initialise(framework, allManagers, activeManagers, galasaTest);
    
    try {
        ExamplePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
    } catch (Exception e) {
        throw new ManagerException("Unable to initialise CPS", e);
    }
}
```

### Using Properties

Access properties in your Manager code:

```java
public void provisionBuild() throws ManagerException {
    try {
        String hostname = ExampleHostname.get(resourceTag);
        int port = ExamplePort.get(resourceTag);
        
        connectToResource(hostname, port);
    } catch (ConfigurationPropertyStoreException e) {
        throw new ExampleManagerException("Failed to read configuration", e);
    }
}
```

### Property Naming Conventions

Properties follow the pattern: `namespace.property[.suffix[.tag]]`

Examples:
```
example.hostname.default=localhost
example.hostname.PRIMARY=prod-server-01
example.hostname.SECONDARY=prod-server-02
example.port.default=8080
example.max.connections=100
```

## Step 8: Building and Testing

### Building the Manager

**Maven:**
```bash
cd dev.galasa.example.manager
mvn clean install
```

**Gradle:**
```bash
cd dev.galasa.example.manager
gradle build publishToMavenLocal
```

The build produces an OSGi bundle JAR in `target/` or `build/libs/`.

### Unit Testing

The generated project includes a basic unit test:

```java
package dev.galasa.example.internal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExampleManagerImplTest {
    
    @Test
    void testResourceCreation() {
        ExampleResourceImpl resource = new ExampleResourceImpl("TEST");
        assertEquals("TEST", resource.getTag());
    }
}
```

Expand tests to cover:
- Resource lifecycle operations
- Property reading
- Dependency interactions
- Error conditions

### Integration Testing

Create Installation Verification Tests (IVTs) that use your Manager:

```java
package dev.galasa.example.ivt;

import dev.galasa.Test;
import dev.galasa.example.ExampleResource;
import dev.galasa.example.IExampleResource;

@Test
public class ExampleManagerIVT {
    
    @ExampleResource(resourceTag = "PRIMARY")
    public IExampleResource resource;
    
    @Test
    public void testResourceAvailable() {
        assertNotNull(resource);
        assertEquals("PRIMARY", resource.getTag());
    }
}
```

## Step 9: Packaging and Distribution

### Including in an OBR

If you created an OBR project, build it to package your Manager:

```bash
cd dev.galasa.example.obr
mvn clean install
```

The OBR JAR contains references to all included bundles.

### Installing in an Ecosystem

Copy your Manager JAR to the Galasa ecosystem:

1. Build the Manager bundle
2. Copy to the ecosystem's bundle directory
3. Restart the ecosystem
4. Verify the Manager is loaded in the framework logs

## Common Patterns and Best Practices

### Resource Tracking

Track all created resources for cleanup:

```java
private final List<IExampleResource> resources = new ArrayList<>();

@GenerateAnnotatedField(annotation = ExampleResource.class)
public IExampleResource generateExampleResource(Field field, List<Annotation> annotations) {
    ExampleResourceImpl resource = new ExampleResourceImpl(tag);
    resources.add(resource);
    return resource;
}

@Override
public void provisionDiscard() {
    for (IExampleResource resource : resources) {
        cleanup(resource);
    }
    resources.clear();
}
```

### Resource Pooling

Implement resource pooling for reusable resources:

```java
private final Map<String, IExampleResource> resourcePool = new HashMap<>();

public IExampleResource allocateResource(String tag) throws ResourceUnavailableException {
    synchronized (resourcePool) {
        IExampleResource resource = resourcePool.get(tag);
        if (resource == null) {
            throw new ResourceUnavailableException("Resource " + tag + " not available");
        }
        resourcePool.remove(tag);
        return resource;
    }
}

public void returnResource(IExampleResource resource) {
    synchronized (resourcePool) {
        resourcePool.put(resource.getTag(), resource);
    }
}
```

### Shared Environment Support

Indicate whether your Manager supports shared environments:

```java
@Override
public boolean doYouSupportSharedEnvironments() {
    // Return true if resources can be shared between tests
    return !provisionsResources;
}
```

Managers that provision resources typically return `false` unless resources can be safely shared.

### Logging

Use Apache Commons Logging for Manager logging:

```java
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExampleManagerImpl extends AbstractManager {
    private static final Log logger = LogFactory.getLog(ExampleManagerImpl.class);
    
    public void provisionBuild() {
        logger.info("Building example resources");
        // ...
        logger.debug("Resource details: " + details);
    }
}
```

### Error Handling

Distinguish between temporary and permanent failures:

```java
@Override
public void provisionBuild() throws ManagerException, ResourceUnavailableException {
    try {
        buildResource();
    } catch (TemporaryNetworkException e) {
        // Temporary - test can retry
        throw new ResourceUnavailableException("Network temporarily unavailable", e);
    } catch (InvalidConfigurationException e) {
        // Permanent - test will fail
        throw new ExampleManagerException("Invalid configuration", e);
    }
}
```

## Troubleshooting

### Manager Not Discovered

**Problem:** Framework doesn't find your Manager

**Solutions:**
- Verify `@Component(service = { IManager.class })` annotation is present
- Check OSGi bundle manifest includes `Service-Component` header
- Ensure bundle is in the ecosystem's bundle directory
- Check framework logs for bundle activation errors

### Fields Not Injected

**Problem:** Test fields remain `null`

**Solutions:**
- Verify annotation has `@ExampleManagerField` meta-annotation
- Check `@ValidAnnotatedFields` includes correct interface type
- Ensure `@GenerateAnnotatedField` method signature is correct
- Call `generateAnnotatedFields()` in `provisionGenerate()`

### Dependency Not Found

**Problem:** `addDependentManager()` returns `null`

**Solutions:**
- Verify dependent Manager bundle is installed
- Check you're requesting the correct SPI interface
- Ensure dependent Manager is registered as OSGi service

### Bundle Won't Load

**Problem:** OSGi runtime rejects the bundle

**Solutions:**
- Verify `Export-Package` and `Import-Package` directives
- Check for package version conflicts
- Review OSGi console for resolution errors
- Ensure all imports can be satisfied

## Next Steps

- Study [existing Managers](/modules/managers/) for patterns and examples
- Review the [Manager lifecycle documentation](/openwiki/concepts/manager-lifecycle.md)
- Understand [Manager architecture](/openwiki/architecture/managers.md)
- Learn about [building test projects](/openwiki/workflows/building-test-projects.md) that use your Manager
