package com.mrbysco.spookyfriends.handler;

import com.mrbysco.spookyfriends.attachment.FakeSteps;
import com.mrbysco.spookyfriends.config.SpookyConfig;
import com.mrbysco.spookyfriends.registry.SpookyRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class SpookyEventHandler {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();
		if (player.isSpectator() || level.isClientSide() || level.getGameTime() % 4 != 0) return;
		if (level.canSeeSky(player.blockPosition()) || player.getY() >= level.getSeaLevel() - 8) return;
		int fakeStepChance = SpookyConfig.COMMON.fakeStepsChance.get();
		if (fakeStepChance == 0) return;

		RandomSource random = level.getRandom();
		FakeSteps fake = player.getData(SpookyRegistry.FAKE_STEPS);
		if (fake.stepsLeft <= 0) {
			if (level.getGameTime() < fake.cooldownUntil || random.nextInt(fakeStepChance) != 0) return;
			int cooldown = SpookyConfig.COMMON.fakeStepsCooldown.get();
			fake.cooldownUntil = level.getGameTime() + (cooldown + random.nextInt(cooldown + 1)) * 20L;
			boolean towards = random.nextBoolean();
			fake.stepsLeft = 6 + random.nextInt(5);
			fake.angle = random.nextDouble() * Math.PI * 2;
			fake.distance = towards ? 14.0D : 3.0D;
			fake.stride = towards ? -1.1D : 1.1D;
		}

		fake.stepsLeft--;
		fake.distance += fake.stride;

		double x = player.getX() + Math.cos(fake.angle) * fake.distance;
		double z = player.getZ() + Math.sin(fake.angle) * fake.distance;
		SoundEvent step = level.getBlockState(player.blockPosition().below()).getSoundType().getStepSound();

		level.playSound(null, x, player.getY(), z, step, SoundSource.HOSTILE, 0.6F, 0.9F + random.nextFloat() * 0.2F);
	}
}
