package com.mrbysco.spookyfriends.entity;

import com.mrbysco.spookyfriends.entity.goal.FakeFightGoal;
import com.mrbysco.spookyfriends.entity.goal.RandomPlaceBlockGoal;
import com.mrbysco.spookyfriends.util.BlockData;
import com.mrbysco.spookyfriends.util.FriendCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Friend extends Mob {
	protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(
			Friend.class, EntityDataSerializers.RESOLVABLE_PROFILE
	);
	private static final EntityDataAccessor<Boolean> DATA_IMMOVABLE = SynchedEntityData.defineId(Friend.class, EntityDataSerializers.BOOLEAN);
	public static final ResolvableProfile DEFAULT_PROFILE = ResolvableProfile.Static.EMPTY;

	public Friend(EntityType<Friend> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FakeFightGoal(this));
		this.goalSelector.addGoal(10, new RandomPlaceBlockGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.FOLLOW_RANGE, 35.0D)
				.add(Attributes.MOVEMENT_SPEED, (double) 0.23F)
				.add(Attributes.ATTACK_DAMAGE, 3.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_PROFILE, DEFAULT_PROFILE);
		entityData.define(DATA_IMMOVABLE, false);
	}

	public ResolvableProfile getProfile() {
		return this.entityData.get(DATA_PROFILE);
	}

	public void setProfile(ResolvableProfile profile) {
		this.entityData.set(DATA_PROFILE, profile);
	}

	private boolean getImmovable() {
		return this.entityData.get(DATA_IMMOVABLE);
	}

	private void setImmovable(boolean immovable) {
		this.entityData.set(DATA_IMMOVABLE, immovable);
	}

	@Override
	protected boolean isImmobile() {
		return this.getImmovable() || super.isImmobile();
	}

	@Override
	public boolean isEffectiveAi() {
		return !this.getImmovable() && super.isEffectiveAi();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.store("profile", ResolvableProfile.CODEC, this.getProfile());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		input.read("profile", ResolvableProfile.CODEC).ifPresent(this::setProfile);
		this.setImmovable(input.getBooleanOr("immovable", false));
	}

	@Override
	public <T> @Nullable T get(DataComponentType<? extends T> type) {
		return type == DataComponents.PROFILE ? castComponentValue((DataComponentType<T>) type, this.getProfile()) : super.get(type);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		this.applyImplicitComponentIfPresent(components, DataComponents.PROFILE);
		super.applyImplicitComponents(components);
	}

	@Override
	protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
		if (type == DataComponents.PROFILE) {
			this.setProfile(castComponentValue(DataComponents.PROFILE, value));
			return true;
		} else {
			return super.applyImplicitComponent(type, value);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.updateSwingTime();
	}

	@Override
	public @Nullable Component getCustomName() {
		return Component.literal(getProfile().name().orElse("Steve"));
	}

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
		// Attach a name (Since profile is required)
		FriendCache.attachGameProfile(this);

		// Give the friend blocks to place
		BlockData chosenBlock = FriendCache.getRandomBlock(random);
		if (chosenBlock != null) {
			setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(chosenBlock.block(), random.nextIntBetweenInclusive(1, chosenBlock.maxCount())));
		}

		return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
	}

	public static boolean canSpawnHere(EntityType<Friend> entityType, ServerLevelAccessor serverLevelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
		return serverLevelAccessor.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(serverLevelAccessor, blockPos, randomSource) &&
				checkMobSpawnRules(entityType, serverLevelAccessor, entitySpawnReason, blockPos, randomSource);
	}
}
