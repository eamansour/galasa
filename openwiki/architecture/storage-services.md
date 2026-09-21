---
type: Architecture Component
title: Storage Services Architecture
description: Explanation of Galasa's pluggable storage services including CPS, DSS, RAS, and Credentials Store with their interfaces, backend implementations, and usage patterns.
tags: [architecture, storage, cps, dss, ras, credentials, etcd, couchdb, configuration]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-f634b3f63332559e4a3e0ebd
    resource: repo://developer-docs/storage-services.md
  - id: openwiki-source-fb4d5cfa653b9de770a6a5cd
    resource: repo://modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/README.md
  - id: openwiki-source-8c49fee9662ea23379f33dea
    resource: repo://modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/src/main/java/dev/galasa/cps/etcd/internal/Etcd3ConfigurationPropertyStore.java
  - id: openwiki-source-f64bc391146adcc6e42bed4f
    resource: repo://modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/src/main/java/dev/galasa/cps/etcd/internal/Etcd3DynamicStatusStore.java
  - id: openwiki-source-3324f90bcc18a4f128804bc9
    resource: repo://modules/extensions/galasa-extensions-parent/dev.galasa.ras.couchdb/src/main/java/dev/galasa/ras/couchdb/internal/CouchdbRasStore.java
  - id: openwiki-source-36e75e1dbc1befa486011f1d
    resource: repo://modules/extensions/README.md
  - id: openwiki-source-1dab6036140214bf32a62835
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/internal/cps/FrameworkConfigurationPropertyService.java
  - id: openwiki-source-9c9faf2a4b024a08cf8c1e5f
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/internal/creds/FrameworkCredentialsService.java
  - id: openwiki-source-8667044ab5d1a4b56b073117
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/internal/dss/FrameworkDynamicStatusStoreService.java
  - id: openwiki-source-0172705720206e976a4dede1
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/internal/ras/FrameworkMultipleResultArchiveStore.java
  - id: openwiki-source-318354617d6dcdef7f3de808
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/creds/ICredentialsStore.java
  - id: openwiki-source-ebb9b266696ee832eaf177d0
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IConfigurationPropertyStore.java
  - id: openwiki-source-3256a705d7c802cd70b28983
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IConfigurationPropertyStoreService.java
  - id: openwiki-source-42a927ccd6a830162f9e3476
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IDynamicStatusStore.java
  - id: openwiki-source-95dcdf0d41e8f29cbd126518
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IDynamicStatusStoreKeyAccess.java
  - id: openwiki-source-933041bf76fc0d14beef4197
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IDynamicStatusStoreService.java
  - id: openwiki-source-ade988ba0be07fc25b7bf147
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IFramework.java
  - id: openwiki-source-0e578b885c44868887883db6
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IResultArchiveStore.java
  - id: openwiki-source-8a2ab3770182a4a69d0bb4a6
    resource: repo://modules/obr/javadocs/target/sources/dev/galasa/framework/spi/IResultArchiveStoreService.java
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Storage Services Architecture

Galasa's storage services provide pluggable backend implementations for managing different types of data during test execution. The framework defines four core storage services, each with well-defined interfaces that allow backend implementations to be swapped without changing client code.

## Overview

The storage services architecture separates interface from implementation through a two-layer design:

1. **Service Layer**: High-level interfaces that managers and tests interact with
2. **Store Layer**: Low-level backend interfaces that implement persistence

```mermaid
flowchart TD
    Test[Test Code]
    Manager[Manager]
    Framework[Framework]
    
    CPS_Service[IConfigurationPropertyStoreService]
    DSS_Service[IDynamicStatusStoreService]
    RAS_Service[IResultArchiveStoreService]
    Creds_Service[ICredentialsService]
    
    CPS_Store[IConfigurationPropertyStore]
    DSS_Store[IDynamicStatusStore]
    RAS_Store[IResultArchiveStore]
    Creds_Store[ICredentialsStore]
    
    EtcdCPS[Etcd3ConfigurationPropertyStore]
    EtcdDSS[Etcd3DynamicStatusStore]
    CouchRAS[CouchdbRasStore]
    EtcdCreds[Etcd3CredentialsStore]
    
    Test --> Framework
    Manager --> Framework
    Framework --> CPS_Service
    Framework --> DSS_Service
    Framework --> RAS_Service
    Framework --> Creds_Service
    
    CPS_Service --> CPS_Store
    DSS_Service --> DSS_Store
    RAS_Service --> RAS_Store
    Creds_Service --> Creds_Store
    
    CPS_Store --> EtcdCPS
    DSS_Store --> EtcdDSS
    RAS_Store --> CouchRAS
    Creds_Store --> EtcdCreds
```

