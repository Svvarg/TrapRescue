// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import org.swarg.mc.traprescue.Config;
import org.swarg.mc.traprescue.OpResult;
import org.swarg.mc.traprescue.data.PlayerDataManager;
import org.swarg.mc.traprescue.data.SafeSpot;
import org.swarg.mc.traprescue.rescue.RescueService;
import static org.swarg.mc.traprescue.cmd.CommandHelper.*;

public class TrapRescueCommand extends CommandBase {
    private final List<String> aliases = new ArrayList<>();

    public TrapRescueCommand() { aliases.add("tra"); }

    @Override public String getCommandName() { return "trap-rescue-admin"; }
    @Override public List<String> getCommandAliases() { return aliases; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/trap-rescue-admin <config/rescue/safespot/blacklist/player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if (isCmd(args, 0, "config", "c")) {
                cmdConfig(sender, args);
            } else if (isCmd(args, 0, "rescue", "r")) {
                cmdRescue(sender, args);
            } else if (isCmd(args, 0, "safespot", "s")) {
                cmdSafeSpot(sender, args);
            } else if (isCmd(args, 0, "blacklist", "bl")) {
                cmdBlacklist(sender, args);
            } else if (isCmd(args, 0, "player", "p")) {
                cmdPlayer(sender, args);
            } else {
                say(sender, "Unknown command: " + args[0]);
            }
        } else {
            say(sender, "Usage: " + getCommandUsage(sender));
        }
    }

    private boolean cmdConfig(ICommandSender sender, String[] args) {
        if (isCmd(args, 1, "help", "h") || args.length < 2) {
            return sayUsage(this, sender, "config <status/reload>");
        }

        if (isCmd(args, 0, "status", "st")) {
            return say(sender, Config.instance().readable());
        }
        if (isCmd(args, 0, "reload", "r")) {
            Config.instance().reload(true);
            return say(sender, "Config reloaded");
        }
        return say(sender, "Unknown command: " + args[0]);
    }

    private boolean cmdRescue(ICommandSender sender, String[] args) {
        if (isCmd(args, 1, "help", "h") || args.length < 2) {
            return sayUsage(this, sender,
                "rescue <player> [x y z [dim] ]/[<safe-point-name>]");
        }
        String pname = argS(args, 1);
        if (pname == null || pname.length() < 2) {
            return say(sender, "Invalid player name: " + pname);
        }
        if (args.length == 3) {
            return doRescueToSafeSpot(sender, args, pname);
        }
        if (args.length >= 5) {
            return doRescueManual(sender, args, pname);
        }
        return sayResult(sender, RescueService.rescue(pname));
    }

    // Manual mode: x y z dim (dim defaults to 0 if not present)
    public boolean doRescueManual(ICommandSender s, String[] args, String pname) {
        int x, y, z, dim = 0;
        try {
            x = Integer.parseInt(args[2]);
            y = Integer.parseInt(args[3]);
            z = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            return say(s, "Invalid coordinates. expected x y z [dim]");
        }
        if (args.length >= 6) {
            try {
                dim = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                return say(s, "Invalid dimension:" + args[5]);
            }
        }
        SafeSpot p = new SafeSpot("MANUAL", x, y, z, dim, 0);
        return sayResult(s, RescueService.rescue(pname, p));
    }

    // tra rescue <player> <safe-point-name>
    public boolean doRescueToSafeSpot(ICommandSender s, String[] args, String pname) {
        String name = argS(args, 2);
        SafeSpot p = Config.instance().getSafeSpot(name);
        if (p == null) {
            return say(s, "Not Found SafeSpot with name: " + name);
        }
        // check radius?
        return sayResult(s, RescueService.rescue(pname, p));
    }

    private boolean cmdSafeSpot(ICommandSender s, String[] args) {
        if (isCmd(args, 1, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "safespot <list/add/remove/get/rename>");
        }
        Config conf = Config.instance();
        if (isCmd(args, 1, "list", "ls")) {
            return say(s, "SafeSpots:\n" + conf.getReadableSafeSpots());
        }
        if (isCmd(args, 1, "add", "a")) {
            cmdSafeSpotAdd(s, args);
        }
        if (isCmd(args, 1, "remove", "rm")) {
            cmdSafeSpotRemove(s, args);
        }
        if (isCmd(args, 1, "get", "g")) {
            cmdSafeSpotGet(s, args);
        }
        if (isCmd(args, 1, "rename", "mv")) {
            cmdSafeSpotRename(s, args);
        }
        return true;
    }

    private boolean cmdSafeSpotAdd(ICommandSender s, String[] args) {
        if (isCmd(args, 2, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "safespot add <name> x y z dim radius");
        }
        int i = 2;
        String name = argS(args, i++);
        int x, y, z, dim, radius;
        try {
            x = Integer.parseInt(args[i++]);
            y = Integer.parseInt(args[i++]);
            z = Integer.parseInt(args[i++]);
            dim = Integer.parseInt(args[i++]);
            radius = Integer.parseInt(args[i++]);
        } catch (Exception e) {
            return say(s, "Invalid input. expected: x y z dim radius");
        }
        if (radius < 5) {
            return say(s, "radius must be greater than 5 (blocks)");
        }
        return sayResult(s, Config.instance().addSafeSpot(name, x, y, z, dim, radius));
    }

    private boolean cmdSafeSpotRemove(ICommandSender s, String[] args) {
        if (isCmd(args, 2, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "safespot remove <name>");
        }
        String name = argS(args, 2);
        SafeSpot removed = Config.instance().removeSafeSpot(name);
        if (removed == null) {
            return say(s, "Not found SafeSpot with name: " + name);
        }
        return say(s, "Removed SafeSpot: " + removed.serialize());
    }

    private boolean cmdSafeSpotGet(ICommandSender s, String[] args) {
        if (isCmd(args, 2, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "safespot get <name>");
        }
        String name = argS(args, 2);
        SafeSpot spot = Config.instance().getSafeSpot(name);
        if (spot == null) {
            return say(s, "Not found SafeSpot with name: " + name);
        }
        return say(s, "SafeSpot: " + spot.serialize());
    }

    private boolean cmdSafeSpotRename(ICommandSender s, String[] args) {
        if (isCmd(args, 2, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "safespot rename <old-name> <new-name>");
        }
        String oldName = argS(args, 2);
        String newName = argS(args, 3);
        if (newName == null || newName.isEmpty()) {
            return say(s, "no new-name");
        }
        if (newName.equalsIgnoreCase(oldName)) {
            return say(s, "old and new name are the same");
        }
        Config conf = Config.instance();
        SafeSpot p = conf.getSafeSpot(oldName);
        if (p == null) {
            return say(s, "Not found SafeSpot with name: " + oldName);
        }
        conf.removeSafeSpot(oldName);
        return sayResult(s, conf.addSafeSpot(p.name, p.x, p.y, p.z, p.dim, p.radius));
    }

    /**
     *
     */
    private boolean cmdBlacklist(ICommandSender sender, String[] args) {
        if (isCmd(args, 1, "help", "h") || args.length < 2) {
            return sayUsage(this, sender, "blacklist <list/add/remove> <player>");
        }
        Config conf = Config.instance();
        if (isCmd(args, 1, "list", "ls")) {
            return say(sender, conf.readableBlacklist());
        }
        String pname = argS(args, 2);
        if (pname == null || pname.length() < 3) {
            return say(sender, "expected valid playerName");
        }
        if (isCmd(args, 1, "add", "a")) {
            return say(sender, "added:" + conf.addToBlacklist(pname));
        }
        if (isCmd(args, 1, "remove", "rm")) {
            return say(sender, "removed:" + conf.removeFromBlacklist(pname));
        }
        return say(sender, "Unknown command: " + args[0]);
    }

    private boolean cmdPlayer(ICommandSender s, String[] args) {
        if (isCmd(args, 1, "help", "h") || args.length < 2) {
            return sayUsage(this, s, "player <uuid/pos> <playerName>");
        }
        String name = argS(args, 2); // PlayerName
        if (name == null || name.length() < 2) {
            return say(s, "Invalid player name: " + name);
        }
        if (isCmd(args, 1, "uuid", "u")) {
            UUID uuid = PlayerDataManager.resolveUUID(name);
            return say(s, "UUID:" + uuid);
        }
        if (isCmd(args, 1, "pos", "p")) {
            return sayResult(s, PlayerDataManager.getPlayerPosByName(name));
        }
        return true;
    }

    private boolean sayResult(ICommandSender sender, OpResult res) {
        if (res == null) {
            return say(sender, "no result");
        }
        String msg = res.message;
        if (res.isSuccess()) {
            if (msg == null || msg.isEmpty()) { msg = "Ok"; }
            return say(sender, msg);
        }
        return say(sender, "[FAIL] " + res.message);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Allows usage in console and by ops
    }

}
