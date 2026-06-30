# OSGi Dependency Management Improvements Plan

## Top-Level Overview

Galasa uses OSGi bundles built with the Apache Felix Maven Bundle Plugin (for Maven modules)
and `biz.aQute.bnd.builder` (for Gradle modules). A number of bundles manually list every
transitive JAR filename inside `-includeresource` blocks in their `bnd.bnd` files, and
several wrapping bundles manually enumerate every transitive dependency in their `pom.xml`
files. When a dependency version changes the JAR filename changes too, so the `-includeresource`
pattern no longer matches. When a dependency's transitive closure changes, the manually
declared dependency list is now incorrect. Both scenarios cause OSGi wiring errors.

The fix is consistent and mechanical: wherever a bundle already uses `Embed-Dependency`
and `Embed-Transitive: true`, the `-includeresource` block is redundant — `Embed-Dependency`
already copies the JARs into the bundle and `Embed-Transitive: true` already walks the
full transitive closure. Removing the `-includeresource` block lets the tooling manage
the embedded JAR list automatically. For wrapping bundles, the manually declared transitive
`<dependency>` entries exist only because Maven will not download a JAR unless it appears
in the declared dependencies; the `Embed-Dependency: *;scope=compile|runtime` directive then
embeds everything. The correct fix is to use `Embed-Transitive: true` so the plugin
resolves and embeds transitives automatically without requiring manual declaration.

**Design decisions confirmed:**
- `io.kubernetes:client-java-api` and `client-java-proto` in the Kubernetes wrapper are
  transitives of `client-java` and will be removed — `Embed-Transitive` finds them.
- All bundles use `scope=compile|runtime` to ensure runtime-only transitive dependencies
  are correctly embedded. The etcd bundle's existing `scope=compile|runtime` is correct
  and should be kept as-is; all other bundles are updated to match.
- Documentation goes into a new `developer-docs/osgi-bundle-conventions.md` covering
  both Maven wrapping bundles and Gradle embedded-dependency bundles.

**Scope:** All bundles in `modules/wrapping/`, plus all Gradle-based bundles that use
`-includeresource` to manually list embedded JARs:
- `dev.galasa.framework.docker.controller`
- `dev.galasa.cps.etcd`
- `dev.galasa.selenium.manager`
- `dev.galasa.mq.manager`
- `dev.galasa.mq.manager.ivt`
- `dev.galasa.db2.manager`
- `dev.galasa.creds.os`
- `dev.galasa.framework.maven.repository`

**Non-goals:**
- Changes to `Import-Package` or `Export-Package` declarations (functional, not maintenance)
- Changes to the OBR or BOM structure
- Upgrading any dependency version as part of this work
- Any refactoring of bundle splitting or consolidation

---

## Sub-Task 1 — Remove `-includeresource` from Gradle bundles that already use `Embed-Dependency`

**Status:** [x] done

### Intent
Six Gradle-based bnd.bnd files use both `Embed-Dependency: *;scope=compile|runtime`
**and** an explicit `-includeresource` block that manually lists every JAR to embed. The
`Embed-Dependency` directive already instructs bnd to copy matching JARs into the bundle's
internal `lib/` directory. The `-includeresource` block is therefore redundant and fragile:
it must be kept perfectly in sync with the resolved dependency set, including exact versioned
filenames for pinned artifacts. Removing `-includeresource` eliminates the manual list entirely
and lets `Embed-Dependency` do its job automatically.

### Expected Outcomes
- `bnd.bnd` files no longer contain any `-includeresource` block
- The bundles still embed all required JARs (verified by inspecting the built bundle JAR contents)
- No change to exported or imported packages
- A version bump to any embedded dependency no longer requires a bnd.bnd edit

### Todo List
1. In `modules/framework/galasa-parent/dev.galasa.framework.docker.controller/bnd.bnd`:
   remove the entire `-includeresource:` block (lines 15–31).
