package maxhyper.dttwilightforest.init;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;
import maxhyper.dttwilightforest.canceller.DTTFTreeFeatureCanceller;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTTTFRegistries {


    public static final FeatureCanceller TREE_CANCELLER = new DTTFTreeFeatureCanceller<>(DynamicTreesTheTwilightForest.location("tree"), TreeConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER);
    }
}
