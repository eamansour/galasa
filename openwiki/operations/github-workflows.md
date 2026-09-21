---
type: CI/CD System
title: GitHub Actions Workflows
description: The CI/CD system orchestrating builds, tests, and deployments across the Galasa project's modular architecture through coordinated GitHub Actions workflows.
tags: [ci-cd, github-actions, build-automation, artifact-passing, pr-validation, orchestration]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-b39f11dbfade28bbb7513743
    resource: repo://.github/scripts/get-changed-modules-pull-request.sh
  - id: openwiki-source-7421183f909addb719980c73
    resource: repo://.github/workflows/buildutils.yaml
  - id: openwiki-source-6a57fb827d34d9cce6a7b201
    resource: repo://.github/workflows/check-client-java-version.yaml
  - id: openwiki-source-b02edb50292ee24b0130b2f7
    resource: repo://.github/workflows/check-required-secrets-configured.yaml
  - id: openwiki-source-80f11b28e7909e80642ffcf6
    resource: repo://.github/workflows/cli.yaml
  - id: openwiki-source-536dbc3e3b9f22178b0efaec
    resource: repo://.github/workflows/codeql-java.yml
  - id: openwiki-source-ee7e20f771635e8e385875e2
    resource: repo://.github/workflows/framework.yaml
  - id: openwiki-source-f329c89444de4e0393d972a2
    resource: repo://.github/workflows/ivts.yaml
  - id: openwiki-source-3bfdb5dfa5feb5589b8e8700
    resource: repo://.github/workflows/obr.yaml
  - id: openwiki-source-81775da1a561d63a6f3867cd
    resource: repo://.github/workflows/platform.yaml
  - id: openwiki-source-f97f7d1c5ca40e1efc66546d
    resource: repo://.github/workflows/pr-framework.yaml
  - id: openwiki-source-2ee32e885f972bcc86af87a3
    resource: repo://.github/workflows/pull-requests.yaml
  - id: openwiki-source-dca48af1473bd950da5310a3
    resource: repo://.github/workflows/pushes.yaml
  - id: openwiki-source-454fc0428187985d845e59aa
    resource: repo://.github/workflows/releases.yaml
  - id: openwiki-source-4ef7085a161cff5b55929dd4
    resource: repo://.github/workflows/test-creds-store.yaml
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# GitHub Actions Workflows

The Galasa project uses a comprehensive GitHub Actions workflow system to build, test, and deploy its multi-module codebase. The system is architected around two main orchestrators—one for the main branch and one for pull requests—that coordinate module-specific workflows based on dependency relationships.

## Orchestrator Workflows

### Main Build Orchestrator (pushes.yaml)

The **Main Build Orchestrator** (`pushes.yaml`) executes on every push to the main branch and can be manually triggered via `workflow_dispatch`. It builds all modules sequentially according to their dependency graph:

**Build Sequence:**
1. **set-build-properties** - Extracts `GALASA_VERSION` from `build.properties` for use by downstream jobs
2. **check-required-secrets-configured** - Validates that all required secrets (GPG keys, GitHub tokens) are present
3. **Parallel builds** - `build-platform` and `build-buildutils` (no dependencies)
4. **Sequential builds** - `build-wrapping` and `build-gradle` (depend on platform)
5. **build-maven** (depends on gradle)
6. **build-framework** (depends on buildutils, wrapping, and maven)
7. **Parallel builds** - `build-extensions` and `build-managers` (both depend on framework)
8. **build-obr** (depends on extensions and managers)
9. **build-ivts** (depends on obr)
10. **build-cli** (depends on obr)
11. **test-creds-store** (depends on ivts and cli)
12. **build-docs** (depends on cli and test-creds-store)

After successful completion, the orchestrator triggers downstream repositories (helm, integratedtests, simplatform, webui, and isolated) for further integration testing.

### Pull Request Build Orchestrator (pull-requests.yaml)

The **Pull Request Build Orchestrator** (`pull-requests.yaml`) runs on every pull request to the main branch. It implements intelligent incremental builds by:

1. **Detecting changed modules** - Uses `.github/scripts/get-changed-modules-pull-request.sh` to identify which modules changed in the PR
2. **Finding artifact baseline** - Locates the last successful Main Build Orchestrator run to download unchanged module artifacts
3. **Selective rebuilds** - Only rebuilds modules that changed, using cached artifacts for unchanged dependencies
4. **Security scanning** - Runs CodeQL Java and Go scans when relevant modules change
5. **Secret detection** - Scans for accidentally committed secrets

The PR orchestrator uses module-specific change flags (e.g., `PLATFORM_CHANGED`, `FRAMEWORK_CHANGED`) to control which build jobs execute. When a foundational module like `platform` changes, all dependent modules are automatically marked for rebuild.

## Module-Specific Workflows

