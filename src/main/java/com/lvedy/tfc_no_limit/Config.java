package com.lvedy.tfc_no_limit;

import net.dries007.tfc.common.component.size.Weight;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final int VANILLA_VERY_LIGHT = 64;
    private static final int VANILLA_LIGHT = 32;
    private static final int VANILLA_MEDIUM = 16;
    private static final int VANILLA_HEAVY = 4;
    private static final int VANILLA_VERY_HEAVY = 1;

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_STACK_SIZES = BUILDER
            .comment("Enable custom max stack sizes based on TFC weight.")
            .define("enableCustomStackSizes", true);

    public static final ModConfigSpec.IntValue VERY_LIGHT_STACK_SIZE = BUILDER
            .comment("Max stack size for VERY_LIGHT items")
            .defineInRange("veryLightStackSize", 64, 1, 99);

    public static final ModConfigSpec.IntValue LIGHT_STACK_SIZE = BUILDER
            .comment("Max stack size for LIGHT items")
            .defineInRange("lightStackSize", 64, 1, 99);

    public static final ModConfigSpec.IntValue MEDIUM_STACK_SIZE = BUILDER
            .comment("Max stack size for MEDIUM items")
            .defineInRange("mediumStackSize", 64, 1, 99);

    public static final ModConfigSpec.IntValue HEAVY_STACK_SIZE = BUILDER
            .comment("Max stack size for HEAVY items")
            .defineInRange("heavyStackSize", 64, 1, 99);

    public static final ModConfigSpec.IntValue VERY_HEAVY_STACK_SIZE = BUILDER
            .comment("Max stack size for VERY_HEAVY items")
            .defineInRange("veryHeavyStackSize", 64, 1, 99);

    static {
        BUILDER.push("vanillaClimate");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_TFC_CLIMATE = BUILDER
            .comment("Enable TFC-style climate (latitude-based temperature, rainfall, seasonal weather) for worlds",
                    "that use vanilla terrain generation. TFC-generated worlds are unaffected.",
                    "Changes apply on world load, or immediately on config reload while the server is running.")
            .define("enableVanillaTfcClimate", true);

    public static final ModConfigSpec.DoubleValue VANILLA_TEMPERATURE_AVERAGE = BUILDER
            .comment("The global average annual temperature (in °C) of vanilla-terrain worlds.",
                    "TFC's own world generation effectively uses ~5.0.")
            .defineInRange("temperatureAverage", 5.0, -40.0, 40.0);

    public static final ModConfigSpec.DoubleValue VANILLA_TEMPERATURE_VARIANCE = BUILDER
            .comment("The spatial variance (in °C²) of the annual average temperature across the world,",
                    "controlling how much temperature varies between polar and equatorial regions.",
                    "TFC's own world generation effectively uses ~208.3 (about -20°C at poles, +30°C at the equator).",
                    "Set to 0 for a uniform-temperature world.")
            .defineInRange("temperatureVariance", 208.3, 0.0, 2000.0);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean isVanillaTfcClimateEnabled() {
        return ENABLE_VANILLA_TFC_CLIMATE.get();
    }

    public static float getVanillaTemperatureAverage() {
        return VANILLA_TEMPERATURE_AVERAGE.get().floatValue();
    }

    public static float getVanillaTemperatureVariance() {
        return VANILLA_TEMPERATURE_VARIANCE.get().floatValue();
    }

    public static int getStackSize(Weight weight) {
        if (!ENABLE_CUSTOM_STACK_SIZES.get()) {
            return getVanillaStackSize(weight);
        }

        return switch (weight) {
            case VERY_LIGHT -> VERY_LIGHT_STACK_SIZE.get();
            case LIGHT -> LIGHT_STACK_SIZE.get();
            case MEDIUM -> MEDIUM_STACK_SIZE.get();
            case HEAVY -> HEAVY_STACK_SIZE.get();
            case VERY_HEAVY -> VERY_HEAVY_STACK_SIZE.get();
        };
    }

    private static int getVanillaStackSize(Weight weight) {
        return switch (weight) {
            case VERY_LIGHT -> VANILLA_VERY_LIGHT;
            case LIGHT -> VANILLA_LIGHT;
            case MEDIUM -> VANILLA_MEDIUM;
            case HEAVY -> VANILLA_HEAVY;
            case VERY_HEAVY -> VANILLA_VERY_HEAVY;
        };
    }
}
