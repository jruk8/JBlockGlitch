---
icon: material/wrench
---

# Extending JBlockGlitch

This page explains where new code plugs in. Every module follows the same
shape: a top-level package under `com.jruk8.jblockglitch`, its own config class
(or section), and a registrar implementing `Bootstrap` that is invoked from
`JBlockGlitchPlugin.setupCoreModules()`.

Dependencies flow one way, constructor-injected: configs -> services ->
commands/listeners. There are no singletons or static state.

## Add a Command

1. Create a package `command.user.<name>` (or `admin`) with a command
   class that builds a Brigadier node:

   ```java
   public class PingCommand {
       private final CommandContext ctx;

       public PingCommand(CommandContext ctx) { this.ctx = ctx; }

       public LiteralArgumentBuilder<CommandSourceStack> node() {
           return Commands.literal("ping")
                   .requires(CommandContext.requiresPermission(Permissions.PING))
                   .executes(context -> {
                       // Own text lives in MessagesConfig; {braces} resolve from the map.
                       ctx.sendMessage(context.getSource(),
                               messages -> messages.getCore().getHelpHeader(), Map.of());
                       return Command.SINGLE_SUCCESS;
                   });
       }
   }
   ```

   Commands are Paper-native Brigadier nodes registered through
   `LifecycleEvents.COMMANDS` (see `CommandsRegistrar`). The root literal
   (`jblockglitch`, plus its configured aliases) carries a bare-root help
   fallback, so a new command only defines its own subcommand node.
   Messages are strictly MiniMessage: plugin placeholders use `{braces}`,
   angle brackets are raw MiniMessage.

2. Register it in the matching registrar
   (`UserCommandsRegistrar.register()`):
   `root.then(new PingCommand(ctx).node());`

3. Declare the permission in `paper-plugin.yml` (with a constant in
   `Permissions`) and document the command in `docs/basics/commands.md`
   and `docs/advanced/permissions.md`. Add a `HelpEntry` in `HelpCommand`
   so `/jblockglitch help` lists it.

## Add a Config Option

The config classes are okaeri-configs. For a small option, add a field to an
existing config class; for a coherent new section, add a nested static class
and give it a field in the parent (see `PluginConfig.Colors` for the
pattern). The section will be created in the yaml file automatically and
reloaded by `ConfigRegistrar` on `/jblockglitch reload`.

To make the option tunable in-game, add a `ConfigKey` entry in
`ConfigCommand` with read/write accessors against `PluginConfig`:
`/jbg config` picks the key up with validation and tab completion.

## Add a Detection Mode

Detection behavior lives in the `listeners` package behind `ModeService`,
which caches the parsed modes from `PluginConfig` and re-reads them on
reload. To add a mode:

1. Extend the mode enum in `ModeService` and its parser (unknown values
   fall back to the current default).
2. Branch on the new mode in the owning listener.
3. Expose the value in the matching `ConfigCommand` key suggestions and
   document it in `docs/configuration.md`.

## Modules Reference

| Package | Key classes |
| --- | --- |
| `command` | `CommandsRegistrar`, `UserCommandsRegistrar`, `AdminCommandsRegistrar`, `CommandContext`, `Permissions` |
| `command.user` | `HelpCommand`, `HelpEntry`, `VersionCommand`, `AuthorFormatter` |
| `command.admin` | `ReloadCommand`, `ConfigCommand` |
| `config` | `ConfigRegistrar`, `PluginConfig` |
| `message` | `MessageBootstrap`, `Messenger`, `MessageParser`, `MessageBundles`, `Languages` |
| `sound` | `SoundPlayer`, `SoundsConfig` |
| `listeners` | `ListenerBootstrap`, `ModeService`, `ProtectedBlockListener`, `GhostItemListener`, `GhostResyncer` |
| `update` | `UpdateCheckerBootstrap`, `UpdateCheckService`, `GitHubReleaseHttp`, `UpdateCheckNotifier`, `UpdateCheckJoinListener` |
