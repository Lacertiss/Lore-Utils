package de.lacertis.client.config;

import de.lacertis.client.solver.LughtsOutSolverMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "loreutils")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean AutoSolveLightsOut = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip(count = 1)
    public LughtsOutSolverMode lughtsOutSolverMode = LughtsOutSolverMode.ALL_ON;
}
