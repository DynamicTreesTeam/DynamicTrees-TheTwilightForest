package maxhyper.dttwilightforest.blocks;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RootSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(RootSoilProperties::new);

    public RootSoilProperties(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootRootyBlock(this, blockProperties);
    }

}