*The layered storage services architecture with service and store interfaces.*

## Configuration Property Store (CPS)

The Configuration Property Store provides hierarchical key-value storage for configuration properties with namespace-based organization and property inheritance.

### Responsibilities

- Store and retrieve configuration properties
- Support namespace-based property organization
- Implement property inheritance through infixes
- Enable property overrides for test runs
- Support bulk operations (get/set/delete prefixed properties)

### Interface Design

The CPS has two primary interfaces:

**IConfigurationPropertyStore** (backend interface):
- `getProperty(String key)`: Retrieve a single property
- `setProperty(String key, String value)`: Store a property
- `deleteProperty(String key)`: Remove a property
- `getPrefixedProperties(String prefix)`: Retrieve all properties with a prefix
- `getPropertiesFromNamespace(String namespace)`: Get all properties in a namespace
- `getNamespaces()`: List all available namespaces

**IConfigurationPropertyStoreService** (client interface):
- `getProperty(String prefix, String suffix, String... infixes)`: Get property with inheritance
- `setProperty(String name, String value)`: Set property in namespace
- `deleteProperty(String name)`: Delete property in namespace
- `getPrefixedProperties(String prefix)`: Get properties by prefix
- `getAllProperties()`: Get all properties in the service's namespace

### Property Inheritance

The CPS implements hierarchical property lookup through infixes. When requesting a property, the framework searches from most specific to least specific:

```mermaid
sequenceDiagram
    participant Test
    participant CPSService as CPS Service<br/>(namespace: zos)
    participant CPSStore as CPS Store
    
    Test->>CPSService: getProperty("image", "credentialid", "PLEXMA", "MVMA")
    
    CPSService->>CPSStore: getProperty("zos.image.PLEXMA.MVMA.credentialid")
    CPSStore-->>CPSService: null
    
    CPSService->>CPSStore: getProperty("zos.image.PLEXMA.credentialid")
    CPSStore-->>CPSService: "PLEXMA_CREDS"
    
    CPSService-->>Test: "PLEXMA_CREDS"
```

*Property inheritance search order from most specific to least specific.*

The search order for `getProperty("image", "credentialid", "PLEXMA", "MVMA")` in namespace "zos" would be:
1. `zos.image.PLEXMA.MVMA.credentialid`
2. `zos.image.PLEXMA.credentialid`
3. `zos.image.credentialid`

### Backend Implementations

**Etcd3 Backend** (`dev.galasa.cps.etcd`):
- Uses jetcd client to connect to etcd clusters
- Implements key-value storage with UTF-8 encoding
- Supports atomic operations through etcd transactions
- Location: `/modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd`

## Dynamic Status Store (DSS)

The Dynamic Status Store maintains dynamic state information during test execution, supporting transactional updates, atomic operations, and watch capabilities.

### Responsibilities

- Store transient runtime state for test runs and resources
- Support atomic compare-and-swap operations
- Enable transactional multi-key updates
- Provide watch capabilities for state change notifications
- Support time-to-live (TTL) for ephemeral properties
- Manage resource allocation state

### Interface Design

The DSS has a three-tier interface hierarchy:

**IDynamicStatusStore** (backend interface):
- Extends `IDynamicStatusStoreKeyAccess`
- `shutdown()`: Clean up resources

