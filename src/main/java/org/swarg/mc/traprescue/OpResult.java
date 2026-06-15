// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue;

public class OpResult {
    public final boolean success;
    public final String message;

    public OpResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }

    public static OpResult ok(String msg) { return new OpResult(true, msg); }
    public static OpResult fail(String msg) { return new OpResult(false, msg); }
}
