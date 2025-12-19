package dev.enjarai.mls.config;



import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ModConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder push = BUILDER.push("Moderate Loading Screen");
    public static final ModConfigSpec.ConfigValue<Integer> backgroundColor = BUILDER.define("backgroundColor", 0x161616);
    public static final ModConfigSpec.ConfigValue<Integer> logoOpacity = BUILDER.defineInRange("logoOpacity", 100, 0, 100);
    public static final ModConfigSpec.ConfigValue<Integer> barOpacity = BUILDER.defineInRange("barOpacity", 100, 0, 100);
    public static final ModConfigSpec.BooleanValue modsOnlyOnce = BUILDER.define("modsOnlyOnce", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> modIdBlacklist = BUILDER.defineList("modIdBlacklist", Arrays.asList(
            "neoforge",
            "minecraft"
    ),()->"mod_id", o -> o instanceof String);

    public static final ModConfigSpec.ConfigValue<Integer> iconSize = BUILDER.define("iconSize", 32);
    public static final ModConfigSpec.EnumValue<ScreenTypes> screenType = BUILDER.defineEnum("screenType", ScreenTypes.SNOWFLAKES);
    public static final ModConfigSpec.EnumValue<Orientation> orientation = BUILDER.defineEnum("orientation", Orientation.DOWN);
    public static final ModConfigSpec.ConfigValue<Integer> cycleSeconds = BUILDER.define("cycleSeconds", 20);
    public static final ModConfigSpec.BooleanValue earlyWindowOverlay = BUILDER.comment("Show LoadingScreen Over NeoForge LoadingScreen")
            .define("earlyWindowOverlay", false);
    public static final ModConfigSpec.ConfigValue<Integer> earlyOverlayOpacity = BUILDER.comment("Set Early LoadingScreen Overlay Alpha")
            .defineInRange("earlyOverlayOpacity", 45, 0, 100);
    private static final ModConfigSpec.Builder pop = BUILDER.pop();
    public static final ModConfigSpec SPEC = BUILDER.build();

}
