# TrapRescue Changelog

## [Unreleased]

### Added
- Basic mod structure: `TrapRescueMod.java`, `Config.java`, `Reference.java`.
- Command `/trap-rescue-admin` with subcommands:
  - `rescue <player>` (stub, not functional)
  - `rescue <player> <x> <y> <z> [dim]`
  - `blacklist add <player>`
  - `blacklist remove <player>`
  - `blacklist list`
  - `config status`
  - `config reload`
  - `help`
- `RescueService` with online player manual teleport support.
- `RescueLogger` for logging rescue operations.
- `OpnResult` utility class for success/failure messaging.
- Player blacklist stored in Forge config (`traprescue.cfg`), editable via commands, persistent.
- Config toggle `enabled` to disable the mod entirely, returning a clear error message.
- Offline rescue design: `PlayerDataManager` to read/write `player.dat`, chunk loading, auto safe-spot search (not yet implemented).
- `SafeLocationFinder` placeholder (not yet implemented).
- Repository documentation: `README.md`, `mcmod.info`, license (AGPL-3.0).
- Server-side only, no GUI.
