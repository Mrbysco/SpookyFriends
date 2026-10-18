package com.mrbysco.spookyfriends.config;

import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.util.FriendCache;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@EventBusSubscriber
public class SpookyConfig {
	public static class Common {
		public final ModConfigSpec.ConfigValue<List<? extends String>> friends;
		public final ModConfigSpec.BooleanValue addWhitelistPlayers;

		public final ModConfigSpec.DoubleValue blockChance;
		public final ModConfigSpec.ConfigValue<List<? extends String>> blocks;

		public final ModConfigSpec.IntValue fakeStepsChance;
		public final ModConfigSpec.IntValue fakeStepsCooldown;

		Common(ModConfigSpec.Builder builder) {
			builder.comment("Friends")
					.push("friend_settings");

			friends = builder
					.comment("A list of users who can be chosen when it spawns a distant friend. ",
							"Format: \"<username>\" or \"<username>,<base64 texture>\" (optional)")
					.defineListAllowEmpty("friends", List.of("shynieke", "mrbysco"), String::new, o -> (o instanceof String));

			addWhitelistPlayers = builder
					.comment("Add the players from the whitelist to the Friends list [default: true]")
					.define("addWhitelistPlayers", true);

			blockChance = builder
					.comment("The chance for a Friend to spawn with block(s) [default: 0.5]")
					.defineInRange("blockChance", 0.5, 0.0, 1.0);

			blocks = builder
					.comment("A list of blocks and max amount a Friend can spawn with",
							"Format: \"<block>,<max_amount>\" (optional)")
					.defineListAllowEmpty("blocks", List.of("minecraft:cobblestone,10", "minecraft:cobbled_deepslate,9", "minecraft:diorite,4", "minecraft:oak_planks,8"), String::new, o -> (o instanceof String));

			fakeStepsChance = builder
					.comment("Chance of fake footsteps while the player is underground (1 out of X, 0 to disable) [default: 1000]")
					.defineInRange("fakeStepsChance", 1000, 0, 100000);

			fakeStepsCooldown = builder
					.comment("Cooldown in seconds between fake footsteps, plus a random extra of up to the same amount [default: 300]")
					.defineInRange("fakeStepsCooldown", 300, 0, 86400);

			builder.pop();
		}
	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		SpookyFriendsMod.LOGGER.debug("Loaded Spooky Friends' config file {}", configEvent.getConfig().getFileName());
		FriendCache.refreshCache();
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		SpookyFriendsMod.LOGGER.debug("Spooky Friends' config just got changed on the file system!");
		FriendCache.refreshCache();
	}
}
