---
type: quickstart guide
title: Galasa Repository Quickstart
description: Entry point routing developers to relevant documentation based on their goals—understanding architecture, building locally, contributing code, or working with specific modules.
tags: [quickstart, getting-started, navigation, workflow, developer-guide]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-f7c89635dfc6efb0ecec007f
    resource: repo://.devcontainer/devcontainer.json
  - id: openwiki-source-f317ee207e1653d2033c81a4
    resource: repo://CONTRIBUTING.md
  - id: openwiki-source-b196523bb0b4af018cec14cc
    resource: repo://developer-docs/install-pre-req-tools.md
  - id: openwiki-source-73795f2248bb997264b9dc35
    resource: repo://developer-docs/README.md
  - id: openwiki-source-a5636814d544e3149bb62408
    resource: repo://developer-docs/test-run-lifecycle.md
  - id: openwiki-source-27c1f4eef72d59f8cd482536
    resource: repo://modules/cli/README.md
  - id: openwiki-source-19c289d51521684350e01d7c
    resource: repo://modules/framework/README.md
  - id: openwiki-source-83d1b64462dc4ee9ad9d27be
    resource: repo://modules/managers/README.md
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
  - id: openwiki-source-d9cc689740542a78d804536c
    resource: repo://tools/build-locally.sh
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Galasa Repository Quickstart

Welcome to the Galasa project! This quickstart guide helps you navigate the documentation and get started based on your goals, whether you're exploring the codebase, building locally, contributing code, or diving into specific modules.

## What is Galasa?

Galasa is an open-source test automation framework designed for enterprise-scale testing across multiple platforms, including mainframe systems (z/OS), cloud environments, and distributed systems. The framework provides a modular architecture with pluggable Managers that handle resource provisioning, test lifecycle orchestration, and integration with various technologies.