**IDynamicStatusStoreKeyAccess** (key operations interface):
- `put(String key, String value)`: Store a key-value pair
- `put(Map<String, String> keyValues)`: Store multiple pairs
- `put(String key, String value, long timeToLiveSecs)`: Store with TTL
- `putSwap(String key, String oldValue, String newValue)`: Atomic compare-and-swap
- `get(String key)`: Retrieve a value
- `getPrefix(String keyPrefix)`: Get all keys with prefix
- `delete(String key)`: Delete a key
- `deletePrefix(String keyPrefix)`: Delete all keys with prefix
- `performActions(IDssAction... actions)`: Execute atomic multi-key transaction
- `watch(IDynamicStatusStoreWatcher watcher, String key)`: Watch a key for changes
- `watchPrefix(IDynamicStatusStoreWatcher watcher, String keyPrefix)`: Watch prefix

**IDynamicStatusStoreService** (client interface):
- Extends `IDynamicStatusStoreKeyAccess`
- `getDynamicResource(String resourceKey)`: Get interface for resource management
- `getDynamicRun()`: Get interface for run status updates

### Transactional Updates

The DSS supports atomic multi-key transactions through the `performActions` method, which accepts multiple `IDssAction` implementations:

- `DssAdd`: Add a new key (fails if exists)
- `DssUpdate`: Update an existing key
- `DssSwap`: Swap a key if it matches old value
- `DssDelete`: Delete a key
- `DssDeletePrefix`: Delete all keys with prefix

<!-- openwiki: mermaid parse failed and this diagram was converted to a text fence so it does not break rendering. Fix the diagram source and restore the mermaid fence. Parser error: Heuristic: an unescaped angle bracket inside a label breaks rendering; rephrase the label. -->
```text
sequenceDiagram
    participant Manager
    participant DSSService as DSS Service
    participant DSSStore as DSS Store (etcd)
    
    Manager->>DSSService: performActions(actions)
    Note over Manager,DSSService: actions = [<br/>DssSwap("lock", null, "allocated"),<br/>DssAdd("resource.port", "8080"),<br/>DssAdd("resource.status", "in-use")<br/>]
    
    DSSService->>DSSStore: txn.If(lock == null)
    DSSStore-->>DSSService: condition checked
    
    DSSService->>DSSStore: txn.Then(<br/>put("lock", "allocated"),<br/>put("resource.port", "8080"),<br/>put("resource.status", "in-use")<br/>)
    DSSStore->>DSSStore: Execute transaction atomically
    DSSStore-->>DSSService: success
    
    DSSService-->>Manager: Transaction succeeded
```

*Atomic multi-key transaction ensuring all operations succeed or fail together.*

### Compare-and-Swap Operations

The `putSwap` operation enables lock-free resource allocation:

```mermaid
sequenceDiagram
    participant Manager1 as Manager 1
    participant Manager2 as Manager 2
    participant DSS as DSS
    
    Manager1->>DSS: get("resource.status")
    Manager2->>DSS: get("resource.status")
    DSS-->>Manager1: "available"
    DSS-->>Manager2: "available"
    
    Manager1->>DSS: putSwap("resource.status", "available", "allocated-M1")
    DSS->>DSS: Check current value == "available"
    DSS-->>Manager1: true (success)
    
    Manager2->>DSS: putSwap("resource.status", "available", "allocated-M2")
    DSS->>DSS: Check current value == "available"
    Note over DSS: Current value is now "allocated-M1"
    DSS-->>Manager2: false (conflict)
```

*Compare-and-swap prevents concurrent allocation conflicts.*

### Watch Capabilities

The DSS provides watch functionality to notify clients of property changes:

```mermaid
sequenceDiagram
    participant Manager
    participant DSS
    participant Watcher as Watcher Implementation
    
    Manager->>DSS: watch(watcher, "resource.status")
    DSS-->>Manager: watchId (UUID)
    
    Note over DSS: Another process updates property
    DSS->>Watcher: propertyModified("resource.status", Event.MODIFIED, "available", "allocated")
    
    Manager->>DSS: unwatch(watchId)
    DSS->>DSS: Close watch
```

*Watch mechanism for monitoring property changes in real-time.*

### Backend Implementations

