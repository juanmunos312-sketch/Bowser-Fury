package com.abril.bowsersfury.client;

import com.abril.bowsersfury.entity.FuryBowserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class FuryBowserRenderer extends MobRenderer<FuryBowserEntity, FuryBowserRenderState, FuryBowserModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("bowsersfury", "textures/entity/fury_bowser.png");

    public FuryBowserRenderer(EntityRendererProvider.Context context) {
        super(context, new FuryBowserModel(context.bakeLayer(FuryBowserModel.LAYER)), 18.0F);
    }

    @Override public FuryBowserRenderState createRenderState() { return new FuryBowserRenderState(); }

    @Override public void extractRenderState(FuryBowserEntity entity, FuryBowserRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override public Identifier getTextureLocation(FuryBowserRenderState state) { return TEXTURE; }

    @Override protected void scale(FuryBowserRenderState state, PoseStack pose) {
        pose.scale(4.8F, 4.8F, 4.8F);
    }
}
