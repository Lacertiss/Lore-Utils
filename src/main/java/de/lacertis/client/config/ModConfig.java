package de.lacertis.client.config;

import de.lacertis.client.solver.LightsOutSolverMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "loreutils")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean AutoSolveLightsOut = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public LightsOutSolverMode lightsOutSolverMode = LightsOutSolverMode.ALL_ON;

    @ConfigEntry.ColorPicker
    @ConfigEntry.Gui.Tooltip
    public int primaryColor = 0x03BEFC;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int alphaPercentage = 80;
}

