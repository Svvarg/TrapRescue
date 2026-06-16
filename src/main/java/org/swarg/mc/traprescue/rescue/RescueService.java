// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.rescue;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.swarg.mc.traprescue.OpResult;
import org.swarg.mc.traprescue.data.PlayerDataManager;
import org.swarg.mc.traprescue.data.SafeSpot;
import java.io.File;
import java.util.UUID;
import org.swarg.mc.traprescue.Config;
import static org.swarg.mc.traprescue.RescueLogger.logInfo;

/**
 * Entry point for all rescue operations.
 * Dispatches to online (EntityPlayerMP) or offline (.dat) handlers,
 * checks config enabled and player blacklist.
 * Returns OperationResult for direct feedback to command callers.
 */
public class RescueService {

    public static OpResult rescue(String playerName) {
        return rescue(playerName, null);
    }

    // Single dispatcher for audo/manual mode (online + offline)
    public static OpResult rescue(String playerName, SafeSpot spot) {
        Config conf = Config.instance();
        if (!conf.enabled) {
            return OpResult.fail("Mod is disabled in the configuration.");
        }
        if (conf.isPlayerBlacklisted(playerName)) {
            return OpResult.fail("Player Blacklisted: " + playerName);
        }
        if (spot != null) {
            return rescueManual(playerName, spot);
        }
        return rescueAuto(playerName);
    }


    // Single dispatcher for manual mode (online + offline)
    private static OpResult rescueManual(String playerName, SafeSpot spot) {
        EntityPlayerMP player = findPlayerOnline(playerName);
        if (player != null) {
            return rescueOnlineManual(player, spot);
        }
        return rescueOfflineManual(playerName, spot);
    }

    // Single dispatcher for auto mode (online + offline)
    private static OpResult rescueAuto(String playerName) {
        EntityPlayerMP player = findPlayerOnline(playerName);
        if (player != null) {
            return rescueOnlineAuto(player);
        }
        return rescueOfflineAuto(playerName);
    }

    private static EntityPlayerMP findPlayerOnline(String playerName) {
        EntityPlayerMP player = MinecraftServer.getServer()
            .getConfigurationManager().func_152612_a(playerName);
        return player;
    }

    /**
     * Returns true if the teleport is successful,
     * false if player not found or error
     */
    public static OpResult rescueOnlineManual(EntityPlayerMP player, SafeSpot p) {
        if (player == null) {
            return OpResult.fail("No Player instance");
        }
        if (p == null) {
            return OpResult.fail("No SafeSpot");
        }
        if (p.y < 0 || p.y > 255) {
            return OpResult.fail("Y coordinate out of bounds: " + p.y);
        }

        // Check that the target dimension exists and is loaded
        WorldServer targetWorld = DimensionManager.getWorld(p.dim);
        if (targetWorld == null) {
            return OpResult.fail("Dimension " + p.dim + " not loaded.");
        }

        if (player.dimension != p.dim) {
            player.travelToDimension(p.dim);
        }

        player.setPositionAndUpdate(p.x + 0.5, p.y, p.z + 0.5);

        String pname = player.getCommandSenderName();
        return logRescued("online", pname, p);
    }

    public static OpResult rescueOnlineAuto(EntityPlayerMP player) {
        return OpResult.fail("Not implemented yet");
    }


    private static OpResult rescueOfflineManual(String playerName, SafeSpot p) {
        UUID uuid = PlayerDataManager.resolveUUID(playerName);
        if (uuid == null) {
            return OpResult.fail("Player not found: " + playerName);
        }

        File datFile = PlayerDataManager.getPlayerDatFile(uuid);
        if (datFile == null || !datFile.exists()) {
            return OpResult.fail("Player data file not found for " + playerName);
        }

        NBTTagCompound playerNbt = PlayerDataManager.loadPlayerData(datFile);
        if (playerNbt == null) {
            return OpResult.fail("Failed to read player data for " + playerName);
        }

        // targetDim = PlayerDataManager.getPlayerDimension(playerNbt);

        PlayerDataManager.setPlayerPos(playerNbt, p.x, p.y, p.z);
        PlayerDataManager.setPlayerDimension(playerNbt, p.dim);

        PlayerDataManager.savePlayerData(datFile, playerNbt);
        return logRescued("offline", playerName, p);
    }

    public static OpResult rescueOfflineAuto(String playerName) {
        return OpResult.fail("Not implemented yet");
    }

    private static OpResult logRescued(String tag, String pname, SafeSpot p) {
        String msg = String.format(
                "Player %s (%s) rescued to '%s' (%d %d %d dim: %d)",
                pname, tag, p.name, p.x, p.y, p.z, p.dim);
        logInfo(msg);
        return OpResult.ok(msg);
    }
}
