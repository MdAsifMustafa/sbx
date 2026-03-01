# SBX CLI

SBX is a Spring Boot productivity CLI for:

- project bootstrap (`app new`, `app init`)
- dependency management (`dependency add/remove/sync`)
- build/runtime workflows (`build`, `start`, `stop`, `status`, `log`, `restart`)
- scaffolding (`make ...`)
- config helpers (`config db ...`)
- diagnostics (`doctor`)

## Prerequisites

- Java 21+
- Maven 3.8+

## Build And Run SBX

Build:

```bash
mvn clean package
```

Output jar:

- `target/sbx-0.1.0-SNAPSHOT.jar`

Run:

```bash
java -jar target/sbx-0.1.0-SNAPSHOT.jar
```

## Global CLI Usage

```bash
sbx [OPTIONS] [COMMAND]
```

Global options:

- `-h`, `--help`: show command help.
- `-V`, `--version`: show SBX version.

Top-level commands:

- `app`
- `build`
- `clean`
- `start`
- `stop`
- `status`
- `log`
- `restart`
- `doctor`
- `dependency`
- `make`
- `config`
- `convert`

## Commands And Flags (Detailed)

## 1) app

Purpose: Create a new project or initialize SBX in an existing project.

### 1.1 `sbx app new <name>`

Purpose: Generate a new Spring Boot project and create `sbx.json`.

Syntax:

```bash
sbx app new <name> [flags]
```

Argument:

- `<name>`: folder/project name to create.

Flags:

- `--group-id`: Java group ID (example: `com.example`).
- `--artifact-id`: artifact/build name.
- `--package-name`: base package for generated Java classes.
- `--description`: project description.
- `--java`: Java version for project runtime metadata.
- `--boot`: Spring Boot version.
- `--build`: build system option selected from Initializr metadata.
- `--packaging`: output packaging (usually `jar` or `war`).
- `--language`: project language option from Initializr.
- `--config`: app config format. Accepted: `properties` or `yaml`.
- `--allow-legacy`: allow creating project with legacy Spring Boot versions.

Behavior notes:

- Missing values are asked interactively.
- Newer supported versions are fetched from Spring Initializr.
- Legacy versions can be generated via SBX templates.

Examples:

```bash
sbx app new my-app --group-id com.example --artifact-id my-app --package-name com.example.myapp
sbx app new my-app --boot 4.0.3 --java 21 --config yaml
```

### 1.2 `sbx app init`

Purpose: Create `sbx.json` inside an existing Spring Boot project.

Syntax:

```bash
sbx app init [--boot-version <version>]
```

Flags:

- `--boot-version`: override detected Spring Boot version in generated `sbx.json`.

Behavior notes:

- Detects build tool, Java, config format, and dependencies.
- Prompts for confirmation before writing `sbx.json`.

## 2) build

Purpose: Build the application using the build tool from `sbx.json`.

Syntax:

```bash
sbx build [flags]
```

Flags:

- `--skip-tests`: skip tests during build.
- `--profile <name>`: pass profile/build variant (`-P...` for Maven or profile property for Gradle).
- `--sync`: apply dependencies from `sbx.json` before building.

Examples:

```bash
sbx build
sbx build --skip-tests --profile prod
sbx build --sync
```

## 3) clean

Purpose: Clean build artifacts.

Syntax:

```bash
sbx clean
```

## 4) start

Purpose: Start the Spring Boot app through Maven/Gradle.

Syntax:

```bash
sbx start [flags]
```

Flags:

- `--profile <name>`: active Spring profile.
- `--host <address>`: bind host/address (`server.address`).
- `--port <number>`: server port (`server.port`).
- `--debug`: enable Spring Boot debug mode.
- `--daemon`: run in background and save runtime state for `status`/`stop`.

Examples:

```bash
sbx start
sbx start --profile dev --port 9090
sbx start --daemon --host 0.0.0.0
```

## 5) stop

Purpose: Stop the running app using stored process ID.

Syntax:

```bash
sbx stop
```

## 6) status

Purpose: Show whether app is running and where.

Syntax:

```bash
sbx status [--health]
```

Flags:

- `--health`: also call actuator health endpoint and print UP/DOWN style status.

## 7) log

Purpose: Read app log file.

Syntax:

```bash
sbx log [flags]
```

Flags:

- `-f`, `--follow`: stream log updates continuously.
- `--tail <n>`: print last `n` lines only.
- `--since <duration>`: only show logs when file was modified within duration.
  - supported suffixes: `s`, `m`, `h`
  - examples: `30s`, `5m`, `1h`

Examples:

```bash
sbx log --tail 100
sbx log --since 10m
sbx log -f
```

## 8) restart

Purpose: Restart app (stop then start).

Syntax:

```bash
sbx restart [--graceful]
```

Flags:

- `--graceful`: attempts graceful shutdown before force kill fallback.

## 9) doctor

Purpose: Validate SBX setup and project health.

Syntax:

```bash
sbx doctor
```

Checks include:

- `sbx.json` presence and schema validity
- build tool detection
- Java compatibility (system vs project)
- Spring Boot compatibility checks
- dependency conflict checks (for example MVC + WebFlux)

