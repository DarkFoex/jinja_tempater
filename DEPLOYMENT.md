# Deployment Guide

This document explains:

- what the build downloads from the internet
- how to prepare a build machine
- how to build the plugin
- how to transfer it into a closed environment
- how to install, verify, update, and remove it

## 1. What is downloaded from the internet during build

The current project does not download anything at runtime inside DBeaver.
All internet access happens only on the build machine while Maven and Tycho resolve dependencies.

### 1.1 Maven artifacts downloaded during build

From the current Maven configuration, the build machine downloads at least these artifact groups:

- Maven core plugins used by the build:
  - `org.apache.maven.plugins:maven-compiler-plugin:3.13.0`
  - `org.apache.maven.plugins:maven-surefire-plugin:3.2.5`
  - `org.apache.maven.plugins:maven-dependency-plugin:3.6.1`
- Tycho build plugins:
  - `org.eclipse.tycho:tycho-maven-plugin:4.0.10`
  - `org.eclipse.tycho:target-platform-configuration:4.0.10`
- Test dependency:
  - `org.junit.jupiter:junit-jupiter:5.10.2`

Important:

- Maven will also download transitive dependencies of these artifacts.
- Exact file names can vary slightly over time because transitive dependency graphs are resolved by Maven repositories.
- If the local Maven cache is empty, the first build downloads the most.

### 1.2 Eclipse / DBeaver bundles downloaded during build

The Tycho target platform is declared in [releng/dbeaver-jinja-templater.target](/G:/DBEAEVR/releng/dbeaver-jinja-templater.target).
It resolves installable units from these remote repositories:

- DBeaver CE P2 repository:
  - `https://repo.dbeaver.net/p2/ce/latest`
- Eclipse release repository:
  - `https://download.eclipse.org/releases/2024-06`

The target currently requests these DBeaver units:

- `com.dbeaver.deps.ce.feature.feature.group`
- `org.jkiss.dbeaver.model`
- `org.jkiss.dbeaver.ui`
- `org.jkiss.dbeaver.ui.editors.sql`

It also requests the Eclipse platform feature:

- `org.eclipse.platform.feature.group`

Important:

- Tycho may download many additional dependent bundles behind these units.
- This is expected and normal for an Eclipse RCP / OSGi build.

## 2. What does not come from the internet

These parts are fully local in this repository:

- all plugin source code
- `templater-core`
- parser, renderer, filters, JSON parser
- examples
- PowerShell packaging scripts
- documentation

At runtime, the plugin does not:

- call remote HTTP endpoints
- download templates
- update itself
- send telemetry
- launch Python
- launch external helper processes

## 3. Build machine requirements

Prepare one machine with internet access and install:

- Java 17 or newer
- Maven 3.9 or newer
- Git

Recommended:

- keep a dedicated local Maven cache for reproducible internal builds
- archive the built ZIP and the Maven cache snapshot together

## 4. Build commands

From the repository root:

```powershell
mvn -q -pl templater-core test
mvn clean package
```

To create the transfer package:

```powershell
.\scripts\build-offline-package.ps1
```

To verify the package:

```powershell
.\scripts\check-package.ps1
```

## 5. Expected build outputs

Main outputs:

- `templater-core\target\templater-core-<version>.jar`
- `dbeaver-jinja-templater-plugin\target\*.jar`

Offline package outputs:

- `dist\dbeaver-jinja-templater-offline\`
- `dist\dbeaver-jinja-templater-offline.zip`

## 6. What to transfer into the closed environment

Transfer the ZIP or the unpacked folder:

```text
dist\dbeaver-jinja-templater-offline\
```

Minimum contents:

- plugin jar
- `README.md`
- `DEPLOYMENT.md`
- `offline\README_OFFLINE_TRANSFER.md`
- `examples\`
- `checksums.sha256`

## 7. Installation in DBeaver

1. Close DBeaver.
2. Unpack the offline package.
3. Copy the built plugin jar into:

```text
<DBeaver>\dropins\plugins\
```

4. Start DBeaver.
5. Open SQL Editor.
6. Verify one of these appears:
   - top SQL Editor menu entry
   - SQL editor context menu entry
   - shortcut `Ctrl+Alt+J`

## 8. First verification after deployment

Use this minimal template:

```jinja
select {{ schema }} as schema_name
{% if date_from %}
where created_at >= {{ date_from | sql_string }}
{% endif %}
```

Use this JSON:

```json
{
  "schema": "public",
  "date_from": "2026-01-01"
}
```

Expected result:

```sql
select public as schema_name
where created_at >= '2026-01-01'
```

## 9. Update procedure

1. Close DBeaver.
2. Remove the old plugin jar from `dropins\plugins`.
3. Copy the new plugin jar from the newly built package.
4. Start DBeaver.
5. Re-run the verification template.

## 10. Removal procedure

1. Close DBeaver.
2. Delete the plugin jar from `dropins\plugins`.
3. Start DBeaver again.

## 11. Recommended closed-contour operating model

For stable internal deployment:

1. Build on one internet-enabled build workstation.
2. Archive:
   - built plugin ZIP
   - commit hash
   - SHA-256 checksum
   - Java version
   - Maven version
   - local Maven repository snapshot if required by policy
3. Move only the packaged ZIP into the closed contour.
4. Install from the ZIP without any external downloads.

