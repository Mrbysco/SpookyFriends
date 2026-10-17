package com.mrbysco.spookyfriends.entity.goal;

import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RandomPlaceBlockGoal extends Goal {
	private final Friend friend;
	private BlockPos targetPos;
	private int placeTimer;
	private int arrivedTicks;

	public RandomPlaceBlockGoal(Friend friend) {
		this.friend = friend;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.friend.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() || !EventHooks.canEntityGrief(getServerLevel(this.friend), this.friend)) {
			return false;
		}

		if (this.friend.getRandom().nextInt(reducedTickDelay(100)) != 0) {
			return false;
		}

		this.targetPos = this.findPlacementPosition();
		return this.targetPos != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetPos != null && this.placeTimer > 0 &&
				!this.friend.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
	}

	@Override
	public void start() {
		this.placeTimer = 200;
		this.arrivedTicks = 0;
		this.friend.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 1.0D);
	}

	@Override
	public void stop() {
		this.targetPos = null;
		this.friend.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.targetPos == null) return;

		this.placeTimer--;

		this.friend.getLookControl()
				.setLookAt(
						this.targetPos.getX() + 0.5D,
						this.targetPos.getY() + 0.5D,
						this.targetPos.getZ() + 0.5D,
						10.0F,
						(float) this.friend.getMaxHeadXRot());

		if (this.friend.distanceToSqr(this.targetPos.getBottomCenter()) <= 6.25D) {
			this.friend.getNavigation().stop();

			if (this.placeTimer <= 190) {
				this.placeBlock();
				this.stop();
			}
		} else if (this.friend.getNavigation().isDone()) {
			this.friend.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 1.0D);
		}
	}

	private void placeBlock() {
		ServerLevel level = getServerLevel(this.friend);
		BlockState state = getBlockState();
		if (state == null) return;

		if (!level.getBlockState(this.targetPos).canBeReplaced() || !level.getBlockState(this.targetPos.below()).isSolid()) {
			return;
		}

		if (EventHooks.onBlockPlace(this.friend, net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, this.targetPos), Direction.UP)) {
			return;
		}

		this.friend.swing(InteractionHand.OFF_HAND);

		level.setBlock(this.targetPos, state, 3);

		SoundType soundType = state.getSoundType();
		level.playSound(null, this.targetPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

		level.gameEvent(GameEvent.BLOCK_PLACE, this.targetPos, GameEvent.Context.of(this.friend, state));

		friend.getItemBySlot(EquipmentSlot.OFFHAND).consume(1, friend);
	}

	@Nullable
	private BlockState getBlockState() {
		ItemStack stack = this.friend.getItemBySlot(EquipmentSlot.OFFHAND);
		if (stack.getItem() instanceof BlockItem blockItem) {
			return blockItem.getBlock().defaultBlockState();
		}
		return null;
	}

	@Nullable
	private BlockPos findPlacementPosition() {
		ServerLevel level = getServerLevel(this.friend);
		for (int i = 0; i < 10; i++) {
			int x = Mth.floor(this.friend.getX() + this.friend.getRandom().nextInt(11) - 5);
			int y = Mth.floor(this.friend.getY() + this.friend.getRandom().nextInt(3) - 1);
			int z = Mth.floor(this.friend.getZ() + this.friend.getRandom().nextInt(11) - 5);

			BlockPos pos = new BlockPos(x, y, z);
			if (this.friend.distanceToSqr(pos.getBottomCenter()) < 9.0D) continue;
			BlockState state = level.getBlockState(pos);
			BlockState belowState = level.getBlockState(pos.below());

			if (state.canBeReplaced() && belowState.isSolid() && level.getFluidState(pos).isEmpty()) {
				if (this.friend.getNavigation().createPath(pos, 1) != null) {
					return pos;
				}
			}
		}
		return null;
	}
}
