---
type: operations-guide
title: Testing the REST API Locally
description: Comprehensive guide for setting up and running the Galasa REST API server locally with CouchDB, etcd, and Dex for development and testing
tags: [operations, local-development, rest-api, docker, couchdb, etcd, dex, authentication]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-f888fdbbee1c3e79c1e1b105
    resource: repo://modules/framework/dev-instructions.md
  - id: openwiki-source-1124f7982820fa93ebe88069
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework.api.common/src/main/java/dev/galasa/framework/api/common/EnvironmentVariables.java
  - id: openwiki-source-2d75fa0aa11d0971c52d985a
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework.api.health/src/main/java/dev/galasa/framework/api/health/internal/Health.java
  - id: openwiki-source-16f7c236de85cb56ed508c6e
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework.api.ras/src/main/java/dev/galasa/framework/api/ras/internal/routes/RasHealthRoute.java
  - id: openwiki-source-7ced97b6b23a5e051800e3e8
    resource: repo://modules/framework/galasa-parent/dev.galasa.framework.api/src/main/java/dev/galasa/framework/api/internal/ApiStartup.java
  - id: openwiki-source-0d1fb3026593e63e5a501fae
    resource: repo://modules/framework/galasa-parent/galasa-boot/src/main/java/dev/galasa/boot/felix/FelixFramework.java
  - id: openwiki-source-b05cea7fa882df5dcf906704
    resource: repo://modules/framework/galasa-parent/galasa-boot/src/main/java/dev/galasa/boot/Launcher.java
  - id: openwiki-source-54b92cc93d4bd31a933106b5
    resource: repo://modules/framework/run-locally.sh
  - id: openwiki-source-d9f3463fff13810e11c7291b
    resource: repo://modules/framework/temp/home/bootstrap.properties
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Testing the REST API Locally

This guide explains how to set up and run the Galasa REST API server locally for development and testing. Running the API locally enables you to test changes to API servlets, authentication flows, and storage backend integrations before deploying to an ecosystem.

## Overview

The local API server setup requires:

- **galasa-boot** - The launcher that starts the API server with the `--api` flag
- **CouchDB** - For the Result Archive Store (RAS) and authentication store
- **etcd** - For the Configuration Property Store (CPS), credentials store, and dynamic status store
- **Dex** - For OpenID Connect authentication

The API server runs in the Felix OSGi framework and loads servlet bundles to handle different endpoint groups (`/ras/*`, `/runs/*`, `/cps/*`, etc.).

## Quick Start with `run-locally.sh`

The framework module provides a convenience script that automates Docker container setup and API server launching:

```bash
# Launch all services
./run-locally.sh --all

# Or launch individual components
./run-locally.sh --etcd
./run-locally.sh --couchdb
./run-locally.sh --dex
./run-locally.sh --api
```

The script automatically:
- Sets up Docker containers for CouchDB, etcd, and Dex
- Configures bootstrap properties pointing to local services
- Builds the correct `galasa-boot` command with proper OBR references
- Starts the API server on `http://localhost:8080`

For first-time CouchDB setup, you must configure the single-node database using the web UI wizard at `http://localhost:5984/_utils`.

## Manual Setup

### Prerequisites

1. **Docker Runtime**: Install Rancher Desktop or Docker Desktop to run container images
2. **Java Development Kit**: Required to run the API server
3. **Maven Build**: Ensure the framework project is built locally (`~/.m2/repository` populated)

### Step 1: Launch CouchDB

CouchDB stores test run results and authentication data.

#### Pull and Run the Docker Image

```bash
docker pull couchdb:3.3.3

export COUCHDB_USER="admin"
export COUCHDB_PASSWORD=$(openssl rand -base64 10)
echo "Couchdb password for $COUCHDB_USER is $COUCHDB_PASSWORD"

docker run -p 5984:5984 -d \
  -e COUCHDB_USER=${COUCHDB_USER} \
  -e COUCHDB_PASSWORD=${COUCHDB_PASSWORD} \
  --name couchdb \
  couchdb:3.3.3
```

#### Configure CouchDB for Local Use

On first launch, open the CouchDB web UI at `http://localhost:5984/_utils` and complete the single-node setup wizard using your admin credentials.

#### Set Environment Variables

Add these to your shell profile (`.zprofile`, `.bashrc`, etc.):

```bash
export COUCHDB_USER="admin"
export COUCHDB_PASSWORD="<your_password>"
export COUCHDB_TOKEN=$(echo -n "${COUCHDB_USER}:${COUCHDB_PASSWORD}" | base64)

export GALASA_RAS_TOKEN=${COUCHDB_TOKEN}
export GALASA_AUTHSTORE_TOKEN=${COUCHDB_TOKEN}
```

#### Health Check

