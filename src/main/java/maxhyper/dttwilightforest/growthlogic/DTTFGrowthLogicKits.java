package maxhyper.dttwilightforest.growthlogic;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;

public class DTTFGrowthLogicKits {

    public static final GrowthLogicKit CANOPY = new CanopyLogic(DynamicTreesTheTwilightForest.location("canopy"));
    public static final GrowthLogicKit MINING = new MiningTreeLogic(DynamicTreesTheTwilightForest.location("mining"));

    public static void register(final Registry<GrowthLogicKit> registry) {
        registry.registerAll(CANOPY, MINING);
    }

}
