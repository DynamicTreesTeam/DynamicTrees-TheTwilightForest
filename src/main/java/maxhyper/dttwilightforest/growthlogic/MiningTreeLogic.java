package maxhyper.dttwilightforest.growthlogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.DirectionManipulationContext;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class MiningTreeLogic extends GrowthLogicKit {

    public MiningTreeLogic(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HEIGHT_VARIATION, 3);
    }

    @Override
    protected void registerProperties() {
        this.register(HEIGHT_VARIATION);
    }

    private Direction getRelativeFace (BlockPos signalPos, BlockPos rootPos){
        if (signalPos.getZ() < rootPos.getZ()){
            return Direction.NORTH;
        } else if (signalPos.getZ() > rootPos.getZ()){
            return Direction.SOUTH;
        }else if (signalPos.getX() > rootPos.getX()){
            return Direction.EAST;
        }else if (signalPos.getX() < rootPos.getX()){
            return Direction.WEST;
        }else {
            return Direction.UP;
        }
    }

    @Override
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        BlockPos pos = context.pos();
        GrowSignal signal = context.signal();
        int[] probMap = super.populateDirectionProbabilityMap(configuration,context);
        Level level = context.level();
        Direction originDir = signal.dir.getOpposite();
        final int lowestBranch = this.getLowestBranchHeight(configuration, new PositionalSpeciesContext(context.level(), signal.rootPos, context.species()));

        if (pos.getY() >= signal.rootPos.getY() + lowestBranch  || !signal.isInTrunk()){
            probMap[Direction.UP.ordinal()] = 0;
            for (Direction dir: Direction.Plane.HORIZONTAL){
                if (TreeHelper.isBranch(level.getBlockState(pos.offset(dir.getNormal())))){
                    probMap[dir.getClockWise(Direction.Axis.Y).ordinal()] = probMap[dir.getCounterClockWise(Direction.Axis.Y).ordinal()] = 0;
                }
            }
        }
        if (!signal.isInTrunk()){
            Direction relativePosToRoot = getRelativeFace(pos, signal.rootPos);
            for (Direction dir: Direction.Plane.HORIZONTAL){
                probMap[dir.ordinal()] = 0;
            }
            Direction[] sides = {relativePosToRoot.getClockWise(Direction.Axis.Y), relativePosToRoot.getCounterClockWise(Direction.Axis.Y), Direction.UP};
            for (Direction dirSides: sides){
                if (level.isEmptyBlock(pos.offset(dirSides.getNormal())) && TreeHelper.getRadius(level, pos) > 1){
                    probMap[dirSides.ordinal()] = 1;
                }
            }
            boolean isBranchSide = TreeHelper.isBranch(level.getBlockState(pos.offset(relativePosToRoot.getNormal())));
            boolean isBranchDown = TreeHelper.isBranch(level.getBlockState(pos.below()));
            probMap[Direction.DOWN.ordinal()] = isBranchSide && !isBranchDown? 0:1;
            probMap[relativePosToRoot.ordinal()] = isBranchDown && !isBranchSide? 0:1;
        }
        probMap[originDir.ordinal()] = 0;

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