Verify CouchDB is running:

```bash
curl http://localhost:5984
curl -H "Authorization: Basic $COUCHDB_TOKEN" http://localhost:5984/_all_dbs
```

The first command should return version information; the second lists available databases.

#### Stop CouchDB

```bash
docker stop couchdb
docker rm couchdb
```

### Step 2: Launch etcd

etcd provides distributed key-value storage for configuration properties and credentials.

#### Pull and Run the Docker Image

```bash
export ETCD_TAG="3.3.27-debian-11-r100"
docker pull bitnami/etcd:${ETCD_TAG}

# Create network bridge
docker network create app-tier --driver bridge

# Run etcd container
docker run -d --name etcd \
  --network app-tier \
  --publish 2379:2379 \
  --publish 2380:2380 \
  --env ALLOW_NONE_AUTHENTICATION=yes \
  --env ETCD_ADVERTISE_CLIENT_URLS=http://etcd:2379 \
  bitnami/etcd:${ETCD_TAG}
```

#### Health Check

```bash
docker logs etcd
curl http://localhost:2379/version
```

The version endpoint should return JSON with etcd version information:

```json
{"etcdserver":"3.5.15","etcdcluster":"3.5.0"}
```

#### Stop etcd

```bash
docker stop etcd
docker rm etcd
```

### Step 3: Launch Dex

Dex provides OpenID Connect authentication for the API server.

#### Pull the Docker Image

```bash
docker pull ghcr.io/dexidp/dex:v2.38.0
```

#### Generate Admin Password

```bash
export DEX_ADMIN_PASSWORD=$(echo password | htpasswd -BinC 10 admin | cut -d: -f2)
echo "Dex admin password is $DEX_ADMIN_PASSWORD"
```

**Important**: Save this password hash to your shell profile so it persists across sessions.

#### Create Dex Configuration

Create `~/.dex/config-dev.yaml` with the following content:

```yaml
# The base path of dex and the external name of the OpenID Connect service.
issuer: http://127.0.0.1:5556/dex

# Storage configuration
storage:
  type: sqlite3
  config:
    file: var/dex/dex.db

# HTTP endpoints
web:
  http: 0.0.0.0:5556

# Telemetry
telemetry:
  http: 0.0.0.0:5558

# gRPC API (required for Galasa)
grpc:
  addr: 0.0.0.0:5557
  reflection: true

# Token expiration configuration
expiry:
  signingKeys: "6h"
  idTokens: "24h"
  refreshTokens:
    disableRotation: true
    validIfNotUsedFor: "2160h" # 90 days

# Skip OAuth approval screen
oauth2:
  skipApprovalScreen: true

# Static client for Galasa Web UI
staticClients:
- id: galasa-webui
  redirectURIs:
  - 'http://localhost:8080/auth/callback'
  name: 'Galasa Web UI'
  secret: example-webui-client-secret

# Enable password database
enablePasswordDB: true

# Static test users
staticPasswords:
- email: "admin@example.com"
  hash: "${DEX_ADMIN_PASSWORD}"
  username: "admin"
  userID: "08a8684b-db88-4b73-90a9-3cd1661f5466"
```

#### Run the Dex Container

```bash
docker run -d \
  -v ~/.dex/config-dev.yaml:/etc/dex/config.docker.yaml \
  -p 5556:5556 \
  -p 5558:5558 \
  -p 5557:5557 \
  --name dex \
  ghcr.io/dexidp/dex:v2.38.0
```

#### Health Check

```bash
docker logs dex
```

Look for messages indicating successful startup and gRPC server initialization.

#### Stop Dex

```bash
docker stop dex
docker rm dex
```

### Step 4: Configure Bootstrap Properties

Create or update `~/.galasa/bootstrap.properties` to point to your local services:

```properties
# Storage backend configuration
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
framework.config.store=etcd:http://127.0.0.1:2379
framework.auth.store=couchdb:http://127.0.0.1:5984
framework.dynamicstatus.store=etcd:http://127.0.0.1:2379
framework.credentials.store=etcd:http://127.0.0.1:2379

# Extra bundles to load
framework.extra.bundles=dev.galasa.ras.couchdb,dev.galasa.cps.etcd
api.extra.bundles=dev.galasa.auth.couchdb
```

#### Configuration Properties Explained

- **framework.resultarchive.store** - URI for the Result Archive Store (RAS); CouchDB stores test run results and artifacts
- **framework.config.store** - URI for the Configuration Property Store (CPS); etcd stores test configuration
- **framework.auth.store** - URI for authentication data; CouchDB stores user tokens and permissions
- **framework.dynamicstatus.store** - URI for dynamic status tracking; etcd stores run states
- **framework.credentials.store** - URI for credentials storage; etcd stores secure credentials
- **framework.extra.bundles** - Comma-separated list of OSGi bundles to load for framework storage backends
- **api.extra.bundles** - Additional bundles required only when running the API server

