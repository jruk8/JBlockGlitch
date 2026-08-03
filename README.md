![JBlockGlitch banner](JBlockGlitch-banner-1280x640.png)
# JBlockGlitch

JBlockGlitch is a lightweight Paper plugin that prevents players 
from creating ghost blocks and from block glitching in protected 
regions.

Download: https://modrinth.com/plugin/jblockglitch

Contribute: https://github.com/jruk8/JBlockGlitch

## Requirements

- Paper 26.x (tested on 26.2+)
- Java 25

## Purpose

The plugin contains two modes: **protect-based block item**, and **vanilla ghost 
item** glitch prevention. Pairing these two modes together, JBlockGlitch 
practically prevents any sort of block glitching from happening. This
makes it the most effective block glitch prevention plugin for modern
Paper servers.

### Protect-based block glitch prevention
When any plugin, be it WorldGuard or GriefProtector, the plugin immediately 
resends the real block state to the player and briefly prevents the upward
movement that can otherwise be triggered by the client-side ghost block.

### Vanilla ghost item glitch prevention
When a player attempts a vanilla ghost item creation (e.g., F+Q in the
same tick), the plugin will prevent any ghost item, intentional or otherwise,
from being created.

## Demos

Plugin configured to `medium` detection mode. Tests done with autoclicker at 100 CPS.

### Demonstration without the fix

This 10-second demonstration shows the block-placement glitch before
JBlockGlitch is installed.

![Block glitch without JBlockGlitch](demos/without-fix.gif)

### Demonstration with the fix

This 10-second demonstration shows the same placement attempt with
JBlockGlitch installed.

![Block glitch fixed by JBlockGlitch](demos/with-fix.gif)

## Commands

| Command | Permission | Notes |
| --- | --- | --- |
| `/jblockglitch help` | `jblockglitch.help` | Shows basic plugin documentation. |
| `/jblockglitch reload` | `jblockglitch.reload` | Reloads `config.yml` and `messages.yml`. |
| `/jbg` | `jblockglitch.help` | Short alias for `/jblockglitch`. |

The help message is configured in `messages.yml` as a multiline list. Messages
use MiniMessage formatting by default. Set `text-format: legacy` in
`config.yml` to use legacy `&` color codes instead.

`Config.yml` provides options for changing detection mode for each of the
two detection engines. If issues occur, regenerate all files, download the
latest version. If the issue persists, report to the issue tracker.

See [CONTRIBUTING.md](CONTRIBUTING.md) for build instructions and contributor
setup.

© 2026 jruk8. Licensed under GNU GPLv3.
