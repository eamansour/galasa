---
type: concept
title: Configuration Property System (CPS)
description: Explains how Galasa uses hierarchical configuration properties for framework and manager configuration, including property file formats, namespace-based organization, and property inheritance.
tags: [configuration, cps, properties, framework, managers]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-8c49fee9662ea23379f33dea
    resource: repo://modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/src/main/java/dev/galasa/cps/etcd/internal/Etcd3ConfigurationPropertyStore.java
  - id: openwiki-source-098b0f8a8d0117a9106fa0c2
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/BaseTestRunner.java
  - id: openwiki-source-1d7daa939426d9bd20e73867
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/Framework.java
  - id: openwiki-source-9904ee682e3d8cb87c6ba678
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/FrameworkInitialisation.java
  - id: openwiki-source-be45e701729dc13e11ebeeea
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/internal/cps/FrameworkConfigurationPropertyService.java
  - id: openwiki-source-59a724f22c8b22071a6da7fa
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/cps/CpsProperties.java
  - id: openwiki-source-d77ef11beec44f3acaf4261e
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/IConfigurationPropertyStore.java
  - id: openwiki-source-c814b3625b0dfda8274f1962
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/IConfigurationPropertyStoreService.java
  - id: openwiki-source-19c289d51521684350e01d7c
    resource: repo://modules/framework/README.md
  - id: openwiki-source-d9f3463fff13810e11c7291b
    resource: repo://modules/framework/temp/home/bootstrap.properties
  - id: openwiki-source-a4e2cb1954523ddd2ad51894
    resource: repo://modules/framework/temp/home/overrides.properties
  - id: openwiki-source-8caac8d47d0213387e9e550e
    resource: repo://modules/managers/galasa-managers-parent/galasa-managers-core-parent/dev.galasa.core.manager/src/main/java/dev/galasa/core/manager/internal/CoreManagerImpl.java
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Configuration Property System (CPS)

The Configuration Property System (CPS) is Galasa's hierarchical key-value store for managing configuration properties. It provides a flexible mechanism for configuring the framework, managers, and test behavior through namespace-based property organization with inheritance and override capabilities.

## Overview

The CPS is one of Galasa's core storage services, designed to:

- Store configuration properties in a hierarchical namespace structure
- Support property inheritance through infixes that allow specific overrides of general settings
- Enable multiple storage backends (file-based, etcd, Kubernetes ConfigMaps)
- Provide both global configuration and test-specific overrides
- Record property access for diagnostic purposes in test results

## Architecture

The CPS architecture consists of two main interfaces that separate storage backend concerns from client usage:

### Core Interfaces

**`IConfigurationPropertyStore`** - The backend storage interface implemented by different storage providers (file, etcd, etc.). This interface handles the actual persistence and retrieval of properties.

**`IConfigurationPropertyStoreService`** - The service interface used by managers and tests to access properties. This interface adds namespace-scoped access, hierarchical property resolution, and automatic recording of property usage.

The framework instantiates `FrameworkConfigurationPropertyService` to wrap a backend store with namespace awareness and property inheritance logic.

## Property File Locations

### GALASA_HOME Directory

By default, Galasa stores local configuration files in the `GALASA_HOME` directory:

- **Default location**: `${HOME}/.galasa` (or `%USERPROFILE%/.galasa` on Windows)
- **Override**: Set via `GALASA_HOME` system property or environment variable

### bootstrap.properties

The `bootstrap.properties` file configures the framework's boot sequence and specifies which storage backends to use.

**Location**: Specified via `--bootstrap` flag to galasa-boot, or loaded from `${GALASA_HOME}/bootstrap.properties`

**Key Properties**:

```properties
# CPS backend location
framework.config.store=etcd:http://127.0.0.1:2379
# or for local file-based
# framework.config.store=file:///${GALASA_HOME}/cps.properties

# Other storage service URIs
framework.dynamicstatus.store=etcd:http://127.0.0.1:2379
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
framework.credentials.store=etcd:http://127.0.0.1:2379
framework.auth.store=couchdb:http://127.0.0.1:5984

# Extra bundles to load for storage backends
framework.extra.bundles=dev.galasa.ras.couchdb,dev.galasa.cps.etcd

# Run name prefix for local runs
framework.request.type.LOCAL.prefix=L

# JVM launch options for local test runs
galasactl.jvm.local.launch.options=-Xmx80m
galasactl.jvm.local.launch.debug.mode=listen
galasactl.jvm.local.launch.debug.port=2970
```

