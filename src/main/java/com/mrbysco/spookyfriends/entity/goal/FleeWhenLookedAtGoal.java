package com.mrbysco.spookyfriends.entity.goal;


import com.mrbysco.spookyfriends.entity.Friend;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FleeWhenLookedAtGoal extends Goal {
	private final Friend friend;
	private Player target;
	private boolean isFleeing;

	public FleeWhenLookedAtGoal(Friend friend) {
		this.friend = friend;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		this.target = this.friend.level().getNearestPlayer(this.friend, 24.0D);
		return this.target != null && this.target.hasLineOfSight(this.friend);
	}

	@Override
	public boolean canContinueToUse() {
		if (this.target == null || !this.target.isAlive()) return false;
		return this.isFleeing ? !this.friend.getNavigation().isDone() : this.target.hasLineOfSight(this.friend);
	}

	@Override
	public void start() {
		this.isFleeing = false;
		this.friend.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.isFleeing) return;

		if (isPlayerStaringBack()) {
			this.isFleeing = fleeFromPlayer();
		} else if (this.friend.getNavigation().isDone()) {
			this.friend.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
		}
	}

	@Override
	public void stop() {
		this.target = null;
		this.isFleeing = false;
	}

	private boolean fleeFromPlayer() {
		RandomSource random = this.friend.getRandom();
		Vec3 awayDirection = this.friend.position().subtract(this.target.position()).multiply(1, 0, 1).normalize();
		for (int i = 0; i < 8; i++) {
			float angleOffset = (random.nextFloat() - 0.5F) * (float) Math.PI;
			double distance = 6.0D + random.nextInt(11);

			Vec3 direction = awayDirection.yRot(angleOffset);
			Vec3 destination = this.friend.position().add(direction.scale(distance));

			Path path = this.friend.getNavigation().createPath(destination.x, destination.y, destination.z, 1);
			if (path != null && path.canReach()) {
				return this.friend.getNavigation().moveTo(path, 1.5D);
			}
		}
		return false;
	}

	private boolean isPlayerStaringBack() {
		Vec3 look = this.target.getViewVector(1.0F).normalize();
		Vec3 toFriend = this.friend.getEyePosition().subtract(this.target.getEyePosition());
		double distance = toFriend.length();
		return look.dot(toFriend.scale(1.0D / distance)) > 1.0D - 0.025D / distance;
	}
}
