package com.mrbysco.spookyfriends.registry;

import com.mrbysco.spookyfriends.SpookyFriendsMod;
import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber
public class SpookyRegistry {
	public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(SpookyFriendsMod.MOD_ID);

	public static final Supplier<EntityType<Friend>> FRIEND = ENTITIES.registerEntityType("friend",
			Friend::new,
			MobCategory.MISC,
			builder -> builder
					.sized(0.6F, 1.8F)
					.eyeHeight(1.62F)
					.vehicleAttachment(Avatar.DEFAULT_VEHICLE_ATTACHMENT)
					.clientTrackingRange(32)
					.updateInterval(2)
					.noLootTable()
	);

	@SubscribeEvent
	public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
		event.register(FRIEND.get(),
				SpawnPlacementTypes.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Friend::canSpawnHere, RegisterSpawnPlacementsEvent.Operation.AND);
	}

	@SubscribeEvent
	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(FRIEND.get(), Friend.createAttributes().build());
	}
}
