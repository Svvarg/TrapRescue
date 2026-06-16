// Copyright (C) 2026 Swarg
// SPDX-License-Identifier: AGPL-3.0-only
package org.swarg.mc.traprescue;

public class OpResult {
    public final boolean success;
    public final String message;
    public final Object data;

    public OpResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    public OpResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }

    public static OpResult ok(String msg) { return new OpResult(true, msg); }
    public static OpResult fail(String msg) { return new OpResult(false, msg); }

    public static OpResult okData(Object data) {
        return new OpResult(true, "", data);
    }

    public boolean isSuccessWithData(Class<?> cls) {
        return success && data != null && cls != null && cls.isInstance(data);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> clazz) {
        return isSuccessWithData(clazz) ? (T) data : null;
    }
}
