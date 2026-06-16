# TrapRescue Changelog

## [Unreleased]

## [0.3.0] - 2026-06-16
### Added
- Safe spots: named teleport destinations with radius, stored in config.
- Commands for managing safe spots: `/tra safespot <add/remove/list/get/check/rename>`.
- `/tra safespot check <player>` command to see which safe spot covers a player (online/offline).
- Support for rescue by safe spot name: `/trap-rescue-admin rescue <player> <safespot_name>`.
- Auto-rescue: if player is inside a safe spot radius, they are teleported to its center.
- `OpResult` extended with `data` payload to carry objects alongside success/failure.
- `Config.findSafeSpotForPosition` for nearest safe spot lookup.
- `RescueService.findRescueTarget` unifying target search for online and offline.
- Minimum radius check (5 blocks) when adding safe spots.
- Safespot rename command.
- README sections: "Why", planned auto-exit detection, clarification of radius-based rescue.
### Changed
- `RescueService` now takes `SafeSpot` instead of raw coordinates.
- `dim` parameter simplified to `int` with default 0 in commands.
- `PlayerDataManager.getPlayerNBTData` renamed to `resolveAndLoadPlayerData`, returns `OpResult` with `PlayerDataResolve`.
- `rescueOnlineAuto` and `rescueOfflineAuto` fully implemented using safe spot search.

## [0.2.0] - 2026-06-15
### Added
- Offline manual rescue: teleport offline players via direct `.dat` editing.
- `PlayerDataManager`: UUID resolution (profile cache + Forge UsernameCache fallback), `.dat` load/save, get/set `Pos`, `Dimension`, world spawn.
- Command `/trap-rescue-admin player uuid <name>` and `player pos <name>` for debugging.
- `RescueService.rescue(String playerName)` for auto-mode entry point (stub).
### Changed
- `rescueManual` dispatcher now routes to offline if player not online.
- `sayResult` simplified (no player name parameter).

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
