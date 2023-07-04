package maxhyper.dttwilightforest.growthlogic;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;
import net.minecraft.resources.ResourceLocation;

public class DTTTFGrowthLogicKits {

    public static final GrowthLogicKit CANOPY = new CanopyLogic(DynamicTreesTheTwilightForest.location("canopy"));
    public static void register(final Registry<GrowthLogicKit> registry) {
        registry.registerAll(CANOPY);
    }

}
