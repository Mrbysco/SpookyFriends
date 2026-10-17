package com.mrbysco.spookyfriends.util;

import com.mojang.datafixers.util.Either;
import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.config.SpookyConfig;
import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FriendCache {
	private static final List<PlayerData> nameList = new ArrayList<>();
	private static final List<BlockData> blocks = new ArrayList<>();

	public static void refreshCache() {
		nameList.clear();
		nameList.addAll(generatePlayers());

		if (SpookyConfig.COMMON.addWhitelistPlayers.get() && ServerLifecycleHooks.getCurrentServer() != null) {
			UserWhiteList whitelist = ServerLifecycleHooks.getCurrentServer().getPlayerList().getWhiteList();
			Arrays.stream(whitelist.getUserList())
					.map(PlayerData::new)
					.forEach(nameList::add);
		}

		refreshBlocks();
	}

	public static List<PlayerData> generatePlayers() {
		List<PlayerData> dataList = new ArrayList<>();
		List<? extends String> names = new ArrayList<>(SpookyConfig.COMMON.friends.get());
		for (String entry : names) {
			if (entry.isEmpty()) {
				continue;
			}
			// Split on comma, left = name, right = texture
			String[] parts = entry.split(",");
			String secondEntry = null;
			PlayerModelType bodyType = null;
			if (parts.length > 1) {
				entry = parts[0].trim();
				secondEntry = parts[1].trim();
				// Check for body type (texture part can be "slim" or "wide" to indicate model type)
				if (secondEntry.equals("slim") || secondEntry.equals("wide")) {
					bodyType = secondEntry.equals("slim") ? PlayerModelType.SLIM : PlayerModelType.WIDE;
					secondEntry = "";
				} else {
					if (parts.length > 2) {
						String type = parts[2].trim().toLowerCase();
						if (type.equals("slim")) {
							bodyType = PlayerModelType.SLIM;
						} else if (type.equals("wide")) {
							bodyType = PlayerModelType.WIDE;
						}
					}
				}
				// Validate Identifier
				if (!secondEntry.isEmpty()) {
					if (Identifier.tryParse(secondEntry) == null) {
						SpookyFriendsMod.LOGGER.error("Malformed Identifier for friend: {}, {}", entry, secondEntry);
						secondEntry = null;
					}
				}
			}

			dataList.add(new PlayerData(entry, secondEntry, bodyType));
		}

		return dataList;
	}

	private static @Nullable PlayerData getRandomPlayer(RandomSource randomSource) {
		if (nameList.isEmpty()) {
			return null;
		}
		return nameList.get(randomSource.nextInt(nameList.size()));
	}

	public static void attachGameProfile(Friend friend) {
		PlayerData data = FriendCache.getRandomPlayer(friend.getRandom());
		if (data != null) {
			final String name = data.name();
			final String texture = data.texture();

			// If texture is specified, it will be used regardless of the name
			Optional<ClientAsset.ResourceTexture> skin = Optional.empty();
			if (texture != null && !texture.isEmpty()) {
				Identifier location = Identifier.tryParse(texture);
				if (location != null)
					skin = Optional.of(new ClientAsset.ResourceTexture(location));
			}
			// If body type is not specified, it will choose the default (wide)
			Optional<PlayerModelType> bodyType = Optional.empty();
			if (data.bodyType() != null) {
				bodyType = Optional.of(data.bodyType());
			}

			PlayerSkin.Patch patch = PlayerSkin.Patch.EMPTY;
			if (skin.isPresent() || bodyType.isPresent()) {
				patch = PlayerSkin.Patch.create(
						skin, Optional.empty(), Optional.empty(), bodyType
				);
			}

			friend.setProfile(new ResolvableProfile.Dynamic(Either.left(name), patch));

		} else {
			friend.setProfile(ResolvableProfile.createUnresolved("herobrine"));
		}
	}

	private static void refreshBlocks() {
		blocks.clear();
		for (String entry : SpookyConfig.COMMON.blocks.get()) {
			// Split on comma, left = block id, right = max amount
			String[] parts = entry.split(",");
			if (parts.length == 2) {
				Identifier blockId = Identifier.tryParse(parts[0].trim());
				int maxAmount;
				try {
					maxAmount = Integer.parseInt(parts[1].trim());
				} catch (NumberFormatException e) {
					SpookyFriendsMod.LOGGER.error("Invalid max amount for block: {}", entry);
					continue;
				}
				Block block = BuiltInRegistries.BLOCK.getValue(blockId);
				if (block != null) {
					blocks.add(new BlockData(block, maxAmount));
				} else {
					SpookyFriendsMod.LOGGER.error("Invalid block ID: {}", blockId);
				}
			} else {
				SpookyFriendsMod.LOGGER.error("Invalid Spooky Friends blocks entry: {}", entry);
			}
		}
	}

	public static @Nullable BlockData getRandomBlock(RandomSource randomSource) {
		if (randomSource.nextDouble() <= SpookyConfig.COMMON.blockChance.get()) {
			if (blocks.isEmpty()) {
				return null;
			}
			return blocks.get(randomSource.nextInt(blocks.size()));
		}
		return null;
	}
}
