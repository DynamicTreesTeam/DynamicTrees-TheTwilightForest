package maxhyper.dttwilightforest.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemapper.StateNode;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.Connections;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import maxhyper.dttwilightforest.DynamicTreesTheTwilightForest;
import maxhyper.dttwilightforest.nodes.RootsDestroyerNode;
import maxhyper.dttwilightforest.nodes.SolidRootsNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

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

    public BasicRootsBlock(ResourceLocation name, BlockBehaviour.Properties properties) {
        super(ResourceLocationUtils.suffix(name, NAME_SUFFIX), properties);
        setCanBeStripped(false);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, GROUND_LOGGED);
    }

    public boolean isWaterlogged (BlockState state){
        return GroundLogged.WATER.equals(state.getValue(GROUND_LOGGED));
    }
    public boolean isExposed (BlockState state){
        return GroundLogged.EXPOSED.equals(state.getValue(GROUND_LOGGED));
    }
    public boolean isSolid (BlockState state){
        return isSolid(state.getValue(GROUND_LOGGED));
    }
    protected boolean isSolid (GroundLogged loggingState){
        return loggingState != GroundLogged.EXPOSED && loggingState != GroundLogged.WATER;
    }
    public Block getLoggedBlock (BlockState rootState){
        return rootState.getValue(GROUND_LOGGED).getBlock();
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
        return !isSolid(state);
        //return true;
    }

    protected int getMaxSignalDepth() {
        return 64;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        if (signal.overflow || (signal.trackVisited && signal.doTrackingVisited(pos))) {
            return signal;
        }

        if (signal.depth++ < getMaxSignalDepth()) {// Prevents going too deep into large networks, or worse, being caught in a network loop
            signal.run(state, level, pos, fromDir);// Run the inspectors of choice
            for (Direction dir : Direction.values()) {// Spread signal in various directions
                if (dir != fromDir) {// don't count where the signal originated from
                    BlockPos deltaPos = pos.relative(dir);

                    BlockState deltaState = level.getBlockState(deltaPos);
                    TreePart treePart = TreeHelper.getTreePart(deltaState);

                    if (treePart.shouldAnalyse(deltaState, level, deltaPos)) {
                        signal = treePart.analyse(deltaState, level, deltaPos, dir.getOpposite(), signal);

                        // This should only be true for the originating block when the root node is found
                        if (signal.foundRoot && signal.localRootDir == null && fromDir == null) {
                            signal.localRootDir = dir;
                        }
                    }
                }
            }
            signal.returnRun(state, level, pos, fromDir);
        } else {
            BlockState state2 = level.getBlockState(pos);
            if (signal.destroyLoopedNodes && state2.getBlock() instanceof BranchBlock branch) {
                branch.breakDeliberate(level, pos, DynamicTrees.DestroyMode.OVERFLOW);// Destroy one of the offending nodes
            }
            signal.overflow = true;
        }
        signal.depth--;

        return signal;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter level, BlockPos pos) {
        Family family = super.getFamily(state, level, pos);
        if (!family.isValid()){
            //cheap way to fix drops issues
            return Family.REGISTRY.get(DynamicTreesTheTwilightForest.location("mangrove"));
        }
        return family;
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
    // BRANCH DESTRUCTION
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
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
//        playerWillDestroy(level, pos, state, player);
//        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

//    @Override
//    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
//        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
//            level.removeBlockEntity(pos);
//        }
//    }

//    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
//        this.spawnDestroyParticles(pLevel, pPlayer, pPos, pState);
//        if (pState.is(BlockTags.GUARDED_BY_PIGLINS)) {
//            PiglinAi.angerNearbyPiglins(pPlayer, false);
//        }
//
//        pLevel.gameEvent(pPlayer, GameEvent.BLOCK_DESTROY, pPos);
//    }

    @Override
    public void futureBreak(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        // Tries to get the face being pounded on.
        final double reachDistance = entity instanceof Player ? Objects.requireNonNull(entity.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue() : 5.0D;
        final BlockHitResult ragTraceResult = this.playerRayTrace(entity, reachDistance, 1.0F);
        final Direction toolDir = ragTraceResult != null ? (entity.isShiftKeyDown() ? ragTraceResult.getDirection().getOpposite() : ragTraceResult.getDirection()) : Direction.DOWN;

        // Play and render block break sound and particles (must be done before block is broken).
        level.levelEvent(null, 2001, cutPos, getId(state));

        // Do the actual destruction.
        final BranchDestructionData destroyData = this.destroyBranchFromNode(level, cutPos, toolDir, false, entity);

        // Get all of the wood drops.
        final ItemStack heldItem = entity.getMainHandItem();
        final int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
        final float fortuneFactor = 1.0f + 0.25f * fortune;
        final NetVolumeNode.Volume woodVolume = destroyData.woodVolume; // The amount of wood calculated from the body of the tree network.
        woodVolume.multiplyVolume(fortuneFactor);
        final List<ItemStack> woodItems = destroyData.species.getBranchesDrops(level, woodVolume, heldItem);

        // Drop the FallingTreeEntity into the level.
        FallingTreeEntity.dropTree(level, destroyData, woodItems, FallingTreeEntity.DestroyType.HARVEST);

        // Damage the axe by a prescribed amount.
        this.damageAxe(entity, heldItem, this.getRadius(state), woodVolume, true);
    }

    public BranchDestructionData destroyBranchFromNode(Level level, BlockPos cutPos, Direction toolDir, boolean wholeTree, @javax.annotation.Nullable final LivingEntity entity) {
        final BlockState blockState = level.getBlockState(cutPos);
        final SolidRootsNode solidRootsNode = new SolidRootsNode();
        final MapSignal signal = analyse(blockState, level, cutPos, null, new MapSignal(solidRootsNode)); // Analyze entire tree network to find root node and species.
        BlockState rootyState = null;
        RootyBlock rooty = null;
        if (signal.root != null){
            rootyState = level.getBlockState(signal.root);
            rooty = TreeHelper.getRooty(rootyState);
        }
        final Species species = rooty != null ? rooty.getSpecies(rootyState, level, signal.root) : Species.REGISTRY.get(DynamicTreesTheTwilightForest.location("mangrove"));

        // Analyze only part of the tree beyond the break point and map out the extended block states.
        // We can't destroy the branches during this step since we need accurate extended block states that include connections.
        StateNode stateMapper = new StateNode(cutPos);
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(stateMapper));

        // Analyze only part of the tree beyond the break point and calculate it's volume, then destroy the branches.
        final NetVolumeNode volumeSum = new NetVolumeNode();
        final RootsDestroyerNode destroyer = new RootsDestroyerNode();
        destroyMode = DynamicTrees.DestroyMode.HARVEST;
        this.analyse(blockState, level, cutPos, wholeTree ? null : signal.localRootDir, new MapSignal(volumeSum, destroyer));
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;

        // Calculate main trunk height.
        int trunkHeight = 1;
        for (BlockPos iter = new BlockPos(0, 1, 0); stateMapper.getBranchConnectionMap().containsKey(iter); iter = iter.above()) {
            trunkHeight++;
        }

        Direction cutDir = signal.localRootDir;
        if (cutDir == null) {
            cutDir = Direction.DOWN;
        }

        return new BranchDestructionData(species, stateMapper.getBranchConnectionMap(), new HashMap<>(), new ArrayList<>(), destroyer.getEnds(), volumeSum.getVolume(), cutPos, cutDir, toolDir, trunkHeight);
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

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (isSolid(state)){
            return new ItemStack(getLoggedBlock(state));
        }
        return new ItemStack(asItem());
    }
}
