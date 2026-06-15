# TrapRescue

Forge mod for Minecraft 1.7.10. Rescue players trapped inside lost claims. Works on online and offline players. Manual teleport or auto safe-spot finder in the same chunk. Works on offline players via direct .dat editing. For servers with no /tp commands. (Server Side Only)

## Commands

All commands require OP level 4 or server console.

- `/trap-rescue-admin rescue <player>`
  Find a safe exit spot in the player's current chunk and teleport them there.

- `/trap-rescue-admin rescue manual <player> <x> <y> <z> [<dim>]`
  Teleport the player to the given coordinates. No auto safe-spot search.

- `/trap-rescue-admin blacklist add <player>`
  Prevent a player from being rescued.

- `/trap-rescue-admin blacklist remove <player>`
  Allow a previously blacklisted player to be rescued again.

- `/trap-rescue-admin blacklist list`
  Show the current blacklist.

- `/trap-rescue-admin config status`
  Print whether the mod is enabled or disabled.

- `/trap-rescue-admin config reload`
  Reload the configuration from disk.

- `/trap-rescue-admin player uuid <player>`
  Look up the UUID of a player.

- `/trap-rescue-admin player pos <player>`
  Show the last known position and dimension of a player (from their .dat file).

- `/trap-rescue-admin help`
  Print help.

## Config

File: `config/traprescue.cfg`

- `enabled` — Set to false to completely disable the mod. Default true.
- `blacklist` — List of player names that cannot be rescued.

## Offline rescue

If the target player is offline, the mod edits the player's `.dat` file directly and rewrites the player's position. No player login required. Manual mode is fully supported. Auto safe-spot detection for offline players is planned.

## Installation

Place the `.jar` in the `mods` folder. Requires Forge for 1.7.10.

## License
AGPL-3.0-only. See [LICENSE](LICENSE).
