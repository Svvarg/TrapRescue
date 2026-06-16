// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue.data;

import static org.swarg.mc.traprescue.RescueLogger.*;

public class SafeSpot {
    public static final String FORMAT = "name:x:y:z:dim:radius";
    public final String name;
    public final int x;
    public final int y;
    public final int z;
    public final int dim;
    public final int radius;

    public SafeSpot(String name, int x, int y, int z, int dim, int radius) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.radius = radius;
    }

    public String serialize() {
        return name + ":" + x + ":" + y + ":" + z + ":" + dim + ":" + radius;
    }

    public static SafeSpot deserialize(String entry) {
        String[] parts = entry.split(":");
        if (parts.length != 6) {
            logWarn("Invalid safe_spot entry (wrong number of parts): " + entry);
            return null;
        }
        try {
            String name = parts[0].toLowerCase().trim();
            int x = Integer.parseInt(parts[1].trim());
            int y = Integer.parseInt(parts[2].trim());
            int z = Integer.parseInt(parts[3].trim());
            int dim = Integer.parseInt(parts[4].trim());
            int radius = Integer.parseInt(parts[5].trim());
            if (radius <= 0) {
                logWarn("Safe spot '" + name + "' has non-positive radius, skipping.");
                return null;
            }
            SafeSpot spot = new SafeSpot(name, x, y, z, dim, radius);
            return spot;
        } catch (NumberFormatException e) {
            logWarn("Invalid number in safe_spot entry: " + entry);
        }
        return null;
    }

}