**Etcd3 Backend** (`dev.galasa.cps.etcd`):
- Uses jetcd client transactions for atomic operations
- Implements compare-and-swap using etcd's conditional transactions
- Supports watch through etcd watch API
- Implements TTL using etcd lease mechanism
- Handles pagination for large prefix queries (10,000 keys per page)
- Location: `/modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd`

## Result Archive Store (RAS)

The Result Archive Store captures and persists test results, logs, and artifacts. The framework supports multiple RAS backends simultaneously through a composite pattern.

### Responsibilities

- Write test logs during execution
- Store test structure and metadata
- Persist test artifacts
- Provide directory services for querying test results
- Support multiple concurrent backends
- Calculate unique run identifiers

### Interface Design

**IResultArchiveStore** (backend interface):
- `writeLog(String message)`: Write a log message
- `writeLog(List<String> messages)`: Write multiple log messages
- `updateTestStructure(TestStructure testStructure)`: Update test metadata
- `createTestStructure(String runId, TestStructure testStructure)`: Create new test record
- `getStoredArtifactsRoot()`: Get filesystem path for artifacts
- `retrieveRunLogLineCount()`: Get current log line count
- `flush()`: Flush pending writes
- `shutdown()`: Clean up resources

**IResultArchiveStoreService**:
- Extends `IResultArchiveStore`
- Marker interface for services

**FrameworkMultipleResultArchiveStore**:
- Implements composite pattern to write to multiple RAS backends
- Delegates all operations to registered backends
- Currently read-only from first backend for artifacts

### Usage Pattern

```mermaid
sequenceDiagram
    participant Test
    participant Framework
    participant MultiRAS as Multiple RAS
    participant CouchDB as CouchDB RAS
    participant File as File RAS
    
    Test->>Framework: getResultArchiveStore()
    Framework-->>Test: MultiRAS instance
    
    Test->>MultiRAS: writeLog("Test started")
    MultiRAS->>CouchDB: writeLog("Test started")
    MultiRAS->>File: writeLog("Test started")
    
    Test->>MultiRAS: getStoredArtifactsRoot()
    Note over MultiRAS: Returns first backend's root
    MultiRAS->>CouchDB: getStoredArtifactsRoot()
    CouchDB-->>MultiRAS: filesystem path
    MultiRAS-->>Test: filesystem path
    
    Test->>MultiRAS: updateTestStructure(structure)
    MultiRAS->>CouchDB: updateTestStructure(structure)
    MultiRAS->>File: updateTestStructure(structure)
```

*Multiple RAS backends receive all writes but only first provides artifact filesystem.*

### Backend Implementations

**CouchDB Backend** (`dev.galasa.ras.couchdb`):
- Uses Apache CouchDB for storing test data
- Three databases: `galasa_run`, `galasa_log`, `galasa_artifacts`
- Implements custom filesystem provider for artifact storage
- Supports views for querying test results
- Batches log writes (100 lines) for efficiency
- Location: `/modules/extensions/galasa-extensions-parent/dev.galasa.ras.couchdb`

**File Backend**:
- Stores results in local filesystem
- Used primarily for local test runs

## Credentials Store

The Credentials Store securely manages and provides access to credentials required by tests.

### Responsibilities

- Store credentials securely
- Retrieve credentials by identifier
- Support multiple credential types (username/password, tokens)
- Integrate with confidential text service to prevent logging
- Enable credential lifecycle management

### Interface Design

**ICredentialsStore** (backend interface):
- `getCredentials(String credsId)`: Retrieve credentials
- `getAllCredentials()`: Get all credentials
- `setCredentials(String credsId, ICredentials credentials)`: Store credentials
- `deleteCredentials(String credsId)`: Remove credentials
- `shutdown()`: Clean up resources

**ICredentialsService** (client interface):
- Same methods as `ICredentialsStore`
- Automatically registers credentials with confidential text service

### Credential Types

The framework supports multiple credential implementations:
- `ICredentialsUsernamePassword`: Username and password pairs
- `ICredentialsToken`: Token-based authentication
- Custom implementations through `ICredentials` interface

### Usage Pattern

