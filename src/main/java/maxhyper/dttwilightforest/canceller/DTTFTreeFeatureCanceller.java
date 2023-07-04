package maxhyper.dttwilightforest.canceller;

import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.TreeFeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Set;

public class DTTFTreeFeatureCanceller<T extends FeatureConfiguration> extends TreeFeatureCanceller<T> {

    private final Class<T> treeFeatureConfigClass;

    public DTTFTreeFeatureCanceller(final ResourceLocation registryName, Class<T> treeFeatureConfigClass) {
        super(registryName, treeFeatureConfigClass);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        if (featureConfig instanceof RandomFeatureConfiguration) {
            for (WeightedPlacedFeature feature : ((RandomFeatureConfiguration) featureConfig).features) {
                final PlacedFeature currentConfiguredFeature = feature.feature.value();
                final ResourceLocation featureRegistryName = currentConfiguredFeature.getFeatures().findFirst().get().feature().getRegistryName();

                if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.feature().value().config()) && featureRegistryName != null &&
                        namespaces.contains(featureRegistryName.getNamespace())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}
