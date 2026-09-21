---
type: setup guide
title: Development Container Setup
description: Guide to using the VSCode dev container for Galasa development, providing a pre-configured environment with all required tools.
tags: [development, docker, vscode, devcontainer, setup, tools]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-60d16e2b30e2587775473b63
    resource: repo://.devcontainer/dev-container-setup.sh
  - id: openwiki-source-f7c89635dfc6efb0ecec007f
    resource: repo://.devcontainer/devcontainer.json
  - id: openwiki-source-b196523bb0b4af018cec14cc
    resource: repo://developer-docs/install-pre-req-tools.md
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Development Container Setup

The Galasa project provides a development container configuration that offers a fully configured development environment with all required tools pre-installed. This is the recommended approach for local development as it eliminates the need to manually install and configure Python, Java, Gradle, Maven, Go, and other dependencies.

## Overview

The dev container uses a containerized environment that includes:

- Java 17 (Semeru distribution)
- Maven 3.9.0
- Gradle 9.0.0
- Python 3.11.0
- Go 1.23.5
- Docker-outside-of-Docker support
- Pre-configured VSCode extensions for Java, Go, and ESLint

This setup ensures consistency across development environments and eliminates common configuration issues.

## Prerequisites

### Container Engine

Before setting up the dev container, you need a container engine installed and running on your system. The following are supported:

- **Docker Desktop** - Commercial container platform with GUI management
- **Rancher Desktop** - Open-source Docker alternative with GUI
- **Podman** - Daemonless container engine

Ensure your chosen container engine is installed and running before proceeding.

### Visual Studio Code

1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) by Microsoft

The Dev Containers extension enables VSCode to connect to and develop inside containers.

## Setup Process

### 1. Clone the Repository

```bash
git clone https://github.com/galasa-dev/galasa.git
cd galasa
```

### 2. Open in Container

1. Start your container engine (Docker Desktop, Rancher Desktop, or Podman)
2. Open the cloned repository in VSCode
3. When prompted, click **"Reopen in Container"**, or:
   - Press `F1` or `Ctrl+Shift+P` (Windows/Linux) / `Cmd+Shift+P` (Mac)
   - Type "Dev Containers: Reopen in Container"
   - Select the command

VSCode will build the dev container based on the configuration in `.devcontainer/devcontainer.json`. This may take a few minutes the first time as it downloads the base image and installs features.

### 3. Container Initialization

On container creation, the `dev-container-setup.sh` script runs automatically to configure the environment:

- Copies local environment variables into the container (if defined)
- Mounts your local `.m2` directory for Maven artifacts
- Configures Docker socket access for Docker-in-Docker workflows

The following environment variables from your local machine are preserved in the container if they are set:

- `GALASA_TOKEN` - Authentication token for Galasa ecosystem services
- `SOURCE_MAVEN` - Maven repository location
- `GALASA_BOOTSTRAP` - Bootstrap configuration URL
- `GPG_PASSPHRASE` - GPG key passphrase for signing artifacts

## Benefits Over Manual Setup

### Consistency

All developers use identical tool versions, eliminating "works on my machine" issues. The container configuration specifies exact versions of:

- Java 17.0.14 (Semeru distribution)
- Maven 3.9.0
- Gradle 9.0.0
- Python 3.11.0
- Go 1.23.5

### Quick Onboarding

New contributors can start developing within minutes instead of hours spent installing and configuring tools manually. No need to:

- Install and configure SDKman
- Manage multiple Java versions
- Configure Gradle properties for SSL trust stores
- Set up Maven settings.xml
- Install Python via pyenv
- Configure Docker access

### Isolated Environment

The container keeps Galasa development tools isolated from your system, preventing conflicts with other projects or system installations.

### Pre-configured Extensions

The container automatically installs VSCode extensions:

- Java Extension Pack (vscjava.vscode-java-pack)
- Go extension (golang.go)
- ESLint (dbaeumer.vscode-eslint)

These extensions are configured and ready to use immediately.

## Configuration Details

### Container Features

The dev container configuration (`.devcontainer/devcontainer.json`) uses Microsoft's devcontainer features:

**Java Feature** (`ghcr.io/devcontainers/features/java:1`):
- Provides Java 17.0.14-sem (Semeru distribution)
- Installs Maven 3.9.0
- Installs Gradle 9.0.0

**Python Feature** (`ghcr.io/devcontainers/features/python:1`):
- Provides Python 3.11.0

**Go Feature** (`ghcr.io/devcontainers/features/go:1`):
- Provides Go 1.23.5

**Docker-outside-of-Docker** (`ghcr.io/devcontainers/features/docker-outside-of-docker:1`):
- Enables Docker CLI inside the container
- Connects to host Docker daemon
- Allows building and running containers from within dev container

### Volume Mounts

The container mounts two important resources:

1. **Maven Repository** - `~/.m2` from your host is mounted at `/home/vscode/.m2`
   - Preserves downloaded Maven dependencies across container rebuilds
   - Allows sharing artifacts between host and container

2. **Docker Socket** - `/var/run/docker.sock` is mounted as `/var/run/docker-host.sock`
   - Enables Docker-in-Docker workflows
   - Required for building and testing Docker images

### Environment Variable Handling

The `dev-container-setup.sh` script runs on container creation and copies specified environment variables from your local system into the container's `.bashrc`. This ensures credentials and configuration are available in all terminal sessions.

**Important:** Environment variable changes on your host system require restarting VSCode to be reflected in the container. The container only reads variables during initialization.

## Working with the Dev Container

### Building Galasa

Once inside the dev container, use the standard build scripts:

```bash
# Build all modules
./tools/build-locally.sh

# Build specific module
./tools/build-locally.sh --module platform

# Build without chaining
./tools/build-locally.sh --chain false

# Build with Docker image creation
./tools/build-locally.sh --docker
```

### Accessing Terminals

All terminals opened in VSCode while connected to the dev container run inside the containerized environment with access to all installed tools.

### Stopping and Starting

- **Close VSCode** - The container continues running
- **"Reopen Locally"** command - Disconnects from container
- **"Rebuild Container"** command - Rebuilds the container from scratch (useful after configuration changes)

### Troubleshooting

**Container fails to build:**
- Ensure your container engine is running
- Check Docker/Podman has sufficient resources allocated
- Try rebuilding with "Dev Containers: Rebuild Container"

**Environment variables not available:**
- Verify variables are set in your shell before launching VSCode
- Restart VSCode to refresh environment variable propagation
- Check the container's `.bashrc` to confirm variables were copied

**Maven artifacts not persisting:**
- Verify the `.m2` mount in `devcontainer.json` references the correct path
- On Windows, ensure path format is compatible

**Docker commands fail inside container:**
- Confirm Docker socket mount is correct
- Verify Docker daemon is running on host
- Check socket permissions

## Related Documentation

- **[Local Development](/openwiki/operations/local-development.md)** - Manual setup alternative without containers
- **[Building Locally](/openwiki/workflows/building-locally.md)** - Build scripts and workflow details

## Configuration Files

The dev container setup is defined by:

- **`.devcontainer/devcontainer.json`** - Container configuration, features, mounts, and extensions
- **`.devcontainer/dev-container-setup.sh`** - Initialization script that runs on container creation
