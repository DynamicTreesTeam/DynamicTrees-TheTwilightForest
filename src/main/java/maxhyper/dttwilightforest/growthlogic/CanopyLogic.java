package maxhyper.dttwilightforest.growthlogic;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class CanopyLogic extends GrowthLogicKit {

    public static final ConfigurationProperty<Integer> CANOPY_HEIGHT = ConfigurationProperty.integer("canopy_height");

    public CanopyLogic(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(CANOPY_HEIGHT, 6)
                .with(HEIGHT_VARIATION, 5);
    }

    @Override
    protected void registerProperties() {
        this.register(CANOPY_HEIGHT, HEIGHT_VARIATION);
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
        final GrowSignal signal = context.signal();
        final int lowestBranch = this.getLowestBranchHeight(configuration, new PositionalSpeciesContext(context.level(), signal.rootPos, context.species()));

        if ((signal.isInTrunk() && signal.delta.getY() > lowestBranch) ||
                signal.delta.getY() >= lowestBranch + configuration.get(CANOPY_HEIGHT)){
            probMap[Direction.UP.ordinal()] = 0; //Forces the tree to flatten out 6 blocks above min branch
        }
        probMap[Direction.DOWN.ordinal()] = 0;
        return probMap;
    }

    @Override
    public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return (int)(super.getLowestBranchHeight(configuration, context) + getHashVariation(configuration, context));
    }

    protected float getHashVariation (GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context){
        long day = context.level().getGameTime() / 24000L;
        int month = (int) day / 30;//Change the hashs every in-game month

        return  (CoordUtils.coordHashCode(context.pos().above(month), 2) % configuration.get(HEIGHT_VARIATION));
    }

}
