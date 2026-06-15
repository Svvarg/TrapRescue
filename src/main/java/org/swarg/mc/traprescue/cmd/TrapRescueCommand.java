// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.cmd;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import org.swarg.mc.traprescue.Config;
import org.swarg.mc.traprescue.OpResult;
import org.swarg.mc.traprescue.rescue.RescueService;
import static org.swarg.mc.traprescue.cmd.CommandHelper.*;

public class TrapRescueCommand extends CommandBase {
    private final List<String> aliases = new ArrayList<>();

    public TrapRescueCommand() { aliases.add("tra"); }

    @Override public String getCommandName() { return "trap-rescue-admin"; }
    @Override public List<String> getCommandAliases() { return aliases; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/trap-rescue-admin <config/rescue/blacklist>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if (isCmd(args, 0, "config", "c")) {
                cmdConfig(sender, args);
            } else if (isCmd(args, 0, "rescue", "r")) {
                cmdRescue(sender, args);
            } else if (isCmd(args, 0, "blacklist", "bl")) {
                cmdBlacklist(sender, args);
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
            return sayUsage(this, sender, "rescue <player> [x y z [dim]]");
        }
        String pname = argS(args, 1);
        if (pname == null || pname.length() < 2) {
            return say(sender, "Invalid player name: " + pname);
        }
        if (args.length >= 5) {
            return doRescueManual(sender, args, pname);
        }
        return sayResult(sender, pname,
                RescueService.rescue(pname, null, null, null, null));
    }

    // Manual mode: x y z dim (dim defaults to 0 if not present)
    public boolean doRescueManual(ICommandSender s, String[] args, String pname) {
        int x, y, z;
        Integer dim = null;
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

        return sayResult(s, pname, RescueService.rescue(pname, x, y, z, dim));
    }

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

    private boolean sayResult(ICommandSender sender, String pname, OpResult res) {
        if (res == null) {
            return say(sender, "no result");
        }
        String msg = res.message;
        if (res.isSuccess()) {
            if (msg == null || msg.isEmpty()) { msg = "Ok"; }
            return say(sender, msg);
        }
        if (msg == null || msg.isEmpty()) {
            msg = "for playerName: " + pname;
        }
        return say(sender, "[FAIL] " + res.message);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Allows usage in console and by ops
    }

}