## 10) dependency

Purpose: Add, remove, or sync dependencies between `sbx.json` and build files.

### 10.1 `sbx dependency add <input>`

Syntax:

```bash
sbx dependency add <keyword|groupId:artifactId[:version]> [flags]
```

Argument:

- `<input>`:
  - keyword search term, or
  - exact Maven coordinates (`groupId:artifactId[:version]`).

Flags:

- `--no-apply`: update `sbx.json` only, do not edit build file.
- `--confirm`: skip confirmation prompts.
- `--format`: format build file after applying dependency.

### 10.2 `sbx dependency remove <input>`

Syntax:

```bash
sbx dependency remove <groupId:artifactId|searchTerm> [flags]
```

Argument:

- `<input>`: exact coords or text to find matching dependency.

Flags:

- `--no-apply`: remove from `sbx.json` only.
- `--confirm`: skip confirmation prompt.

### 10.3 `sbx dependency sync`

Syntax:

```bash
sbx dependency sync [flags]
```

Flags:

- `--format`: run formatter after sync.
- `--dry-run`: preview what would be applied.

## 11) make

Purpose: Generate source files quickly.

### 11.1 `sbx make controller <name>`

Purpose: Generate MVC/REST/CRUD/GraphQL controller.

Flags:

- `--rest`: generate REST controller style.
- `--crud`: generate CRUD endpoints and supporting layer.
- `--graphql`: generate GraphQL-style controller.
- `--service`: generate service and inject it.
- `--model <name>`: model name to bind in generated code.
- `--path <path>`: custom request mapping path.
- `--versioned`: prefix endpoint path with `/api/v1` behavior.
- `--package <relativePackage>`: custom package under base package.
- `--test`: also generate test class.
- `--dry-run`: print output without writing files.
- `--force`: overwrite existing files.

### 11.2 `sbx make crud <name>`

Purpose: Generate CRUD stack (entity + DTO + mapper + controller, optional GraphQL).

Flags:

- `--graphql`: also generate GraphQL files.
- `--lombok`: Lombok support where templates allow it.
- `--record`: generate DTOs as records.
- `--dry-run`: preview only.
- `--force`: overwrite existing files.

### 11.3 `sbx make dto <name>`

Purpose: Generate DTO classes.

Flags:

- `--request`: generate request DTO (`<Name>RequestDto`).
- `--response`: generate response DTO (`<Name>ResponseDto`).
- `--record`: generate Java record.
- `--validation`: include validation annotations.
- `--from-entity`: shape DTO based on entity model.
- `--lombok`: use Lombok annotations.
- `--dry-run`: preview only.
- `--force`: overwrite existing file.

### 11.4 `sbx make entity <name>`

Purpose: Generate JPA entity.

Flags:

- `--table <tableName>`: custom DB table name.
- `--lombok`: include Lombok annotations.
- `--uuid`: use UUID primary key.
- `--auditable`: add created/updated fields.
- `--soft-delete`: add soft-delete field.
- `--dry-run`: preview only.
- `--force`: overwrite existing file.

### 11.5 `sbx make repository <name>`

Purpose: Generate repository interface.

Flags:

- `--custom`: generate non-JPA repository style.
- `--query <methodName>`: include query method stub.
- `--dry-run`: preview only.
- `--force`: overwrite existing file.

### 11.6 `sbx make service <name>`

Purpose: Generate service interface and/or implementation.

Flags:

- `--interface`: generate only interface.
- `--impl`: generate only implementation.
- `--dry-run`: preview only.
- `--force`: overwrite existing files.

### 11.7 `sbx make mapper <name>`

Purpose: Generate mapper class/interface.

Flags:

- `--dry-run`: preview only.
- `--force`: overwrite existing file.

### 11.8 `sbx make graphql <name>`

Purpose: Generate GraphQL resolver/schema.

Flags:

- `--query`: include query operations.
- `--mutation`: include mutation operations.
- `--schema`: generate schema only.
- `--dry-run`: preview only.
- `--force`: overwrite existing files.

### 11.9 `sbx make migration <name>`

Purpose: Generate Flyway migration SQL file.

Flags:

- `--sql`: generate empty SQL migration.
- `--dry-run`: preview only.
- `--force`: overwrite existing file.

### 11.10 `sbx make event <name>`

Purpose: Generate domain event class and optional Spring event listener.

Flags:

- `--payload`: add a generic `payload` field to event class.
- `--listener`: also generate a listener class with `@EventListener`.
- `--dry-run`: preview only.
- `--force`: overwrite existing files.

### 11.11 `sbx make mail <name>`

Purpose: Generate mail service class and optional HTML template.

Flags:

- `--template`: also generate HTML template under `src/main/resources/templates/mail/`.
- `--async`: mark generated send method with `@Async`.
- `--dry-run`: preview only.
- `--force`: overwrite existing files.

### 11.12 `sbx make scheduler <name>`

Purpose: Generate scheduled job scaffold.

Flags:

