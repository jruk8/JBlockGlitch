<div align="center">

![JBlockGlitch banner](https://raw.githubusercontent.com/jruk8/JBlockGlitch/main/docs/assets/banner-1280x640.png)

<h3>Stops ghost items and block glitches in protected regions.</h3>

### [Modrinth](https://modrinth.com/plugin/jblockglitch) · [Wiki](https://jruk8.github.io/JBlockGlitch/) · [GitHub](https://github.com/jruk8/JBlockGlitch)

</div>

# Features

- **Protected-block prevention** » resends the real block state when a
  protection plugin denies a placement, and stops movement through the
  ghost block (`medium` and `strict` modes).
- **Ghost item prevention** » detects vanilla ghost item techniques and
  forces a client resync (`medium`, `hard` and `brute-force` modes).
- **Ghost block resync** » periodically resends the blocks around
  moving players to clear client-side ghosts.
- **Brigadier commands** » Paper-native nodes with permission gates,
  config-driven root aliases, and a bare-root help listing.
- **MiniMessage everywhere** » `{braces}` for plugin placeholders,
  angle brackets reserved for raw MiniMessage, shared color palette.
- **Sounds** » config-driven Adventure sounds per feedback event.
- **Update checker** » async GitHub releases check (console log, join
  notice, `/version` line), with per-severity toggles.
- **Reloadable** » `/jblockglitch reload` re-reads every config and
  re-registers listeners without a restart.

### Requirements

- Server Software » [Paper 26.2+](https://papermc.io/downloads/paper)
- Java Version » **Java 25**
- Dependencies » **None** (PlaceholderAPI is optional, only for
  `%placeholders%` inside the plugin messages)

### Commands

| Command | Description |
| --- | --- |
| `/jblockglitch <help\|version\|reload\|config>` | Help, version, config reload, and in-game settings (alias `/jbg`). |

See the full [Commands Page](https://jruk8.github.io/JBlockGlitch/basics/commands/) for more info.

### Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `jblockglitch.help` | Use `/jblockglitch help`. | Everyone |
| `jblockglitch.version` | Use `/jblockglitch version`. | Everyone |
| `jblockglitch.reload` | Use `/jblockglitch reload`. Also receives update notices. | OP |
| `jblockglitch.config` | Use `/jblockglitch config`. | OP |

### Config

Find `config.yml`, `messages.yml` and `sounds.yml` in `plugins/JBlockGlitch/`:

- `updateChecker` » checks for updates, with per-severity toggles.
- `aliases` » aliases for `/jbg` root command (needs **R**estart).
- `defaultLanguage` » `auto` follows each client, or one forced language.
- `protectedBlock` » denied-placement handling (`medium`, `strict`).
- `ghostItem` » ghost item detection (`medium`, `hard`, `brute-force`)
  and the nearby-block resync interval.
- `colors` » color palette for messages.
- `metrics` » anonymous bStats stats (**R**).
- `messages.yml` / `sounds.yml` » messages and sounds.
- `lang/<code>.yml` » per-language overlays, admin-editable (English
  fallback for missing keys).

See the full [Configuration Page](https://jruk8.github.io/JBlockGlitch/configuration/) for more info.

# Links

- [Documentation](https://jruk8.github.io/JBlockGlitch/)
- [Discord](https://discord.com/invite/hkWmCVmWDC)
- [GitHub](https://github.com/jruk8/JBlockGlitch)

### Donate ☕

If you like my plugins, you can support my work through a [**small donation**](https://ko-fi.com/jruk). **Thanks!** 👐