```mermaid
sequenceDiagram
    participant Test
    participant Framework
    participant CredService as Credentials Service
    participant CredStore as Credentials Store (etcd)
    participant CTS as Confidential Text Service
    
    Test->>Framework: getCredentialsService()
    Framework-->>Test: CredService instance
    
    Test->>CredService: getCredentials("SYSTEM_USER")
    CredService->>CredStore: getCredentials("SYSTEM_USER")
    CredStore-->>CredService: ICredentialsUsernamePassword
    
    CredService->>CTS: registerText(password, "Token for credentials id SYSTEM_USER")
    Note over CTS: Password masked in logs
    
    CredService-->>Test: ICredentialsUsernamePassword
```

*Credentials retrieval with automatic registration in confidential text service.*

### Backend Implementations

**Etcd3 Backend** (`dev.galasa.cps.etcd`):
- Stores encrypted credentials in etcd
- Uses JSON serialization for credential objects
- Location: `/modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd`

## Namespace Organization

All storage services use namespaces to isolate data between different managers and tests. Namespaces are specified when obtaining a service instance from the framework:

```java
// Each manager gets its own namespace
IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("zos");
IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("zosbatch");
```

Namespace rules:
- Must be alphanumeric (no dots)
- Cannot be null or empty
- Typically matches the manager name
- Properties are prefixed with namespace: `zos.image.credentialid`

## Service Initialization

Storage services are initialized during framework startup through a registration pattern:

```mermaid
sequenceDiagram
    participant Framework
    participant CPS_Reg as CPS Registration
    participant CPS_Store as CPS Store Implementation
    participant DSS_Reg as DSS Registration
    participant DSS_Store as DSS Store Implementation
    
    Framework->>CPS_Reg: initialize(frameworkInit)
    CPS_Reg->>Framework: getConfigurationPropertyStoreURI()
    Framework-->>CPS_Reg: "etcd:http://localhost:2379"
    
    CPS_Reg->>CPS_Store: new Etcd3ConfigurationPropertyStore(uri)
    CPS_Store->>CPS_Store: Connect to etcd
    
    CPS_Reg->>Framework: registerConfigurationPropertyStore(store)
    Framework->>Framework: Set active CPS
    
    Framework->>DSS_Reg: initialize(frameworkInit)
    DSS_Reg->>Framework: getDynamicStatusStoreURI()
    Framework-->>DSS_Reg: "etcd:http://localhost:2379"
    
    DSS_Reg->>DSS_Store: new Etcd3DynamicStatusStore(uri)
    DSS_Store->>DSS_Store: Connect to etcd
    
    DSS_Reg->>Framework: registerDynamicStatusStore(store)
    Framework->>Framework: Set active DSS
```

*Storage service registration during framework initialization.*

Only one CPS and one DSS can be active during a test run or server instance. Multiple RAS backends can be active simultaneously.

## Integration with Test Execution

Storage services integrate throughout the test lifecycle:

1. **Before Test**: 
   - CPS loads configuration properties
   - DSS allocates resources through compare-and-swap
   - Credentials Store provides authentication

2. **During Test**: 
   - RAS records logs and artifacts
   - DSS updates resource state
   - CPS properties are read as needed

3. **After Test**: 
   - RAS stores final test structure
   - DSS releases resources
   - All services flush pending writes

## Configuration

Storage services are configured through bootstrap properties:

- `framework.config.store`: URI for CPS backend (e.g., `etcd:http://localhost:2379`)
- `framework.dynamicstatus.store`: URI for DSS backend
- `framework.resultarchive.store`: URI(s) for RAS backend(s), comma-separated for multiple

## Extension Development

To implement a new storage backend:

1. Create an OSGi bundle in `/modules/extensions/galasa-extensions-parent/`
2. Implement the appropriate store interface (`IConfigurationPropertyStore`, `IDynamicStatusStore`, etc.)
3. Implement the registration interface to register the store with the framework
4. Package dependencies as a "fat jar" if external libraries are required
5. Use the `bnd.bnd` file to control bundle packaging

The etcd and CouchDB extensions serve as reference implementations.
