# Offline Transfer Guide

This document describes how to build, transfer, install, update, and remove the DBeaver Jinja Templater plugin in a closed environment.

## 1. What to build on the home laptop

Build on a machine that has:

- Java 17+
- Maven 3.9+
- Internet access for resolving the Eclipse/DBeaver target platform during build

Run:

```powershell
mvn clean package
.\scripts\build-offline-package.ps1
```

Expected outputs:

- plugin jar from `dbeaver-jinja-templater-plugin\target`
- `templater-core` jar copied into plugin `lib` during packaging
- offline distribution zip in `dist`

## 2. Files to transfer

Transfer the full offline package directory or its zip:

```text
dist/dbeaver-jinja-templater-offline/
```

The package contains:

- plugin jar
- `DEPLOYMENT.md`
- nested runtime libraries if present
- `README.md`
- `offline/README_OFFLINE_TRANSFER.md`
- `examples/`
- `checksums.sha256`

## 3. How to verify checksums

On the source machine:

```powershell
Get-FileHash .\dist\dbeaver-jinja-templater-offline.zip -Algorithm SHA256
```

On the target machine, compare the value with the separately communicated checksum, or run:

```powershell
.\scripts\check-package.ps1 -PackageRoot .\dist\dbeaver-jinja-templater-offline
```

## 4. How to install in DBeaver without internet

1. Close DBeaver.
2. Unpack the offline package.
3. Copy the plugin jar to:

```text
<DBeaver>\dropins\plugins\
```

4. If the package contains runtime libraries outside the plugin jar, copy them exactly as shipped.
5. Start DBeaver.
6. Open SQL Editor and check:
   - top menu contains `Render Jinja Template`
   - SQL editor context menu contains `Render Jinja Template`

## 5. How to remove the plugin

1. Close DBeaver.
2. Delete the plugin jar from:

```text
<DBeaver>\dropins\plugins\
```

3. If a sibling plugin folder or additional library directory was copied, remove it too.
4. Start DBeaver again.

## 6. How to update the plugin

1. Close DBeaver.
2. Remove the old plugin jar.
3. Copy the new plugin jar from the new offline package.
4. Start DBeaver.
5. Re-test the command in SQL Editor.

## 7. How to confirm the plugin does not require network

The plugin:

- does not execute Python
- does not start external processes
- does not download templates or dependencies during runtime
- does not contain telemetry
- does not contain auto-update logic
- performs only in-process text parsing and rendering

Operational validation options:

- Run DBeaver on a workstation with network disabled and confirm rendering still works.
- Inspect plugin source and manifest files in this repository.
- Search the built jar for `http`, `https`, `java.net.HttpURLConnection`, `java.net.http`, or telemetry-related dependencies.

## 8. How to keep dependencies local

- Build once on an internet-connected build machine.
- Archive the generated offline zip in your internal artifact storage.
- Keep the exact source commit, zip checksum, and Java/Maven versions together with the package.
- If your process requires reproducibility, also archive the populated local Maven repository used for the build.
