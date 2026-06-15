// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RescueLogger {
    public static final Logger LOG = LogManager.getLogger(Reference.MOD_ID);

    public static void logInfo(String msg) {
        LOG.info("[TrapRescue] {}", msg);
    }
}
