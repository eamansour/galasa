---
name: troubleshooting-galasa
description: How to troubleshoot common issues with Galasa
---

## Troubleshooting Common Issues

### Build Failures
- Ensure all manager dependencies are correctly specified in `build.gradle` or `pom.xml`
- Check that Java version is compatible (Java 17 or later recommended)

### Test Execution Failures
- Verify CPS properties are correctly configured in `~/.galasa/cps.properties`
- Ensure credentials are properly set in `~/.galasa/credentials.properties`
- Check that the OBR coordinates match your project's group ID and version
- Review `run.log` in the RAS directory for detailed error messages

### Manager Import Errors
- Run the build command after adding new manager dependencies
- Verify the manager artifact ID matches the documentation
- For z/OS 3270 manager, ensure both z/OS and z/OS 3270 managers are added

## Quick Reference Examples

### Common Manager Combinations
- **z/OS Testing**: z/OS Manager + z/OS 3270 Manager
- **CICS Testing**: CICS Region Manager + z/OS Manager + z/OS 3270 Terminal Manager
- **Web Testing**: HTTP Manager + Selenium Manager

## CLI Error Codes Quick Reference

| Error code | Cause | Fix |
|---|---|---|
| `GAL1037E` | Invalid Java package name — contains a forbidden character | Check `--package`: only lowercase letters, numbers, and `.` separators are allowed |
| `GAL1038E` | Invalid Java package name — starts with a forbidden character | Package name cannot start with `.` or a digit |
| `GAL1039E` | Invalid Java package name — ends with a forbidden character | Package name cannot end with `.` |
| `GAL1040E` | Package name is blank | Supply a value to `--package` |
| `GAL1044E` | Package name contains a Java reserved keyword (e.g. `class`, `int`, `return`) | Rename the offending segment to avoid reserved words |
| `GAL1045E` | Feature name cannot be used as a Java package name | Check the value passed to `--features` |
| `GAL1050E` | `JAVA_HOME` environment variable is not set | Set `JAVA_HOME` to point to a Java 17 JDK (see section below) |
| `GAL1060E`–`GAL1063E` | Malformed OBR parameter | Format must be `mvn:groupId/artifactId/version/obr` |
| `GAL1064E`–`GAL1066E` | Malformed `--class` parameter | Format must be `osgi-bundle-id/fully.qualified.ClassName` — no `.class` suffix |
| `GAL1089E` | No build system selected for `project create` | Add `--gradle` and/or `--maven` to the `galasactl project create` command |
| `GAL1094E` | No OBR supplied for a local run | Add `--obr mvn:groupId/artifactId/version/obr` to `galasactl runs submit local` |

For the full list of CLI error codes, see https://galasa.dev/docs/reference/cli-syntax/errors-list/


## JAVA_HOME not set (GAL1050E)

**Symptoms:** `GAL1050E: JAVA_HOME environment variable is not set`

**Fix:** Set `JAVA_HOME` to a Java 17 JDK installation before running any `galasactl runs submit local` command.

- **macOS (Homebrew / `java_home` helper):**
  ```shell
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ```
- **Linux (OpenJDK package):**
  ```shell
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
  ```
- **Windows:** Set via *System Properties → Environment Variables*, adding `JAVA_HOME` pointing to your JDK 17 installation directory (e.g. `C:\Program Files\Java\jdk-17`).

> **Note:** Galasa currently supports Java 17 only. Java 21 or later is **not** currently supported.

**Verify the setting is correct:**
```shell
$JAVA_HOME/bin/java -version
```
The output should report `openjdk version "17.x.x"` (or equivalent).

Reference: [Running tests locally](https://galasa.dev/docs/cli-command-reference/runs-submit-local/)


## Insufficient capacity / resource exhaustion

**Symptoms:** `Insufficient capacity for images` (or similar) when running tests locally, even though the target system appears available.

**Cause:** The Dynamic Status Store (`~/.galasa/dss.properties`) still holds resource locks from a previously-failed test run. Galasa cannot allocate the required image slot because it believes the resource is still in use.

**Fix:**
1. Delete any failed test run records.
2. Clear the **content** of `~/.galasa/dss.properties` — do **not** delete the file itself, because Galasa expects the file to exist:
   ```shell
   > ~/.galasa/dss.properties   # truncates to zero bytes, preserving the file
   ```
3. Re-run the test.

Reference: https://galasa.dev/docs/writing-own-tests/binding-tests/


## Ecosystem authentication failures

**Symptoms:** `401 Unauthorized` responses, or `galasactl` refusing to connect to a remote Galasa Ecosystem.

**Checks:**

1. **Token is present** — confirm `GALASA_TOKEN` is set in `~/.galasa/galasactl.properties`:
   ```properties
   GALASA_TOKEN=<your-personal-access-token>
   ```
2. **Bootstrap URL is correct** — verify `GALASA_BOOTSTRAP` (or the `--bootstrap` flag) points to the correct Ecosystem URL, for example:
   ```shell
   export GALASA_BOOTSTRAP=https://my-ecosystem.example.com/bootstrap
   ```
3. **Refresh the bearer token** — run the login command explicitly to obtain a fresh token:
   ```shell
   galasactl auth login
   ```
4. **Token revoked** — if the personal access token has been revoked or expired, request a new one from the Galasa Web UI and update `galasactl.properties`.

Reference: see [`galasa-auth-and-secrets.md`](./galasa-auth-and-secrets.md) in this skill.
