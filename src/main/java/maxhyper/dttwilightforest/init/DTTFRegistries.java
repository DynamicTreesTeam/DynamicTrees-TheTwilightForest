package maxhyper.dttwilightforest.init;

import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.google.common.base.Suppliers;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;
import maxhyper.dttwilightforest.blocks.BasicRootsBlock;
import maxhyper.dttwilightforest.blocks.UndergroundRootsBlock;
import maxhyper.dttwilightforest.canceller.DTTFTreeFeatureCanceller;
import maxhyper.dttwilightforest.cellkits.DTTFCellKits;
import maxhyper.dttwilightforest.genfeatures.DTTFGenFeatures;
import maxhyper.dttwilightforest.growthlogic.DTTFGrowthLogicKits;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTTFRegistries {

    public static final ResourceLocation MANGROVE_RESLOC = DynamicTreesTheTwilightForest.location("mangrove_roots");
    public static final ResourceLocation UNDERGROUND_RESLOC = DynamicTreesTheTwilightForest.location("underground_roots");

    public static final Supplier<BasicRootsBlock> MANGROVE_ROOTS = Suppliers.memoize(()->new BasicRootsBlock(MANGROVE_RESLOC));
    public static final Supplier<BasicRootsBlock> UNDERGROUND_ROOTS = Suppliers.memoize(()->new UndergroundRootsBlock(UNDERGROUND_RESLOC));

    public static void setup() {
        RegistryHandler.addBlock(MANGROVE_RESLOC, MANGROVE_ROOTS);
        RegistryHandler.addBlock(UNDERGROUND_RESLOC, UNDERGROUND_ROOTS);
    }

    public static final FeatureCanceller TREE_CANCELLER = new DTTFTreeFeatureCanceller<>(DynamicTreesTheTwilightForest.location("tree"), TreeConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER);
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
        DTTFGrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
        DTTFCellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
        DTTFGenFeatures.register(event.getRegistry());
    }

}
