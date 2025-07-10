package net.nebula.calamity_api.client;

import org.lwjgl.glfw.GLFW;

import net.nebula.calamity_api.CalamityApiMod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ToggleShaderEffect {
	public static final KeyMapping TOGGLE_SHADER_EFFECTS = new KeyMapping("key.calamity_api.toggle_shader_effects", GLFW.GLFW_KEY_UNKNOWN, "key.categories.calamity_api") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ShaderCore.toggleRender();
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(TOGGLE_SHADER_EFFECTS);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				TOGGLE_SHADER_EFFECTS.consumeClick();
			}
		}
	}
}
