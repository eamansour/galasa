---
name: galasa-user
description: Documentation with references to what Galasa is, how to use the Galasa CLI tool (galasactl), and how to create Galasa test projects. Use this skill when you are asked to do anything using Galasa.
---

# Galasa User Skill

## When to use this skill

Use this skill when you are asked to create or do anything using Galasa.

## What this skill contains

This skill contains individual skill references that explain how the various aspects of the Galasa testing framework work.

## Context Optimization Strategy

**IMPORTANT**: To minimize token usage, load files selectively based on task type:

### Always Load First
1. **This file** (SKILL.md)
2. **[Quick Reference](./references/galasa-quick-reference.md)** - Common commands, patterns, and configurations

### Load Based on Task Type

**Understanding Galasa** (what is Galasa, why use it):
- [Galasa overview](./references/galasa-overview.md) - What Galasa is, key features, use cases
- [Galasa architecture](./references/galasa-architecture.md) - Framework components, managers, test runner
- [Galasa ecosystem](./references/galasa-ecosystem.md) - Running at scale, stores, services

**Authentication & Ecosystem Access** (connecting to an Ecosystem, tokens, secrets):
- [Galasa auth and secrets](./references/galasa-auth-and-secrets.md) - GALASA_TOKEN setup, auth login/logout, token management, secrets

**Ecosystem Operations** (remote test submission, run management, CPS properties, resources, streams):
- [Galasa auth and secrets](./references/galasa-auth-and-secrets.md) - Load first for auth setup
- [Galasa ecosystem management](./references/galasa-ecosystem-management.md) - Remote runs, properties, resources, streams

**CLI Operations** (creating projects, running tests locally, viewing results):
- [Galasa CLI tool](./references/galasa-cli-tool.md) - Local CLI operations — project creation (including manager projects), `runs submit local`, debugging (`--debug`, `--debugMode`, `--debugPort`), viewing local RAS results

**Test Development** (writing test classes, configuring environments):
- [Writing Galasa tests](./references/writing-galasa-tests.md) - Test structure, build commands, CPS/credentials, `ITestResultProvider`, `@ContinueOnTestFailure`, clustering, hybrid mode, debug mode
- [Test development best practices](./references/test-development-best-practices.md) - Design principles for scalable tests
- [Galasa CLI tool](./references/galasa-cli-tool.md) - For project creation context

**Manager Setup** (adding dependencies, injecting managers):
- [Using Galasa managers](./references/using-galasa-managers.md) - Manager dependencies and injection patterns
- Load terminal reference ONLY if user mentions screens/fields/timing

**Terminal Interaction** (working with 3270 screens):
- [Terminal Interaction Reference](./references/terminal-interaction-reference.md) - Detailed timing, navigation, data extraction
- Load ONLY when user explicitly works with terminal screens or mentions timing issues
- For basic terminal patterns, use quick reference instead

**Troubleshooting** (errors, build failures, test failures):
- [Troubleshooting Galasa](./references/troubleshooting-galasa.md) - Load only when errors occur

### Critical Rules

1. **Never load all files at once** - This wastes tokens
2. **Start with quick reference** - Covers 80% of common tasks
3. **Load terminal reference last** - Only when explicitly needed (200+ lines)
4. **Avoid redundant loading** - If quick reference answers the question, stop there
5. **Load conceptual docs only when asked** - Architecture, ecosystem, best practices

## Skill References

### Quick Start
- [Quick Reference](./references/galasa-quick-reference.md) - **START HERE** - Common commands, patterns, configurations

### Understanding Galasa
- [Galasa overview](./references/galasa-overview.md) - What Galasa is, key features, and use cases
- [Galasa architecture](./references/galasa-architecture.md) - Framework components, managers, and test runner
- [Galasa ecosystem](./references/galasa-ecosystem.md) - Running tests at scale, stores, and services

### Working with Galasa
- [Galasa CLI tool](./references/galasa-cli-tool.md) - CLI command details and project structure
- [Writing Galasa tests](./references/writing-galasa-tests.md) - Test structure, build commands, environment configuration
- [Test development best practices](./references/test-development-best-practices.md) - Design principles for scalable, maintainable tests
- [Using Galasa managers](./references/using-galasa-managers.md) - Manager dependencies, injection patterns, basic terminal usage
- [Galasa auth and secrets](./references/galasa-auth-and-secrets.md) - Authentication, tokens, and secrets management
- [Galasa ecosystem management](./references/galasa-ecosystem-management.md) - Remote run submission, run management, CPS properties, resources, streams

### Advanced Topics
- [Terminal Interaction Reference](./references/terminal-interaction-reference.md) - **Load only when needed** - Detailed terminal timing and navigation

### Troubleshooting
- [Troubleshooting Galasa](./references/troubleshooting-galasa.md) - Error resolution guidance
