# TrapRescue

Forge mod for Minecraft 1.7.10. Rescue players trapped inside lost claims. Works on online and offline players via direct .dat editing. Manual teleport (to raw coordinates or a named safe spot) and auto rescue to the center of a pre‑configured safe spot when the player is within its radius. Safe spots are defined in the config or added via commands. For servers with no /tp commands. (Server Side Only)

## Why

Players on survival servers can lose access to their own claims (e.g., unpaid tax). The claim becomes a trap: no teleport commands, no way out. Admins need a tool to move a trapped player without altering the world's "no teleport" rule. TrapRescue provides exactly that - a console-only, non‑cheat way to free a stuck player while keeping the server's physics intact. Works even if the player is offline, by directly editing the player's `.dat` file.

## How It Works

Typical case: player `Steve` lost access to his claim due to tax non‑payment. His house is now a trap. Steve is offline, stuck inside. Admin wants to free him without teleporting to spawn.

### Steps
1. Admin defines a safe spot near the trapped area:
   ```
   /tra safespot add exit_north 150 64 180 0 4096
   ```
   This creates a 4096‑block radius rescue point at x=150 y=64 z=180 in the Overworld (dim 0).

2. Admin checks if Steve is inside the safe spot radius (optional):
   ```
   /tra safespot check Steve
   ```
  Output shows whether Steve is inside a safe spot (and which one). Works for both online and offline players.

3. Admin runs auto‑rescue:
   ```
   /tra rescue Steve
   ```

### What the mod does
- If Steve is online: directly teleports him to the safe spot center.
- If Steve is offline: loads his `.dat` file, reads current coordinates, checks if they fall within any safe spot radius.
- If a safe spot is found: writes new coordinates into `.dat`, logs old and new position.
- If no safe spot matches: command fails with a clear message (planned: automatic roof/edge search).

### Result
Steve logs in later and appears exactly at the safe spot, one step away from leaving the trapped chunk. No damage, no items lost. The admin never entered the game.

## Commands

All commands require OP level 4 or server console.

- `/trap-rescue-admin rescue <player>`
  Find a safe exit spot in the player's current chunk and teleport them there.

- `/trap-rescue-admin rescue <player> <x> <y> <z> [<dim>]`
  Teleport the player to the given coordinates. No safe-spot search.
  `/trap-rescue-admin rescue <player> <safespot_name>`
  Teleport the player to the named safe spot coordinates (from configuration).

- `/trap-rescue-admin safespot add <name> <x> <y> <z> <dim> <radius>`
  Add a named safe spot to configuration. Radius must be at least 5 blocks.
- `/trap-rescue-admin safespot remove <name>`
  Remove a named safe spot.
- `/trap-rescue-admin safespot list`
  List all safe spots.
- `/trap-rescue-admin safespot get <name>`
  Show coordinates and radius of a safe spot.

- `/trap-rescue-admin safespot check <player>`
  Check which safe spot covers a player's current position

- `/trap-rescue-admin safespot rename <old-name> <new-name>`
  Rename an existing safe spot

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
- `safe_spots` — Pre‑configured rescue points.
- `blacklist` — List of player names that cannot be rescued.

### SafeSpots

Pre‑configured rescue targets with a radius trigger.
Auto‑rescue teleports any player (online or offline) inside the radius to the spot's center.
Manual rescue accepts a spot name instead of raw coordinates.
Managed via `/tra safespot` or directly in `traprescue.cfg`.

Configuration format per entry: `name:x:y:z:dim:radius`. All values are integers. `radius` is in blocks (minimum 5).
Safe spots are used by the `auto` rescue mode and by `rescue <player> <safespot_name>`.
Spots can also be added/removed/renamed in‑game via the `/tra safespot` command.
Example:
```
safe_spots {
    S:entries <
        overworld_escape:0:100:0:0:10000
        nether_escape:0:70:0:-1:10000
    >
}
```

## Offline rescue

If the target player is offline, the mod edits the player's `.dat` file directly and rewrites the player's position. No player login required. Manual and auto (safe spot) rescue modes work for offline players.

## Auto-Exit Detection (Planned)

When the player is not within the radius of any safe spot, the auto‑rescue will find an exit point directly:
- For a closed house: the player is moved to the highest solid surface in the chunk with open sky above.
- For a pit: the player is moved to the edge of the pit (nearby walkable block towards the chunk border).
The goal is to put the player in a position where a single step takes them out of the trapped area, without breaking the server's "no teleport" rule.
Implementation is planned for a future version.

## Installation

Place the `.jar` in the `mods` folder. Requires Forge for 1.7.10.

## License
AGPL-3.0-only. See [LICENSE](LICENSE).