### cps.properties

The `cps.properties` file contains actual configuration properties used by managers when running tests locally (not in an ecosystem).

**Location**: Defaults to `${GALASA_HOME}/cps.properties` if not specified in bootstrap

**Format**: Standard Java properties format with namespace.prefix.suffix structure

```properties
# Framework properties
framework.continue.on.test.failure=false

# z/OS image configuration example
zos.cluster.CLUSTERA.images=IMAGEA,IMAGEB

zos.image.IMAGEA.default.hostname=dev.galasa.system1
zos.image.IMAGEA.ipv4.hostname=dev.galasa.system1
zos.image.IMAGEA.telnet.port=992
zos.image.IMAGEA.telnet.tls=true
zos.image.IMAGEA.credentials=MY_KEY_INTO_GALASA_CREDENTIALS_STORE
zos.image.IMAGEA.max.slots=4

# Docker engine configuration example
docker.default.engines=LOCAL

docker.engine.LOCAL.hostname=localhost
docker.engine.LOCAL.port=2375

# Gherkin terminal configuration
zos3270.gherkin.terminal.rows=24
zos3270.gherkin.terminal.columns=80
```

### overrides.properties

The `overrides.properties` file provides test-specific property overrides that take precedence over CPS values.

**Location**: 
- Defaults to `${GALASA_HOME}/overrides.properties`
- Can be specified via `--overrides` flag or loaded from URL

**Purpose**: Overrides are consulted first during property lookup, allowing temporary test-specific configuration without modifying the main CPS.

```properties
# Override framework behavior for specific test runs
framework.continue.on.test.failure=true

# Enable CPS REST caching
framework.cps.rest.cache.is.enabled=false

# Override terminal size for specific gherkin test
zos3270.gherkin.terminal.rows=30
zos3270.gherkin.terminal.columns=120
```

## Namespace-Based Property Organization

Properties in the CPS are organized by namespace, which groups related configuration. Each manager typically uses its own namespace.

### Namespace Format

Namespaces must contain only lowercase letters (a-z) and digits (0-9). The framework validates namespaces against the pattern `[a-z0-9]+`.

**Common namespaces**:
- `framework` - Core framework configuration
- `core` - Core manager properties
- `zos` - z/OS manager properties
- `docker` - Docker manager properties
- `http` - HTTP client manager properties
- `test` - Test-specific namespace

### Accessing Namespaced Properties

Managers obtain a namespace-scoped CPS service from the framework:

```java
IConfigurationPropertyStoreService cps = 
    framework.getConfigurationPropertyService("zos");
```

All property accesses through this service are automatically prefixed with the namespace.

## Property Hierarchy and Inheritance

The CPS supports hierarchical property resolution through **infixes**, allowing specific configurations to override general ones.

### Property Structure

A property is constructed as: `namespace.prefix.infix1.infix2...infixN.suffix`

When retrieving a property, the CPS searches in order from most specific to most general:

```java
// Request: cps.getProperty("image", "credentialid", "PLEXMA", "MVMA")
// Search order in "zos" namespace:
// 1. zos.image.PLEXMA.MVMA.credentialid
// 2. zos.image.PLEXMA.credentialid
// 3. zos.image.credentialid
```

### Hierarchical Search Example

For each candidate property, the system checks:
1. **Overrides** first (from overrides.properties)
2. **CPS store** second (from cps.properties or etcd)

The first non-null value found is returned and recorded in the result archive.

### Property Inheritance in Practice

```properties
# General default for all images
zos.image.telnet.port=23

# Specific override for PLEXMA
zos.image.PLEXMA.telnet.port=992

# More specific override for PLEXMA's MVMA LPAR
zos.image.PLEXMA.MVMA.telnet.port=9923
```

When code calls:
```java
String port = cps.getProperty("image", "telnet.port", "PLEXMA", "MVMA");
// Returns: "9923" (most specific match)

String port = cps.getProperty("image", "telnet.port", "PLEXMA");
// Returns: "992" (next level match)

String port = cps.getProperty("image", "telnet.port", "OTHER");
// Returns: "23" (general default)
```

## Accessing Properties via IConfigurationPropertyStoreService

### Basic Property Retrieval

