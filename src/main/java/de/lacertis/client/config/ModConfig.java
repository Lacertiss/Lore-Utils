package de.lacertis.client.config;

import de.lacertis.client.solver.LightsOutSolverMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "loreutils")
@Config.Gui.Background("minecraft:textures/block/dark_prismarine.png")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("appearance")
    @ConfigEntry.ColorPicker
    @ConfigEntry.Gui.Tooltip
    public int primaryColor = 0x03BEFC;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int alphaPercentage = 80;

    @ConfigEntry.Category("lightsout")
    @ConfigEntry.Gui.Tooltip
    public boolean autoSolveLightsOut = true;

    @ConfigEntry.Category("lightsout")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public LightsOutSolverMode lightsOutSolverMode = LightsOutSolverMode.ALL_ON;

    @ConfigEntry.Category("lightsout")
    @ConfigEntry.Gui.Tooltip
    public boolean autoMarkAnuarButtons = true;
}

