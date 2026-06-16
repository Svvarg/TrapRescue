// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.UsernameCache;
import com.mojang.authlib.GameProfile;
import org.swarg.mc.traprescue.OpResult;
import static org.swarg.mc.traprescue.RescueLogger.*;

public class PlayerDataManager {
    public static final int NBTTAG_INT = 3;
    public static final int NBTTAG_DOUBLE = 6;
    public static final int NBTTAG_LIST = 9;
    public static final int NBTTAG_COMPOUND = 10;

    /**
     * Look up the UUID for a player name using the server's profile cache.
     * Returns null if not found or cache unavailable.
     * @return UUID or null if server not running or critical error.
     */
    public static UUID resolveUUID(String playerName) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return null;
        }

        UUID uuid = getPlayerUUIDfromForgeUsernameCache(playerName);
        if (uuid != null) {
            return uuid;
        }

        try {
            // Try the profile cache first (it should work for online-mode and
            // previously joined players)
            // getPlayerProfileCache().getGameProfileForName(playerName);
            GameProfile profile = server.func_152358_ax().func_152655_a(playerName);
            if (profile != null && profile.getId() != null) {
                return profile.getId();
            }
        } catch (Exception ignored) {
            // Fall through to offline UUID
        }
        return null;
    }

    /**
     * Retrieve a player's UUID from the Forge UsernameCache by
     * case-insensitive name match.
     * Returns null if the cache is empty or the name is not found.
     */
    public static UUID getPlayerUUIDfromForgeUsernameCache(String name) {
        if (name != null && !name.isEmpty()) {
            Map<UUID, String> map = UsernameCache.getMap();
            for (Map.Entry<UUID, String> entry : map.entrySet()) {
                if (name.equalsIgnoreCase(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Build the File for a player's .dat given their UUID.
     * Uses the Overworld save directory: <worldDir>/playerdata/<uuid>.dat
     * Returns null if the Overworld is not loaded or the UUID is null.
     */
    public static File getPlayerDatFile(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        WorldServer world = DimensionManager.getWorld(0);
        if (world == null) {
            return null;
        }
        File worldDir = world.getSaveHandler().getWorldDirectory();
        return new File(worldDir, "playerdata" + File.separator + uuid.toString() + ".dat");
    }

    /**
     * Read NBT from a compressed .dat file.
     * Returns null on any failure (missing file, corrupt data, I/O error).
     */
    public static NBTTagCompound loadPlayerData(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return CompressedStreamTools.readCompressed(fis);
        } catch (IOException e) {
            logInfo("Failed to load player data from " + file.getAbsolutePath()
                + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Write an NBT compound to a compressed .dat file.
     * No explicit return; failures are logged and swallowed.
     */
    public static void savePlayerData(File file, NBTTagCompound data) {
        if (file == null || data == null) {
            return;
        }
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            CompressedStreamTools.writeCompressed(data, new FileOutputStream(file));
        } catch (IOException e) {
            logInfo("Failed to save player data to " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    /**
     * Extract player position as [x, y, z] from the "Pos" tag.
     * Returns null if the tag is missing, not a list of 3 doubles, or playerData is null.
     */
    public static double[] getPlayerPos(NBTTagCompound playerData) {
        if (playerData == null || !playerData.hasKey("Pos", NBTTAG_LIST)) {
            return null;
        }
        NBTTagList posList = playerData.getTagList("Pos", NBTTAG_DOUBLE);
        if (posList.tagCount() != 3) {
            return null;
        }
        double[] pos = new double[3];
        for (int i = 0; i < 3; i++) {
            pos[i] = posList.func_150309_d(i); // getDoubleAt
        }
        return pos;
    }

    /**
     * Replace the "Pos" tag with a new list of three doubles.
     * Does nothing if playerData is null.
     */
    public static void setPlayerPos(NBTTagCompound playerData,
            double x, double y, double z) {
        if (playerData == null) {
            return;
        }
        NBTTagList posList = new NBTTagList();
        posList.appendTag(new NBTTagDouble(x));
        posList.appendTag(new NBTTagDouble(y));
        posList.appendTag(new NBTTagDouble(z));
        playerData.setTag("Pos", posList);
    }

    /**
     * Get the player's dimension from the "Dimension" tag.
     * Defaults to 0 (Overworld) if missing.
     */
    public static Integer getPlayerDimension(NBTTagCompound playerData) {
        if (playerData == null || !playerData.hasKey("Dimension", NBTTAG_INT)) {
            return null;
        }
        return playerData.getInteger("Dimension");
    }

    /**
     * Set the "Dimension" tag to the given value.
     * Does nothing if playerData is null.
     */
    public static void setPlayerDimension(NBTTagCompound playerData, int dimension) {
        if (playerData == null) {
            return;
        }
        playerData.setInteger("Dimension", dimension);
    }

    /**
     * Get the global spawn coordinates for a dimension.
     * Returns null if the dimension is not loaded or has no spawn point.
     */
    public static ChunkCoordinates getWorldSpawn(int dimension) {
        WorldServer world = DimensionManager.getWorld(dimension);
        if (world == null) {
            return null;
        }
        return world.getSpawnPoint();
    }

    /**
     * getPlayerNBTData
     */
    public static OpResult resolveAndLoadPlayerData(String pname) {
        UUID uuid = PlayerDataManager.resolveUUID(pname);
        if (uuid == null) {
            return OpResult.fail("Not found UUID for player: " + pname);
        }
        File datFile = PlayerDataManager.getPlayerDatFile(uuid);
        if (datFile == null) {
            return OpResult.fail("Cannot get the file for player: " + pname);
        }
        if (!datFile.exists()) {
            return OpResult.fail("Not found player dat-file: " + datFile);
        }
        NBTTagCompound nbt = PlayerDataManager.loadPlayerData(datFile);
        if (nbt == null) {
            return OpResult.fail("Failed to read player data for " + pname);
        }
        return OpResult.okData(new PlayerDataResolve(datFile, nbt));
    }

    /**
     * @return OpResult with readable coords and dimension
     */
    public static OpResult getPlayerPosByName(String pname) {
        int x, y, z, dim;
        final EntityPlayerMP onlinePlayer = MinecraftServer.getServer()
                .getConfigurationManager().func_152612_a(pname);

        if (onlinePlayer != null) {
            x = (int) Math.round(onlinePlayer.posX);
            y = (int) Math.round(onlinePlayer.posY);
            z = (int) Math.round(onlinePlayer.posZ);
            dim = onlinePlayer.dimension;
        } else {
            OpResult res = resolveAndLoadPlayerData(pname);
            PlayerDataResolve pdata = res.getData(PlayerDataResolve.class);
            if (pdata == null) {
                return res;
            }

            double[] pos = PlayerDataManager.getPlayerPos(pdata.nbt);
            if (pos == null) {
                return OpResult.fail("Cannot get the Pos from NBT of:" + pname);
            }
            Integer adim = getPlayerDimension(pdata.nbt);
            dim = (adim == null) ? 0 : adim;
            x = (int) pos[0];
            y = (int) pos[1];
            z = (int) pos[2];
        }

        return OpResult.ok(String.format("Pos: %.1f %.1f %.1f dim: %s",
                    x, y, z, dim));
    }

}
