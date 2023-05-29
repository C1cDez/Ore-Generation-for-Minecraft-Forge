import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class BiomePreference {
    public final Predicate<Biome> preferredBiomes;
    private BiomePreference(Predicate<Biome> preferredBiomes) {
        this.preferredBiomes = preferredBiomes;
    }

    public static final BiomePreference ALL = new BiomePreference(biome -> true);
    public static final BiomePreference NOTHING = new BiomePreference(biome -> false);
    public static BiomePreference biomes(Biome... biomes) {
        List<Biome> list = Arrays.stream(biomes).collect(Collectors.toList());
        return new BiomePreference(list::contains);
    }
    public static BiomePreference categories(Biome.Category... categories) {
        List<Biome.Category> list = Arrays.stream(categories).collect(Collectors.toList());
        return new BiomePreference(biome -> list.contains(biome.getCategory()));
    }

    public boolean allow(Biome biome) {
        return preferredBiomes.test(biome);
    }
}