```java
// Get a simple property
String value = cps.getProperty("prefix", "suffix");

// Get with hierarchical infixes
String value = cps.getProperty("image", "hostname", "SYSTEMA", "LPAR1");

// Get prefixed properties as a map
Map<String, String> props = cps.getPrefixedProperties("image.SYSTEMA.");

// Get all properties in the namespace
Map<String, String> allProps = cps.getAllProperties();
```

### Setting and Deleting Properties

```java
// Set a property (writes to CPS backend, not overrides)
cps.setProperty("image.SYSTEMA.hostname", "system.example.com");

// Set multiple properties atomically
Map<String, String> properties = new HashMap<>();
properties.put("image.SYSTEMA.hostname", "system.example.com");
properties.put("image.SYSTEMA.port", "23");
cps.setProperties(properties);

// Delete a property
cps.deleteProperty("image.SYSTEMA.hostname");

// Delete all properties with a prefix
cps.deletePrefixedProperties("image.SYSTEMA.");
```

### Property Variants and Diagnostics

When a property cannot be found, managers can report all possible property names that would satisfy the request:

```java
String[] variants = cps.reportPropertyVariants("image", "hostname", "SYSTEMA", "LPAR1");
// Returns: [
//   "zos.image.SYSTEMA.LPAR1.hostname",
//   "zos.image.SYSTEMA.hostname",
//   "zos.image.hostname"
// ]

String variantsString = cps.reportPropertyVariantsString("image", "hostname", "SYSTEMA");
// Returns: "[zos.image.SYSTEMA.hostname,zos.image.hostname]"
```

## CPS Backend Implementations

### File-Based CPS

The default local implementation stores properties in a simple Java properties file.

**Configuration**:
```properties
framework.config.store=file:///${GALASA_HOME}/cps.properties
```

**Characteristics**:
- Simple text file format
- Suitable for local development
- No concurrency support
- Automatic creation of missing file

### etcd CPS

The production-grade distributed implementation using etcd for cluster environments.

**Configuration**:
```properties
framework.config.store=etcd:http://127.0.0.1:2379
framework.extra.bundles=dev.galasa.cps.etcd
```

**Characteristics**:
- Distributed key-value store
- Supports concurrent access
- Suitable for Galasa ecosystems
- Atomic operations
- Namespace listing support

The etcd implementation uses the JETCD client library and packages all transitive dependencies in a "fat jar" OSGi bundle.

### Kubernetes ConfigMap CPS

Configuration stored in Kubernetes ConfigMaps for cloud-native deployments.

**Characteristics**:
- Native Kubernetes resource
- Managed via kubectl or Kubernetes API
- Suitable for containerized Galasa ecosystems

## Property Recording and Diagnostics

The CPS automatically records all property accesses during test execution for diagnostic purposes. These records are saved to the Result Archive Store (RAS) in the `framework/` directory.

### Recorded Information

For each property access, the system records:
- **Property name**: Full namespaced property name
- **Value**: The value retrieved
- **Source**: Where the value came from (`overrides`, `cps`, or `missing`)

Example recorded properties:
```properties
zos.image.SYSTEMA.hostname=system.example.com
zos.image.SYSTEMA.hostname._source=cps

zos.image.SYSTEMA.credentials=MYSYSTEM_CREDS
zos.image.SYSTEMA.credentials._source=overrides

zos.image.SYSTEMA.ipv6.hostname=*** MISSING ***
zos.image.SYSTEMA.ipv6.hostname._source=missing
```

### Property Files in RAS

Test runs generate two property files in the RAS:

1. **`framework/overrides.properties`** - All override properties passed to the test
2. **`framework/cps.properties`** - Record of all CPS properties accessed, with source annotations

## Common Properties Used by Managers

### Framework Properties

```properties
# Continue test execution after method failure
framework.continue.on.test.failure=false

# CPS over REST caching
framework.cps.rest.cache.is.enabled=false

# Dynamic Status Store location (can be in CPS)
framework.dynamicstatus.store=etcd:http://localhost:2379

# Result Archive Store location
framework.resultarchive.store=file:///galasa/ras
```

### Core Manager Properties

```properties
# Resource string allocation patterns
core.resource.string.pattern=A-Z0-9
```

### z/OS Manager Properties

