package maxhyper.dttwilightforest.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import maxhyper.dttwilightforest.blocks.BasicRootsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class UndergroundRootsGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> ROOTS = ConfigurationProperty.block("roots");
    public static final ConfigurationProperty<Block> SECONDARY_ROOTS = ConfigurationProperty.block("secondary_roots");
    public static final ConfigurationProperty<Float> ROOT_BRANCH_CHANCE = ConfigurationProperty.floatProperty("root_branch_chance");
    public static final ConfigurationProperty<Float> GROW_CHANCE = ConfigurationProperty.floatProperty("grow_chance");
    public static final ConfigurationProperty<Float> FAIL_EXPOSED_CHANCE = ConfigurationProperty.floatProperty("fail_underground_chance");
    public static final ConfigurationProperty<Integer> MAX_RADIUS = ConfigurationProperty.integer("max_radius");
    public static final ConfigurationProperty<Integer> WORLD_GEN_MAX_PULSES = ConfigurationProperty.integer("world_gen_max_attempts");
    public static final ConfigurationProperty<Integer> MAX_DEPTH = ConfigurationProperty.integer("max_depth");
    public static final ConfigurationProperty<Integer> SECONDARY_THRESHOLD = ConfigurationProperty.integer("secondary_threshold");

    public UndergroundRootsGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(ROOTS, Blocks.AIR)
                .with(SECONDARY_ROOTS, Blocks.AIR)
                .with(ROOT_BRANCH_CHANCE, 0.2f)
                .with(GROW_CHANCE, 0.1f)
                .with(FAIL_EXPOSED_CHANCE, 0.4f)
                .with(MAX_RADIUS, 8)
                .with(WORLD_GEN_MAX_PULSES, 20)
                .with(MAX_DEPTH, 20)
                .with(SECONDARY_THRESHOLD, 4);
    }
    @Override
    protected void registerProperties() {
        this.register(ROOTS, SECONDARY_ROOTS, ROOT_BRANCH_CHANCE, GROW_CHANCE, FAIL_EXPOSED_CHANCE, MAX_RADIUS, WORLD_GEN_MAX_PULSES, MAX_DEPTH, SECONDARY_THRESHOLD);
    }

    Direction[] dirsExceptUp = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    Direction[] dirsAll = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};

    private boolean isGroundBlock(BlockState block){
        return SoilHelper.isSoilAcceptable(block, SoilHelper.getSoilFlags(SoilHelper.DIRT_LIKE));
    }
    private boolean isGrass(BlockState block){
        return block.is(Blocks.GRASS_BLOCK);
    }
    private boolean isWater(BlockState block){
        return block.is(Blocks.WATER);
    }
    private boolean isThisRootBlock(BlockState state, GenFeatureConfiguration configuration){
        return isAnyRootBlock(state)
                && (state.is(configuration.get(ROOTS)) || state.is(configuration.get(SECONDARY_ROOTS)));
    }
    private boolean isAnyRootBlock(BlockState state){
        return state.getBlock() instanceof BasicRootsBlock;
    }
    private int getRootRadius(BlockState state){
        if (state.getBlock() instanceof BasicRootsBlock)
            return ((BasicRootsBlock)state.getBlock()).getRadius(state);
        return 0;
    }
    private BlockState getRootState(int radius, BasicRootsBlock.GroundLogged logging, boolean secondary, GenFeatureConfiguration configuration){
        BasicRootsBlock roots;
        if (secondary && configuration.get(SECONDARY_ROOTS) instanceof BasicRootsBlock){
            roots = ((BasicRootsBlock) configuration.get(SECONDARY_ROOTS));
        } else if (configuration.get(ROOTS) instanceof BasicRootsBlock) {
            roots = ((BasicRootsBlock) configuration.get(ROOTS));
        } else {
            return Blocks.AIR.defaultBlockState();
        }
        return roots.getStateForRadius(radius).setValue(BasicRootsBlock.GROUND_LOGGED, logging);
    }

    private boolean checkAvailableAround(Level world, BlockPos blockPos, Direction cameFrom){
        for (int i=0;i<6;i++){
            Direction dir = dirsAll[i];
            if (dir == cameFrom){
                continue;
            }
            BlockPos offPos = blockPos.offset(dir.getNormal());
            if(world.isLoaded(offPos)) {
                if (TreeHelper.isBranch(world.getBlockState(offPos))){
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private Direction findRandomFreeDir(Level world, BlockPos blockPos, Random rand, Direction cameFrom, BlockPos rootPos){
//        if (rand.nextInt(3) == 0){
//            return null;
//        }
        boolean underTrunk = rootPos.getX() == blockPos.getX() && rootPos.getZ() == blockPos.getZ();

        Direction[] possibleDirections = new Direction[5];
        int dirCount = 0;
        for (int i=0;i<5;i++){
            Direction dir = dirsExceptUp[i];
            if (dir == cameFrom) continue;
            if (dir == Direction.DOWN && underTrunk) continue;

            BlockState offsetState = world.getBlockState(blockPos.offset(dir.getNormal()));
            if ((offsetState.isAir() || isWater(offsetState) || isGroundBlock(offsetState)) && checkAvailableAround(world,blockPos.offset(dir.getNormal()), dir.getOpposite())){
                if (dir==Direction.DOWN && rand.nextInt(8) != 0){
                    return Direction.DOWN;
                }
                possibleDirections[dirCount] = dir;
                dirCount++;
            }
        }
        if (dirCount == 0) return null;
        return possibleDirections[rand.nextInt(dirCount)];
    }

    private Direction findRandomRootDir(Level world, BlockPos blockPos, Random rand, Direction cameFrom, BlockPos rootPos, GenFeatureConfiguration configuration){
        Direction[] possibleDirections = new Direction[5];
        int dirCount = 0;
        for (int i=0;i<5;i++){
            Direction dir = dirsExceptUp[i];
            if (dir == cameFrom){
                continue;
            }
            BlockState offsetState = world.getBlockState(blockPos.offset(dir.getNormal()));
            if (isThisRootBlock(offsetState, configuration)){
//                if (dir==Direction.DOWN && rand.nextInt(4) != 0){
//                    return Direction.DOWN;
//                }
                possibleDirections[dirCount] = dir;
                dirCount++;
            }
        }
        if (dirCount == 0) return findRandomFreeDir(world, blockPos, rand, cameFrom, rootPos);
        return possibleDirections[rand.nextInt(dirCount)];
    }

//    private boolean isOverRootBlock(BlockPos pos, BlockPos rootPos){
//        return offsetSpawn(pos, true) != rootPos;
//    }
//
//    public BlockPos offsetSpawn(BlockPos root){
//        return offsetSpawn(root, false);
//    }
//    public BlockPos offsetSpawn(BlockPos root, boolean invert){
//        if (invert){
//            return root.above();
//        } else {
//            return root.below();
//        }
//    }

    private boolean cancelGrowChance(Random rand, GenFeatureConfiguration configuration){
        return rand.nextFloat() < configuration.get(FAIL_EXPOSED_CHANCE);
    }

    private boolean iterateRootGrow(Level world, BlockPos blockPos, Random rand, int radius, Direction cameFrom, BlockPos rootPos, int currentStep, GenFeatureConfiguration configuration){
        if (!TreeHelper.isRooty(world.getBlockState(rootPos))) return false;
        if (currentStep > configuration.get(MAX_DEPTH)) return false;
        BlockState state = world.getBlockState(blockPos);

        int deltaY = rootPos.getY() - blockPos.getY();
        boolean secondary = deltaY > configuration.get(SECONDARY_THRESHOLD);

        if (isThisRootBlock(state, configuration)){
            int currentRadius = getRootRadius(state);
            boolean grow = currentRadius<radius && currentRadius<8; //isOverRootBlock(blockPos, rootPos) &&
            radius = currentRadius;
            ((BasicRootsBlock)state.getBlock()).setRadius(world, blockPos, currentRadius + (grow?1:0), cameFrom);
        }
        else if (world.isEmptyBlock(blockPos) || isWater(state)){
            if (cancelGrowChance(rand, configuration))
                return false;
            world.setBlock(blockPos, getRootState(radius > 4 ? radius / 2 : 2, isWater(state) ? BasicRootsBlock.GroundLogged.WATER : BasicRootsBlock.GroundLogged.EXPOSED, secondary, configuration), 3);
        }
        else if (isGroundBlock(state)){
            world.setBlock(blockPos, getRootState(radius > 4 ? radius / 2 : 2, isGrass(state) ? BasicRootsBlock.GroundLogged.GRASS : BasicRootsBlock.GroundLogged.DIRT, secondary, configuration), 3);
        }
        else {
            return false;
        }

        if (rand.nextFloat() <= 0.2)
            radius--;
        if (radius > 0){
            Direction chosenDir = findRandomRootDir(world,blockPos,rand, cameFrom, rootPos, configuration);
            if (chosenDir == null) return true;
            iterateRootGrow(world,blockPos.offset(chosenDir.getNormal()), rand, radius, chosenDir.getOpposite(), rootPos, currentStep + 1, configuration);
            if ( (rootPos.getY() == blockPos.getY()) || rand.nextFloat() < configuration.get(ROOT_BRANCH_CHANCE)){ //isOverRootBlock(blockPos, rootPos) ||
                chosenDir = findRandomFreeDir(world,blockPos, rand, cameFrom, rootPos);
                if (chosenDir == null) return true;
                iterateRootGrow(world,blockPos.offset(chosenDir.getNormal()), rand, radius, chosenDir.getOpposite(), rootPos, currentStep + 1, configuration);
            }
        }
        return true;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        Random rand = new Random();
        Level world = context.levelContext().level();
        BlockPos blockPos = context.pos();

        BlockState state = world.getBlockState(blockPos.below());
        if (!isAnyRootBlock(state)){
            boolean placed = iterateRootGrow(world, blockPos.below(), rand, 2, Direction.UP, blockPos, 0, configuration);
            if (!placed) return false;
        }
        for (int a = configuration.get(WORLD_GEN_MAX_PULSES); a>0; a--){
            //int radius = getRootRadius(state);
            float chance = configuration.get(GROW_CHANCE);
            boolean grow = chance > 0 && rand.nextFloat() < chance;
            iterateRootGrow(world, blockPos.below(), rand, getRootRadius(state)+(grow?1:0), Direction.UP, blockPos, 0, configuration);
        }
        return true;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() <= 0) return false;
        Random rand = new Random();
        Level level = context.levelContext().level();
        BlockPos blockPos = context.pos();

        BlockState downState = level.getBlockState(blockPos.below());
        if (!isAnyRootBlock(downState)){
            return iterateRootGrow(level, blockPos.below(), rand, 2, Direction.UP, blockPos, 0, configuration);
        } else {
            int radius = getRootRadius(downState);
            int treeRadius = TreeHelper.getRadius(level, blockPos.above());
            if (radius < Math.min(treeRadius, configuration.get(MAX_RADIUS))){
                float chance = configuration.get(GROW_CHANCE);
                boolean grow = chance > 0 && rand.nextFloat() < chance;
                iterateRootGrow(level, blockPos.below(), rand, radius+(grow?1:0), Direction.UP, blockPos, 0, configuration);
            }
        }
        return true;
    }
}