2. In `modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/bnd.bnd`:
   remove the entire `-includeresource:` block (lines 16–58).
3. In `modules/managers/galasa-managers-parent/galasa-managers-testingtools-parent/dev.galasa.selenium.manager/bnd.bnd`:
   remove the entire `-includeresource:` block (lines 48–89).
4. In `modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.mq.manager/bnd.bnd`:
   remove the `-includeresource:` block (lines 19–21).
5. In `modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.mq.manager.ivt/bnd.bnd`:
   remove the `-includeresource:` line (line 5).
6. In `modules/managers/galasa-managers-parent/galasa-managers-database-parent/dev.galasa.db2.manager/bnd.bnd`:
   remove the `-includeresource:` block (line 34).

### Relevant Context
- Files modified: the six `bnd.bnd` files listed above.
- `Embed-Dependency: *;scope=compile|runtime` with `Embed-Transitive: true` is the correct
  bnd approach for embedding all dependencies and their full transitive closure, including
  runtime-only transitives. The `-includeresource` block is a lower-level override that was
  originally added to work around bnd issues but is now causing the maintenance burden.
- Any existing `Embed-Dependency: *;scope=compile` lines should be updated to
  `*;scope=compile|runtime` at the same time.
- The `-fixupmessages` lines in these files can remain — they suppress a cosmetic bnd warning
  about class file locations inside embedded JARs and are still appropriate.

---

## Sub-Task 2 — Remove `-includeresource` from Gradle bundles that do NOT use `Embed-Dependency`

**Status:** [x] done

### Intent
Two Gradle-based bnd.bnd files use `-includeresource` to embed JARs but do **not** declare
`Embed-Dependency`. These bundles need `Embed-Dependency: *;scope=compile|runtime` and
`Embed-Transitive: true` added so the tooling takes ownership of the JAR list.

- `dev.galasa.creds.os` — embeds `jna-platform-*.jar` and `jna-*.jar`
- `dev.galasa.framework.maven.repository` — embeds `maven-repository-metadata-*.jar`,
  `plexus-utils-*.jar`, `plexus-xml-*.jar`

### Expected Outcomes
- Both bundles declare `Embed-Dependency: *;scope=compile|runtime` and `Embed-Transitive: true`
- Both bundles no longer contain `-includeresource` blocks
- The JARs that were manually listed are still embedded (they are already declared as
  `implementation` dependencies in the corresponding `build.gradle`)

### Todo List
1. In `modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/bnd.bnd`:
   add `Embed-Transitive: true` and `Embed-Dependency: *;scope=compile|runtime` after the
   `Import-Package` block; remove the `-includeresource` block.
2. In `modules/framework/galasa-parent/dev.galasa.framework.maven.repository/bnd.bnd`:
   add `Embed-Transitive: true` and `Embed-Dependency: *;scope=compile|runtime` after the
   `Import-Package` line; remove the `-includeresource` block.
3. Verify that the corresponding `build.gradle` files for both bundles already list the
   previously embedded JARs as `implementation` dependencies (so Maven/Gradle still
   downloads them). If any are missing, add them.

### Relevant Context
- `modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/bnd.bnd`
- `modules/framework/galasa-parent/dev.galasa.framework.maven.repository/bnd.bnd`
- The corresponding `build.gradle` files for both modules.

---

## Sub-Task 3 — Add `Embed-Transitive: true` to wrapping bundles (`modules/wrapping/`)

**Status:** [x] done

### Intent
All ten wrapping bundles in `modules/wrapping/` use Maven's Apache Felix Bundle Plugin with
`<Embed-Dependency>*;scope=compile|runtime</Embed-Dependency>`. None of them declared
`<Embed-Transitive>true</Embed-Transitive>`. This means Maven only embeds the JARs that are
directly declared as `<dependency>` entries — transitives of those JARs are **not** embedded
unless they are also explicitly listed. This is why the wrapping `pom.xml` files had grown
to list every transitive dependency manually (e.g., the Kubernetes wrapper at 156+ lines,
the gRPC wrapper at 171 lines).

