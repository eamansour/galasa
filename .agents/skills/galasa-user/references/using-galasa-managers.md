---
name: using-galasa-managers
description: Provides information about how to add Galasa managers to a Galasa test project and best practices for commonly-used managers.
---

## Using Galasa Managers

- Galasa managers provide interfaces to work with various technologies. For example, the Galasa z/OS manager can be used to connect to z/OS systems and the Galasa Docker manager can be used to manipulate Docker containers.
- Refer to https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/index.md for the most up-to-date list of managers that Galasa offers.

Core managers provided by the Galasa framework:
- z/OS Managers: z/OS Batch, z/OS Console, z/OS File, z/OS Program, z/OS TSO, z/OS UNIX
- CICS TS Managers: CICS Terminal, CICS Region, CICS Resource
- HTTP Manager: HTTP client functionality
- Artifact Manager: Test artifact management
- Core Manager: Core Galasa functionality

### Injecting Managers into Test Classes

Managers are injected into test classes using annotations. Example:

```java
@ZosImage(imageTag = "PRIMARY")
public IZosImage zosImage;

@Zos3270Terminal(imageTag = "PRIMARY")
public ITerminal terminal;

// You can customize terminal size if needed (default is 24x80)
@Zos3270Terminal(imageTag = "PRIMARY", primaryColumns = 100, primaryRows = 30)
public ITerminal largeTerminal;

@HttpClient
public IHttpClient httpClient;

@CicsRegion(cicsTag = "PRIMARY")
public ICicsRegion cics;

@CicsTerminal(cicsTag = "PRIMARY")
public ICicsTerminal terminal;
```

The `imageTag` and `cicsTag` parameters reference CPS properties that define target systems.

### Terminal Interaction Basics

When using the `ITerminal` interface from the z/OS 3270 manager or the `ICicsTerminal` interface from the CICS TS manager:

**Critical Rules:**
- Always chain method calls: `terminal.type("X").enter().waitForKeyboard()`
- Always call `waitForKeyboard()` after `enter()`, `clear()`, or PF keys
- Wait for content with `waitForTextInField("expected text")` before proceeding
- Extract field values: `terminal.retrieveFieldTextAfterFieldWithString("Label").trim()`
- **Match exact capitalization** in field names and expected values

**Common Pattern:**
```java
terminal.type("X")
    .enter()
    .waitForKeyboard()
    .waitForTextInField("EXPECTED MESSAGE");  // Verify screen loaded

String value = terminal.retrieveFieldTextAfterFieldWithString("Field Name").trim();
assertThat(value).as("Value should match").isEqualTo("Expected Value");
```

**For detailed terminal interaction guidance** (timing, field navigation, complete examples, troubleshooting), see [Terminal Interaction Reference](terminal-interaction-reference.md). Load that file only when working with terminal screens or debugging timing issues.

### Adding a manager to your project

To add a manager to your project:
1. Check that the manager has not already been added to `build.gradle` or `pom.xml`
2. Add the manager dependency:
    - **Gradle**: `implementation 'dev.galasa:dev.galasa.{manager}.manager'`
    - **Maven**: `<artifactId>dev.galasa.{manager}.manager</artifactId>` with `<scope>provided</scope>`
