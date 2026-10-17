package com.mrbysco.spookyfriends;

import com.mojang.logging.LogUtils;
import com.mrbysco.spookyfriends.config.SpookyConfig;
import com.mrbysco.spookyfriends.registry.SpookyRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(SpookyFriendsMod.MOD_ID)
public class SpookyFriendsMod {
	public static final String MOD_ID = "spookyfriends";
	public static final Logger LOGGER = LogUtils.getLogger();

	public SpookyFriendsMod(IEventBus eventBus, Dist dist, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, SpookyConfig.commonSpec);

		SpookyRegistry.ENTITIES.register(eventBus);

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}

	public static Identifier modLoc(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
