package net.nebula.calamity_api.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.PostChain;
import java.io.IOException;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.GameRenderer;
import java.util.Map;
import com.google.common.base.Function;
import java.util.HashMap;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import java.lang.reflect.Field;

import net.nebula.calamity_api.network.CalamityAPIGamerules;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShaderCore {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static Minecraft mc;
        private static GameRenderer gr;
		private static final ResourceLocation coreShader = new ResourceLocation("calamity_api:shaders/post/blank.json");
		private static final Map<String, AdvancedPostPass> registered = new HashMap<>();
		private static boolean forceEnabled = true;
		
		private static AdvancedEffectInstance effect;

		public static class REGISTER extends Event {
			public REGISTER() {}

			public static void register(String shader, Function<AdvancedPostPass, Boolean> context, boolean updateOnTick) {
				registered.put(shader, new AdvancedPostPass(shader, null, context, updateOnTick));
				LOGGER.info("[CalamityAPI] Registered shader {}",shader);
			}
		}

	    @SubscribeEvent
	    public static void onClientSetup(ClientPlayerNetworkEvent.LoggingIn event) {
	        MinecraftForge.EVENT_BUS.post(new REGISTER());
	        LOGGER.info("[CalamityAPI] Fired RegisterShaderEvent to allow shader registrations");
	    }

	    private static boolean canRun(Player player) {
	    	return mc != null && player != null && player.level().isClientSide() && player instanceof Player && gr != null;
	    }

	    public static void setForced(boolean value) {
	    	forceEnabled = value;
	    }

		@SuppressWarnings("unchecked")
	    private static void setRenderTrueIfNot() {
	    	if (mc != null && gr != null) {
			    try {
			        Field field = GameRenderer.class.getDeclaredField("effectActive");
			        field.setAccessible(true);
			        boolean res = (boolean) field.get(gr);
			        if (res == false) {
			        	field.set(gr,true);
			        }
			    } catch (Exception e) {
			        throw new RuntimeException("[CalamityAPI] Failed to force render", e);
			    }
	    	}
	    }

		@SubscribeEvent
		public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
			if (forceEnabled == true) {
				setRenderTrueIfNot();
			}
			
			if ( event.phase == TickEvent.Phase.END && canRun(event.player) && gr.currentEffect() == null ) {
				gr.loadEffect(coreShader);
				PostChain pc = gr.currentEffect();
				effect = new AdvancedEffectInstance(pc);

				for (Map.Entry<String, AdvancedPostPass> entry : registered.entrySet()) {
	                AdvancedPostPass data = entry.getValue();
	                EffectInstance effectInst = effect.Add(entry.getKey(), data.func(), data.updateable());
	            }
				effect.End("blit");
	            pc.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
			} else if (mc != null && gr != null && effect != null ) {
				RenderSystem.recordRenderCall(() -> { effect.Update(false); });
			} else if ( gr == null && mc != null ) {
				gr = mc.gameRenderer;
			} else if ( mc == null ) {
				mc = Minecraft.getInstance();
			}
		}
}