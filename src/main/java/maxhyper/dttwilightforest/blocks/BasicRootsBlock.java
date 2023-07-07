package maxhyper.dttwilightforest.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.Connections;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.ConnectionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

public class BasicRootsBlock extends BranchBlock implements SimpleWaterloggedBlock {

    public static final String NAME_SUFFIX = "_roots";

    protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
    //public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<GroundLogged> GROUND_LOGGED = EnumProperty.create("ground_logged", GroundLogged.class);

    public enum GroundLogged implements StringRepresentable {
        EXPOSED ("exposed", Blocks.AIR),
        GRASS ("grass", Blocks.GRASS_BLOCK),
        DIRT ("dirt", Blocks.DIRT),
        WATER ("water", Blocks.WATER);
        private final String name;
        private final Block block;
        GroundLogged(String pName, Block block) {
            this.name = pName;
            this.block = block;
        }
        @Override public @NotNull String getSerializedName() {
            return name;
        }
        public Block getBlock() {
            return block;
        }
    }

    public BasicRootsBlock(ResourceLocation name) {
        super(name, Properties.of(Material.WOOD).randomTicks());
        setCanBeStripped(false);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, GROUND_LOGGED);
    }

    protected boolean isWaterlogged (BlockState state){
        return GroundLogged.WATER.equals(state.getValue(GROUND_LOGGED));
    }
    protected boolean isExposed (BlockState state){
        return GroundLogged.EXPOSED.equals(state.getValue(GROUND_LOGGED));
    }
    protected boolean isSolid (BlockState state){
        return isSolid(state.getValue(GROUND_LOGGED));
    }
    protected boolean isSolid (GroundLogged loggingState){
        return loggingState != GroundLogged.EXPOSED && loggingState != GroundLogged.WATER;
    }

    public float rotChance(int radius, boolean waterlogged) {
        if (radius == 0) {
            return 0;
        }
        return 0.3f + ((1f / (radius * (waterlogged ? 3f : 1f) )));// Thicker branches take longer to postRot
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (!isSolid(pState) && pLevel.getRandom().nextFloat() < rotChance(getRadius(pState), isWaterlogged(pState))){
            Connections connections = getConnectionData(pLevel, pPos, pState);
            int numConnections = 0;
            for (int i : connections.getAllRadii()) {
                numConnections += (i != 0) ? 1 : 0;
            }
            if (numConnections <= 1){
                pLevel.setBlock(pPos, pState.getValue(GROUND_LOGGED).getBlock().defaultBlockState(), 2);
            }
        }
        super.randomTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return 0;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return getRadius(state);
    }

    @Override
    public int getRadius(BlockState state) {
        return state.getValue(RADIUS);
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @javax.annotation.Nullable Direction originDir, int flags) {
        destroyMode = DynamicTrees.DestroyMode.SET_RADIUS;
        GroundLogged loggedState = GroundLogged.EXPOSED;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BasicRootsBlock)
            loggedState = state.getValue(GROUND_LOGGED);
        if (state.is(Blocks.DIRT))
            loggedState = GroundLogged.DIRT;
        else if (state.is(Blocks.GRASS_BLOCK))
            loggedState = GroundLogged.GRASS;
        else if (state.getFluidState() == Fluids.WATER.getSource(false) && radius <= maxRadiusForWaterLogging)
            loggedState = GroundLogged.WATER;
        level.setBlock(pos, getStateForRadius(radius).setValue(GROUND_LOGGED, loggedState), flags);
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;
        return radius;
    }

    @Override
    public BlockState getStateForRadius(int radius) {
        return defaultBlockState().setValue(RADIUS, radius);
    }

    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        return signal;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos pos) {
        return Family.NULL_FAMILY;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public boolean checkForRot(LevelAccessor level, BlockPos pos, Species species, int fertility, int radius, Random rand, float chance, boolean rapid) {
        return false;
    }

    ///////////////////////////////////////////
    // SOUNDS
    ///////////////////////////////////////////

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        GroundLogged logged = state.getValue(GROUND_LOGGED);
        if (logged == GroundLogged.DIRT)
            return SoundType.GRAVEL;
        if (logged == GroundLogged.GRASS)
            return SoundType.GRASS;
        return super.getSoundType(state, level, pos, entity);
    }


    ///////////////////////////////////////////
    // WATERLOGGING
    ///////////////////////////////////////////

    protected int maxRadiusForWaterLogging = 7; //the maximum radius for a branch to be allowed to be water logged

    @Override
    public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        if (getRadius(pState) > maxRadiusForWaterLogging) {
            return false;
        }
        return isExposed(pState) && pFluid == Fluids.WATER;
    }

    @Override
    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (isExposed(pState) && pFluidState.getType() == Fluids.WATER) {
            if (!pLevel.isClientSide()) {
                pLevel.setBlock(pPos, pState.setValue(GROUND_LOGGED, GroundLogged.WATER), 3);
                pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        if (isWaterlogged(pState)) {
            pLevel.setBlock(pPos, pState.setValue(GROUND_LOGGED, GroundLogged.EXPOSED), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }
            return new ItemStack(Items.WATER_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return isWaterlogged(state) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (isWaterlogged(stateIn)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    ///////////////////////////////////////////
    // BRANCH OVERRIDES
    ///////////////////////////////////////////

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        GroundLogged logged = state.getValue(GROUND_LOGGED);
        if (isSolid(logged)){
            level.setBlock(pos, state.setValue(GROUND_LOGGED, GroundLogged.EXPOSED), level.isClientSide ? 11 : 3);
            this.spawnDestroyParticles(level, player, pos, state);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.isCreative()) dropResources(logged.getBlock().defaultBlockState(), level, pos);
            return false;
        }

        playerWillDestroy(level, pos, state, player);
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            level.removeBlockEntity(pos);
        }
    }

    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        this.spawnDestroyParticles(pLevel, pPlayer, pPos, pState);
        if (pState.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(pPlayer, false);
        }

        pLevel.gameEvent(pPlayer, GameEvent.BLOCK_DESTROY, pPos);
    }

    protected boolean canPlace(Player player, Level level, BlockPos clickedPos, BlockState pState) {
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return pState.canSurvive(level, clickedPos) && level.isUnobstructed(pState, clickedPos, collisioncontext);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isSolid(state)) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(hand);
        GroundLogged loggedBlock = null;
        SoundEvent sound = null;
        if (handStack.getItem() == Items.DIRT){
            loggedBlock = GroundLogged.DIRT;
            sound = SoundEvents.GRAVEL_PLACE;
        } else if (handStack.getItem() == Items.GRASS_BLOCK){
            loggedBlock = GroundLogged.GRASS;
            sound = SoundEvents.GRASS_PLACE;
        }
        if (loggedBlock != null){
            BlockState newState = state.setValue(GROUND_LOGGED, loggedBlock);
            if (canPlace(player, level, pos, newState)){
                level.setBlock(pos, newState, 3);
                if (!player.isCreative()) handStack.shrink(1);
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1f, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        GroundLogged loggingState = state.getValue(GROUND_LOGGED);
        if (loggingState != GroundLogged.EXPOSED && loggingState != GroundLogged.WATER) {
            return Shapes.block();
        }
        int thisRadiusInt = getRadius(state);
        double radius = thisRadiusInt / 16.0;
        VoxelShape core = Shapes.box(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);

        for (Direction dir : Direction.values()) {
            int sideRadiusInt = Math.min(getSideConnectionRadius(level, pos, thisRadiusInt, dir), thisRadiusInt);
            double sideRadius = sideRadiusInt / 16.0f;
            if (sideRadius > 0.0f) {
                double gap = 0.5f - sideRadius;
                AABB aabb = new AABB(0.5 - sideRadius, 0.5 - sideRadius, 0.5 - sideRadius, 0.5 + sideRadius, 0.5 + sideRadius, 0.5 + sideRadius);
                aabb = aabb.expandTowards(dir.getStepX() * gap, dir.getStepY() * gap, dir.getStepZ() * gap);
                core = Shapes.or(core, Shapes.create(aabb));
            }
        }

        return core;
    }

    protected int getSideConnectionRadius(BlockGetter level, BlockPos pos, int radius, Direction side) {
        final BlockPos deltaPos = pos.relative(side);
        final BlockState blockState = CoordUtils.getStateSafe(level, deltaPos);

        // If adjacent block is not loaded assume there is no connection.
        return blockState == null ? 0 : TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, level, deltaPos, this, side, radius);
    }

}
