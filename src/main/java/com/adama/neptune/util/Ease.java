package com.adama.neptune.util;

public final class Ease {
    private Ease() {
    }

    public static float clamp01(float t) {
        return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
    }

    public static float smooth(float t) {
        t = clamp01(t);
        return t * t * (3.0F - 2.0F * t);
    }

    public static float outCubic(float t) {
        t = clamp01(t) - 1.0F;
        return t * t * t + 1.0F;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float approach(float current, float target, float dt, float speed) {
        float delta = target - current;
        float step = speed * dt;
        if (Math.abs(delta) <= step) return target;
        return current + Math.signum(delta) * step;
    }

    public static float expSmooth(float current, float target, float dt, float halfLife) {
        if (halfLife <= 0.0F) return target;
        float factor = 1.0F - (float) Math.pow(0.5D, dt / halfLife);
        return current + (target - current) * factor;
    }
}
