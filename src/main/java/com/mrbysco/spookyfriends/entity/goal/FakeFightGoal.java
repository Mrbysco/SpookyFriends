package com.mrbysco.spookyfriends.entity.goal;

import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FakeFightGoal extends Goal {
	private final Friend friend;
	private int fightTicks;
	private int nextSoundTick;

	public FakeFightGoal(Friend friend) {
		this.friend = friend;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!isUnderground()) return false;
		return this.friend.getRandom().nextInt(reducedTickDelay(400)) == 0;
	}

	@Override
	public boolean canContinueToUse() {
		return this.fightTicks > 0 && isUnderground();
	}

	@Override
	public void start() {
		this.fightTicks = 60 + this.friend.getRandom().nextInt(100);
		this.nextSoundTick = 0;
		this.friend.getNavigation().stop();
	}

	@Override
	public void stop() {
		this.fightTicks = 0;
	}

	@Override
	public void tick() {
		this.fightTicks--;

		if (this.nextSoundTick-- > 0) return;
		this.nextSoundTick = 6 + this.friend.getRandom().nextInt(10);

		this.friend.setYRot(this.friend.getYRot() + (this.friend.getRandom().nextFloat() - 0.5F) * 60.0F);
		this.friend.swing(InteractionHand.MAIN_HAND);

		float pitch = 0.9F + this.friend.getRandom().nextFloat() * 0.2F;

		play(SoundEvents.PLAYER_ATTACK_SWEEP, pitch);

		float rand = this.friend.getRandom().nextFloat();
		if (rand < 0.15F) {
			play(SoundEvents.PLAYER_ATTACK_CRIT, pitch);
		} else if (rand < 0.65F) {
			play(SoundEvents.PLAYER_ATTACK_STRONG, pitch);
		}
	}

	private void play(SoundEvent sound, float pitch) {
		getServerLevel(this.friend).playSound(null, this.friend.getX(), this.friend.getY(), this.friend.getZ(), sound, SoundSource.HOSTILE, 1.0F, pitch);
	}

	private boolean isUnderground() {
		ServerLevel level = getServerLevel(this.friend);
		return !level.canSeeSky(this.friend.blockPosition()) && this.friend.getY() < level.getSeaLevel() - 8;
	}

}
