# DBeaver Jinja Templater

DBeaver Jinja Templater is an offline-first Eclipse plugin for DBeaver Community Edition. It lets users keep Jinja-like SQL templates visible in the SQL Editor and render them only at execution time without Python, external processes, telemetry, or network calls.

## Features

- Keep template syntax visible in SQL Editor while executing rendered SQL against the database.
- Intercept standard SQL Editor run commands for Jinja-aware execution.
- Jinja-like syntax for SQL templating:
  - `{{ variable }}`
  - `{{ user.name }}`
  - `{% if variable %}...{% endif %}`
  - `{% if not variable %}...{% endif %}`
  - `{% if env == 'dev' %}...{% endif %}`
  - `{% for item in items %}...{% endfor %}`
  - `{# comments #}`
- Built-in filters:
  - `upper`
  - `lower`
  - `trim`
  - `default`
  - `join`
  - `sql_string`
- JSON variables dialog with:
  - multi-line JSON editor
  - `Load example`
  - `Render`
  - `Cancel`
  - optional persistence of last variables
- Preview command that renders the current selection or full script without modifying editor text.
- Fully offline operation.

## Project Layout

```text
dbeaver-jinja-templater/
  pom.xml
  releng/
  templater-core/
  dbeaver-jinja-templater-plugin/
  offline/
  examples/
  scripts/
```

## Build

### Requirements

- Java 17 or newer available on `PATH`
- Maven 3.9+ available on `PATH`
- Internet access only on the build machine for resolving the Eclipse/DBeaver target platform

### Commands

```powershell
mvn -q -pl templater-core test
mvn clean package
```

### Recommended local build against installed DBeaver

The public DBeaver P2 repository is not sufficient for resolving all `org.jkiss.dbeaver.*` plugin units needed by this plugin build.
For the most reliable build, use a locally installed DBeaver as the target platform:

1. Copy [releng/dbeaver-local.target.template](/G:/DBEAEVR/releng/dbeaver-local.target.template) to `releng/dbeaver-local.target`
2. Replace `C:/Path/To/DBeaver` with your actual DBeaver installation path
3. Run:

```powershell
mvn "-Dtycho.target.file=%CD%/releng/dbeaver-local.target" clean package
```

Example Windows path:

```text
C:\Users\<user>\AppData\Local\DBeaver
```

To build the offline distribution package:

```powershell
.\scripts\build-offline-package.ps1
```

Using a local DBeaver target file:

```powershell
.\scripts\build-offline-package.ps1 -TychoTargetFile "$PWD/releng/dbeaver-local.target"
```

Detailed deployment instructions:

- [DEPLOYMENT.md](/G:/DBEAEVR/DEPLOYMENT.md)
- [offline/README_OFFLINE_TRANSFER.md](/G:/DBEAEVR/offline/README_OFFLINE_TRANSFER.md)

## Installation in DBeaver

1. Build the project and locate the generated plugin jar in `dbeaver-jinja-templater-plugin\target`.
2. Copy the plugin jar to:

```text
<DBeaver>\dropins\plugins\
```

3. Restart DBeaver.
4. Open SQL Editor and use:
   - `SQL Editor -> Jinja Variables / Preview`
   - SQL editor context menu: `Jinja Variables / Preview`
   - optional shortcut: `Ctrl+Alt+J`

## Usage

1. Open a SQL Editor in DBeaver.
2. Press `Ctrl+Alt+J` or run `Jinja Variables / Preview`.
3. Enter variables JSON in the dialog.
4. Click `Render` to preview the currently selected fragment or the whole editor.
5. Keep the original template text in the editor unchanged.
6. Use the normal DBeaver run commands such as `Ctrl+Enter` or `Alt+X`.
7. When the SQL contains `{{`, `{%`, or `{#`, the plugin renders a temporary SQL string and sends that to DBeaver execution.

## Example Template

See [examples/sample_template.sql](/G:/DBEAEVR/examples/sample_template.sql) and [examples/sample_vars.json](/G:/DBEAEVR/examples/sample_vars.json).

## Supported Syntax

### Variables

```jinja
{{ schema }}
{{ user.name }}
```

### Conditions

```jinja
{% if date_from %}
  and created_at >= {{ date_from | sql_string }}
{% endif %}

{% if not archived %}
  and archived = false
{% endif %}

{% if env == 'dev' %}
  limit 100
{% endif %}
```

### Loops

```jinja
{% for table in tables %}
select * from {{ schema }}.{{ table }};
{% endfor %}
```

### Filters

```jinja
{{ name | upper }}
{{ name | lower }}
{{ value | trim }}
{{ missing | default('fallback') }}
{{ tables | join(', ') }}
{{ value | sql_string }}
```

## Syntax Limits

- This is not full Python Jinja2 compatibility.
- Not supported:
  - `macro`
  - `include`
  - `extends`
  - `block`
  - `set`
  - Python expressions
  - code execution
- Conditions support only:
  - truthy / falsy checks
  - `not`
  - equality with `==`

## Troubleshooting

- `Unknown variable`:
  - Either add the variable to JSON, or disable strict mode in persisted settings.
- `Unknown filter`:
  - Check filter spelling. Only the built-in filters are supported in v1.
- `Unclosed if` / `Unclosed for`:
  - Check matching `{% endif %}` and `{% endfor %}` blocks.
- Command is not visible in DBeaver:
  - Verify the plugin jar is inside `dropins/plugins` and restart DBeaver.
- SQL still reaches the database with `{{ ... }}`:
  - Open `Jinja Variables / Preview` once and save variables JSON.
  - Re-run the query using a standard SQL Editor command such as `Ctrl+Enter`.
  - If needed, restart DBeaver with `-clean` after replacing the plugin jar.
- Build fails resolving Eclipse bundles:
  - The build machine needs internet access to resolve the target platform once.

## Roadmap

- Preference page for strict variables and cached JSON management
- Native editor integration for explain/load-plan Jinja execution
- More filters
- `else` / `elif`
- Template preview diff
