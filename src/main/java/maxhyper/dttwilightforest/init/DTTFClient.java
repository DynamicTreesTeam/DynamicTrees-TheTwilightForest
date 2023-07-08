package maxhyper.dttwilightforest.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.GrassColor;

public class DTTFClient {

    public static void setup() {
        registerRenderLayers();
        registerColorHandlers();
    }

    private static void registerRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(DTTFRegistries.MANGROVE_ROOTS.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(DTTFRegistries.UNDERGROUND_ROOTS.get(), RenderType.cutoutMipped());
    }

    private static void registerColorHandlers(){

        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        blockColors.register((state, level, pos, tintIndex) -> {
                    if (tintIndex != 1) return 0xFFFFFF;
                    return level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.get(0.5D, 1.0D);
                },
                DTTFRegistries.MANGROVE_ROOTS.get(),
                DTTFRegistries.UNDERGROUND_ROOTS.get());

    }


}