### Step 5: Set API Server Environment Variables

The API server requires several environment variables for authentication and CORS configuration. Add these to your shell profile:

```bash
export GALASA_OBR_VERSION=0.36.0
export GALASA_BOOT_JAR_VERSION=0.36.0
export GALASA_EXTERNAL_API_URL="http://localhost:8080"
export GALASA_USERNAME_CLAIMS="preferred_username,name,sub"
export GALASA_ALLOWED_ORIGINS="*"

# Dex configuration - must match your Dex config-dev.yaml
export GALASA_DEX_ISSUER="http://127.0.0.1:5556/dex"
export GALASA_DEX_GRPC_HOSTNAME="127.0.0.1:5557"
```

#### Environment Variables Explained

- **GALASA_EXTERNAL_API_URL** - External URL clients use to access the API server
- **GALASA_USERNAME_CLAIMS** - Ordered, comma-separated list of JWT claims to extract usernames from
- **GALASA_ALLOWED_ORIGINS** - CORS allowed origins; use `*` for local development, restrict for production
- **GALASA_DEX_ISSUER** - Dex issuer URL for OpenID Connect; must match `issuer` in Dex configuration
- **GALASA_DEX_GRPC_HOSTNAME** - Hostname and port for Dex gRPC API; must match `grpc.addr` in Dex configuration

### Step 6: Start the API Server

Launch the API server using `galasa-boot`:

```bash
java -jar ~/.m2/repository/dev/galasa/galasa-boot/${GALASA_BOOT_JAR_VERSION}/galasa-boot-${GALASA_BOOT_JAR_VERSION}.jar \
  --api \
  --localmaven file://${HOME}/.m2/repository/ \
  --remotemaven https://development.galasa.dev/main/maven-repo/obr \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr
```

#### Startup Sequence

When you run this command, the following occurs:

1. **Felix Framework Initialization** - `galasa-boot` starts the Felix OSGi framework
2. **Bundle Loading** - Loads framework bundles and OBR repositories from Maven
3. **Jetty Server Start** - Loads the `org.apache.felix.http.jetty` bundle to provide HTTP services
4. **ApiStartup Invocation** - Retrieves and invokes the `ApiStartup` OSGi service
5. **Framework Initialization** - `ApiStartup` configures bootstrap properties and initializes the framework
6. **Servlet Bundle Loading** - All bundles providing `javax.servlet.Servlet` services are loaded and activated
7. **Route Registration** - Each servlet registers its URL patterns and routes
8. **Main Loop** - Server enters a monitoring loop, processing requests until shutdown

#### Command Line Options

- `--api` - Launches the API server mode (as opposed to test runner or resource management)
- `--localmaven` - Path to local Maven repository for resolving artifacts
- `--remotemaven` - URL of remote Maven repository for additional artifacts
- `--obr` - Maven coordinates of the OSGi Bundle Repository containing framework bundles
- `--bootstrap` (optional) - Override default bootstrap properties location
- `--trace` (optional) - Enable TRACE-level logging for debugging

## Verifying the API Server

### Health Check Endpoints

Check that the API server is healthy:

```bash
# General health check
curl http://localhost:8080/health

# RAS-specific health check
curl http://localhost:8080/ras/health
```

Both endpoints should return HTTP 200 with status information. The general `/health` endpoint verifies the Galasa framework is initialized; the `/ras/health` endpoint checks Result Archive Store connectivity.

### Bootstrap Endpoint

Retrieve bootstrap configuration:

```bash
curl http://localhost:8080/bootstrap
```

This returns the bootstrap properties that clients need to connect to the ecosystem.

### Using galasactl with the Local API

Test the API using `galasactl`:

```bash
# Set a property
galasactl properties set \
  --namespace $USER \
  --name test.prop1 \
  --value "$(date)" \
  --bootstrap http://localhost:8080/bootstrap

# Get properties from a namespace
galasactl properties get \
  --namespace $USER \
  --bootstrap http://localhost:8080/bootstrap

# Query test runs
galasactl runs get \
  --age 1d \
  --bootstrap http://localhost:8080/bootstrap
```

## Initializing the Configuration Property Store

For full API functionality, initialize the CPS with required framework properties:

### Create Properties YAML File

Save the following as `needed-properties.yaml`:

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resource.management.dead.heartbeat.timeout
data:
    value: "40"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: request.type.CLI.prefix
data:
    value: C
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: request.type.REQUEST.prefix
data:
    value: R
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resource.management.finished.timeout
data:
    value: "40"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: auth.store
