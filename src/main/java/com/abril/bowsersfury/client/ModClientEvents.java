package com.abril.bowsersfury.client;

import com.abril.bowsersfury.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ModClientEvents {
    public static void register(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        EntityRenderersEvent.RegisterLayerDefinitions.getBus(modBusGroup).addListener(ModClientEvents::layers);
        EntityRenderersEvent.RegisterRenderers.getBus(modBusGroup).addListener(ModClientEvents::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FuryBowserModel.LAYER, FuryBowserModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FURY_BOWSER.get(), FuryBowserRenderer::new);
    }

    private ModClientEvents() {}
}
