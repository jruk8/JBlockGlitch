---
icon: material/key
---

# Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `jblockglitch.help` | Use `/jblockglitch help` (entries are permission-filtered). | Everyone |
| `jblockglitch.version` | Use `/jblockglitch version`. | Everyone |
| `jblockglitch.reload` | Use `/jblockglitch reload`. Also receives update notices. | OP |
| `jblockglitch.config` | Use `/jblockglitch config`. | OP |

Permissions are checked on the command nodes with Brigadier `requires()`
predicates (see `CommandContext.requiresPermission`); the node constants
live in `Permissions`. Update notices (join message and the
`/jblockglitch version` line) go to `jblockglitch.reload` holders; the
old `jblockglitch.admin` node no longer exists. See
[commands.md](../basics/commands.md).
