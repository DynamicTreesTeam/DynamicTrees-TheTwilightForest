package maxhyper.dttwilightforest.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(MangroveSpecies::new);

    private SoilProperties defaultSoil;
    private int worldgenHeightOffset = 4;

    public void setDefaultSoil(SoilProperties defaultSoil) {
        this.defaultSoil = defaultSoil;
    }

    public void setWorldgenHeightOffset(int worldgenHeightOffset) {
        this.worldgenHeightOffset = worldgenHeightOffset;
    }

    public MangroveSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    public boolean placeRootyDirtBlock(LevelAccessor level, BlockPos rootPos, int fertility) {
        BlockState dirtState = level.getBlockState(rootPos);
        Block dirt = dirtState.getBlock();

        if (!SoilHelper.isSoilRegistered(dirt) && !(dirt instanceof RootyBlock)) {
            //soil is not valid so we place default roots
            level.setBlock(rootPos, defaultSoil.getSoilState(dirtState, fertility, this.doesRequireTileEntity(level, rootPos)), 3);
            return true;
        }

        return super.placeRootyDirtBlock(level, rootPos, fertility);
    }

    @Override
    public boolean generate(GenerationContext context) {
        context.rootPos().move(Direction.UP, worldgenHeightOffset);
        return super.generate(context);
    }

}
