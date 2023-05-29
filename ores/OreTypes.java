import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraft.world.gen.feature.template.TagMatchRuleTest;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public enum OreTypes {
    DIAMOND(() -> Blocks.DIAMOND_ORE, 8, 10, 11, 8,
            BiomePreference.ALL, new TagMatchRuleTest(BlockTags.BASE_STONE_OVERWORLD))
    ;

    public final Lazy<Block> block;
    public final int minHeight, preferredHeight, maxHeight, veinSize;
    public final BiomePreference biomePreference;
    public final RuleTest replaceableBlock;

    OreTypes(Supplier<Block> blockSupplier,
             int minHeight, int preferredHeight, int maxHeight,
             int veinSize,
             BiomePreference biomePreference, RuleTest replaceableBlock) {
        this.block = Lazy.of(blockSupplier);
        this.minHeight = minHeight;
        this.preferredHeight = preferredHeight;
        this.maxHeight = maxHeight;
        this.veinSize = veinSize;
        this.biomePreference = biomePreference;
        this.replaceableBlock = replaceableBlock;
    }

    public boolean allowForGeneration(Biome biome) {
        return biomePreference.allow(biome);
    }

    public static void generate(BiomeLoadingEvent event) {
        for (OreTypes ore : values()) {
            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
            if (ore.allowForGeneration(biome)) {
                OreFeatureConfig oreFeatureConfig = new OreFeatureConfig(
                        ore.replaceableBlock,
                        ore.block.get().getDefaultState(),
                        ore.veinSize
                );

                ConfiguredPlacement<TopSolidRangeConfig> configuredPlacement = Placement.RANGE.configure(
                        new TopSolidRangeConfig(ore.minHeight, ore.preferredHeight, ore.maxHeight)
                );

                ConfiguredFeature<?, ?> oreFeature = registerOreFeature(ore,
                        oreFeatureConfig, configuredPlacement);
                event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, oreFeature);
            }
        }
    }
    private static ConfiguredFeature<?, ?> registerOreFeature(OreTypes ore, OreFeatureConfig config,
                                                              ConfiguredPlacement<?> configuredPlacement) {
        return Registry.register(
                WorldGenRegistries.CONFIGURED_FEATURE,
                ore.block.get().getRegistryName(),
                Feature.ORE.withConfiguration(config)
                        .withPlacement(configuredPlacement)
                        .square()
                        .count(ore.veinSize)
        );
    }
}
