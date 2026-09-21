# Agent Guidelines for Galasa

## Repository Structure & Modules
This repository is a multi-module monorepo powering the Galasa test automation framework:
- **`modules/framework/`**: Core Java framework OSGi bundles (Gradle build).
- **`modules/managers/`**: Official Galasa Managers (e.g. z/OS, Core, HTTP, etc.) built as OSGi bundles (Gradle build).
- **`modules/cli/`**: `galasactl` command-line tool implemented in Go.
- **`modules/buildutils/`**: Build utilities, helper tools, and container images (Go & Docker).
- **`modules/extensions/`**: Galasa extensions and plugins (Gradle build).
- **`modules/gradle/` & `modules/maven/`**: Build plugin integrations for Galasa tests.
- **`modules/platform/`, `modules/obr/`, `modules/wrapping/`**: OSGi bundles, packaging, and dependency resolution.
- **`modules/ivts/`**: Integration and verification test suites.
- **`developer-docs/`**: Architecture diagrams, test lifecycles, and contributor guides.

## Build and Test Workflows
- **Targeted module testing**: Prefer running narrow, module-specific test suites before full monolithic builds:
  - Java/Gradle modules: `gradle test` within the module directory or targeting specific tasks (use system `gradle` CLI, do not use `gradlew`).
  - Java/Maven modules: `mvn test` in the target module directory.
  - CLI (Go): `go test ./...` in `modules/cli`.
- **Full local build**: Use `tools/build-locally.sh` for complete builds across all modules.

## Commit & PR Rules
- **Developer Certificate of Origin (DCO)**: All commits must include a sign-off (`git commit -s`).
- **GPG Signing**: Commits should be GPG-signed where feasible (`git commit -S`).
- **Conventional Commits**: Commit messages should follow `type(scope): description` (e.g., `feat(cli): add command`, `fix(framework): resolve lifecycle issue`, `docs: update guides`).

## Agent Constraints & Safety
- Do not edit generated OpenWiki pages in `openwiki/` directly; let the scheduled OpenWiki workflow regenerate them.
- Reference `.agents/skills/galasa-user/` for domain knowledge on Galasa concepts and CLI usage.

<!-- OPENWIKI:START -->

## OpenWiki

This repository has a generated `openwiki/` evidence index. It is optional just-in-time context, not required startup reading.

- Treat source code and tests as authoritative. A brief's unknowns and review items are verification gaps, not automatic requirements.
- Prefer the narrowest quiet validation that proves the changed behavior. Preserve complete failure output.

The scheduled OpenWiki GitHub Actions workflow refreshes the repository wiki. Do not hand-edit generated OpenWiki pages unless explicitly asked; prefer updating source code/docs and letting OpenWiki regenerate.

<!-- OPENWIKI:END -->
