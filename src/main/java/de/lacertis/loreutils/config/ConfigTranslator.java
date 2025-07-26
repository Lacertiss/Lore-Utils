package de.lacertis.loreutils.config;

public class ConfigTranslator {
    public static float[] translate(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return new float[]{r, g, b};
    }

    public static float translateAlpha(int alphaPercentage) {
        return alphaPercentage / 100f;
    }
}