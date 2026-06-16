// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.rescue;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.swarg.mc.traprescue.OpResult;
import org.swarg.mc.traprescue.data.PlayerDataManager;
import org.swarg.mc.traprescue.data.PlayerDataResolve;
import org.swarg.mc.traprescue.data.SafeSpot;
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
            return rescueOnline(player, spot);
        }
        return rescueOffline(playerName, spot);
    }

    // Single dispatcher for auto mode (online + offline)
    private static OpResult rescueAuto(String playerName) {
        EntityPlayerMP player = findPlayerOnline(playerName);
        if (player != null) {
            return rescueOnlineAuto(player);
        }
        return rescueOfflineAuto(playerName);
    }

    /**
     * Finds a rescue target (SafeSpot now, later extended to auto-exit point).
     * @return OpResult with data = SafeSpot on success, or failure message.
     */
    private static OpResult findRescueTarget(int x, int y, int z, int dim) {
        SafeSpot spot = Config.instance().findSafeSpotForPosition(x, y, z, dim);
        if (spot != null) {
            return OpResult.okData(spot);
        }
        // todo 0.4.0: fallback to safe location finder (roof / chunk edge)
        return OpResult.fail("Player is not in range of any safe spot");
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
    public static OpResult rescueOnline(EntityPlayerMP player, SafeSpot p) {
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
        if (player == null) {
            return OpResult.fail("No Player instance");
        }
        int dim = player.dimension;
        int px = (int) Math.round(player.posX);
        int py = (int) Math.round(player.posY);
        int pz = (int) Math.round(player.posZ);

        OpResult res = findRescueTarget(px, py, pz, dim);
        SafeSpot spot = res.getData(SafeSpot.class);
        if (spot == null) {
            return res;
        }
        return rescueOnline(player, spot);
    }

    private static OpResult rescueOffline(String playerName, SafeSpot p) {
        OpResult res = PlayerDataManager.resolveAndLoadPlayerData(playerName);
        PlayerDataResolve pdata = res.getData(PlayerDataResolve.class);
        if (pdata == null) {
            return res;
        }
        if (p == null) {
            return OpResult.fail("no SafeSpot");
        }
        int dim = PlayerDataManager.getPlayerDimension(pdata.nbt);
        double[] pos = PlayerDataManager.getPlayerPos(pdata.nbt);
        logOldPos(playerName, (int) pos[0], (int) pos[1], (int) pos[2], dim);
        return applyRescueOffline(playerName, p, pdata);
    }

    public static OpResult rescueOfflineAuto(String playerName) {
        OpResult res = PlayerDataManager.resolveAndLoadPlayerData(playerName);
        PlayerDataResolve pdata = res.getData(PlayerDataResolve.class);
        if (pdata == null) {
            return res;
        }
        int dim = PlayerDataManager.getPlayerDimension(pdata.nbt);
        double[] pos = PlayerDataManager.getPlayerPos(pdata.nbt);

        int px = (int) Math.round(pos[0]); // x
        int py = (int) Math.round(pos[1]); // y
        int pz = (int) Math.round(pos[2]); // z

        res = findRescueTarget(px, py, pz, dim);
        SafeSpot spot = res.getData(SafeSpot.class);
        if (spot == null) {
            return res;
        }
        logOldPos(playerName, px, py, pz, dim);
        return applyRescueOffline(playerName, spot, pdata);
    }

    private static OpResult applyRescueOffline(
            String playerName, SafeSpot p, PlayerDataResolve data) {
        PlayerDataManager.setPlayerPos(data.nbt, p.x, p.y, p.z);
        PlayerDataManager.setPlayerDimension(data.nbt, p.dim);
        PlayerDataManager.savePlayerData(data.file, data.nbt);
        return logRescued("offline", playerName, p);
    }

    /**
     * tag is online/offline
     * pname - the Player Name
     */
    private static OpResult logRescued(String tag, String pname, SafeSpot p) {
        String msg = String.format(
                "Player %s (%s) rescued to '%s' (%d %d %d dim: %d)",
                pname, tag, p.name, p.x, p.y, p.z, p.dim);
        logInfo(msg);
        return OpResult.ok(msg);
    }

    /**
     * log the player's old position before moving
     */
    private static void logOldPos(String pname, int x, int y, int z, int dim) {
        logInfo(String.format("Player %s rescued from %d %d %d dim: %d",
                pname, x, y, z, dim));
    }

    /**
     * Checks whether a player (online or offline) is inside the radius of any
     * safe spot.
     * @param pname The player's exact name.
     * @return OpResult with a human-readable message about the safe spot and
     * distance, or an error.
     */
    public static OpResult checkSafeSpot(String pname) {
        EntityPlayerMP onlinePlayer = findPlayerOnline(pname);
        int px, py, pz, dim;

        if (onlinePlayer != null) {
            px = (int) Math.round(onlinePlayer.posX);
            py = (int) Math.round(onlinePlayer.posY);
            pz = (int) Math.round(onlinePlayer.posZ);
            dim = onlinePlayer.dimension;
        } else {
            OpResult res = PlayerDataManager.resolveAndLoadPlayerData(pname);
            PlayerDataResolve pdata = res.getData(PlayerDataResolve.class);
            if (pdata == null) {
                return res;
            }
            double[] pos = PlayerDataManager.getPlayerPos(pdata.nbt);
            if (pos == null) {
                return OpResult.fail("Cannot get position for player: " + pname);
            }
            Integer dimObj = PlayerDataManager.getPlayerDimension(pdata.nbt);
            dim = (dimObj != null) ? dimObj : 0;
            px = (int) Math.round(pos[0]);
            py = (int) Math.round(pos[1]);
            pz = (int) Math.round(pos[2]);
        }

        SafeSpot p = Config.instance().findSafeSpotForPosition(px, py, pz, dim);
        if (p == null) {
            return OpResult.ok("Player " + pname + " is not in range of any safe spot.");
        }
        double dist = p.distanceTo(px, py, pz);
        return OpResult.ok(String.format(
                "Player %s is inside safe spot '%s' (distance %.0f blocks, radius %d).",
                pname, p.name, dist, p.radius));
    }
}
