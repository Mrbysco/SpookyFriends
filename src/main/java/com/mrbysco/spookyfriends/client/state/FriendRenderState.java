package com.mrbysco.spookyfriends.client.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;

public class FriendRenderState extends HumanoidRenderState {
	public PlayerSkin skin = DefaultPlayerSkin.getDefaultSkin();
	public String name = "Steve";
}
