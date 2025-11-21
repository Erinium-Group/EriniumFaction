package fr.eriniumgroup.erinium_faction.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.entity.RocketEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Modèle 3D de la fusée créé avec Blockbench 5.0.3
 * Texture: 512x512
 */
public class RocketModel extends EntityModel<RocketEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
		ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "rocket"), "main");

	private final ModelPart hexadecagon1;
	private final ModelPart bone;
	private final ModelPart bone3;
	private final ModelPart bone2;
	private final ModelPart hexadecagon4;
	private final ModelPart hexadecagon3;
	private final ModelPart hexadecagon5;
	private final ModelPart hexadecagon2;
	private final ModelPart legs;

	public RocketModel(ModelPart root) {
		this.hexadecagon1 = root.getChild("hexadecagon1");
		this.bone = root.getChild("bone");
		this.bone3 = root.getChild("bone3");
		this.bone2 = root.getChild("bone2");
		this.hexadecagon4 = root.getChild("hexadecagon4");
		this.hexadecagon3 = root.getChild("hexadecagon3");
		this.hexadecagon5 = root.getChild("hexadecagon5");
		this.hexadecagon2 = root.getChild("hexadecagon2");
		this.legs = root.getChild("legs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition hexadecagon1 = partdefinition.addOrReplaceChild("hexadecagon1", CubeListBuilder.create(), PartPose.offset(0.0F, -85.0F, 0.0F));

		PartDefinition hexadecagon_r1 = hexadecagon1.addOrReplaceChild("hexadecagon_r1", CubeListBuilder.create().texOffs(212, 40).addBox(-23.0F, -7.0F, -1.9783F, 25.99F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, -0.7854F));

		PartDefinition hexadecagon_r2 = hexadecagon1.addOrReplaceChild("hexadecagon_r2", CubeListBuilder.create().texOffs(212, 81).addBox(-23.0F, -7.0F, -1.9783F, 25.99F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition hexadecagon_r3 = hexadecagon1.addOrReplaceChild("hexadecagon_r3", CubeListBuilder.create().texOffs(57, 124).addBox(-23.0F, -7.0F, -1.9783F, 25.99F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, -0.7854F));

		PartDefinition hexadecagon_r4 = hexadecagon1.addOrReplaceChild("hexadecagon_r4", CubeListBuilder.create().texOffs(218, 160).addBox(-2.99F, -7.0F, -6.9783F, 25.99F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.7854F));

		PartDefinition hexadecagon_r5 = hexadecagon1.addOrReplaceChild("hexadecagon_r5", CubeListBuilder.create().texOffs(0, 213).addBox(-2.99F, -7.0F, -6.9783F, 25.99F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition hexadecagon_r6 = hexadecagon1.addOrReplaceChild("hexadecagon_r6", CubeListBuilder.create().texOffs(212, 9).addBox(-2.99F, -7.0F, -2.0217F, 25.99F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.7854F));

		PartDefinition hexadecagon_r7 = hexadecagon1.addOrReplaceChild("hexadecagon_r7", CubeListBuilder.create().texOffs(69, 164).addBox(-2.4783F, -7.0F, -2.99F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r8 = hexadecagon1.addOrReplaceChild("hexadecagon_r8", CubeListBuilder.create().texOffs(171, 0).addBox(-2.4783F, -7.0F, -2.99F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r9 = hexadecagon1.addOrReplaceChild("hexadecagon_r9", CubeListBuilder.create().texOffs(171, 41).addBox(-2.4783F, -7.0F, -2.99F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition hexadecagon_r10 = hexadecagon1.addOrReplaceChild("hexadecagon_r10", CubeListBuilder.create().texOffs(171, 82).addBox(-2.4783F, -7.0F, -2.99F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r11 = hexadecagon1.addOrReplaceChild("hexadecagon_r11", CubeListBuilder.create().texOffs(144, 138).addBox(-1.9783F, -7.0F, -2.99F, 9.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.7854F, 0.0F));

		PartDefinition hexadecagon_r12 = hexadecagon1.addOrReplaceChild("hexadecagon_r12", CubeListBuilder.create().texOffs(0, 164).addBox(-5.0217F, -7.0F, -23.0F, 8.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r13 = hexadecagon1.addOrReplaceChild("hexadecagon_r13", CubeListBuilder.create().texOffs(71, 204).addBox(-1.5217F, -7.0F, -23.0F, 4.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r14 = hexadecagon1.addOrReplaceChild("hexadecagon_r14", CubeListBuilder.create().texOffs(97, 124).addBox(-4.5217F, -7.0F, -23.0F, 10.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition hexadecagon_r15 = hexadecagon1.addOrReplaceChild("hexadecagon_r15", CubeListBuilder.create().texOffs(110, 178).addBox(-4.5217F, -7.0F, -23.0F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r16 = hexadecagon1.addOrReplaceChild("hexadecagon_r16", CubeListBuilder.create().texOffs(177, 178).addBox(-4.5217F, -7.0F, -23.0F, 7.0F, 13.0F, 25.99F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.7854F, 0.0F));

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 292).addBox(-3.8034F, 14.1648F, -1.0F, 10.0F, 25.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-26.1966F, -79.1648F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(144, 133).addBox(7.25F, -0.25F, -1.0F, 10.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(238, 103).addBox(-30.0F, -73.0F, -1.0F, 10.0F, 25.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r2 = bone3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(57, 143).addBox(7.25F, -0.25F, -1.0F, 10.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-26.1966F, -87.1648F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(68, 164).addBox(0.6966F, 38.6648F, -1.0F, 10.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(43, 164).addBox(50.6966F, 38.6648F, -1.0F, 10.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-30.6966F, -39.6648F, 0.0F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(25, 292).addBox(10.8819F, -4.1219F, -1.0F, 10.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.4712F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 164).addBox(-12.0F, -19.25F, -1.0F, 10.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(61.1254F, 35.3025F, 0.0F, 0.0F, 0.0F, -0.48F));

		PartDefinition hexadecagon4 = partdefinition.addOrReplaceChild("hexadecagon4", CubeListBuilder.create().texOffs(114, 42).addBox(-3.9783F, -32.0F, -20.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(57, 0).addBox(-3.9783F, -32.0F, 15.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(286, 225).addBox(15.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(281, 9).addBox(-20.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition hexadecagon_r17 = hexadecagon4.addOrReplaceChild("hexadecagon_r17", CubeListBuilder.create().texOffs(278, 95).addBox(-20.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(286, 136).addBox(15.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(0, 41).addBox(-3.9783F, -32.0F, 15.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(114, 0).addBox(-3.9783F, -32.0F, -20.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r18 = hexadecagon4.addOrReplaceChild("hexadecagon_r18", CubeListBuilder.create().texOffs(281, 50).addBox(-20.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(289, 266).addBox(15.0F, -32.0F, -3.9783F, 5.0F, 32.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(57, 42).addBox(-3.9783F, -32.0F, 15.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(114, 83).addBox(-3.9783F, -32.0F, -20.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r19 = hexadecagon4.addOrReplaceChild("hexadecagon_r19", CubeListBuilder.create().texOffs(0, 0).addBox(-3.9783F, -32.0F, 15.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(57, 83).addBox(-3.9783F, -32.0F, -20.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r20 = hexadecagon4.addOrReplaceChild("hexadecagon_r20", CubeListBuilder.create().texOffs(0, 82).addBox(-3.9783F, -32.0F, 15.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 123).addBox(-3.9783F, -32.0F, -20.0F, 7.9565F, 32.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition hexadecagon3 = partdefinition.addOrReplaceChild("hexadecagon3", CubeListBuilder.create().texOffs(135, 275).addBox(-3.9783F, -48.0F, -20.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(81, 244).addBox(-3.9783F, -48.0F, 15.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 235).addBox(15.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(159, 218).addBox(-20.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition hexadecagon_r21 = hexadecagon3.addOrReplaceChild("hexadecagon_r21", CubeListBuilder.create().texOffs(132, 218).addBox(-20.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(213, 218).addBox(15.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(54, 244).addBox(-3.9783F, -48.0F, 15.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(235, 270).addBox(-3.9783F, -48.0F, -20.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r22 = hexadecagon3.addOrReplaceChild("hexadecagon_r22", CubeListBuilder.create().texOffs(186, 218).addBox(-20.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(27, 235).addBox(15.0F, -48.0F, -3.9783F, 5.0F, 48.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(262, 262).addBox(-3.9783F, -48.0F, 15.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(162, 275).addBox(-3.9783F, -48.0F, -20.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r23 = hexadecagon3.addOrReplaceChild("hexadecagon_r23", CubeListBuilder.create().texOffs(240, 213).addBox(-3.9783F, -48.0F, 15.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(108, 270).addBox(-3.9783F, -48.0F, -20.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r24 = hexadecagon3.addOrReplaceChild("hexadecagon_r24", CubeListBuilder.create().texOffs(267, 179).addBox(-3.9783F, -48.0F, 15.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(189, 275).addBox(-3.9783F, -48.0F, -20.0F, 7.9565F, 48.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition hexadecagon5 = partdefinition.addOrReplaceChild("hexadecagon5", CubeListBuilder.create().texOffs(114, 41).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F))
		.texOffs(0, 204).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition hexadecagon_r25 = hexadecagon5.addOrReplaceChild("hexadecagon_r25", CubeListBuilder.create().texOffs(189, 151).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(114, 0).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r26 = hexadecagon5.addOrReplaceChild("hexadecagon_r26", CubeListBuilder.create().texOffs(212, 0).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(114, 82).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r27 = hexadecagon5.addOrReplaceChild("hexadecagon_r27", CubeListBuilder.create().texOffs(57, 83).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r28 = hexadecagon5.addOrReplaceChild("hexadecagon_r28", CubeListBuilder.create().texOffs(0, 123).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition hexadecagon2 = partdefinition.addOrReplaceChild("hexadecagon2", CubeListBuilder.create().texOffs(57, 1).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F))
		.texOffs(189, 133).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition hexadecagon_r29 = hexadecagon2.addOrReplaceChild("hexadecagon_r29", CubeListBuilder.create().texOffs(144, 124).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(0, 41).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition hexadecagon_r30 = hexadecagon2.addOrReplaceChild("hexadecagon_r30", CubeListBuilder.create().texOffs(189, 142).addBox(-20.0F, 0.0F, -3.9783F, 40.0F, 0.0F, 7.9565F, new CubeDeformation(0.0F))
		.texOffs(57, 42).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition hexadecagon_r31 = hexadecagon2.addOrReplaceChild("hexadecagon_r31", CubeListBuilder.create().texOffs(0, 0).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition hexadecagon_r32 = hexadecagon2.addOrReplaceChild("hexadecagon_r32", CubeListBuilder.create().texOffs(0, 82).addBox(-3.9783F, 0.0F, -20.0F, 7.9565F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(331, 13).addBox(-1.0F, -15.8609F, -52.6391F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 26.0F));

		PartDefinition cube_r5 = legs.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(330, 0).addBox(-1.0F, -9.182F, -3.6516F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, -13.0466F, -16.2034F, 2.3562F, 1.4835F, -3.1416F));

		PartDefinition cube_r6 = legs.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(331, 13).addBox(-1.0F, -2.8143F, -2.4357F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, -13.0466F, -16.2034F, 3.1416F, 1.4835F, 3.1416F));

		PartDefinition cube_r7 = legs.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(330, 0).addBox(-1.0F, -9.182F, -3.6516F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -13.0466F, -15.2034F, 2.3562F, -1.309F, -3.1416F));

		PartDefinition cube_r8 = legs.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(331, 13).addBox(-1.0F, -2.8143F, -2.4357F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -13.0466F, -15.2034F, -3.1416F, -1.309F, 3.1416F));

		PartDefinition cube_r9 = legs.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(330, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -20.0F, -47.0F, -0.7854F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(RocketEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Animations peuvent être ajoutées ici plus tard
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		hexadecagon1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hexadecagon4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hexadecagon3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hexadecagon5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		hexadecagon2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
