package com.mrbysco.spookyfriends.datagen;

import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.datagen.client.SpookyLanguageProvider;
import com.mrbysco.spookyfriends.registry.SpookyRegistry;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber
public class SpookyDatagen {
	private static final RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
			.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
				final HolderGetter<Biome> biomeHolderGetter = context.lookup(Registries.BIOME);
				final BiomeModifier addSpawn = BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
						biomeHolderGetter.getOrThrow(BiomeTags.IS_OVERWORLD),
						new Weighted<>(
								new MobSpawnSettings.SpawnerData(SpookyRegistry.FRIEND.get(), 1, 2),
								10
						)
				);

				context.register(createKey("add_spooky_friend"), addSpawn);
			});

	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createDatapackRegistryObjects(registryBuilder);

		event.createProvider(SpookyLanguageProvider::new);
	}

	private static ResourceKey<BiomeModifier> createKey(String name) {
		return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SpookyFriendsMod.modLoc(name));
	}
}
