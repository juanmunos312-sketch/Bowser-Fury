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

/** A broad, heavily armored Fury Bowser built from individually UV-mapped cuboids. */
public class FuryBowserModel extends EntityModel<FuryBowserRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("bowsersfury", "fury_bowser"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart shell;
    private final ModelPart tail;
    private final ModelPart fireCrest;

    public FuryBowserModel(ModelPart root) {
        super(root);
        body = root.getChild("body");
        head = body.getChild("head");
        leftArm = body.getChild("left_arm");
        rightArm = body.getChild("right_arm");
        shell = body.getChild("shell");
        tail = body.getChild("tail");
        fireCrest = head.getChild("fire_crest");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
    }

    private static void cube(PartDefinition parent, String name, int tile,
                             float x, float y, float z, float width, float height, float depth) {
        int u = (tile % 4) * 256;
        int v = (tile / 4) * 128;
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v)
                .addBox(x, y, z, width, height, depth, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // The complete silhouette spans about 229 model units vertically. At 2.8x,
        // this renders at roughly 40 blocks tall (16 model units = 1 block).
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0F, 68.0F, 0.0F));

        // Broad torso, belly plates and layered dark armor.
        cube(body, "torso_core", 0, -39, -32, -20, 78, 64, 46);
        cube(body, "chest_upper", 1, -34, -27, -24, 68, 24, 12);
        cube(body, "belly_lower", 2, -31, 2, -23, 62, 28, 12);
        cube(body, "belly_center", 3, -23, 8, -29, 46, 17, 8);
        cube(body, "left_pectoral", 4, -39, -25, -15, 17, 25, 27);
        cube(body, "right_pectoral", 5, 22, -25, -15, 17, 25, 27);
        cube(body, "left_side_rib", 6, -42, -5, -8, 9, 31, 25);
        cube(body, "right_side_rib", 7, 33, -5, -8, 9, 31, 25);
        cube(body, "sternum_ridge", 8, -8, -29, -27, 16, 42, 7);

        // Massive shoulders and forearms.
        PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(),
                PartPose.offset(43.0F, -19.0F, -1.0F));
        cube(leftArm, "upper", 9, -5, -7, -13, 25, 35, 27);
        cube(leftArm, "outer_shoulder", 10, 12, -14, -12, 19, 23, 29);
        cube(leftArm, "forearm", 11, -3, 22, -12, 25, 26, 26);
        cube(leftArm, "fist", 12, -8, 42, -17, 35, 25, 34);
        cube(leftArm, "wrist_guard", 13, -7, 35, -19, 34, 10, 37);
        cube(leftArm, "shoulder_spike_a", 14, 21, -27, -8, 10, 16, 10);
        cube(leftArm, "shoulder_spike_b", 15, 31, -17, 1, 8, 13, 9);
        cube(leftArm, "wrist_spike", 16, 20, 30, -23, 9, 13, 9);

        PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(),
                PartPose.offset(-43.0F, -19.0F, -1.0F));
        cube(rightArm, "upper", 17, -20, -7, -13, 25, 35, 27);
        cube(rightArm, "outer_shoulder", 18, -31, -14, -12, 19, 23, 29);
        cube(rightArm, "forearm", 19, -22, 22, -12, 25, 26, 26);
        cube(rightArm, "fist", 20, -27, 42, -17, 35, 25, 34);
        cube(rightArm, "wrist_guard", 21, -27, 35, -19, 34, 10, 37);
        cube(rightArm, "shoulder_spike_a", 22, -31, -27, -8, 10, 16, 10);
        cube(rightArm, "shoulder_spike_b", 23, -39, -17, 1, 8, 13, 9);
        cube(rightArm, "wrist_spike", 24, -29, 30, -23, 9, 13, 9);

        // Oversized domed shell, thick rim and a crown of large ivory-tipped spikes.
        PartDefinition shell = body.addOrReplaceChild("shell", CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 19.0F));
        cube(shell, "shell_dome", 25, -47, -39, -1, 94, 76, 24);
        cube(shell, "shell_rim", 26, -51, -41, -5, 102, 82, 8);
        cube(shell, "shell_lower_plate", 27, -39, 23, -2, 78, 16, 22);
        cube(shell, "shell_center_plate", 28, -30, -27, 18, 60, 48, 7);
        // Top row: stepped cuboids make each spike read as a chunky tapered cone.
        cube(shell, "spike_top_1_base", 29, -9, -59, 3, 18, 15, 16);
        cube(shell, "spike_top_1_mid", 30, -6, -72, 6, 12, 15, 11);
        cube(shell, "spike_top_1_tip", 31, -3, -82, 9, 6, 12, 7);
        cube(shell, "spike_top_2_base", 32, -37, -51, 4, 15, 13, 14);
        cube(shell, "spike_top_2_mid", 33, -34, -63, 7, 9, 13, 10);
        cube(shell, "spike_top_2_tip", 34, -31, -71, 10, 6, 10, 6);
        cube(shell, "spike_top_3_base", 35, 22, -51, 4, 15, 13, 14);
        cube(shell, "spike_top_3_mid", 36, 25, -63, 7, 9, 13, 10);
        cube(shell, "spike_top_3_tip", 37, 28, -71, 10, 6, 10, 6);
        cube(shell, "spike_side_left", 38, -62, -15, 2, 17, 15, 15);
        cube(shell, "spike_side_right", 39, 45, -15, 2, 17, 15, 15);

        // Heavy tail with overlapping armor bands and a pointed end.
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offset(0.0F, 13.0F, 34.0F));
        cube(tail, "tail_root", 40, -13, -8, 0, 26, 22, 27);
        cube(tail, "tail_mid", 41, -10, -5, 20, 20, 17, 30);
        cube(tail, "tail_end", 42, -7, -3, 46, 14, 13, 27);
        cube(tail, "tail_tip", 43, -4, -2, 68, 8, 9, 19);
        cube(tail, "tail_spike", 44, -5, -17, 18, 10, 13, 12);

        // Wide reptilian head, heavy muzzle, jaw, tusks, glowing eyes and horns.
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(0.0F, -35.0F, -24.0F));
        cube(head, "skull", 45, -30, -23, -28, 60, 43, 39);
        cube(head, "brow", 46, -32, -15, -32, 64, 15, 25);
        cube(head, "muzzle", 47, -25, -4, -51, 50, 25, 28);
        cube(head, "nose_bridge", 48, -16, -10, -48, 32, 13, 16);
        cube(head, "lower_jaw", 49, -23, 17, -43, 46, 14, 25);
        cube(head, "mouth_dark", 50, -18, 10, -49, 36, 11, 5);
        cube(head, "upper_teeth", 51, -17, 8, -51, 7, 8, 5);
        cube(head, "lower_teeth", 52, 10, 14, -51, 7, 8, 5);
        cube(head, "left_eye_socket", 53, -27, -12, -48, 13, 11, 7);
        cube(head, "right_eye_socket", 54, 14, -12, -48, 13, 11, 7);
        cube(head, "left_eye_glow", 55, -24, -10, -54, 8, 6, 4);
        cube(head, "right_eye_glow", 56, 16, -10, -54, 8, 6, 4);
        cube(head, "left_horn_base", 57, -30, -38, -12, 13, 17, 14);
        cube(head, "left_horn_mid", 58, -29, -53, -10, 10, 18, 11);
        cube(head, "left_horn_tip", 59, -26, -66, -7, 6, 15, 7);
        cube(head, "right_horn_base", 60, 17, -38, -12, 13, 17, 14);
        cube(head, "right_horn_mid", 61, 19, -53, -10, 10, 18, 11);
        cube(head, "right_horn_tip", 62, 20, -66, -7, 6, 15, 7);
        cube(head, "cheek_left", 63, -35, 0, -28, 13, 22, 22);
        cube(head, "cheek_right", 64, 22, 0, -28, 13, 22, 22);

        // Flame mane: stacked, jagged red/orange/yellow cuboids rising behind the brow.
        PartDefinition crest = head.addOrReplaceChild("fire_crest", CubeListBuilder.create(),
                PartPose.offset(0.0F, -24.0F, 3.0F));
        cube(crest, "flame_core", 65, -17, -26, -5, 34, 26, 15);
        cube(crest, "flame_lower_left", 66, -28, -17, -1, 15, 21, 13);
        cube(crest, "flame_lower_right", 67, 13, -17, -1, 15, 21, 13);
        cube(crest, "flame_mid_left", 68, -22, -43, 1, 13, 22, 11);
        cube(crest, "flame_mid_center", 69, -9, -49, -1, 18, 29, 13);
        cube(crest, "flame_mid_right", 70, 9, -40, 1, 13, 20, 11);
        cube(crest, "flame_high_left", 71, -18, -61, 4, 11, 22, 9);
        cube(crest, "flame_high_center", 72, -7, -70, 2, 14, 26, 9);
        cube(crest, "flame_high_right", 73, 7, -58, 4, 11, 20, 9);
        cube(crest, "flame_tip_left", 74, -15, -78, 7, 8, 20, 7);
        cube(crest, "flame_tip_center", 75, -5, -88, 5, 10, 22, 7);
        cube(crest, "flame_tip_right", 76, 7, -74, 7, 8, 18, 7);

        // Broad, grounded legs and oversized armored feet.
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(),
                PartPose.offset(22.0F, 100.0F, 0.0F));
        cube(leftLeg, "thigh", 77, -15, -4, -13, 30, 23, 27);
        cube(leftLeg, "shin", 78, -14, 15, -12, 28, 22, 25);
        cube(leftLeg, "foot", 79, -20, 32, -24, 40, 17, 39);
        cube(leftLeg, "toe_claws", 80, -16, 43, -39, 32, 7, 17);
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(),
                PartPose.offset(-22.0F, 100.0F, 0.0F));
        cube(rightLeg, "thigh", 81, -15, -4, -13, 30, 23, 27);
        cube(rightLeg, "shin", 82, -14, 15, -12, 28, 22, 25);
        cube(rightLeg, "foot", 83, -20, 32, -24, 40, 17, 39);
        cube(rightLeg, "toe_claws", 84, -16, 43, -39, 32, 7, 17);

        return LayerDefinition.create(mesh, 1024, 2816);
    }

    @Override
    public void setupAnim(FuryBowserRenderState state) {
        resetPose();
        float walk = state.walkAnimationPos;
        float walkAmount = state.walkAnimationSpeed;
        float age = state.ageInTicks;
        head.yRot = state.yRot * ((float) Math.PI / 180.0F) * 0.35F;
        head.xRot = state.xRot * ((float) Math.PI / 180.0F) * 0.30F;
        leftLeg.xRot = (float) Math.cos(walk * 0.48F) * 0.48F * walkAmount;
        rightLeg.xRot = (float) Math.cos(walk * 0.48F + Math.PI) * 0.48F * walkAmount;
        leftArm.xRot = (float) Math.cos(walk * 0.42F + Math.PI) * 0.22F * walkAmount;
        rightArm.xRot = (float) Math.cos(walk * 0.42F) * 0.22F * walkAmount;
        body.y = 68.0F + (float) Math.sin(age * 0.055F) * 0.9F;
        shell.yRot = (float) Math.sin(age * 0.022F) * 0.025F;
        tail.yRot = (float) Math.sin(age * 0.055F) * 0.12F;
        tail.xRot = 0.06F + (float) Math.sin(age * 0.04F) * 0.045F;
        fireCrest.yRot = (float) Math.sin(age * 0.09F) * 0.035F;
        fireCrest.xRot = (float) Math.sin(age * 0.12F) * 0.025F;
    }
}
