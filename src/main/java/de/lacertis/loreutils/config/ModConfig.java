package de.lacertis.loreutils.config;

import de.lacertis.loreutils.lectern.CopyAction;
import de.lacertis.loreutils.lectern.OutputMode;
import de.lacertis.loreutils.solver.lightsout.LightsOutSolverMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;


@Config(name = "loreutils")
@Config.Gui.Background("minecraft:textures/block/prismarine.png")
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

    @ConfigEntry.Category("ingenuity")
    @ConfigEntry.Gui.Tooltip
    public boolean ingenuityTip = true;

    @ConfigEntry.Category("ingenuity")
    @ConfigEntry.Gui.Tooltip
    public boolean ingenuityDebug = false;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    public CopyAction defaultLecternAction = CopyAction.PAGE;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    public OutputMode defaultLecternMode = OutputMode.RAW;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public boolean includeTitleAuthor = true;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public boolean numberPages = true;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public String pageSeparator = "\n\n---\n\n";

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public boolean formattedKeepPageBreaks = true;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public boolean exportToFile = false;

    @ConfigEntry.Category("lectern")
    @ConfigEntry.Gui.Tooltip
    public String filePattern = "{title}-{sha1}.{ext}";
}
