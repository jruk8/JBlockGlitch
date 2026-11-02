---
icon: material/tune
---

# Configuration

The plugin writes `config.yml`, `messages.yml` and `sounds.yml` into its data
folder (`plugins/JBlockGlitch/`) on first start. Edit a file and run
`/jblockglitch reload` to apply the changes without restarting the server.
Missing keys are re-added with defaults; unknown keys are removed.

## config.yml

```yaml
updateChecker:
  enabled: true
  releases:
    major: true
    minor: true
    hotfix: true
aliases:
  - jbg
defaultLanguage: auto
protectedBlock:
  enabled: true
  mode: strict
ghostItem:
  enabled: true
  mode: medium
  resyncTickInterval: 5
colors:
  plainColor: '<#e685b5>'
  brandColor: '<#e68595>'
  highlightColor: '<#85b5e6>'
  errorColor: '<#e68585>'
metrics:
  enabled: true
```

### `updateChecker`

- `enabled` » checks GitHub releases for new versions on enable and on
  reload. The check runs asynchronously; findings go to the console and
  in-game to players holding `jblockglitch.reload` (on join and via
  `/jblockglitch version`). Update notices link both the GitHub release and
  the Modrinth project page.
- `releases.major` / `releases.minor` / `releases.hotfix` » a release
  only notifies when its severity over the running version is enabled
  here.

### `aliases`

Extra labels for the `/jblockglitch` root command (`/jbg` works out of
the box). Running the root with no arguments shows the help listing.
Blanks, duplicates and the `jblockglitch` label itself are ignored.
Applies on restart; `/jblockglitch reload` reports when a restart is
needed.

### `defaultLanguage`

Default language for players without a language pin: `auto` follows
each player's client locale, any other [language key](#languages)
forces that language. Unknown keys warn and behave as `auto`. Takes
effect on reload.

### `protectedBlock`

- `enabled` » resend the real block state and stop movement through
  denied placements.
- `mode` » `medium` rubberbands the player to their position on the
  last tick; `strict` teleports the player to the ground using advanced
  detection. Unknown values fall back to `strict`.

### `ghostItem`

- `enabled` » detect ghost item attempts and force a client inventory
  resync.
- `mode` » `medium` watches the well-known event patterns (recommended);
  `hard` additionally monitors every inventory interaction including
  creative mode (prevents MC-277905); `brute-force` resyncs every
  inventory every tick (laggy). Unknown values fall back to `medium`.
- `resyncTickInterval` » how often the blocks around each player
  resync, in ticks. Only players who moved since the previous resync
  are processed, so stationary players cost nothing. `-1` disables the
  resync entirely.

### `colors`

The message color palette. Every `messages.yml` entry uses these via
`{plain}`, `{brand}`, `{hl}` and `{error}`, resolved when the message is
rendered; editing a color and reloading is enough, the messages
themselves never hardcode colors. All four share one saturation and
value and differ in hue only: pastel pink body text, a rose brand tone
matching the prefix gradient, pastel blue highlights, and a soft red
for errors.

### `metrics`

- `enabled` » sends anonymous usage statistics via bStats (open-source,
  no personal data or IP addresses). Read once at startup: disabling it
  needs a server restart to take effect.

### Upgrading from an older config

The schema moved from kebab-case to camelCase sections, `sounds` moved
to `sounds.yml`, and `text-format` was removed (messages are always
MiniMessage now). The old keys map to the new ones as follows:

| Old key | New key |
| --- | --- |
| `toggle-anonymous-statistics` | `metrics.enabled` |
| `protected-block.enabled` | `protectedBlock.enabled` |
| `protected-block.mode` | `protectedBlock.mode` |
| `ghost-item.enabled` | `ghostItem.enabled` |
| `ghost-item.mode` | `ghostItem.mode` |
| `ghost-item.resync-tick-interval` | `ghostItem.resyncTickInterval` |
| `update-checker.enabled` | `updateChecker.enabled` |
| `update-checker.releases.major` | `updateChecker.releases.major` |
| `update-checker.releases.minor` | `updateChecker.releases.minor` |
| `update-checker.releases.hotfix` | `updateChecker.releases.hotfix` |

## messages.yml

The plugin messages, always in MiniMessage format. Every message starts
with `{prefix}` (which includes its trailing space), and colors come
from the `config.yml` palette. Placeholders use `{braces}`; angle
brackets are raw MiniMessage, and `\n` breaks the line:

- `prefix` » prefix prepended via the `{prefix}` placeholder. The
  default renders `[JBlockGlitch]` in a `#DB6D77` to `#E08A8A`
  gradient.
- `core.reloadSuccess` / `core.reloadFailure` » reload outcome
  (use `{time}`).
- `core.reloadRestartRequired` » shown when root labels changed and need
  a restart to apply.
- `core.helpHeader` / `core.helpEntryFormat` » help command layout
  (use `{version}` for the header; `{syntax}` and `{description}` per entry).
- `core.helpNoMatch` » help query matched no commands.
- `core.helpNoCommands` » no commands are visible to the sender.
- `core.versionSuccess` » version command output (use `{version}` and
  `{authors}`; linked author names arrive pre-formatted).
- `core.updateAvailable` » update notice (use `{latest}`, `{current}`,
  `{url}` and `{modrinth-url}`).
- `core.noPermission` » the sender lacks a permission (use `{permission}`).
- `core.linkModrinth` / `core.linkGithub` / `core.linkDiscord` /
  `core.linkDonate` » link button templates (use `{url}`); the
  messenger injects them per recipient so labels and tooltips follow
  the reader's language.
- `core.versionComma` / `core.versionAnd` » joining words between
  version authors.
- `config.header` / `config.entry` » config listing layout
  (use `{key}` and `{value}` per entry).
- `config.updated` » a value was set (use `{key}` and `{value}`).
- `config.unknownKey` » unknown config key (use `{key}` and `{keys}`).
- `config.invalidValue` » invalid value (use `{key}`, `{value}` and
  `{allowed}`).
- `help.help` / `help.version` / `help.reload` / `help.config` »
  help menu descriptions per command.

Messages can embed PlaceholderAPI placeholders (`%player_name%`) when
PlaceholderAPI is installed.

## Languages

Messages are per player, never per server: every recipient reads the
bundle matching their own language. Without a pin the language follows
the client's locale live. English is the fallback everywhere: console,
unknown locales, missing files and missing keys all read `messages.yml`.
Each used language seeds its `lang/<code>.yml` on first use; the files
stay admin-editable and overlay the English schema, so deleting a key
(or a whole file) just reads English again. Only English ships with the
plugin: translate `messages.yml` into `lang/<code>.yml` to add a
language.

The `defaultLanguage` key in `config.yml` decides what players read:
`auto` (the default) keeps following each player's client locale,
while any other key forces that language on everyone. Unknown keys
warn in the console and behave as `auto`.

| Code | Language | Code | Language |
| --- | --- | --- | --- |
| `en` | English | `da` | Dansk |
| `fi` | Suomi | `cs` | Čeština |
| `de` | Deutsch | `hu` | Magyar |
| `es` | Español | `vi` | Tiếng Việt |
| `fr` | Français | `id` | Bahasa Indonesia |
| `zh_cn` | 简体中文 | `ar` | العربية |
| `ru` | Русский | `hi` | हिन्दी |
| `pt_br` | Português | `el` | Ελληνικά |
| `ja` | 日本語 | `sk` | Slovenčina |
| `pl` | Polski | `ro` | Română |
| `tr` | Türkçe | `bg` | Български |
| `it` | Italiano | `he` | עברית |
| `ko` | 한국어 | `th` | ไทย |
| `nl` | Nederlands | `ms` | Bahasa Melayu |
| `uk` | Українська | `lt` | Lietuvių |
| `sv` | Svenska | `lv` | Latviešu |
| `no` | Norsk | `et` | Eesti |
| `pt_pt` | Português | `hr` | Hrvatski |
| `zh_tw` | 繁體中文 | `sr` | Српски |
| `ca` | Català | `bs` | Bosanski |
| `gl` | Galego | `sl` | Slovenščina |
| `is` | Íslenska | `mk` | Македонски |
| `ga` | Gaeilge | `ka` | ქართული |
| `hy` | Հայերեն | `az` | Azərbaycanca |
| `kk` | Қазақша | `lol_us` | LOLCAT |
| `en_pt` | Pirate Speak | `qya_aa` | Quenya |

## sounds.yml

Sound effects for command feedback. Each entry supports `enabled`, a
namespaced `sound` key, `pitch` and `volume`
([sound explorer](https://jruk8.github.io/JSoundExplorer/)):

- `neutral` » neutral feedback for ordinary commands, heard by the executor
  and every affected player.
- `celebration` » played for reloads, help and version output.
- `angry` » played on failed commands (invalid input, missing permission,
  failed reload).
