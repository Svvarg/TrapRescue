// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.swarg.mc.traprescue.data.SafeSpot;
import net.minecraftforge.common.config.Configuration;

import static org.swarg.mc.traprescue.Reference.*;
import static org.swarg.mc.traprescue.RescueLogger.*;

public class Config {
    private static volatile Config instance;

    public Configuration forgeConfig;
    public boolean enabled = true;
    public boolean verbose;
    public String[] playerBlacklist;
    private List<SafeSpot> safeSpots = new ArrayList<>();


    private static final String C_BLACKLIST =
            "Player names that cannot be rescued.";

    private static final String C_ENABLED =
            "Set to false to completely disable the TrapRescue mod. ";

    private static final String C_SAFE_SPOTS =
            "Named safe spots. Format: " + SafeSpot.FORMAT;

    public static Config instance() {
        if (instance == null) throw new IllegalStateException("Config not initialized");
        return instance;
    }

    private Config(Configuration forgeConfig) {
       this.forgeConfig = forgeConfig;
    }

    public static Config preInit(File configDir) {
		if (instance != null) {
            throw new IllegalStateException("Preinit can't be called twice.");
        }
        File confFile = new File(configDir, MOD_ID + ".cfg");
        Configuration fcfg = new Configuration(confFile);
        LOG.info("{}-{}: preInit Config from: {}", MOD_ID, MOD_VERSION, confFile);
        instance = new Config(fcfg);

        return instance;
    }

    public Config reload(boolean forced) {
        LOG.info("[{}]: Config reloading...", MOD_ID);

        final Configuration c = instance.forgeConfig;
        if (forced) c.load();

        enabled = c.getBoolean("enabled", "main", true, C_ENABLED);

        playerBlacklist = c.getStringList("blacklist", "main",
                new String[] {}, C_BLACKLIST);

        safeSpots = loadSafeSpots(c);

		if (c.hasChanged()) c.save(); // to save default values

        return instance;
    }

    // save to disk
    private void updateBlackList() {
        forgeConfig.get("main", "blacklist", new String[] {}, C_BLACKLIST)
                .set(this.playerBlacklist);
        forgeConfig.save();
    }

    public String readable() {
        return String.format("Enabled: %s\n", this.enabled);
    }

    public String[] getBlacklist() {
        return playerBlacklist != null ? playerBlacklist : new String[0];
    }

    public boolean isPlayerBlacklisted(String playerName) {
        if (playerBlacklist != null && playerBlacklist.length > 0) {
            for (String banned : playerBlacklist) {
                if (banned.equalsIgnoreCase(playerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addToBlacklist(String playerName) {
        if (playerName == null || playerName.length() < 3) {
            return false;
        }
        String[] updated;
        String[] current = playerBlacklist;
        if (current != null && current.length > 0) {
            for (String s : current) {
                if (s.equalsIgnoreCase(playerName)) return false;
            }
            updated = Arrays.copyOf(current, current.length + 1);
        } else {
            updated = new String[] {playerName};
        }
        updated[current.length] = playerName;
        playerBlacklist = updated;
        updateBlackList();
        return true;
    }

    public boolean removeFromBlacklist(String pname) {
        String[] current = playerBlacklist;
        if (current == null || current.length == 0 || !isPlayerBlacklisted(pname)) {
            return false;
        }
        String[] updated = new String[current.length -1];
        int i = 0;
        for (String name : current) {
            if (!name.equalsIgnoreCase(pname)) {
                updated[i++] = name;
            }
        }
        playerBlacklist = updated;
        updateBlackList();
        return true;
    }

    public String readableBlacklist() {
        StringBuilder sb = new StringBuilder();
        if (this.playerBlacklist != null) {
            for (String name : this.playerBlacklist) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(name);
            }
        }
        return sb.toString();
    }

    private List<SafeSpot> loadSafeSpots(Configuration c) {
        String[] entries = c.getStringList("entries", "safe_spots",
                new String[] {}, C_SAFE_SPOTS);

        Map<String, SafeSpot> map = new HashMap<>();
        for (String entry : entries) {
            SafeSpot spot = SafeSpot.deserialize(entry);
            if (spot != null) {
                if (map.containsKey(spot.name)) {
                    logWarn("Duplicate safe spot name '" + spot.name + "'.");
                }
                map.put(spot.name, spot);
            }
        }
        return new ArrayList<>(map.values());
    }

    public int getSafeSpotIndexByName(String name) {
        if (name != null && !name.isEmpty()) {
            for (int i = 0; i < this.safeSpots.size(); i++) {
                SafeSpot spot = this.safeSpots.get(i);
                if (name.equalsIgnoreCase(spot.name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public SafeSpot getSafeSpot(String name) {
        int i = getSafeSpotIndexByName(name);
        return i > -1 ? this.safeSpots.get(i) : null;
    }

    public Collection<SafeSpot> getAllSafeSpots() {
        return Collections.unmodifiableCollection(safeSpots);
    }

    public Integer getIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
        }
        return null;
    }

    public OpResult addSafeSpot(String name, int x, int y, int z, int dim, int radius) {
        String key = name == null ? null : name.trim().toLowerCase();
        if (y < 0 || y > 255 || radius < 0 || key == null || key.isEmpty()) {
            return OpResult.fail("invalid params");
        }
        if (getIntOrNull(key) != null || key.indexOf(":") > -1) {
            return OpResult.fail("invalid name (should not be a number)");
        }
        int i = getSafeSpotIndexByName(key);
        if (i > -1) {
            logWarn("Safe spot '" + key + "' already exists. Overwriting.");
            this.safeSpots.remove(i);
        }
        SafeSpot spot = new SafeSpot(key, x, y, z, dim, radius);
        safeSpots.add(spot);
        writeSafeSpotsToConfig();
        return OpResult.ok("added");
    }

    public SafeSpot removeSafeSpot(String name) {
        String key = name.toLowerCase().trim();
        int i = getSafeSpotIndexByName(key);
        if (i > -1) {
            SafeSpot removed = safeSpots.remove(i);
            writeSafeSpotsToConfig();
            return removed;
        }
        return null;
    }

    private void writeSafeSpotsToConfig() {
        String[] lines = new String[safeSpots.size()];
        int i = 0;
        for (SafeSpot spot : safeSpots) {
            lines[i++] = spot.serialize();
        }
        forgeConfig.get("safe_spots", "entries", new String[] {}, C_SAFE_SPOTS)
            .set(lines);
        forgeConfig.save();
    }

    public String getReadableSafeSpots() {
        StringBuilder sb = new StringBuilder();
        for (SafeSpot spot : safeSpots) {
            sb.append(spot.serialize()).append("\n");
        }
        return sb.toString();
    }

    public SafeSpot findSafeSpotForPosition(int x, int y, int z, int dim) {
        SafeSpot best = null;
        int bestDistSq = Integer.MAX_VALUE;
        for (SafeSpot spot : safeSpots) {
            if (spot.dim == dim) {
                int dx = x - spot.x;
                int dy = y - spot.y;
                int dz = z - spot.z;
                int distSq = dx * dx + dy * dy + dz * dz;
                if (distSq <= spot.radius * spot.radius && distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = spot;
                }
            }
        }
        return best;
    }
}