```properties
# Cluster definitions
zos.cluster.CLUSTERA.images=IMAGEA,IMAGEB

# Image-specific properties
zos.image.IMAGEA.default.hostname=mainframe.example.com
zos.image.IMAGEA.ipv4.hostname=192.168.1.100
zos.image.IMAGEA.telnet.port=992
zos.image.IMAGEA.telnet.tls=true
zos.image.IMAGEA.credentials=MAINFRAME_CREDS
zos.image.IMAGEA.max.slots=4
```

### Docker Manager Properties

```properties
# Default engines
docker.default.engines=LOCAL,REMOTE

# Engine configuration
docker.engine.LOCAL.hostname=localhost
docker.engine.LOCAL.port=2375
docker.engine.LOCAL.credentials.id=DOCKER_CREDS

# Registry configuration
docker.registry.PRIMARY.hostname=registry.example.com
docker.registry.PRIMARY.port=5000
```

### HTTP Manager Properties

```properties
# Timeout settings
http.client.timeout=30
http.client.connection.timeout=10

# TLS configuration
http.client.tls.truststore=/path/to/truststore.jks
```

## Manager Property Access Patterns

Managers typically follow a standard pattern for accessing properties:

### Singleton Properties Class

```java
@Component(service = CorePropertiesSingleton.class, immediate = true)
public class CorePropertiesSingleton {
    private IConfigurationPropertyStoreService cps;
    
    public static IConfigurationPropertyStoreService cps() throws ManagerException {
        return singletonInstance.cps;
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) {
        singletonInstance.cps = cps;
    }
}
```

### Utility Methods

Managers use `CpsProperties` utility methods for type-safe property access:

```java
// Get int with default
int timeout = CpsProperties.getIntWithDefault(cps, 30, "client", "timeout");

// Get string, nulled if empty
String hostname = CpsProperties.getStringNulled(cps, "server", "hostname", "SYSTEMA");

// Get string with default
String protocol = CpsProperties.getStringWithDefault(cps, "http", "client", "protocol");

// Get comma-separated list
List<String> images = CpsProperties.getStringList(cps, "cluster", "images", "CLUSTERA");
```

## Integration with Framework Initialization

The CPS is initialized early in the framework boot sequence:

1. **Load bootstrap.properties** - Contains URI for CPS backend
2. **Determine CPS location** - From bootstrap property `framework.config.store` or default to `${GALASA_HOME}/cps.properties`
3. **Create backend store** - Instantiate appropriate `IConfigurationPropertyStore` implementation
4. **Register with framework** - Framework wraps backend with namespace-aware service
5. **Load overrides** - Load overrides.properties for test-specific configuration
6. **Initialize managers** - Each manager gets namespace-scoped CPS service

The framework ensures only one CPS backend is active per framework instance.

## Best Practices

### Property Naming Conventions

- Use lowercase namespace names: `zos`, `docker`, `http`
- Use descriptive prefixes: `image`, `cluster`, `engine`, `server`
- Use meaningful suffixes: `hostname`, `port`, `credentials`, `timeout`
- Use UPPERCASE for infixes representing specific instances: `SYSTEMA`, `CLUSTERA`, `PRIMARY`

### Hierarchical Organization

- Define general defaults at the top level
- Override with specific infixes for particular instances
- Use multiple infix levels for complex hierarchies (e.g., cluster → image → LPAR)

### Override Strategy

- Keep `cps.properties` with stable, shared configuration
- Use `overrides.properties` for temporary test-specific settings
- Pass overrides via `--overridefile` for automation scenarios

### Diagnostic Support

- Use `reportPropertyVariants()` in error messages to help users configure missing properties
- Check recorded properties in RAS when debugging configuration issues
- Review property source annotations to understand override behavior

## Security Considerations

### Sensitive Properties

- Never store passwords or credentials directly in CPS
- Use credential store references: `zos.image.SYSTEMA.credentials=CREDS_ID`
- The credentials store provides secure storage and retrieval

### Property Encryption

The CPS itself does not encrypt property values. Sensitive data should be:
- Stored in the dedicated Credentials Store
- Referenced by ID in CPS properties
- Retrieved at runtime via `ICredentialsService`

### Access Control

- File-based CPS: Protected by filesystem permissions
- etcd CPS: Support for etcd authentication and TLS
- Kubernetes CPS: RBAC-controlled ConfigMap access

## Related Documentation

- **Storage Services Architecture**: `/openwiki/architecture/storage-services.md`
- **Storage Backend Integrations**: `/openwiki/integrations/storage-backends.md`
- **Local Development Setup**: `/openwiki/operations/local-development.md`
