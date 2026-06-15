// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue;

import java.io.File;
import java.util.Arrays;
import net.minecraftforge.common.config.Configuration;

import static org.swarg.mc.traprescue.Reference.*;
import static org.swarg.mc.traprescue.RescueLogger.*;

public class Config {
    private static volatile Config instance;

    public Configuration forgeConfig;
    public boolean enabled = true;
    public boolean verbose;
    public String[] playerBlacklist;

    private static final String C_BLACKLIST =
            "Player names that cannot be rescued.";

    private static final String C_ENABLED =
            "Set to false to completely disable the TrapRescue mod. ";

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
}