3. Build the project (see [writing-galasa-tests.md](writing-galasa-tests.md#building-galasa-projects))

**Note**: Manager dependencies inherit version from the Galasa framework version in your build configuration.

**IMPORTANT**: The z/OS 3270 manager requires the z/OS manager to also be added as a dependency, so **ALWAYS** be sure to add both if the user wants to interact with z/OS 3270 terminals in their tests.
**IMPORTANT**: If a user wants to interact with a CICS region, you should use the CICS TS manager, which includes using the `@CicsRegion` and `@CicsTerminal` annotations, and the z/OS and z/OS 3270 managers. See the [CICS TS IVT](https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/modules/ivts/galasa-ivts-parent/dev.galasa.zos.ivts/dev.galasa.zos.ivts.cicsts/src/main/java/dev/galasa/zos/ivts/cicsts/CICSTSManagerIVT.java) for an example CICS test suite.

Examples of how Galasa managers are used in test classes can be found in the Galasa repository's `ivts` subproject at https://github.com/galasa-dev/galasa/tree/main/modules/ivts

---

## Manager Catalogue

A quick-reference catalogue of all Galasa managers, organised by category. Managers already detailed in the [Injecting Managers into Test Classes](#injecting-managers-into-test-classes) section above (z/OS, z/OS 3270, CICS TS, HTTP Client) are noted as "covered above" — see that section for injection examples.

### CICS TS Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| CECI Manager | CECI 3270 interaction — supports containers and link programs | `dev.galasa.cicsts.ceci.manager` | [Docs](https://galasa.dev/docs/managers/cics-ts-managers/cics-ts-ceci-manager/) |
| CEDA Manager | CEDA 3270 interaction | `dev.galasa.cicsts.ceda.manager` | [Docs](https://galasa.dev/docs/managers/cics-ts-managers/) |
| CEMT Manager | CEMT 3270 interaction | `dev.galasa.cicsts.cemt.manager` | [Docs](https://galasa.dev/docs/managers/cics-ts-managers/) |
| CICS TS Manager | Provides `@CicsRegion` / `@CicsTerminal` — see injection examples above | `dev.galasa.cicsts.manager` | Covered above |

### IMS TM Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| IMS TM Manager | Provides IMS TM functions to Galasa tests; drives terminal interaction via z/OS 3270 | `dev.galasa.imstm.manager` | [Docs](https://galasa.dev/docs/managers/ims-tm-managers/ims-tm-manager/) |

### Cloud Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| Docker Manager | Runs Docker containers on Galasa-managed Docker Engines; used as a base by other managers (e.g. JMeter) | `dev.galasa.docker.manager` | [Docs](https://galasa.dev/docs/managers/cloud-managers/docker-manager/) |
| Kubernetes Manager | Provisions a Kubernetes namespace with test-supplied YAML resources | `dev.galasa.kubernetes.manager` | [Docs](https://galasa.dev/docs/managers/cloud-managers/kubernetes-manager/) |
| OpenStack Manager | Provisions Linux images on an OpenStack instance; depends on Linux Manager | `dev.galasa.openstack.manager` | [Docs](https://galasa.dev/docs/managers/cloud-managers/open-stack-manager/) |

### Communications Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| HTTP Client Manager | HTTP/HTTPS client for REST and web service calls — see injection examples above | `dev.galasa.http.manager` | Covered above |
| IP Network Manager | TCP/IP address and port resolution from CPS; used as a base by Linux and Docker managers | `dev.galasa.ipnetwork.manager` | [Docs](https://galasa.dev/docs/managers/communications-managers/ipnetwork-manager/) |
| MQ Manager | Connects tests to existing IBM MQ queue managers; supports put/get/clear on queues | `dev.galasa.mq.manager` | [Docs](https://galasa.dev/docs/managers/communications-managers/mq-manager/) |

### Core Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| Artifact Manager | Loads files bundled alongside test code; provides symbolic substitution helpers | `dev.galasa.artifact.manager` | [Docs](https://galasa.dev/docs/managers/core-managers/artifact-manager/) |
| Core Manager | Always-active framework manager; provides `@Logger`, `@StoredArtifactRoot`, `@RunName`, `@ResourceString` | `dev.galasa.core.manager` | [Docs](https://galasa.dev/docs/managers/core-managers/core-manager/) |

### Logging Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| ElasticLog Manager | Exports test results to an Elasticsearch endpoint for Kibana dashboarding | `dev.galasa.elasticlog.manager` | [Docs](https://galasa.dev/docs/managers/logging-managers/elasticlog-manager/) |

### Ecosystem Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| Galasa Ecosystem Manager | Provisions a fully running Galasa Ecosystem for testing Galasa itself | `dev.galasa.galasaecosystem.manager` | [Docs](https://galasa.dev/docs/managers/ecosystem-managers/galasa-ecosystem-manager/) |

### Test Tool Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| JMeter Manager | Runs JMeter sessions inside Docker containers; depends on Docker Manager | `dev.galasa.jmeter.manager` | [Docs](https://galasa.dev/docs/managers/test-tool-managers/jmeter-manager/) |
| SDV Manager | Records CICS Security Definitions witnessed during role-based tests and produces a YAML report | `dev.galasa.sdv.manager` | [Docs](https://galasa.dev/docs/managers/test-tool-managers/sdv-manager/) |
| Selenium Manager | Drives Selenium WebDrivers (Gecko, Chrome, etc.) to automate browser interactions | `dev.galasa.selenium.manager` | [Docs](https://galasa.dev/docs/managers/test-tool-managers/selenium-manager/) |

### Unix Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| Linux Manager | Connects to a Linux image and provides a command shell; depends on IP Network Manager | `dev.galasa.linux.manager` | [Docs](https://galasa.dev/docs/managers/unix-managers/linux-manager/) |

### Workflow Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| GitHub Manager | Annotates tests with `@GitHubIssue` so known-failing tests are reported as "Failed with Defects" | `dev.galasa.githubissue.manager` | [Docs](https://galasa.dev/docs/managers/workflow-managers/github-manager/) |

### z/OS Managers

| Manager | Description | Gradle artifact ID | Docs |
|---|---|---|---|
| z/OS Manager | Core z/OS provisioning — `@ZosImage`; required by all other z/OS managers | `dev.galasa.zos.manager` | Covered above |
| z/OS 3270 Manager | 3270 terminal interaction — `@Zos3270Terminal`; requires z/OS Manager | `dev.galasa.zos3270.manager` | Covered above |
| RSE API Manager | Provides RSE API connectivity to z/OS images | `dev.galasa.zos.rseapi.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/rse-api-manager/) |
| z/OS Batch (zOS MF) | Submits and monitors JCL batch jobs via z/OSMF | `dev.galasa.zosbatch.zosmf.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-batch-zos-mf-manager/) |
| z/OS Batch (RSE API) | Submits and monitors JCL batch jobs via RSE API | `dev.galasa.zosbatch.rseapi.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-batch-rse-api-manager/) |
| z/OS Console (oeconsol) | Issues z/OS operator console commands via oeconsol | `dev.galasa.zosconsole.oeconsol.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-console-oeconsol-manager/) |
| z/OS Console (zOS MF) | Issues z/OS operator console commands via z/OSMF | `dev.galasa.zosconsole.zosmf.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-console-zos-mf-manager/) |
| z/OS File (RSE API) | Read, write, and manage z/OS data sets and UNIX files via RSE API | `dev.galasa.zosfile.rseapi.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-file-rse-api-manager/) |
| z/OS File (zOS MF) | Read, write, and manage z/OS data sets and UNIX files via z/OSMF | `dev.galasa.zosfile.zosmf.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-file-zos-mf-manager/) |
| z/OS MF Manager | Provides a z/OSMF session used by other zOS MF-backed managers | `dev.galasa.zosmf.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-mf-manager/) |
| z/OS Program Manager | Compiles and links z/OS programs as part of a test | `dev.galasa.zosprogram.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-program-manager/) |
| z/OS TSO (SSH) | Executes TSO commands on z/OS via SSH | `dev.galasa.zostsocommand.ssh.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-tso-command-ssh-manager/) |
| z/OS UNIX (SSH) | Executes z/OS UNIX shell commands via SSH | `dev.galasa.zosunixcommand.ssh.manager` | [Docs](https://galasa.dev/docs/managers/zos-managers/zos-unix-command-ssh-manager/) |

---

## Manager Dependency Relationships

Some managers depend on others being present at runtime. The most important relationships are listed here so the correct set of artifacts can be added to `build.gradle` / `pom.xml`.

```
z/OS Manager  ◄─────────────────────────── z/OS 3270 Manager
     ▲                                             ▲
     │                                             │
     └──────── CICS TS Manager ───────────────────┘
                    (terminal interaction path)

IP Network Manager  ◄──── Linux Manager  ◄──── OpenStack Manager
         ▲
         └──── Docker Manager ◄──── JMeter Manager

z/OS Manager  ◄──── IMS TM Manager ────► z/OS 3270 Manager
                    (drives IMS via 3270 terminals)

Kubernetes Manager  (standalone — provisions namespaces for test resources)
```

| Dependency rule | Details |
|---|---|
| **z/OS 3270 → z/OS Manager** | `dev.galasa.zos.manager` must be on the classpath whenever `dev.galasa.zos3270.manager` is used. |
| **CICS TS → z/OS Manager + z/OS 3270 Manager** | CICS TS terminal interaction (`@CicsTerminal`) requires both the z/OS Manager and the z/OS 3270 Manager. |
| **OpenStack → Linux Manager → IP Network Manager** | OpenStack provisions Linux images; Linux Manager then requires IP Network Manager to establish TCP/IP connectivity. |
| **Docker → IP Network Manager** | Docker Manager uses IP Network Manager to resolve container host/port. |
| **JMeter → Docker Manager** | JMeter Manager runs JMX scripts inside Docker containers; Docker Manager must be present. |
| **IMS TM → z/OS Manager + z/OS 3270 Manager** | IMS TM drives terminal sessions via the z/OS 3270 Manager, so both must be on the classpath. |
| **Kubernetes** | Self-contained — provisions Kubernetes namespaces independently; no mandatory manager dependencies. |
| **z/OS *MF-backed managers → z/OS MF Manager** | Managers suffixed `zosmf` (z/OS Batch, Console, File) require `dev.galasa.zosmf.manager` to provide the z/OSMF session. |
