package com.lvedy.tfc_no_limit;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

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
}
