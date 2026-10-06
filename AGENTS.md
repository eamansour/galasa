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

<!-- OPENWIKI:START -->

## OpenWiki

This repository has a generated `openwiki/` evidence index. It is optional just-in-time context, not required startup reading.

- Do not enumerate, preload, or search wikis at task start. Use retrieval when the user asks for it, when unfamiliar architecture or dependency behavior materially affects the task, or when source inspection leaves an important uncertainty. Stop once the question is grounded.
- When those conditions apply and OpenWiki retrieval tools are available, use `openwiki_search` for just-in-time context and `openwiki_read` for the relevant complete sections. If search returns `workspace_required`, ask which listed workspace to use and retry with its ID.
- Use `openwiki_list_workspaces` or `openwiki_list_wikis` when workspace membership itself needs to be discovered.
- If the retrieval tools are unavailable, read `openwiki/quickstart.md` and follow its links to the relevant pages.
- Treat source code and tests as authoritative. A brief's unknowns and review items are verification gaps, not automatic requirements.
- Prefer the narrowest quiet validation that proves the changed behavior. Preserve complete failure output.

The scheduled OpenWiki GitHub Actions workflow refreshes the repository wiki. Do not hand-edit generated OpenWiki pages unless explicitly asked; prefer updating source code/docs and letting OpenWiki regenerate.

<!-- OPENWIKI:END -->
