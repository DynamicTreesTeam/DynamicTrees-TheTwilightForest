package maxhyper.dttwilightforest.blocks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

public class UndergroundRootsBlock extends BasicRootsBlock {

    public static final BooleanProperty LIVE = BooleanProperty.create("live");

    public UndergroundRootsBlock(ResourceLocation name) {
        super(name, Properties.of(Material.WOOD).randomTicks());
        registerDefaultState(defaultBlockState().setValue(LIVE, false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, GROUND_LOGGED, LIVE);
    }

}