data:
    value: couchdb:http://localhost:5984
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: credentials.store
data:
    value: etcd:http://localhost:2379
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: dynamicstatus.store
data:
    value: etcd:http://localhost:2379
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resultarchive.store
data:
    value: couchdb:http://localhost:5984
```

### Apply the Properties

```bash
galasactl resources apply \
  -f needed-properties.yaml \
  --bootstrap http://localhost:8080/bootstrap
```

## Authentication Testing

### Obtaining an Access Token

To authenticate API requests, obtain a JWT token from Dex:

1. Navigate to `http://localhost:8080/auth/login` in a browser
2. Log in with credentials from your Dex configuration (e.g., `admin@example.com` / `password`)
3. Complete the OAuth flow
4. Extract the JWT token from the callback

### Using Tokens with galasactl

Store your token for CLI use:

```bash
export GALASA_TOKEN="<your_jwt_token>"
```

Most `galasactl` commands will use this token automatically when connecting to the local API.

## Troubleshooting

### API Server Won't Start

**Symptom**: Server fails during startup or immediately exits

**Solutions**:
- Verify all Docker containers (CouchDB, etcd, Dex) are running: `docker ps`
- Check CouchDB single-node setup is complete
- Confirm environment variables are set correctly
- Review logs for bundle loading failures
- Ensure bootstrap.properties paths are correct

### Authentication Failures

**Symptom**: 401 Unauthorized responses from API endpoints

**Solutions**:
- Verify Dex is running and accessible: `curl http://localhost:5556/dex/.well-known/openid-configuration`
- Confirm `GALASA_DEX_ISSUER` matches Dex config `issuer` field exactly
- Check `GALASA_DEX_GRPC_HOSTNAME` matches Dex config `grpc.addr`
- Ensure JWT token hasn't expired (24 hour default)
- Verify user exists in Dex `staticPasswords`

### CouchDB Connection Errors

**Symptom**: RAS health check fails, can't query test runs

**Solutions**:
- Test CouchDB directly: `curl http://localhost:5984`
- Verify authentication token is correct: `curl -H "Authorization: Basic $COUCHDB_TOKEN" http://localhost:5984/_all_dbs`
- Check bootstrap.properties `framework.resultarchive.store` URI
- Confirm `dev.galasa.ras.couchdb` bundle is in `framework.extra.bundles`

### etcd Connection Errors

**Symptom**: CPS operations fail, can't set/get properties

**Solutions**:
- Test etcd directly: `curl http://localhost:2379/version`
- Verify bootstrap.properties `framework.config.store` URI
- Confirm `dev.galasa.cps.etcd` bundle is in `framework.extra.bundles`
- Check Docker network bridge exists: `docker network ls | grep app-tier`

### Port Conflicts

**Symptom**: Docker containers fail to start due to port binding errors

**Solutions**:
- Check for processes using required ports (5984, 2379, 2380, 5556, 5557, 5558, 8080)
- Stop conflicting services or change port mappings in Docker run commands
- Update corresponding URIs in bootstrap.properties and environment variables

## Advanced Configuration

### Running with Trace Logging

Enable detailed logging for debugging:

```bash
java -jar galasa-boot.jar \
  --api \
  --trace \
  --localmaven file://${HOME}/.m2/repository/ \
  --remotemaven https://development.galasa.dev/main/maven-repo/obr \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr
```

### Using Custom Bootstrap Location

Override the default bootstrap properties file:

```bash
java -jar galasa-boot.jar \
  --api \
  --bootstrap file:///path/to/custom/bootstrap.properties \
  --localmaven file://${HOME}/.m2/repository/ \
  --remotemaven https://development.galasa.dev/main/maven-repo/obr \
  --obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr
```

### Loading Additional Bundles

To load extra API bundles beyond those providing servlet services, add them to `api.extra.bundles`:

```properties
# In bootstrap.properties
api.extra.bundles=dev.galasa.auth.couchdb,com.example.custom.bundle
```

The API startup process loads bundles listed in `api.extra.bundles` in addition to automatically discovering all bundles that provide `javax.servlet.Servlet` services.

## Integration with Web UI

To test the API with the Galasa Web UI:

1. Clone and build the `webui` project
2. Run the Web UI locally: `./run-locally.sh` in the webui directory
3. Navigate to `http://localhost:3000`
4. The Web UI will connect to your local API at `http://localhost:8080`

The Dex configuration includes a static client for the Web UI with the appropriate redirect URI.

## Related Documentation

- [REST API Architecture](/openwiki/architecture/rest-api.md) - Detailed API server architecture and request routing
- [Storage Backend Implementations](/openwiki/integrations/storage-backends.md) - CouchDB, etcd, and other storage backends
- [Local Development](/openwiki/operations/local-development.md) - General local development setup and workflows