**Repository Structure:**
- **modules/** - Source code for all components (CLI, Framework, Managers, Extensions, IVTs)
- **tools/** - Build scripts and utilities
- **docs/** - Documentation website source
- **developer-docs/** - Technical notes for contributors

## Quick Navigation by Goal

### 🔍 I want to understand the architecture

Start with these pages to build a mental model of how Galasa works:

1. **[Architecture Overview](/openwiki/architecture/overview.md)** - High-level component relationships and architectural principles
2. **[Module Structure](/openwiki/concepts/modules.md)** - Organization of the repository's eleven modules
3. **[Framework Core Architecture](/openwiki/architecture/framework-core.md)** - Central orchestrator that manages test execution
4. **[Manager Architecture](/openwiki/architecture/managers.md)** - Pluggable components that provide technology-specific capabilities
5. **[Test Execution Lifecycle](/openwiki/concepts/test-execution-lifecycle.md)** - How tests progress from submission to completion

**Key Concepts:**
- [OSGi Bundles and OBRs](/openwiki/concepts/osgi-bundles.md) - Packaging and deployment model
- [Configuration Property System](/openwiki/concepts/configuration-properties.md) - Hierarchical configuration mechanism
- [Storage Services](/openwiki/architecture/storage-services.md) - CPS, DSS, RAS, and Credentials Store

### 🔨 I want to build the code locally

Follow this path to set up your development environment and build Galasa:

1. **[Building Locally](/openwiki/workflows/building-locally.md)** - Comprehensive guide to building all modules
   - Prerequisites and tool installation
   - Using `build-locally.sh` script
   - Module build order and dependencies
   - Common build scenarios

2. **[Development Container Setup](/openwiki/operations/dev-container.md)** *(Recommended)* - Pre-configured VSCode environment with all tools

3. **[Local Development Environment](/openwiki/operations/local-development.md)** - Setting up GALASA_HOME and property files

**Quick Build Commands:**
```bash
# Build everything (recommended first build)
./tools/build-locally.sh

# Build from a specific module
./tools/build-locally.sh --module framework

# Build a single module without chaining
./tools/build-locally.sh --module managers --chain false

# Build with Docker images (CLI and buildutils modules)
./tools/build-locally.sh --docker
```

**Prerequisites:**
- Java 17 (Semeru 17.0.12 or later)
- Gradle 8.9
- Maven 3.9.0
- Go 1.23.5 (for CLI and buildutils)
- Python 3.11.0 (for tools and scripts)

### 🤝 I want to contribute code

Follow the contribution workflow to submit your changes:

1. **[Contributing Code](/openwiki/workflows/contributing-code.md)** - Complete contribution process
   - Forking the repository
   - Setting up GitHub Actions secrets and variables
   - Signing commits with GPG
   - Opening pull requests
   - DCO requirements

2. **[Build Dependencies](/openwiki/concepts/build-dependencies.md)** - Understanding dependency management and versioning

3. **[GitHub Workflows](/openwiki/operations/github-workflows.md)** - CI/CD pipeline and PR validation

**Before You Start:**
- All commits must be signed with GPG (`-s -S` flags)
- Contributions must adhere to the [Developer Certificate of Origin](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTIONS.md)
- Set up repository secrets for fork builds: `GPG_KEY`, `GPG_KEYID`, `GPG_PASSPHRASE`, `WRITE_GITHUB_PACKAGES_TOKEN`, `WRITE_GITHUB_PACKAGES_USERNAME`

### 🧪 I want to run or write tests

Learn how to create test projects and run them in various environments:

1. **[Using galasactl CLI](/openwiki/workflows/using-galasactl.md)** - Command-line tool for test management
   - Initializing local environment (`galasactl local init`)
   - Creating test projects
   - Running tests locally

2. **[Building Test Projects](/openwiki/workflows/building-test-projects.md)** - Creating and building test bundles with Maven or Gradle

3. **[Running Tests](/openwiki/workflows/running-tests.md)** - Three execution modes:
   - Local JVM (direct execution)
   - Local ecosystem (full framework)
   - Remote ecosystem (deployed service)

4. **[Installation Verification Tests (IVTs)](/openwiki/testing/ivts.md)** - Manager validation test suite

**Quick Test Commands:**
```bash
# Initialize local environment
galasactl local init

# Create a test project
galasactl project create --package dev.galasa.example.banking

# Run a test locally
galasactl runs submit local --class dev.galasa.example.banking/TestBanking
```

### 🔧 I want to work with specific modules

Jump directly to module-specific documentation:

#### CLI Module
- **[CLI Architecture](/openwiki/architecture/cli.md)** - Go-based command-line tool structure
- **[Using galasactl](/openwiki/workflows/using-galasactl.md)** - Command reference and workflows
- **Location:** `modules/cli/`
- **Build Tool:** Go + Docker

#### Framework Module
- **[Framework Core Architecture](/openwiki/architecture/framework-core.md)** - Test orchestration and lifecycle management
- **[Galasa Boot JAR](/openwiki/operations/galasa-boot.md)** - Launch modes and command-line options
- **[Kubernetes Controller](/openwiki/operations/kubernetes-controller.md)** - Test scheduling in K8s
- **[Testing API Locally](/openwiki/operations/testing-api-locally.md)** - REST API development setup
- **Location:** `modules/framework/`
- **Build Tool:** Gradle

#### Managers Module
- **[Manager Architecture](/openwiki/architecture/managers.md)** - Plugin system and lifecycle
- **[Manager Lifecycle Methods](/openwiki/concepts/manager-lifecycle.md)** - Detailed phase documentation
- **[Creating a Custom Manager](/openwiki/workflows/creating-a-manager.md)** - Step-by-step implementation guide
- **Location:** `modules/managers/`
- **Build Tool:** Gradle

#### Extensions Module
- **[Storage Backend Implementations](/openwiki/integrations/storage-backends.md)** - Concrete CPS, DSS, RAS implementations
- **Location:** `modules/extensions/`
- **Build Tool:** Gradle

#### Other Modules
- **Platform** - Core APIs and shared utilities (`modules/platform/`)
- **Buildutils** - Go-based build utilities and galasabld tool (`modules/buildutils/`)
- **Wrapping** - Third-party dependency wrappers (`modules/wrapping/`)
- **Gradle/Maven** - Build plugins and tooling (`modules/gradle/`, `modules/maven/`)
- **OBR** - OSGi Bundle Repository aggregation (`modules/obr/`)
- **IVTs** - Installation Verification Tests (`modules/ivts/`)

### 🚀 I want to create a custom Manager

Managers extend Galasa's capabilities for specific technologies. Follow this workflow:

1. **[Creating a Custom Manager](/openwiki/workflows/creating-a-manager.md)** - Complete implementation guide
   - Manager interface implementation
   - Lifecycle method hooks
   - Annotation processing and field injection
   - Dependency management

2. **[Manager Lifecycle Methods](/openwiki/concepts/manager-lifecycle.md)** - When each method is called

3. **[Manager Architecture](/openwiki/architecture/managers.md)** - Design patterns and best practices

**Quick Start:**
```bash
# Create a manager project
galasactl project create --package dev.galasa.example.docker --manager --managerName docker
```

### 📚 I want to understand key concepts

Dive deeper into fundamental Galasa concepts:

- **[Configuration Properties](/openwiki/concepts/configuration-properties.md)** - Hierarchical namespace system (CPS)
- **[OSGi Bundles](/openwiki/concepts/osgi-bundles.md)** - Runtime module system and OBR packaging
- **[Authentication](/openwiki/concepts/authentication.md)** - Personal access tokens and RBAC
- **[Build Dependencies](/openwiki/concepts/build-dependencies.md)** - Version management across modules
- **[Test Execution Lifecycle](/openwiki/concepts/test-execution-lifecycle.md)** - State transitions and resource provisioning
- **[Manager Lifecycle](/openwiki/concepts/manager-lifecycle.md)** - Detailed phase documentation

### 🔗 I want to integrate with Galasa services

Learn how to interact with deployed Galasa instances:

- **[REST API Architecture](/openwiki/architecture/rest-api.md)** - Server structure and endpoints
- **[Testing API Locally](/openwiki/operations/testing-api-locally.md)** - Setting up local API server with dependencies
- **[Authentication](/openwiki/concepts/authentication.md)** - Creating and using personal access tokens
- **[Kubernetes Controller](/openwiki/operations/kubernetes-controller.md)** - Test scheduling in K8s ecosystems

### ⚙️ I want operational knowledge

Understand deployment, versioning, and operational aspects:

- **[Version Management](/openwiki/operations/versioning.md)** - Using `set-version.sh` and galasabld
- **[GitHub Workflows](/openwiki/operations/github-workflows.md)** - CI/CD pipeline orchestration
- **[Galasa Boot JAR](/openwiki/operations/galasa-boot.md)** - Launching framework services
- **[Development Container](/openwiki/operations/dev-container.md)** - Pre-configured development environment
- **[Local Development Environment](/openwiki/operations/local-development.md)** - GALASA_HOME setup and configuration

## Common Development Tasks

### Setting Up for First-Time Development

1. **Clone the repository:**
   ```bash
   git clone https://github.com/galasa-dev/galasa.git
   cd galasa
   ```

2. **Choose your environment:**
   - **Recommended:** Open in VSCode and use the dev container (see [Dev Container Setup](/openwiki/operations/dev-container.md))
   - **Alternative:** Install tools manually (see [Building Locally](/openwiki/workflows/building-locally.md))

3. **Run your first build:**
   ```bash
   ./tools/build-locally.sh
   ```

4. **Initialize local test environment:**
   ```bash
   # Download galasactl binary or build from modules/cli
   galasactl local init
   ```

### Making Your First Contribution

1. **Find an issue:** Check the [Kanban board](https://github.com/orgs/galasa-dev/projects/3) for issues tagged `good first issue`

2. **Fork and set up:** Follow [Contributing Code](/openwiki/workflows/contributing-code.md) to configure secrets

3. **Make changes:** Build and test locally with `./tools/build-locally.sh --module <name>`

4. **Submit PR:** Ensure commits are signed (`git commit -s -S`) and pass DCO checks

### Debugging Test Execution

1. **Enable trace logging:**
   ```bash
   java -jar galasa-boot.jar --test MyBundle/MyTest --trace
   ```

2. **Examine logs in GALASA_HOME:**
   - Test runs: `~/.galasa/ras/`
   - Framework logs: Check console output or RAS artifacts

3. **Understand lifecycle:** Review [Test Execution Lifecycle](/openwiki/concepts/test-execution-lifecycle.md)

### Working with Storage Services

1. **Local file-based stores:** Default for local development (see [Local Development](/openwiki/operations/local-development.md))

2. **Remote stores:** Configure bootstrap properties to point to CouchDB/etcd (see [Storage Services](/openwiki/architecture/storage-services.md))

3. **Implementation details:** See [Storage Backend Implementations](/openwiki/integrations/storage-backends.md)

## Module Build Order

When building with `--chain true` (default), modules build in this order:

1. **platform** - Core APIs and shared interfaces
2. **buildutils** - Go utilities for build and release management
3. **wrapping** - Third-party dependency OSGi bundles
4. **gradle** - Gradle plugin for Galasa projects
5. **maven** - Maven plugin for Galasa projects
6. **framework** - Framework Core and test runner
7. **extensions** - Storage backend implementations
8. **managers** - All Manager implementations
9. **obr** - OSGi Bundle Repository aggregation
10. **ivts** - Installation Verification Tests
11. **cli** - galasactl command-line tool

Each module depends on artifacts from previous modules. Use `--module <name>` to start from a specific point in the chain.

## Getting Help

- **Slack Channel:** [galasa.slack.com](https://galasa.slack.com) ([join here](https://join.slack.com/t/galasa/shared_invite/zt-ele2ic8x-VepEO1o13t4Jtb3ZuM4RUA))
- **Issues:** [Project Management Repository](https://github.com/galasa-dev/projectmanagement/issues)
- **Documentation:** [galasa.dev](https://galasa.dev)
- **Source Code:** [GitHub - galasa-dev](https://github.com/galasa-dev)

## Key Resources

- **Main Repository:** [github.com/galasa-dev/galasa](https://github.com/galasa-dev/galasa)
- **Contributing Guidelines:** [CONTRIBUTING.md](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTING.md)
- **Developer Certificate of Origin:** [CONTRIBUTIONS.md](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTIONS.md)
- **License:** [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE)

## Next Steps

Based on your primary interest, jump to one of these starting points:

- **Learning Architecture:** Start with [Architecture Overview](/openwiki/architecture/overview.md)
- **Building Code:** Go to [Building Locally](/openwiki/workflows/building-locally.md)
- **Contributing:** Begin with [Contributing Code](/openwiki/workflows/contributing-code.md)
- **Writing Tests:** See [Using galasactl](/openwiki/workflows/using-galasactl.md)
- **Creating Managers:** Read [Creating a Custom Manager](/openwiki/workflows/creating-a-manager.md)
