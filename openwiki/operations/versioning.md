---
type: operations guide
title: Version Management
description: Managing version numbers across Galasa modules using set-version scripts and the galasabld versioning utility
tags: [versioning, build, operations, galasabld, version-management]
verified:
  - by: openwiki/0.5.2
    at: 2026-09-21T12:35:12.771Z
sources:
  - id: openwiki-source-cc28a08b9614839ab4a8b182
    resource: repo://build.properties
  - id: openwiki-source-ee8679e321a8f2ead197713c
    resource: repo://modules/buildutils/pkg/cmd/versioning.go
  - id: openwiki-source-5cb0e3eec08ddc33d02033e3
    resource: repo://modules/buildutils/pkg/cmd/versioningList.go
  - id: openwiki-source-25b03f2b9280798a0079768b
    resource: repo://modules/buildutils/pkg/cmd/versioningSuffix.go
  - id: openwiki-source-1dd206739bd503561da224c6
    resource: repo://modules/buildutils/pkg/cmd/versioningSuffixRemove.go
  - id: openwiki-source-762ebde46fece4bfbed60fb5
    resource: repo://modules/buildutils/pkg/cmd/versioningSuffixSet.go
  - id: openwiki-source-4be7b7be08bad0c06b3ce76c
    resource: repo://modules/buildutils/pkg/versioning/list.go
  - id: openwiki-source-6a39641dff223b9dd974474c
    resource: repo://modules/buildutils/pkg/versioning/suffixRemove.go
  - id: openwiki-source-0c0d477b13932a1cb71ad45b
    resource: repo://modules/buildutils/pkg/versioning/suffixSet_test.go
  - id: openwiki-source-26b8cb014aa9a83b4faae32d
    resource: repo://modules/buildutils/pkg/versioning/suffixSet.go
  - id: openwiki-source-925a57b76bc49106b0371afe
    resource: repo://modules/buildutils/README.md
  - id: openwiki-source-1a45f1bcd39656cc7574577f
    resource: repo://modules/framework/set-version.sh
  - id: openwiki-source-f711e5eef636ed90b0f3f220
    resource: repo://modules/gradle/set-version.sh
  - id: openwiki-source-e0b4baf834d72f1a8a0c94fb
    resource: repo://modules/maven/set-version.sh
  - id: openwiki-source-2f8166d16f7247159a59b5d2
    resource: repo://tools/set-version.sh
generated: { by: "openwiki/0.5.2", at: "2026-09-21T12:35:12.771Z" }
---

# Version Management

Galasa uses a coordinated versioning strategy across its multiple modules. Version management is handled through two complementary mechanisms: module-level `set-version.sh` scripts that update version numbers in build files and configuration, and the `galasabld` command-line utility that can list and manipulate version suffixes across all Gradle modules in the repository.

## Version Format

Galasa versions follow semantic versioning with optional suffixes:

- **Base version**: `X.Y.Z` (e.g., `0.36.0`, `1.1.1`)
- **Suffixed version**: `X.Y.Z-SUFFIX` or `X.Y.Z_SUFFIX` (e.g., `0.36.0-SNAPSHOT`, `0.21.0-alpha`)

Version suffixes are separated from the base version by either a hyphen (`-`) or underscore (`_`). Common suffixes include:

- `-SNAPSHOT`: Development versions that change frequently
- `-alpha`: Early preview releases
- `-beta`: Pre-release versions
- Custom suffixes for specific builds or branches

## Repository Version Management with set-version.sh

### Top-Level Script

The repository root contains a top-level `set-version.sh` script that orchestrates version updates across all modules.

**Usage:**
```bash
./set-version.sh --version <version>
```

**Example:**
```bash
./set-version.sh --version 1.1.1
```

### What It Updates

The top-level script updates versions in multiple locations:

1. **Module versions**: Calls each module's `set-version.sh` script
2. **Documentation**: Updates version references in docs
3. **README.md**: Updates example commands showing version numbers
4. **build.properties**: Updates `GALASA_VERSION` property

### Module-Specific Scripts

Each module under `modules/` has its own `set-version.sh` script that updates version-specific content within that module:

- `modules/buildutils/set-version.sh`
- `modules/cli/set-version.sh`
- `modules/extensions/set-version.sh`
- `modules/framework/set-version.sh`
- `modules/gradle/set-version.sh`
- `modules/managers/set-version.sh`
- `modules/maven/set-version.sh`
- And others...

Each module script is responsible for updating its own build files, configuration, and documentation. The exact files updated vary by module but typically include:

- Gradle build files (`build.gradle`, `settings.gradle`)
- Maven POM files (`pom.xml`)
- Release manifests (`release.yaml`)
- Module-specific configuration files

