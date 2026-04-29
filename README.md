# DBeaver Jinja Templater

DBeaver Jinja Templater is an offline-first Eclipse plugin for DBeaver Community Edition. It lets users render practical SQL templates with a small Jinja-like syntax directly inside the SQL Editor without Python, external processes, telemetry, or network calls.

## Features

- Render selected SQL text or the entire SQL editor contents.
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

To build the offline distribution package:

```powershell
.\scripts\build-offline-package.ps1
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
   - `SQL Editor -> Render Jinja Template`
   - SQL editor context menu: `Render Jinja Template`
   - optional shortcut: `Ctrl+Alt+J`

## Usage

1. Open a SQL Editor in DBeaver.
2. Select a template fragment, or leave nothing selected to render the whole editor.
3. Run `Render Jinja Template`.
4. Enter variables JSON in the dialog.
5. Click `Render`.
6. The rendered SQL is shown in a preview dialog by default.
7. Use `Copy to Clipboard` to paste it back into SQL Editor.
8. Replacement modes remain available through persisted settings for MVP experiments.

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
- Build fails resolving Eclipse bundles:
  - The build machine needs internet access to resolve the target platform once.

## Roadmap

- Preference page for render mode and strict variables
- Open rendered SQL in a brand new DBeaver SQL Editor tab
- More filters
- `else` / `elif`
- Template preview diff
