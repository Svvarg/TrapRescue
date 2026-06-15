// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.cmd;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandHelper {

    public static boolean say(ICommandSender sender, String s) {
        sender.addChatMessage(new ChatComponentText(s));
        return true;
    }

    public static boolean sayUsage(CommandBase c, ICommandSender sndr, String sub) {
        say(sndr, "USAGE: /" + c.getCommandName() + " " + sub);
        return true;
    }

    public static boolean isCmd(String[] args, int n, String full, String shortname) {
        return (n < args.length && n > -1) && args[n] != null
                && (args[n].equals(full) || args[n].equals(shortname));
    }

    public static boolean isCmdHelp(String[] args, int n) {
        return isCmd(args, n, "help", "h") || isCmd(args, n, "--help", "-h");
    }

    public static boolean isCmdHelpOrNoArgs(String[] args, int n) {
        return n >= args.length || isCmdHelp(args, n);
    }

    public static String argS(String[] args, int n) {
        return (n < args.length && n > -1) ? args[n] : null;
    }

    public static int argI(String[] args, int n, int def) {
        try {
           return Integer.parseInt(argS(args, n));
        } catch (Exception e) {
            return def;
        }
    }

}
