package com.mrbysco.spookyfriends.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.spookyfriends.client.ClientHandler;
import com.mrbysco.spookyfriends.client.model.FriendModel;
import com.mrbysco.spookyfriends.client.state.FriendRenderState;
import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;

public class FriendRenderer extends LivingEntityRenderer<Friend, FriendRenderState, FriendModel> {
	private PlayerSkinRenderCache playerSkinRenderCache;
	private final FriendModel playerModel;
	private final FriendModel slimPlayerModel;
	public static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.get(Mannequin.DEFAULT_PROFILE.partialProfile());
	public boolean isSlim = false;

	public FriendRenderer(EntityRendererProvider.Context context, boolean slim) {
		super(context, new FriendModel(context.bakeLayer(ClientHandler.FRIEND), slim), 0.0F);
		this.playerModel = new FriendModel(context.bakeLayer(ClientHandler.FRIEND), false);
		this.slimPlayerModel = new FriendModel(context.bakeLayer(ClientHandler.FRIEND_SLIM), true);
		this.addLayer(
				new HumanoidArmorLayer<>(
						this,
						ArmorModelSet.bake(
								slim ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR,
								context.getModelSet(),
								model -> new FriendModel(model, slim)
						),
						context.getEquipmentRenderer()
				)
		);
		this.addLayer(new ItemInHandLayer<>(this));
		this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
	}

	public FriendRenderer(EntityRendererProvider.Context context) {
		this(context, false);
		this.playerSkinRenderCache = context.getPlayerSkinRenderCache();
	}

	@Override
	public void submit(FriendRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		this.isSlim = state.skin != null && state.skin.model() == PlayerModelType.SLIM;
		this.model = isSlim ? this.slimPlayerModel : playerModel;
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	@Override
	public FriendRenderState createRenderState() {
		return new FriendRenderState();
	}

	@Override
	public void extractRenderState(Friend entity, FriendRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
		state.skin = getSkin(entity.getProfile());
		state.name = entity.getProfile().name().orElse("Steve");
	}

	@Override
	public Identifier getTextureLocation(FriendRenderState state) {
		return state.skin.body().texturePath();
	}

	private PlayerSkin getSkin(@Nullable ResolvableProfile profile) {
		if (profile == null) {
			return DEFAULT_SKIN;
		} else {
			return playerSkinRenderCache.getOrDefault(profile).playerSkin();
		}
	}
}