Adding `<Embed-Transitive>true</Embed-Transitive>` to each wrapping bundle's plugin
configuration tells the Felix Bundle Plugin to walk the full transitive dependency tree and
embed all JARs, so only the true direct dependencies need to be declared in `<dependencies>`.

### Expected Outcomes
- Each wrapping bundle's `pom.xml` contains `<Embed-Transitive>true</Embed-Transitive>` in
  its bundle plugin `<instructions>` block.
- The manually declared transitive `<dependency>` entries can then be removed from the
  `pom.xml` (covered in Sub-Task 4).
- A version bump to any direct dependency automatically pulls in the correct transitive JARs.

### Todo List
1. In `modules/wrapping/dev.galasa.wrapping.io.kubernetes.client-java/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
2. In `modules/wrapping/dev.galasa.wrapping.io.grpc.java/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
3. In `modules/wrapping/dev.galasa.wrapping.kafka.clients/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
4. In `modules/wrapping/dev.galasa.wrapping.httpclient-osgi/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
5. In `modules/wrapping/dev.galasa.wrapping.httpclient5/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
6. In `modules/wrapping/dev.galasa.wrapping.com.auth0.jwt/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
7. In `modules/wrapping/dev.galasa.wrapping.gson/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
8. In `modules/wrapping/dev.galasa.wrapping.jta/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
9. In `modules/wrapping/dev.galasa.wrapping.protobuf-java/pom.xml`:
   add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.
10. In `modules/wrapping/dev.galasa.wrapping.velocity-engine-core/pom.xml`:
    add `<Embed-Transitive>true</Embed-Transitive>` inside `<instructions>`.

### Relevant Context
- All ten `pom.xml` files under `modules/wrapping/`.
- The parent POM `modules/wrapping/pom.xml` does NOT declare `Embed-Transitive` centrally
  because different bundles may need different settings. Add it per-module.
- Apache Felix Maven Bundle Plugin 5.1.1 (in use) fully supports `Embed-Transitive`.

---

## Sub-Task 4 — Remove manually declared transitive dependencies from wrapping `pom.xml` files

**Status:** [x] done

### Intent
Once `Embed-Transitive: true` is active (Sub-Task 3), the Felix Bundle Plugin resolves and
embeds all transitives automatically. The manually declared transitive `<dependency>` entries
that were added solely to force Maven to download those JARs are no longer needed. Removing
them makes each `pom.xml` declare only true direct dependencies, which is what automated
tools like Dependabot/Renovate understand and manage correctly.

Security overrides (version pins, exclusions) that override a vulnerable transitive are a
special case: these must be **kept** because they represent intentional version pinning for
CVE mitigation. They should be documented with a comment explaining why.

### Expected Outcomes
- `modules/wrapping/dev.galasa.wrapping.io.kubernetes.client-java/pom.xml` reduced from
  156+ dependency lines to ~15 direct dependencies (plus retained security overrides).
- `modules/wrapping/dev.galasa.wrapping.io.grpc.java/pom.xml` reduced from ~18 declared
  dependencies to the core gRPC artifacts only.
- All other wrapping `pom.xml` files contain only direct dependencies.
- Security override entries (e.g., netty-codec CVE fix in kubernetes wrapper,
  jackson-core CVE fix in kafka wrapper, commons-logging exclusion in httpclient-osgi,
  slf4j-api upgrade in velocity wrapper) are retained with explanatory comments.
