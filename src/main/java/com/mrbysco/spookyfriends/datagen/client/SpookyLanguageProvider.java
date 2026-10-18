package com.mrbysco.spookyfriends.datagen.client;

import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.registry.SpookyRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.locale.Language;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.Nullable;

public class SpookyLanguageProvider extends LanguageProvider {

	public SpookyLanguageProvider(PackOutput packOutput) {
		super(packOutput, SpookyFriendsMod.MOD_ID, Language.DEFAULT);
	}

	@Override
	protected void addTranslations() {
		addEntityType(SpookyRegistry.FRIEND, "Friend");

		addConfig("friend_settings", "Friends", "Friend Settings");
		addConfig("friends", "Friends", "A list of users who can be chosen when it spawns a distant friend.\nFormat: \"<username>\" or \"<username>,<base64 texture>\" (optional)");
		addConfig("addWhitelistPlayers", "Add Whitelist Players", "Add the players from the whitelist to the Friends list [default: true]");
		addConfig("blockChance", "Block Chance", "The chance for a Friend to spawn with block(s) [default: 0.5]");
		addConfig("blocks", "Blocks", "A list of blocks and max amount a Friend can spawn with\nFormat: \"<block>,<max_amount>\" (optional)");
		addConfig("fakeStepsChance", "Fake Steps Chance", "Chance of fake footsteps while the player is underground (1 out of X, 0 to disable) [default: 1000]");
		addConfig("fakeStepsCooldown", "Fake Steps Cooldown", "Cooldown in seconds between fake footsteps, plus a random extra of up to the same amount [default: 300]");
	}

	/**
	 * Add the translation for a config entry
	 *
	 * @param path        The path of the config entry
	 * @param name        The name of the config entry
	 * @param description The description of the config entry (optional in case of targeting "title" or similar entries that have no tooltip)
	 */
	private void addConfig(String path, String name, @Nullable String description) {
		this.add(SpookyFriendsMod.MOD_ID + ".configuration." + path, name);
		if (description != null && !description.isEmpty())
			this.add(SpookyFriendsMod.MOD_ID + ".configuration." + path + ".tooltip", description);
	}
}
