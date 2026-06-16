// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.data;

import java.io.File;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerDataResolve {
    public final File file;
    public final NBTTagCompound nbt;

    public PlayerDataResolve(File file, NBTTagCompound nbt) {
        this.file = file;
        this.nbt = nbt;
    }
}
