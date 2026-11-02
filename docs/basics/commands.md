---
icon: material/console
---

# Commands

All commands are available under `/jblockglitch` and its aliases
(default `/jbg`, see `aliases` in
[configuration.md](../configuration.md)). Running the root with no
arguments shows the help listing.

| Command | Description | Permission |
| --- | --- | --- |
| `/jblockglitch help [query]` | Shows the in-game command list. | `jblockglitch.help` |
| `/jblockglitch version` | Shows the plugin version and authors. | `jblockglitch.version` |
| `/jblockglitch reload` | Reloads all configuration files. | `jblockglitch.reload` |
| `/jblockglitch config [key] [value]` | Views or changes settings in-game. | `jblockglitch.config` |

- **`help`** supports an optional query argument that filters the listed
  commands (`/jblockglitch help reload`). Entries are filtered by permission,
  so every sender only sees what they may use.
- **`version`** prints the plugin's version and author list with links,
  and repeats a pending update notice to admins.
- **`reload`** re-reads `config.yml`, `messages.yml` and `sounds.yml`,
  then re-caches the detection modes, re-registers the listeners and
  re-checks for updates with the new settings. Changed root labels need
  a restart; `reload` says so when it detects a change.
- **`config`** lists every tunable key, shows one value
  (`/jblockglitch config ghostItem.mode`), or validates, saves and
  applies a new value live
  (`/jblockglitch config ghostItem.mode hard`). Keys and values tab
  complete. `metrics.enabled` still needs a restart (bStats cannot be
  toggled at runtime).

See [permissions.md](../advanced/permissions.md) for defaults.

## Adding a Command

1. Create a command class in a new subpackage of `command` (e.g.
   `command.user.fun`) that builds a Brigadier node with
   `Commands.literal(...)`, gates it with `requires()`, and runs its logic
   in `executes(...)`.
2. Attach the node to the root in the matching registrar
   (`UserCommandsRegistrar` or `AdminCommandsRegistrar`) with
   `root.then(new MyCommand(...).node())`, injecting only the services it
   needs (commonly `CommandContext`).
3. Declare the permission in `paper-plugin.yml` (with a constant in
   `Permissions`), document it in [permissions.md](../advanced/permissions.md)
   and add a `HelpEntry` so the command shows up in `/jblockglitch help`.

See [extending.md](../advanced/extending.md) for the full walkthrough.
