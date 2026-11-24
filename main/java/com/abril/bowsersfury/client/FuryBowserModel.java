package com.abril.bowsersfury.client;


import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class FuryBowserModel extends EntityModel<FuryBowserRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("bowsersfury", "fury_bowser"), "main");
    private final ModelPart body, head, leftArm, rightArm, leftLeg, rightLeg, shell, tail;

    public FuryBowserModel(ModelPart root) {
        super(root);
        body = root.getChild("body"); head = body.getChild("head"); shell = body.getChild("shell"); tail = body.getChild("tail");
        leftArm = body.getChild("left_arm"); rightArm = body.getChild("right_arm");
        leftLeg = root.getChild("left_leg"); rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-17, -19, -11, 34, 35, 23, new CubeDeformation(0.0F)), PartPose.offset(0, 1, 0));
        body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-10, -12, -16, 20, 19, 18)
                .texOffs(58, 82).addBox(-8, -5, -22, 16, 9, 8)
                .texOffs(0, 140).addBox(-11, -17, -3, 5, 9, 5)
                .texOffs(24, 140).addBox(6, -17, -3, 5, 9, 5), PartPose.offset(0, -22, -5));
        body.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-16, -17, 0, 32, 29, 9)
                .texOffs(132, 0).addBox(-12, -25, 1, 4, 10, 4)
                .texOffs(132, 20).addBox(-3, -28, 1, 4, 13, 4)
                .texOffs(132, 42).addBox(6, -25, 1, 4, 10, 4)
                .texOffs(80, 0).addBox(-11, -11, 8, 22, 20, 4), PartPose.offset(0, -1, 10));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(132, 160)
                .addBox(-4, -2, 0, 8, 5, 20), PartPose.offset(0, 10, 18));
        body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(80, 50)
                .addBox(-5, -3, -6, 11, 26, 12).texOffs(120, 50).addBox(-7, 15, -8, 15, 13, 15), PartPose.offset(19, -8, 0));
        body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(80, 110)
                .addBox(-6, -3, -6, 11, 26, 12).texOffs(120, 110).addBox(-8, 15, -8, 15, 13, 15), PartPose.offset(-19, -8, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 160).addBox(-7, 0, -8, 14, 21, 16), PartPose.offset(10, 19, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 160).addBox(-7, 0, -8, 14, 21, 16), PartPose.offset(-10, 19, 0));
        return LayerDefinition.create(mesh, 192, 224);
    }

    @Override public void setupAnim(FuryBowserRenderState state) {
        this.resetPose();
        float walk = state.walkAnimationPos;
        float walkAmount = state.walkAnimationSpeed;
        float age = state.ageInTicks;
        head.yRot = state.yRot * ((float)Math.PI / 180F) * 0.45F;
        head.xRot = state.xRot * ((float)Math.PI / 180F) * 0.45F;
        leftLeg.xRot = (float)Math.cos(walk * 0.55F) * 0.75F * walkAmount;
        rightLeg.xRot = (float)Math.cos(walk * 0.55F + Math.PI) * 0.75F * walkAmount;
        leftArm.xRot = (float)Math.cos(walk * 0.45F + Math.PI) * 0.35F * walkAmount;
        rightArm.xRot = (float)Math.cos(walk * 0.45F) * 0.35F * walkAmount;
        body.y = 1.0F + (float)Math.sin(age * 0.08F) * 0.7F;
        shell.yRot = (float)Math.sin(age * 0.025F) * 0.035F;
        tail.yRot = (float)Math.sin(age * 0.07F) * 0.16F;
        tail.xRot = 0.08F + (float)Math.sin(age * 0.045F) * 0.06F;
    }
}