Each module has both a main workflow (e.g., `framework.yaml`) and a PR-specific workflow (e.g., `pr-framework.yaml`). These reusable workflows are invoked by the orchestrators using `workflow_call`.

### Build Tool Categories

**Gradle-based modules:**
- `platform` - Core platform components (Java 17, Gradle 9.0.0)
- `gradle` - Gradle plugin for Galasa
- `framework` - Core framework with OpenAPI bean generation
- `extensions` - Framework extensions
- `managers` - Test managers
- `ivts` - Integration Verification Tests

**Maven-based modules:**
- `wrapping` - Maven wrapper components
- `maven` - Maven plugin for Galasa
- `obr` - OSGi Bundle Repository (uses galasabld tool to generate BOM and OBR)

**Go-based modules:**
- `buildutils` - Build utilities including galasabld and openapi2beans
- `cli` - Galasa CLI (galasactl)

### Common Workflow Patterns

All module workflows follow similar patterns:

1. **Sparse checkout** - Only checks out the specific module directory
2. **Artifact download** - Downloads dependencies from previous jobs or prior workflow runs
3. **Build and test** - Executes module-specific build commands
4. **Artifact upload** - Uploads built artifacts to GitHub Actions artifact storage
5. **Failure reporting** - Sends Slack notifications on failure (for galasa-dev organization only)

### Framework Module Example

The `framework` module demonstrates a complex build process:

```yaml
# Downloads dependencies from previous workflow jobs
- Download platform artifacts
- Download wrapping artifacts
- Download gradle artifacts
- Download maven artifacts

# Generates Java beans from OpenAPI specification
- Login to GitHub Container Registry
- Build servlet beans with openapi2beans (Docker container)

# Builds the framework
- Build Framework source code with Gradle
- Sign artifacts with GPG (if isMainOrRelease=true)
- Upload framework artifacts
```

The framework requires the openapi2beans tool from the buildutils module to generate API beans before compilation.

## Artifact Passing and Dependencies

### Artifact Storage Strategy

Built artifacts are passed between workflow jobs using GitHub Actions' `upload-artifact` and `download-artifact` actions. Each module uploads its built artifacts to a named artifact (e.g., `platform`, `framework`, `obr`).

**Artifact Locations:**
- **Gradle/Java modules**: Upload to `modules/<module-name>/repo` (Maven repository structure)
- **Go modules**: Upload binaries to `modules/<module-name>/bin`
- **Docker images**: Push to GitHub Container Registry (`ghcr.io/<namespace>`)

### Dependency Resolution in PR Workflows

PR workflows use a fallback strategy to resolve dependencies:

```yaml
# Try to download from current workflow run (if module changed)
- name: Download platform from this workflow
  id: download-platform
  continue-on-error: true
  uses: actions/download-artifact@v4
  with:
    name: platform
    path: modules/artifacts

# Fall back to last successful main build (if module unchanged)
- name: Download platform from last successful workflow
  if: ${{ steps.download-platform.outcome == 'failure' }}
  uses: actions/download-artifact@v4
  with:
    name: platform
    path: modules/artifacts
    github-token: ${{ github.token }}
    run-id: ${{ inputs.artifact-id }}
```

This pattern allows PR builds to be faster by reusing artifacts from unchanged modules.

### Module Dependency Graph

<!-- openwiki: mermaid parse failed and this diagram was converted to a text fence so it does not break rendering. Fix the diagram source and restore the mermaid fence. Parser error: Heuristic: an unescaped angle bracket inside a label breaks rendering; rephrase the label. -->
```text
graph TD
    platform[platform<br/>Gradle]
    buildutils[buildutils<br/>Go/Docker]
    wrapping[wrapping<br/>Maven]
    gradle_mod[gradle<br/>Gradle]
    maven[maven<br/>Maven]
    framework[framework<br/>Gradle]
    extensions[extensions<br/>Gradle]
    managers[managers<br/>Gradle]
    obr[obr<br/>Maven]
    ivts[ivts<br/>Gradle]
    cli[cli<br/>Go]
    docs[docs]
    
    platform --> wrapping
    platform --> gradle_mod
    gradle_mod --> maven
    wrapping --> framework
    maven --> framework
    buildutils --> framework
    framework --> extensions
    framework --> managers
    extensions --> obr
    managers --> obr
    obr --> ivts
    obr --> cli
    ivts --> docs
    cli --> docs
    
    style platform fill:#e1f5ff
    style buildutils fill:#e1f5ff
    style framework fill:#fff4e1
    style obr fill:#ffe1e1
    style ivts fill:#e1ffe1
    style cli fill:#e1ffe1
    style docs fill:#f0f0f0
```

## Integration Verification Tests (IVTs)

The IVT workflow (`ivts.yaml`) serves dual purposes:

