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

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
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
