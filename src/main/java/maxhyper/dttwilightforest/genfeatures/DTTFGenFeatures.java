package maxhyper.dttwilightforest.genfeatures;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;

public class DTTFGenFeatures {

    public static final GenFeature UNDERGROUND_ROOTS = new UndergroundRootsGenFeature(DynamicTreesTheTwilightForest.location("underground_roots"));

    public static void register(final Registry<GenFeature> registry) {
        registry.registerAll(UNDERGROUND_ROOTS);
    }


}
