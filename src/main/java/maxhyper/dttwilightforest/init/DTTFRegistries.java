package maxhyper.dttwilightforest.init;

import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypeRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.base.Suppliers;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;
import maxhyper.dttwilightforest.blocks.BasicRootsBlock;
import maxhyper.dttwilightforest.blocks.RootSoilProperties;
import maxhyper.dttwilightforest.blocks.UndergroundRootsBlock;
import maxhyper.dttwilightforest.canceller.DTTFTreeFeatureCanceller;
import maxhyper.dttwilightforest.cellkits.DTTFCellKits;
import maxhyper.dttwilightforest.genfeatures.DTTFGenFeatures;
import maxhyper.dttwilightforest.growthlogic.DTTFGrowthLogicKits;
import maxhyper.dttwilightforest.trees.MangroveFamily;
import maxhyper.dttwilightforest.trees.MangroveSpecies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTTFRegistries {

    public static final ResourceLocation MANGROVE_RESLOC = DynamicTreesTheTwilightForest.location("mangrove_roots");
    public static final ResourceLocation UNDERGROUND_RESLOC = DynamicTreesTheTwilightForest.location("underground_roots");
    public static final ResourceLocation LIVE_UNDERGROUND_RESLOC = DynamicTreesTheTwilightForest.location("live_underground_roots");

    public static final Supplier<BasicRootsBlock> MANGROVE_ROOTS = Suppliers.memoize(()->new BasicRootsBlock(MANGROVE_RESLOC, BlockBehaviour.Properties.of(Material.WOOD).randomTicks()));
    public static final Supplier<BasicRootsBlock> UNDERGROUND_ROOTS = Suppliers.memoize(()->new BasicRootsBlock(UNDERGROUND_RESLOC, BlockBehaviour.Properties.of(Material.WOOD).randomTicks()));
    public static final Supplier<BasicRootsBlock> LIVE_UNDERGROUND_ROOTS = Suppliers.memoize(()->new BasicRootsBlock(LIVE_UNDERGROUND_RESLOC, BlockBehaviour.Properties.of(Material.WOOD).randomTicks()));

    public static void setup() {
        RegistryHandler.addBlock(MANGROVE_RESLOC, MANGROVE_ROOTS);
        RegistryHandler.addItem(MANGROVE_RESLOC, ()-> new BlockItem(MANGROVE_ROOTS.get(), new Item.Properties()));

        RegistryHandler.addBlock(UNDERGROUND_RESLOC, UNDERGROUND_ROOTS);
        RegistryHandler.addItem(UNDERGROUND_RESLOC, ()-> new BlockItem(UNDERGROUND_ROOTS.get(), new Item.Properties()));

        RegistryHandler.addBlock(LIVE_UNDERGROUND_RESLOC, LIVE_UNDERGROUND_ROOTS);
        RegistryHandler.addItem(LIVE_UNDERGROUND_RESLOC, ()-> new BlockItem(LIVE_UNDERGROUND_ROOTS.get(), new Item.Properties()));
    }

    @SubscribeEvent
    public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
        event.registerType(DynamicTreesTheTwilightForest.location("mangrove"), MangroveFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
        event.registerType(DynamicTreesTheTwilightForest.location("mangrove"), MangroveSpecies.TYPE);
    }

    @SubscribeEvent
    public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
        event.registerType(DynamicTreesTheTwilightForest.location("roots"), RootSoilProperties.TYPE);
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
