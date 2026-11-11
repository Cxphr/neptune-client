package com.adama.neptune.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class KeyUtil {
    private KeyUtil() {
    }

    public static boolean isDown(int code) {
        if (code == 0) return false;
        if (code >= -100 && code <= -98) {
            return Mouse.isButtonDown(-100 - code);
        }
        return Keyboard.isKeyDown(code);
    }
}
