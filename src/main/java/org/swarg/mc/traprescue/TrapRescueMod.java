/*
 *  Copyright (C) 2026 Swarg
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.swarg.mc.traprescue;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import org.swarg.mc.traprescue.cmd.TrapRescueCommand;
import static org.swarg.mc.traprescue.Reference.*;
import static org.swarg.mc.traprescue.RescueLogger.*;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION, acceptableRemoteVersions = "*")
public class TrapRescueMod {
	@Instance(Reference.MOD_ID)
	public static TrapRescueMod instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
        // by design should already be loaded via CoreMod
        Config.preInit(event.getModConfigurationDirectory()).reload(false);
    }

	@EventHandler
	public void initialize(FMLInitializationEvent event) {
		// FMLCommonHandler.instance().bus().register(new PlayerTracker());
		// MinecraftForge.EVENT_BUS.register(new PlayerInteractHandler());
    }

	@EventHandler
	public void postInit (FMLPostInitializationEvent event) {
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logInfo("Registering TrapRescue command...");

        event.registerServerCommand(new TrapRescueCommand());
    }

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(Reference.MOD_ID)) {
            Config.instance().reload(false);
        }
	}

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    }
}
