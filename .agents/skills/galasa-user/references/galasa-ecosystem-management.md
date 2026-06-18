---
name: galasa-ecosystem-management
description: Managing tests and configuration in a Galasa Ecosystem — remote test submission, run management, CPS properties, resources, and test streams.
---

## Overview

Ecosystem operations require a running Galasa service (Kubernetes-hosted). They are distinct from the local operations covered in `galasa-cli-tool.md`, which write to files on disk.

Every Ecosystem command needs to know which service to talk to. Supply the URL once with `--bootstrap http://example.com:30960/bootstrap`, or set it for the whole session with the environment variable:

```shell
export GALASA_BOOTSTRAP="http://example.com:30960/bootstrap"
```

When `GALASA_BOOTSTRAP` is set, the `--bootstrap` flag can be omitted from all commands below.

Authentication is handled separately — see `galasa-auth-and-secrets.md` for how to log in and manage personal access tokens.


## Submitting tests to an Ecosystem (`runs submit`)

`galasactl runs submit` schedules one or more tests on the Ecosystem and polls until they finish.

Key flags:
- `--bootstrap` — service URL (or use `GALASA_BOOTSTRAP`)
- `--class <bundle>/<TestClass>` — specify a test class directly (repeatable)
- `--stream <name>` — the test stream that contains the OBR for the class
- `--portfolio <file>` — alternative to `--class`; submit a prepared portfolio
- `--override <key=value>` — CPS override for this run (repeatable; lower precedence than portfolio overrides)
- `--throttle <n>` — max parallel submissions (0 = unlimited)
- `--poll <seconds>` — how often to check run status
- `--log -` — stream progress to stderr

Submit two test classes from a named stream:

```shell
galasactl runs submit \
    --bootstrap http://example.com:30960/bootstrap \
    --class dev.galasa.simbank.tests/SimBankIVT \
    --class dev.galasa.simbank.tests/BasicAccountCreditTest \
    --stream BestSoFar \
    --log -
```

Submit a portfolio with throttling:

```shell
galasactl runs submit \
    --bootstrap http://example.com:30960/bootstrap \
    --portfolio my_portfolio.yaml \
    --throttle 5 \
    --poll 5 \
    --progress 1 \
    --log -
```

Full docs: https://galasa.dev/docs/manage-ecosystem/runs-submit/


## Querying test run results (`runs get`)

Key flags:
- `--name <runName>` — fetch a specific run (e.g. `C123`)
- `--age <from>[:<to>]` — time window using `w`, `d`, `h`, `m` units (e.g. `2w:1w`, `1d`)
- `--requestor <user>` — filter by submitting user
- `--result <Passed,Failed,...>` — comma-separated result filter; mutually exclusive with `--active`
- `--active` — show only in-progress runs; mutually exclusive with `--result`
- `--group <name>` — filter by run group
- `--format summary|details|raw` — output format (`summary` is default)

Fetch a single run in detail:

```shell
galasactl runs get --name C123 --format details
```

Fetch all failed runs from the last day:

```shell
galasactl runs get --age 1d --result Failed,EnvFail
```

Full docs: https://galasa.dev/docs/manage-ecosystem/runs-get/


## Downloading test artifacts (`runs download`)

Downloads all artifacts from the Result Archive Store (RAS) for a completed (or in-progress) run into a local folder named after the run.

```shell
galasactl runs download --name C1234
```

Use `--destination` to choose a folder, and `--force` to overwrite an existing one:

```shell
galasactl runs download --name C1234 --destination /tmp/test-results --force
```

If the run is still in progress when the command is issued, a timestamp is appended to the folder name to indicate incomplete artifact collection. If the run was retried, each attempt gets a numbered sub-folder (e.g. `C1234-1-…`, `C1234-2-…`, `C1234-3`).

Full docs: https://galasa.dev/docs/manage-ecosystem/runs-download/


## Managing runs (reset, cancel, delete)

### Reset (retry a stuck run)

