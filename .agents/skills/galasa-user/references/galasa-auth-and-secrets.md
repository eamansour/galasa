---
name: galasa-auth-and-secrets
description: Authentication setup for the Galasa Ecosystem, managing personal access tokens, and managing secrets/credentials in a remote Ecosystem.
---

## Prerequisites

Every `galasactl` command that contacts a remote Galasa Ecosystem needs two things:

1. **Bootstrap URL** — tells galasactl where the Ecosystem lives. Set once via the environment variable or pass it per-command:
   ```bash
   export GALASA_BOOTSTRAP="https://my.ecosystem.url/api/bootstrap"
   # or per-command:
   galasactl <command> --bootstrap https://my.ecosystem.url/api/bootstrap
   ```

2. **Personal access token** — stored in `~/.galasa/galasactl.properties` (created by `galasactl local init`):
   ```properties
   GALASA_TOKEN=<token-value>
   ```
   Alternatively, export `GALASA_TOKEN` as an environment variable (valid for that session only).

**Getting a token from the Web UI**: Log into the Galasa Web UI → *My Settings* → create a new personal access token → copy the displayed value into `GALASA_TOKEN`. The Web UI shows copy instructions in the dialog.

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-authentication/

---

## Logging In and Out

galasactl logs in **implicitly** whenever it needs to contact the Ecosystem (using `GALASA_TOKEN` to obtain a short-lived bearer token). You can also control login state explicitly.

```bash
# Explicit login — useful in CI/CD pipelines to pre-authenticate
galasactl auth login --bootstrap https://my.ecosystem.url/api/bootstrap

# Log out — deletes the cached bearer token from ~/.galasa/bearer-tokens/
galasactl auth logout
```

After `auth logout`, the next galasactl command that talks to the Ecosystem will re-authenticate automatically using `GALASA_TOKEN`.

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-authentication/

---

## Managing Personal Access Tokens

These commands require Ecosystem admin access or are scoped to tokens the caller owns.

```bash
# List all active personal access tokens in the Ecosystem
galasactl auth tokens get
# tokenid         created(YYYY/MM/DD)  user                description
# 09823128318238  2024-02-03           m.smith@gmail.com   Ecosystem1 access
# 87a6s2y8hqwd27  2024-05-04           s_jones@gmail.com   CLI access from VSCode
# Total:2

# Filter by user
galasactl auth tokens get --user m.smith@gmail.com

# Revoke a token by ID (use the tokenid column from `auth tokens get`)
galasactl auth tokens delete --tokenid 09823128318238
```

> **Note**: Revoking a personal access token does not immediately invalidate an active JWT (bearer token) already on the user's machine — JWTs expire on their own schedule. To remove the bearer token immediately, run `galasactl auth logout` on the user's machine.

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-authentication/

---

## Managing Secrets in the Ecosystem

Secrets are credentials (username, password, token, or combinations) stored in the Ecosystem's CREDs store (etcd). Tests running remotely retrieve credentials from the CREDs store by name rather than having credentials embedded in test code or config.

Supported secret types depend on which flags are combined:

| Flags used                    | Secret type created  |
| ----------------------------- | -------------------- |
| `--username` + `--password`   | UsernamePassword     |
| `--username` + `--token`      | UsernameToken        |
| `--token` only                | Token                |
| `--username` only             | Username             |

```bash
# Create (or update) a UsernamePassword secret
galasactl secrets set --name SYSTEM1 --username "my-username" --password "my-password" --description "credentials for SYSTEM1"

# Create a Token secret
galasactl secrets set --name SYSTEM1 --token "my-token"

# List all secrets (summary format by default)
galasactl secrets get
# name    type             last-updated(UTC)   last-updated-by description
# SYSTEM1 UsernamePassword 2024-10-30 16:23:49 galasa-user     credentials for SYSTEM1
# Total:1

# Get a specific secret by name
galasactl secrets get --name SYSTEM1

# Get all secrets in YAML format (can be used with `galasactl resources` commands)
galasactl secrets get --format yaml

# Delete a secret
galasactl secrets delete --name SYSTEM1
```

Base64-encoded values can be supplied with `--base64-username`, `--base64-password`, and `--base64-token` flags. Use `--type` to explicitly change the type of an existing secret.

Full docs: https://galasa.dev/docs/ecosystem/ecosystem-manage-creds/
