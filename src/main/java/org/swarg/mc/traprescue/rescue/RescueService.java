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

    public static OpResult rescue(String playerName){
        return rescue(playerName, null, null, null, null);
    }

    // Single dispatcher for audo/manual mode (online + offline)
    public static OpResult rescue(String playerName,
            Integer x, Integer y, Integer z, Integer dim) {
        Config conf = Config.instance();
        if (!conf.enabled) {
            return OpResult.fail("Mod is disabled in the configuration.");
        }
        if (conf.isPlayerBlacklisted(playerName)) {
            return OpResult.fail("Player Blacklisted: " + playerName);
        }
        if (x != null && y != null && z != null) {
            return rescueManual(playerName, x, y, z, dim);
        }
        return rescueAuto(playerName);
    }


    // Single dispatcher for manual mode (online + offline)
    private static OpResult rescueManual(
            String playerName, int x, int y, int z, Integer dim) {
        EntityPlayerMP player = findPlayerOnline(playerName);
        if (player != null) {
            return rescueOnlineManual(player, x, y, z, dim);
        }
        return rescueOfflineManual(playerName, x, y, z, dim);
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
    public static OpResult rescueOnlineManual(EntityPlayerMP player,
            int x, int y, int z, Integer dimension) {
        if (player == null) {
            return OpResult.fail("No Player instance");
        }
        if (y < 0 || y > 255) {
            return OpResult.fail("Y coordinate out of bounds: " + y);
        }

        if (dimension == null) {
            dimension = player.dimension;
        }

        // Check that the target dimension exists and is loaded
        WorldServer targetWorld = DimensionManager.getWorld(dimension);
        if (targetWorld == null) {
            return OpResult.fail("Dimension " + dimension + " not loaded.");
        }

        if (player.dimension != dimension) {
            player.travelToDimension(dimension);
        }

        player.setPositionAndUpdate(x + 0.5, y, z + 0.5);

        String pname = player.getCommandSenderName();
        return logRescued("online manual", pname, x, y, z, dimension);
    }

    public static OpResult rescueOnlineAuto(EntityPlayerMP player) {
        return OpResult.fail("Not implemented yet");
    }


    private static OpResult rescueOfflineManual(String playerName,
            int x, int y, int z, Integer dimension) {

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

        int targetDim;
        if (dimension != null) {
            targetDim = dimension;
        } else {
            targetDim = PlayerDataManager.getPlayerDimension(playerNbt);
        }

        PlayerDataManager.setPlayerPos(playerNbt, x, y, z);
        PlayerDataManager.setPlayerDimension(playerNbt, targetDim);

        PlayerDataManager.savePlayerData(datFile, playerNbt);
        return logRescued("offline manual", playerName, x, y, z, targetDim);
    }

    public static OpResult rescueOfflineAuto(String playerName) {
        return OpResult.fail("Not implemented yet");
    }

    private static OpResult logRescued(String tag, String playerName,
            int x, int y, int z, int dim) {
        String msg = "Player: " + playerName + " rescued to " +
                x + " " + y + " " + z + " dim: " + dim + " (" + tag + ")";
        logInfo(msg);
        return OpResult.ok(msg);
    }
}
