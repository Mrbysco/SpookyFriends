package com.mrbysco.spookyfriends.client;

import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.client.model.FriendModel;
import com.mrbysco.spookyfriends.client.renderer.FriendRenderer;
import com.mrbysco.spookyfriends.registry.SpookyRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientHandler {
	public static final ModelLayerLocation FRIEND = new ModelLayerLocation(SpookyFriendsMod.modLoc("friend"), "main");
	public static final ModelLayerLocation FRIEND_SLIM = new ModelLayerLocation(SpookyFriendsMod.modLoc("friend"), "slim");


	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(SpookyRegistry.FRIEND.get(), FriendRenderer::new);

	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(FRIEND, () -> LayerDefinition.create(FriendModel.createMesh(CubeDeformation.NONE, false), 64, 64));
		event.registerLayerDefinition(FRIEND_SLIM, () -> LayerDefinition.create(FriendModel.createMesh(CubeDeformation.NONE, true), 64, 64));
	}
}
