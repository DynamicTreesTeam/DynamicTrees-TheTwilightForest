package maxhyper.dttwilightforest.growthlogic;

import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class CanopyLogic extends GrowthLogicKit {

    public CanopyLogic(ResourceLocation registryName) {
        super(registryName);
    }

    public static int heightLimitOverLowestBranch = 10;

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final BlockPos pos = context.pos();
        final GrowSignal signal = context.signal();
        final int[] probMap = context.probMap();

        if (pos.getY() > signal.rootPos.getY() + this.getLowestBranchHeight(configuration, context) + heightLimitOverLowestBranch){
            probMap[Direction.UP.ordinal()] = 0; //Forces the tree to flatten out 5 blocks above min branch
        }
        return super.populateDirectionProbabilityMap(configuration, context);
    }

}