- The comment block in `dev.galasa.wrapping.io.grpc.java/pom.xml` ("This dependency is
  relied upon by io.grpc:grpc-protobuf but we need to depend on it directly to avoid OSGi
  issues") becomes obsolete once `Embed-Transitive` is active; remove it along with the
  redundant direct `protobuf-java` dependency.

### Todo List

**dev.galasa.wrapping.io.kubernetes.client-java/pom.xml:**
1. Keep: `io.kubernetes:client-java` (direct)
2. Keep: `org.bouncycastle:bcpkix-jdk18on` (direct)
3. Keep: `dev.galasa:dev.galasa.wrapping.protobuf-java` (direct Galasa wrapping dep)
4. Keep: `dev.galasa:dev.galasa.wrapping.gson` (direct Galasa wrapping dep)
5. Keep: `io.netty:netty-codec` **with** the CVE override comment (security pin)
6. Remove: `io.kubernetes:client-java-api` and `client-java-proto` (confirmed: transitives,
   `Embed-Transitive` will resolve them)
7. Remove: all other `<!-- Transitive dependencies -->` entries (okhttp, okio, jackson,
   snakeyaml, swagger, jose4j, bouncycastle extras, kotlin-osgi-bundle, gsonfire,
   commons-*, javax/jakarta annotation, jsr305, jakarta.ws.rs)

**dev.galasa.wrapping.io.grpc.java/pom.xml:**
9. Keep: `io.grpc:grpc-api`, `grpc-context`, `grpc-core`, `grpc-netty-shaded`,
   `grpc-protobuf`, `grpc-protobuf-lite`, `grpc-stub`, `grpc-util` (direct gRPC deps)
10. Remove: `com.google.protobuf:protobuf-java` (transitive of grpc-protobuf; no longer
    needs to be declared directly once `Embed-Transitive` is active)
11. Remove: `com.google.protobuf:protobuf-javalite` (same reasoning)
12. Remove: `io.perfmark:perfmark-api`, `com.google.android:annotations`,
    `com.google.api.grpc:proto-google-common-protos`, `com.google.code.findbugs:jsr305`,
    `com.google.errorprone:error_prone_annotations`, `com.google.guava:guava`,
    `com.google.guava:failureaccess`, `com.google.guava:listenablefuture`,
    `com.google.code.gson:gson`, `com.google.j2objc:j2objc-annotations`,
    `org.checkerframework:checker-qual`, `org.codehaus.mojo:animal-sniffer-annotations`
    (all transitives of the gRPC artifacts)

**dev.galasa.wrapping.kafka.clients/pom.xml:**
13. Keep: `org.apache.kafka:kafka-clients`, `org.apache.kafka:kafka-server-common` (direct)
14. Keep: `com.fasterxml.jackson.core:jackson-core` **with** CVE comment (security pin)
15. Remove: `org.slf4j:slf4j-api:1.7.36` (transitive; Embed-Transitive will resolve it)

**dev.galasa.wrapping.httpclient-osgi/pom.xml:**
16. Keep: `org.apache.httpcomponents:httpclient` with the `<exclusion>` for commons-logging
    (the exclusion is a security measure, not just a transitive workaround)
17. Keep: explicit `commons-logging:commons-logging` version override with its CVE comment
18. Remove any other transitively-pulled entries that are now handled automatically.

**dev.galasa.wrapping.velocity-engine-core/pom.xml:**
19. Keep: `org.apache.velocity:velocity-engine-core` with the `<exclusion>` for old slf4j
20. Keep: explicit `org.slf4j:slf4j-api:2.0.16` version upgrade with its comment
21. Remove any other transitively-pulled entries now handled automatically.

**dev.galasa.wrapping.com.auth0.jwt/pom.xml:**
22. Keep: `com.auth0:java-jwt` (direct)
23. Remove: Jackson dependencies (`jackson-databind`, `jackson-annotations`, `jackson-core`)
    — these are transitives of java-jwt and will be embedded automatically.

**dev.galasa.wrapping.gson/pom.xml:**
24. Keep: `com.google.code.gson:gson` (direct)
25. Remove: `com.google.errorprone:error_prone_annotations` (transitive)

**dev.galasa.wrapping.httpclient5/pom.xml, protobuf-java/pom.xml, jta/pom.xml:**
26. These are already minimal (2-3 direct deps each) — no transitive removals needed;
    just add `Embed-Transitive` from Sub-Task 3 and leave dependency declarations as-is.

### Relevant Context
- All wrapping `pom.xml` files under `modules/wrapping/`.
- Security overrides in: kubernetes wrapper (netty-codec), kafka wrapper (jackson-core),
  httpclient-osgi (commons-logging exclusion), velocity wrapper (slf4j exclusion).
- The CVE comments in the affected files are the source of truth for which pins are intentional.

---

## Sub-Task 5 — Remove manually declared transitive `implementation` deps from Gradle build files

**Status:** [x] done

### Intent
The Gradle-based bundles that use `Embed-Dependency: *;scope=compile|runtime` with
`Embed-Transitive: true` in their `bnd.bnd` files (after Sub-Tasks 1 and 2) still declare
all transitive JARs explicitly as `implementation` dependencies in their `build.gradle`
files. These were added solely so Gradle downloads the JARs to the local cache for bnd
to embed. With `Embed-Transitive: true`, the tooling resolves and downloads transitives
automatically, so these explicit `implementation` declarations are no longer required.

The same security-override exception applies: any dependency added specifically to pin a
version for CVE mitigation must be retained.

Once the `implementation` entries are removed from the `build.gradle` files, the
corresponding version constraints in `modules/platform/dev.galasa.platform/build.gradle`
that existed solely to govern those transitive-forcing entries must also be removed.
A codebase audit identified **42 platform constraints** that are referenced only by the
transitive-forcing `implementation` entries being removed, with no other usage anywhere
in the build. The one exception is `io.netty:netty-codec`, which must be retained in the
platform as it is the subject of an explicit CVE security override in the Kubernetes wrapper.

### Expected Outcomes
- `dev.galasa.cps.etcd/build.gradle` reduced from 38+ explicit `implementation` entries
  to 2 (jetcd-core and gson — the only true direct deps).
- `dev.galasa.selenium.manager/build.gradle` reduced to direct Selenium + project deps only.
- `dev.galasa.framework.docker.controller/build.gradle` reduced to core Docker Java deps only.
- `dev.galasa.mq.manager/build.gradle` already minimal — no transitive removals needed.
- `dev.galasa.db2.manager/build.gradle` already minimal — no transitive removals needed.
- In all cases, comments explaining "required to force download" are removed along with
  the entries they describe.
- `modules/platform/dev.galasa.platform/build.gradle` has 42 constraints removed; the
  platform retains `io.netty:netty-codec` and all entries with genuine independent usage.

### Todo List

**dev.galasa.cps.etcd/build.gradle:**
1. Keep: `io.etcd:jetcd-core`, `com.google.code.gson:gson` (only true direct deps)
2. Remove: all other `implementation(...)` entries (grpc-*, netty-*, protobuf-*, guava-*,
   failsafe, jetcd-api/common/grpc, jackson-core, vertx-*, checker-qual, slf4j-api,
   javax.annotation-api, validation-api, perfmark-api, listenablefuture, failureaccess,
   proto-google-common-protos, protobuf-java-util)
3. Remove the explanatory comment block about "Not required for compile, but required to
   force the download of the jars to embed by bnd".

**dev.galasa.selenium.manager/build.gradle:**
4. Keep: `selenium-java`, `selenium-api`, `selenium-support` (direct), the four `api`-scoped
   Selenium driver entries, `commons-io:commons-io`, `com.google.code.gson:gson`, and the
   project-level `implementation project(...)` entries.
5. Remove: all entries under the "Transitive dependencies not required for compilation"
   comment block (auto-service-annotations, jspecify, selenium-manager, selenium-json,
   selenium-os, commons-exec, guava, failureaccess, listenablefuture, jsr305, checker-qual,
   error_prone_annotations, j2objc-annotations, opentelemetry-*, byte-buddy,
   selenium-http, failsafe, selenium-chromium-driver, selenium-devtools-v*)
6. Remove the "Transitive dependencies not required for compilation" comment block.

**dev.galasa.framework.docker.controller/build.gradle:**
7. Keep: `docker-java-core`, `docker-java-transport-httpclient5`, `commons-codec`,
   `commons-io`, `io.prometheus:simpleclient*`, `commons-compress`,
   `bcpkix-jdk18on`, `bcprov-jdk18on` (all direct)
8. Keep: `com.fasterxml.jackson.core:jackson-core` **with** its CVE comment (security pin)
9. No removals needed — this build.gradle is already fairly lean; the manual listing
   burden was in the `bnd.bnd` file addressed in Sub-Task 1.

**modules/platform/dev.galasa.platform/build.gradle:**
10. Remove the following 42 constraints (verified by codebase audit to have no usage
    outside the transitive-forcing `implementation` entries being removed above):

    *Netty (12 — all except `netty-codec` which is a CVE pin):*
    `netty-buffer`, `netty-codec-dns`, `netty-codec-http`, `netty-codec-http2`,
    `netty-codec-socks`, `netty-common`, `netty-handler`, `netty-handler-proxy`,
    `netty-resolver`, `netty-resolver-dns`, `netty-transport`,
    `netty-transport-native-unix-common`

    *OpenTelemetry (12):*
    `opentelemetry-api`, `opentelemetry-api-incubator`, `opentelemetry-context`,
    `opentelemetry-exporter-logging`, `opentelemetry-sdk`, `opentelemetry-sdk-common`,
    `opentelemetry-sdk-extension-autoconfigure-spi`, `opentelemetry-sdk-extension-autoconfigure`,
    `opentelemetry-sdk-trace`, `opentelemetry-sdk-metrics`, `opentelemetry-sdk-logs`,
    `opentelemetry-semconv`

    *Selenium transitive support (9):*
    `selenium-chromium-driver`, `selenium-http`, `selenium-json`, `selenium-manager`,
    `selenium-os`, `selenium-devtools-v85`, `selenium-devtools-v129`,
    `selenium-devtools-v130`, `selenium-devtools-v131`

    *Google/annotation-only (2):*
    `com.google.android:annotations`, `org.codehaus.mojo:animal-sniffer-annotations`

    *etcd transitive support (7):*
    `io.etcd:jetcd-api`, `io.etcd:jetcd-common`, `io.etcd:jetcd-grpc`,
    `io.vertx:vertx-core`, `io.vertx:vertx-grpc`,
    `com.google.protobuf:protobuf-java-util`,
    `javax.annotation:javax.annotation-api`

    *BouncyCastle / Jakarta (2):*
    `org.bouncycastle:bcutil-jdk18on`, `jakarta.annotation:jakarta.annotation-api`

11. Keep: `io.netty:netty-codec` (CVE pin — overrides the version pulled by the Kubernetes
    wrapper's `client-java` transitive dep).
12. Keep: all other platform entries that have genuine independent usage elsewhere in
    the codebase (verified by audit).

### Relevant Context
- `modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/build.gradle`
- `modules/managers/galasa-managers-parent/galasa-managers-testingtools-parent/dev.galasa.selenium.manager/build.gradle`
- `modules/framework/galasa-parent/dev.galasa.framework.docker.controller/build.gradle`
- `modules/platform/dev.galasa.platform/build.gradle`
- Audit findings: all 42 platform entries listed above were confirmed to appear only in
  the `build.gradle` files being cleaned up, with no other references in the codebase.

---

## Sub-Task 6 — Update developer documentation

**Status:** [x] done

### Intent
The existing `modules/wrapping/README.md` and `modules/obr/README.md` contain no guidance
on the dependency management conventions maintainers must follow. This gap means the next
developer to add a new wrapping bundle or a new embedded dependency will copy an old pattern
(manually listing transitives) rather than the correct one. A concise conventions section
prevents regression.

### Expected Outcomes
- A new `developer-docs/osgi-bundle-conventions.md` exists and covers:
  - Maven wrapping bundles: `Embed-Transitive: true` required, only direct deps declared,
    security override exception with CVE comment, no `-includeresource`.
  - Gradle embedded-dependency bundles: `Embed-Dependency: *;scope=compile|runtime` +
    `Embed-Transitive: true` in `bnd.bnd`, no `-includeresource`, security override
    exception, comments explaining any intentional version pins.
- `modules/wrapping/README.md` contains a brief "Dependency management" section that
  references `developer-docs/osgi-bundle-conventions.md`.

### Todo List
1. Create `developer-docs/osgi-bundle-conventions.md` covering both Maven wrapping bundles
   and Gradle embedded-dependency bundles, as described in Expected Outcomes above.
2. Add a brief "Dependency management" section to `modules/wrapping/README.md` that
   references `developer-docs/osgi-bundle-conventions.md`.

### Relevant Context
- `modules/wrapping/README.md` (currently 37 lines, no dependency guidance)
- `modules/obr/README.md` (9 lines, no dependency guidance)
- `developer-docs/` directory for existing documentation style reference

---

## Sub-Task 7 — Replace `-includeresource: *.jar;lib:=true` with `-conditionalpackage`

**Status:** [x] done

### Intent

After Sub-Tasks 1–2, all eight affected `bnd.bnd` files were reduced to a single line:
`-includeresource: *.jar;lib:=true`. This glob embeds **every JAR** on the BND classpath
into the bundle as raw library files. It is still fragile in the same way the original
lists were: it embeds unreferenced packages and is sensitive to classpath composition.

`-conditionalpackage` is the correct BND directive for this pattern. BND analyses the
compiled bytecode of the bundle's own source classes, determines which packages from the
classpath are *actually referenced*, and includes only those classes directly into the
bundle's own class namespace (not as `lib/*.jar` files). The result is a leaner bundle
containing only what the code actually uses, with no JAR-level file sensitivity.

For `-conditionalpackage` to work, the embedded dependency JARs must still be on BND's
classpath. This is already satisfied: the `jar { bundle { classpath = configurations.embedImplementation } }`
block in each module's `build.gradle` puts those JARs on the BND classpath. That wiring
does **not** change.

**`dev.galasa.mq.manager.ivt` is a special case:** its `bnd.bnd` still uses the old
`Embed-Dependency: *;scope=compile` + `-includeresource` pattern (not `embedImplementation`).
Its source only uses `javax.jms.*`, which is already an `Import-Package`. There is nothing
from the classpath to embed. The `Embed-Dependency`, `Embed-Transitive`, and
`-includeresource` lines should all be removed, and the `javax.jms-api` dependency in
`build.gradle` changed from `implementation` to a regular dependency (it does not need to
be embedded).

### Expected Outcomes

- All eight `bnd.bnd` files replace `-includeresource: *.jar;lib:=true` with a
  `-conditionalpackage` directive listing the third-party package namespaces their source
  code actually references.
- `dev.galasa.mq.manager.ivt/bnd.bnd` has `Embed-Dependency`, `Embed-Transitive`, and
  `-includeresource` removed entirely — no embedding needed.
- No changes to `build.gradle` files (the `embedImplementation` configuration and
  `jar { bundle { classpath } }` block remain unchanged — they put JARs on the classpath
  for BND to analyse).
- Bundles no longer contain a `lib/` directory of raw JARs; instead, only the referenced
  classes are inlined directly into the bundle's class namespace.
- The `-fixupmessages "Classes found in the wrong directory"` suppressions become obsolete
  once `lib/*.jar` embedding is gone and can be removed at the same time.

### Todo List

**`modules/framework/galasa-parent/dev.galasa.framework.docker.controller/bnd.bnd`:**
1. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: com.github.dockerjava.*, io.prometheus.client.*`
2. Remove the `-fixupmessages` line.

**`modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/bnd.bnd`:**
3. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: io.etcd.jetcd.*, io.vertx.core.*`
4. Remove the `-fixupmessages` line.

**`modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/bnd.bnd`:**
5. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: com.sun.jna.*`
   (Note: `com.sun.jna` is the JNA library package — it is embedded from
   `net.java.dev.jna:jna`, not the JDK `com.sun.*` namespace.)

**`modules/managers/galasa-managers-parent/galasa-managers-testingtools-parent/dev.galasa.selenium.manager/bnd.bnd`:**
6. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: org.openqa.selenium.*`
7. Remove the `-fixupmessages` line.

**`modules/framework/galasa-parent/dev.galasa.framework.maven.repository/bnd.bnd`:**
8. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: org.apache.maven.artifact.*, org.codehaus.plexus.*`

**`modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.mq.manager/bnd.bnd`:**
9. Replace `-includeresource: *.jar;lib:=true` with:
   `-conditionalpackage: com.ibm.msg.client.*, com.ibm.mq.*`
   (The `com.ibm.mq:com.ibm.mq.allclient` JAR contains both `com.ibm.msg.client.*` and
   `com.ibm.mq.*` namespaces; both must be covered.)

**`modules/managers/galasa-managers-parent/galasa-managers-database-parent/dev.galasa.db2.manager/bnd.bnd`:**
10. Replace `-includeresource: *.jar;lib:=true` with:
    `-conditionalpackage: com.ibm.db2.*`
    (The DB2 JDBC driver is loaded via `DriverManager` — no direct import — but the driver
    classes must be embedded for OSGi class-loader visibility. `-conditionalpackage` includes
    referenced packages; since the source does not directly import `com.ibm.db2.*`, also
    add `com.ibm.db2.*` to the `Import-Package` block with `resolution:=optional` so BND
    knows to retain those classes.)

**`modules/managers/galasa-managers-parent/galasa-managers-comms-parent/dev.galasa.mq.manager.ivt/bnd.bnd`:**
11. Remove `Embed-Transitive: true`, `Embed-Dependency: *;scope=compile`, and
    `-includeresource: javax.jms-api-*.jar; lib:=true` entirely. The `javax.jms.*` API
    is already resolved via OSGi wiring from `dev.galasa.mq.manager` (which embeds it).
    No embedding is needed in the IVT bundle.

### Relevant Context
- All eight `bnd.bnd` files listed above.
- Third-party imports confirmed by source code inspection of `src/main/java` for each module.
- The `embedImplementation` configuration and `jar { bundle { classpath } }` blocks in the
  corresponding `build.gradle` files are unchanged — they remain necessary to put the JARs
  on BND's classpath so `-conditionalpackage` can analyse and pull from them.
- `dev.galasa.creds.os`: `com.google.gson` is declared as `implementation` (not
  `embedImplementation`) — it is imported, not embedded. The `Import-Package` already
  includes `com.google.gson`. No change needed there.
- `dev.galasa.mq.manager.ivt`: `javax.jms-api` is declared as `implementation` in
  `build.gradle` (not `embedImplementation`), and there is no `jar { bundle { classpath } }`
  block — no changes to `build.gradle` are needed for this module.
- The `dev.galasa.db2.manager` note on step 10: if BND's analysis of the source bytecode
  finds no direct references to `com.ibm.db2.*`, `-conditionalpackage` alone will not
  include those classes. The alternative is to use `-includeresource` specifically for the
  db2 driver JAR only, or to declare `com.ibm.db2.*` in `Import-Package` with
  `resolution:=optional` to signal that it should be wired at runtime. Confirm the correct
  approach by inspecting whether the db2 driver is loaded via `Class.forName` or
  `DriverManager.getConnection` (service-loading), and adjust the bnd.bnd accordingly.