### Build-Time Testing
Builds the IVT module containing integration tests that verify framework functionality. The IVTs depend on all prior modules (platform through obr) and use the generated `galasa-bom` to resolve dependencies.

### Daily and Deployment Testing
The IVT workflow:
1. **Builds IVT artifacts** - Creates test JAR files
2. **Builds Docker images** - Creates Maven registry images for development environment:
   - `ivts-maven-artefacts` - Standard IVT Maven repository
   - `ivts-auth-maven-artefacts` - Authenticated Maven repository
3. **Builds compilation test images** - Creates test environments for:
   - **Isolated compilation tests** - Tests against isolated Galasa distribution
   - **MVP compilation tests** - Tests against MVP Galasa distribution
4. **Deploys to ArgoCD** - Recycles IVT applications in Kubernetes via ArgoCD
5. **Waits for health** - Verifies deployed applications are healthy

The IVT Docker images push to `ghcr.io/<namespace>/ivts-maven-artefacts:<branch>` and are automatically deployed to development environments for continuous verification.

## Required Secrets for Fork Builds

To build Galasa on a forked repository, contributors must configure repository secrets and variables. The `check-required-secrets-configured.yaml` workflow validates their presence.

### Repository Variables
- **WRITE_GITHUB_PACKAGES_USERNAME** - GitHub username for authenticating to GitHub Container Registry

### Repository Secrets

**GPG Signing Secrets** (required for Maven/Gradle artifact signing):
- **GPG_KEY** - Base64-encoded GPG key payload
- **GPG_KEYID** - GPG key ID in plain text
- **GPG_PASSPHRASE** - GPG key passphrase in plain text

**GitHub Container Registry Secrets**:
- **WRITE_GITHUB_PACKAGES_TOKEN** - GitHub Personal Access Token with `write:packages` scope

### Module Secret Requirements

| Module | Build Tool | Required Secrets |
|--------|------------|------------------|
| platform | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| buildutils | Go/Docker | WRITE_GITHUB_PACKAGES_USERNAME, WRITE_GITHUB_PACKAGES_TOKEN |
| wrapping | Maven | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| gradle | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| maven | Maven | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| framework | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE, WRITE_GITHUB_PACKAGES_USERNAME, WRITE_GITHUB_PACKAGES_TOKEN |
| extensions | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| managers | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE |
| obr | Maven | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE, WRITE_GITHUB_PACKAGES_USERNAME, WRITE_GITHUB_PACKAGES_TOKEN |
| ivts | Gradle | GPG_KEY, GPG_KEYID, GPG_PASSPHRASE, WRITE_GITHUB_PACKAGES_USERNAME, WRITE_GITHUB_PACKAGES_TOKEN |
| cli | Go/Docker | WRITE_GITHUB_PACKAGES_USERNAME, WRITE_GITHUB_PACKAGES_TOKEN |

Framework, obr, and ivts require GitHub Packages credentials because they pull Docker images (openapi2beans, galasabld) during their builds.

## Security and Quality Workflows

### CodeQL Scanning

Two CodeQL workflows provide static security analysis:

**codeql-java.yml** - Scans Java/Kotlin code:
- Runs on PR when Java modules change
- Runs weekly on Sunday at 13:41 UTC
- Uses `build-mode: none` (no compilation needed, reads from artifacts)
- Downloads all built artifacts for dependency resolution

**codeql-go.yml** - Scans Go code:
- Runs on PR when buildutils or cli modules change
- Analyzes Go source code for security vulnerabilities

Both workflows require the `all-artifacts` artifact containing the complete Maven repository state.

### Secret Detection

The PR orchestrator runs `detect-secrets.sh` on every pull request to prevent accidental secret commits.

### Dependency Monitoring

**check-client-java-version.yaml** runs daily at 9 AM UTC to:
1. Check Maven Central for new Kubernetes client-java versions
2. Compare with current version in `modules/platform/dev.galasa.platform/build.gradle`
3. Automatically create a PR if an update is available
4. Calculate Kubernetes version compatibility ranges
5. Update documentation with new compatibility information

This ensures the Kubernetes client dependency stays current without manual monitoring.

## Release Workflows

### Release Build Orchestrator (releases.yaml)

The Release Build Orchestrator is manually triggered via `workflow_dispatch` and accepts configuration inputs:

**Inputs:**
- **jacoco_enabled** - Controls code coverage collection (default: `false` for releases)
- **sign_artifacts** - Controls artifact signing (default: `true` for releases)

The release orchestrator follows the same build sequence as the main orchestrator but:
- Only runs on `galasa-dev` organization (enforced with `if: ${{ github.repository_owner == 'galasa-dev' }}`)
- Passes signing and coverage flags to module workflows
- Can be run on release branches or tags
- Triggers additional automation and isolated builds

### Release CLI Workflow (release-cli.yaml)