- `--cron <expr>`: cron schedule (default).
- `--fixed-rate <ms>`: fixed-rate scheduler.
- `--fixed-delay <ms>`: fixed-delay scheduler.
- `--async`: add `@Async` to job method.
- `--dry-run`, `--force`.

### 11.13 `sbx make exception <name>`

Purpose: Generate custom exception and optional global handler.

Flags:

- `--status <code>`: HTTP status used in generated handler.
- `--code <ERR_CODE>`: exception code constant.
- `--handler`: generate `GlobalExceptionHandler`.
- `--dry-run`, `--force`.

### 11.14 `sbx make validator <name>`

Purpose: Generate custom Bean Validation annotation and validator.

Flags:

- `--field-type <type>`: value type for validator.
- `--message <text>`: default validation message.
- `--groups`: include groups guidance comment.
- `--dry-run`, `--force`.

### 11.15 `sbx make spec <name>`

Purpose: Generate JPA Specification helper class.

Flags:

- `--entity <Entity>`: target entity class.
- `--paging`: add paging helper.
- `--sorting`: add sorting helper.
- `--dry-run`, `--force`.

### 11.16 `sbx make security <name>`

Purpose: Generate Spring Security config starter.

Flags:

- `--jwt`: include JWT integration TODO section.
- `--roles`: include role matcher examples.
- `--method-security`: enable method security annotation.
- `--dry-run`, `--force`.

### 11.17 `sbx make cache <name>`

Purpose: Generate cache service scaffold.

Flags:

- `--provider <caffeine|redis>`
- `--ttl <duration>`
- `--key-prefix <cacheName>`
- `--dry-run`, `--force`.

### 11.18 `sbx make message <name>`

Purpose: Generate producer/consumer scaffolding (topic/queue style).

Flags:

- `--topic <name>`
- `--group <name>`
- `--payload`: also generate payload class.
- `--dry-run`, `--force`.

### 11.19 `sbx make mapstruct <entity>`

Purpose: Generate MapStruct mapper scaffold.

Flags:

- `--dto <DtoClass>`
- `--component-model <spring|default|...>`
- `--update-method`: generate update method with `@MappingTarget`.
- `--dry-run`, `--force`.

### 11.20 `sbx make test <name>`

Purpose: Generate test class scaffold.

Flags:

- `--type <unit|integration|webmvc|datajpa>`
- `--mockito`: include Mockito static import.
- `--dry-run`, `--force`.

### 11.21 `sbx make module <name>`

Purpose: Generate module package structure and module README.

Flags:

- `--with-crud`: add domain/api/service package markers.
- `--with-event`: add event package marker.
- `--with-mail`: add mail package marker.
- `--dry-run`, `--force`.

## 12) config

Purpose: Manage configuration modules.

Current module:

- `db`

### 12.1 `sbx config db list`

Purpose: Show DB connections and active default.

Flags:

- `--profile <profile>`: use profile-specific config source.

### 12.2 `sbx config db set`

Purpose: Add/update DB connection.

Flags:

- `--profile <profile>`: profile context.
- `--engine <engineKey>`: engine (example: postgres, mysql, h2, sqlite).
- `--host <host>`: server host.
- `--port <port>`: server port.
- `--database <name>`: database/schema name.
- `--username <user>`: DB username.
- `--password <password>`: DB password.
- `--file <path>`: DB file path for file-based engines.
- `--h2-mode <file|mem>`: H2 storage mode.
- `--default`: make this connection default.

### 12.3 `sbx config db use <name>`

Purpose: Set connection as default.

Flags:

- `--profile <profile>`

### 12.4 `sbx config db remove <name>`

Purpose: Remove named connection.

Flags:

- `--profile <profile>`

### 12.5 `sbx config db rename <oldName> <newName>`

Purpose: Rename connection key.

Flags:

- `--profile <profile>`

### 12.6 `sbx config db test [name]`

Purpose: Test JDBC connection (named or default).

Flags:

- `--profile <profile>`

### 12.7 `sbx config db validate`

Purpose: Validate DB config consistency (default exists, etc.).

Flags:

- `--profile <profile>`

## 13) convert

Purpose: Convert project build tool (Maven <-> Gradle).

Syntax:

```bash
sbx convert --to <maven|gradle> [flags]
```

Flags:

- `--to <maven|gradle>`: required target build tool.
- `--kotlin`: when converting to Gradle, use Kotlin DSL.
- `--no-sync`: skip dependency sync after conversion.

Behavior note:

- command asks for confirmation token: `convert-to-<target>`.

## Practical Examples

Create project:

```bash
sbx app new demo-api --group-id com.demo --package-name com.demo.api
```

Initialize existing project:

```bash
sbx app init
sbx doctor
```

Dependency workflow:

```bash
sbx dependency add spring-boot-starter-data-jpa
sbx dependency sync --format
```

Runtime workflow:

```bash
sbx start --daemon --profile dev
sbx status --health
sbx log --tail 100
sbx stop
```

## Troubleshooting

- `sbx.json not found`: run `sbx app new <name>` or `sbx app init`.
- build tool errors: verify Maven/Gradle is installed and discoverable.
- runtime/log issues: start app with `sbx start` first.

## License

MIT

## Author

Md Asif Mustafa