### Script Structure

All `set-version.sh` scripts follow a common pattern:

1. Parse command-line arguments (requires `--version` flag)
2. Validate the provided version
3. Use `sed` to find and replace version patterns in files
4. Update temporary files, then copy them over originals
5. Report success or failure with colored output

## The galasabld Versioning Utility

The `galasabld` utility is a Go-based build tool that provides commands for managing versions across Gradle modules. It's built from the `modules/buildutils` module.

### Listing Module Versions

The `versioning list` command discovers and displays all module versions in a source tree.

**Command:**
```bash
galasabld versioning list --sourcefolderpath <path>
```

**Example:**
```bash
galasabld versioning list --sourcefolderpath /workspace/galasa
```

**Sample Output:**
```
a.b.c. 0.21.0
a.b.d. 0.25.0-SNAPSHOT
```

### How Module Discovery Works

The versioning utility identifies Gradle modules by scanning for directories that contain:

1. A `build.gradle` file with a version line: `version = "X.Y.Z"`
2. A `settings.gradle` file with a project name: `rootProject.name = "project.name"`

Both files must be present for a directory to be recognized as a module. Modules are listed sorted alphabetically by project name.

### Setting Version Suffixes

The `versioning suffix set` command adds or replaces version suffixes across all discovered modules.

**Command:**
```bash
galasabld versioning suffix set --sourcefolderpath <path> --suffix <suffix>
```

**Example:**
```bash
galasabld versioning suffix set --sourcefolderpath /workspace/galasa --suffix "-alpha"
```

**Behavior:**

- Strips any existing suffix from the version
- Appends the new suffix
- Updates the `build.gradle` file in each module
- `0.0.1` becomes `0.0.1-alpha`
- `0.0.1-SNAPSHOT` becomes `0.0.1-alpha`
- `0.0.1-dev-mine` becomes `0.0.1-alpha`

**Suffix Requirements:**

The `--suffix` parameter **must** start with either `-` or `_`. Invalid suffixes (e.g., `alpha` without a prefix) are rejected with an error.

### Removing Version Suffixes

The `versioning suffix remove` command strips all suffixes from module versions, leaving only the base version number.

**Command:**
```bash
galasabld versioning suffix remove --sourcefolderpath <path>
```

**Example:**
```bash
galasabld versioning suffix remove --sourcefolderpath /workspace/galasa
```

**Behavior:**

- Removes everything after the first `-` or `_` character
- `0.0.1-SNAPSHOT` becomes `0.0.1`
- `0.0.1-alpha` becomes `0.0.1`
- `0.0.1_dev` becomes `0.0.1`
- `0.0.1-dev-mine` becomes `0.0.1`

## Version Management Workflow

### Development Workflow

During active development, versions typically use the `-SNAPSHOT` suffix:

```bash
# Set all modules to a snapshot version
galasabld versioning suffix set --sourcefolderpath . --suffix "-SNAPSHOT"
```

### Release Preparation

Before creating a release, remove suffixes to produce clean release versions:

```bash
# Step 1: Remove suffixes from all modules
galasabld versioning suffix remove --sourcefolderpath .

# Step 2: Set the official version number across the repository
./set-version.sh --version 1.2.0
```

### Pre-Release Versions

For alpha or beta releases, apply appropriate suffixes:

```bash
# Create an alpha release
galasabld versioning suffix set --sourcefolderpath . --suffix "-alpha"
./set-version.sh --version 1.2.0-alpha
```

## Implementation Details

### Version Pattern Matching

The versioning code uses regular expressions to match version patterns in Gradle files:

```go
version = "0.36.0"
version="0.36.0"
version = '0.36.0'
```

All three formats are recognized and updated correctly.

### Project Name Extraction

Module project names come from `settings.gradle`:

```gradle
rootProject.name = "dev.galasa.examples/module2"
```

The project name is used to identify modules in listings and ensure correct module discovery.

### Suffix Stripping Logic

When setting a new suffix or removing suffixes, the code:

1. Splits the version at the first `-` delimiter
2. Takes only the part before the delimiter (base version)
3. Splits again at the first `_` delimiter
4. Takes only the part before the delimiter
5. Appends the new suffix (if any)

This ensures that complex suffixes like `0.0.1-dev-mine` are completely removed before applying a new suffix.

## Build Integration

The version management system integrates with the broader Galasa build system:

- **build.properties**: Central version file used by GitHub Actions workflows
- **Release manifests**: Module-specific `release.yaml` files reference component versions
- **Gradle plugins**: Custom Gradle plugins read version properties
- **Maven artifacts**: Version numbers propagate to published Maven coordinates

The coordinated version management ensures that all parts of the Galasa system use consistent version numbers during builds and releases.