A separate workflow specifically for releasing CLI binaries, callable from other release automation workflows.

## Cross-Repository Orchestration

After successful main builds, the orchestrator triggers workflows in related repositories:

```yaml
- name: Triggering helm build
  env:
    GH_TOKEN: ${{ secrets.GALASA_TEAM_GITHUB_TOKEN }}
  run: |
    gh workflow run build-helm.yaml --repo https://github.com/galasa-dev/automation --ref ${{ env.BRANCH }}

- name: Triggering integratedtests build
  run: |
    gh workflow run build.yaml --repo https://github.com/galasa-dev/integratedtests --ref ${{ env.BRANCH }}
```

This creates a cascade of builds across the Galasa ecosystem:
1. **galasa** (main repo) → builds core modules
2. **automation** → builds Helm charts for Kubernetes deployment
3. **integratedtests** → runs comprehensive integration tests
4. **simplatform** → builds simulation platform for testing
5. **webui** → builds web interface
6. **isolated** → builds isolated distribution packages

## Operational Considerations

### Workflow Execution Time

The complete Main Build Orchestrator takes approximately 15-20 minutes on the `galasa-dev` organization. Fork builds may take longer depending on runner availability and cache state.

### Artifact Retention

GitHub Actions artifacts are retained according to repository settings (typically 90 days). The PR workflows depend on artifacts from recent main builds, so retention should be sufficient to cover the PR cycle.

### Failure Handling

Failed builds report to Slack via the `galasabld-ibm` Docker image's `slackpost` command:

```yaml
- name: Report failure in workflow to Slack
  if: ${{ failure() && github.repository_owner == 'galasa-dev' }}
  env: 
    SLACK_WEBHOOK: ${{ secrets.SLACK_WEBHOOK }}
  run: |
    docker run --rm ghcr.io/galasa-dev/galasabld-ibm:main slackpost workflows \
      --repo "galasa" --module "framework" --workflowName "${{ github.workflow }}" \
      --workflowRunNum "${{ github.run_id }}" --ref "${{ env.BRANCH }}" \
      --hook "${{ env.SLACK_WEBHOOK }}"
```

This only executes for the `galasa-dev` organization to avoid spam from forks.

### ArgoCD Integration

For IVT deployments, workflows interact with ArgoCD to deploy and verify Maven registry images:

1. **Recycle deployment** - Restarts the Kubernetes Deployment to pick up new image
2. **Wait for health** - Polls ArgoCD until the deployment is healthy
3. **Retry logic** - Attempts connection up to 10 times with 10-second delays

This enables continuous deployment of IVT images to development environments.

## Workflow Reusability

Most module workflows are designed as reusable workflows using `workflow_call`, accepting inputs like:

- **changed** - Boolean indicating if module needs rebuild (PR workflows)
- **artifact-id** - Workflow run ID to download baseline artifacts from (PR workflows)
- **jacoco_enabled** - Controls code coverage (release workflows)
- **sign_artifacts** - Controls GPG signing (release workflows)
- **galasa-version** - Version string from build.properties (obr workflow)

This design allows the same workflow to be used by main builds, PR builds, and release builds with different parameters.

## Testing Infrastructure

### Credentials Store Testing (test-creds-store.yaml)

Tests platform-specific credential storage implementations:

**Windows Runner:**
- Creates test credentials in Windows Credential Manager using `cmdkey`
- Runs `TestCredentialsStoreAccess` IVT with `GALASA_CREDENTIALS_STORE=os:auto`
- Verifies Windows Credential Manager integration

**macOS Runner:**
- Creates test credentials in macOS Keychain using `security add-generic-password`
- Runs the same IVT with Keychain integration
- Tests credential retrieval from macOS security framework

Both test jobs:
1. Download CLI binaries (galasactl)
2. Download all Galasa artifacts
3. Initialize local Galasa environment
4. Create platform-specific test credentials
5. Run IVT with OS-specific credential store configured
6. Verify credentials are correctly retrieved

This ensures the CLI and framework can access OS-native credential stores on all supported platforms.

## Change Detection Logic

The `get-changed-modules-pull-request.sh` script analyzes PR file changes to determine which modules need rebuilding. It uses:

**Module-to-path mapping:**
- Maps module names to directory prefixes (e.g., `framework` → `modules/framework`)
- Special handling for cross-cutting changes (e.g., OpenAPI spec changes affect framework, CLI, and docs)

**Dependency propagation:**
- Changes to `platform` trigger rebuilds of wrapping, gradle, maven, framework, extensions, managers, and obr
- Changes to `framework` trigger docs rebuild
- Changes to workflow files (`.github/`) trigger docs rebuild

**Output flags:**
The script sets boolean flags (e.g., `PLATFORM_CHANGED=true`) that control PR workflow job execution via GitHub Actions conditional expressions.
