# Contributing

## Build

Run the full build before opening a pull request. It compiles, runs tests,
and runs checkstyle:

```shell
./gradlew build
```

The CI pipeline runs the same command on JDK 25 via the reusable workflow
in `jruk8/plugin-conventions/.github/workflows/build.yml`:

```shell
./gradlew checkstyleMain
```

## Project structure

Main code lives in `src/main/java/com/jruk8/jblockglitch`:

- `commands/` — command implementations and `CommandBootstrap`
- `listeners/` — event listeners, `ModeService`, and `ListenerBootstrap`
- Root package — plugin entry point (`JBlockGlitchPlugin`) and shared services
  such as `MessageService`

Subsystems implement the `Bootstrap` interface and register their components
in `register()`. New commands go in `commands/`, new listeners in `listeners/`,
and shared logic in the root package.

## Conventions

- Java 25 (configured by the `com.jruk8.plugin-conventions` convention plugin)
- Follow the checkstyle rules bundled in the convention plugin: no star
  imports, no unused imports, braces required, max line length 120
- Plugin metadata (name, version, main class, etc.) is defined in
  `gradle.properties` and expanded into `src/main/resources/plugin.yml` at
  build time — don't hardcode it in `plugin.yml`

## User-facing changes

- When adding config options or messages, update `src/main/resources/config.yml`
  and `messages.yml` and make sure they're picked up by the reload cycle in
  `JBlockGlitchPlugin.reload()`
- Update the README when setup or user-facing behavior changes.