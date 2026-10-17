package com.mrbysco.spookyfriends.datagen;

import com.mrbysco.spookyfriends.datagen.client.SpookyLanguageProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class SpookyDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createProvider(SpookyLanguageProvider::new);
	}
}