Sets the run back to `queued` status so the Ecosystem will retry it. RAS history is preserved.

```shell
galasactl runs reset --name C1234
```

### Cancel (remove a stuck run from the scheduler)

Removes the run from the DSS (Dynamic Status Store). RAS history is preserved, but the run is marked as `Lost`.

```shell
galasactl runs cancel --name C1234
```

Prefer `reset` over `cancel`; `cancel` should be a last resort.

### Delete (remove completed run history)

Removes completed run results from the RAS to free space. Use `runs get` first to identify run names.

```shell
galasactl runs delete --name C1234
```

Full docs (reset/cancel): https://galasa.dev/docs/manage-ecosystem/runs-reset-cancel/  
Full docs (delete): https://galasa.dev/docs/manage-ecosystem/runs-delete/


## Managing CPS properties (`galasactl properties`)

The Configuration Property Store (CPS) holds all key-value configuration consumed by the Galasa framework, Managers, and tests. In an Ecosystem the CPS is served over REST; the CLI commands below read and write it directly.

Properties are organised into **namespaces** (e.g. `framework`, `docker`, `secure`). The `--namespace` flag is mandatory on all `properties` sub-commands. Values in `secure`-type namespaces are redacted in output.

### List namespaces

```shell
galasactl properties namespaces get
```

### Get properties

Retrieve all properties in a namespace:

```shell
galasactl properties get --namespace framework
```

Retrieve one property by name:

```shell
galasactl properties get --namespace docker --name engine.LOCAL.hostname
```

Filter by prefix/suffix/infix and return as YAML (useful for bulk export):

```shell
galasactl properties get --namespace docker \
    --prefix engine --suffix hostname --infix LOCAL,REMOTE \
    --format yaml
```

### Set a property

Creates the property if it does not exist; updates it otherwise:

```shell
galasactl properties set --namespace docker --name engine.REMOTE.hostname --value 103.67.89.6
```

### Delete a property

```shell
galasactl properties delete --namespace docker --name engine.REMOTE.hostname
```

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-manage-cps/


## Managing Ecosystem resources (`galasactl resources`)

Galasa resources are typed YAML documents (`GalasaProperty`, `GalasaSecret`, `GalasaStream`, `GalasaTag`, …) that describe Ecosystem configuration. You can manage several resources of different types in a single YAML file, separated by `---`.

This is the preferred way to configure a new Ecosystem or apply a reproducible set of changes: export YAML with `properties get --format yaml` or `streams get --format yaml`, edit the file, then apply it.

Apply (create-or-update) all resources in a file:

```shell
galasactl resources apply -f my-resources.yaml
```

Other sub-commands:

```shell
galasactl resources create -f my-resources.yaml   # fails if any resource already exists
galasactl resources update -f my-resources.yaml   # fails if any resource does not exist
galasactl resources delete -f my-resources.yaml   # deletes resources listed in the file
```

Minimal `GalasaProperty` YAML shape:

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: engine.LOCAL.hostname
  namespace: docker
data:
  value: 127.0.0.1
```

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-manage-resources/


## Managing test streams (`galasactl streams`)

A test stream groups tests that run in automation, identified by an OBR and a test catalog. Streams are referenced by name in `runs submit --stream <name>`.

### List streams

```shell
galasactl streams get
```

Export streams as YAML (for use with `resources apply`):

```shell
galasactl streams get --format yaml
```

### Create or update a stream

```shell
galasactl streams set \
    --name BestSoFar \
    --description "In-development integration tests" \
    --maven-repo-url https://my-maven-repo/path/to/tests \
    --obr mvn:dev.galasa/dev.galasa.simbank.obr/0.0.1/obr \
    --testcatalog-url https://my-maven-repo/path/to/testcatalog.json
```

When updating an existing stream, supply only the flags you want to change.

### Delete a stream

```shell
galasactl streams delete --name BestSoFar
```

Full docs: https://galasa.dev/docs/manage-ecosystem/test-streams/
