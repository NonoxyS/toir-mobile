# CI Workflow Setup

## Workflows Overview

| Workflow              | Trigger                          | Description                      |
|-----------------------|----------------------------------|----------------------------------|
| `android-checks.yml`  | Pull Request → `main`, `develop` | Detekt + Android Lint            |
| `ios-checks.yml`      | Pull Request → `main`, `develop` | SwiftLint + SwiftFormat          |
| `android-release.yml` | Manual (`workflow_dispatch`)     | Release APK (dev or prod flavor) |

---

## Android Checks & iOS Checks

No setup required — these run automatically on every PR.

---

## Android Release APK

Requires two GitHub Actions secrets for APK signing.

### Step 1 — Create a release keystore (skip if you already have one)

```bash
keytool -genkey -v \
  -keystore keystores/release.keystore.jks \
  -alias <your-key-alias> \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Step 2 — Create `signing.properties`

```properties
keystorePassword=<your-keystore-password>
keyAlias=<your-key-alias>
keyPassword=<your-key-password>
```

> `signing.properties` and `keystores/release.keystore.jks` are gitignored — never commit them.

### Step 3 — Encode files to base64

```bash
# macOS
base64 -i signing.properties | pbcopy           # paste as SIGNING_PROPERTIES_BASE64
base64 -i keystores/release.keystore.jks | pbcopy  # paste as RELEASE_KEYSTORE_BASE64

# Linux
base64 -w 0 signing.properties
base64 -w 0 keystores/release.keystore.jks
```

### Step 4 — Add secrets to GitHub

Go to **Settings → Secrets and variables → Actions → New repository secret** and add:

| Secret                      | Value                                                       |
|-----------------------------|-------------------------------------------------------------|
| `SIGNING_PROPERTIES_BASE64` | base64-encoded contents of `signing.properties`             |
| `RELEASE_KEYSTORE_BASE64`   | base64-encoded contents of `keystores/release.keystore.jks` |

### Step 5 — Run the workflow

Go to **Actions → Android Release APK → Run workflow**, choose `dev` or `prod` flavor, click **Run workflow**.

The signed APK will be available as a workflow artifact for 7 days.
