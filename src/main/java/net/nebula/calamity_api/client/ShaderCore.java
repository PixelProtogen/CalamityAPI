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
import javax.annotation.Nullable;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShaderCore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Minecraft mc;
    private static GameRenderer gr;

    private static final ResourceLocation coreShader = new ResourceLocation("calamity_api:shaders/post/blank.json");
	private static final String endProgram = "blit";
    
    private static final Map<String, AdvancedPostPass> registered = new HashMap<>();
    private static final Map<String, AdvancedPostPass> active = new HashMap<>();
    private static final Map<String, Boolean> lastState = new HashMap<>();

    private static long lastRebuild = 0;
	private static final long REBUILD_COOLDOWN_MS = 100;
	private static boolean pendingRebuild = false;

    private static boolean forceEnabled = true;
    @Nullable
    private static AdvancedEffectInstance effect;

    public static class REGISTER extends Event {
        public static void register(String shader, Function<AdvancedPostPass, Boolean> context, boolean updateOnTick) {
            registered.put(shader, new AdvancedPostPass(shader, null, context, updateOnTick));
            lastState.put(shader, true);
            LOGGER.info("[CalamityAPI] Registered shader {}", shader);
        }
    }

    @SubscribeEvent
    public static void onClientSetup(ClientPlayerNetworkEvent.LoggingIn event) {
        MinecraftForge.EVENT_BUS.post(new REGISTER());
        LOGGER.info("[CalamityAPI] Fired ShaderCore.REGISTER to allow shader registrations");
    }

    private static boolean canRun(Player player) {
        return mc != null && player != null && player.level().isClientSide() && gr != null;
    }

    public static void setForced(boolean value) {
        forceEnabled = value;
    }

    private static void setRenderTrueIfNot() {
        if (mc != null && gr != null) {
            try {
                Field field = GameRenderer.class.getDeclaredField("effectActive");
                field.setAccessible(true);
                boolean res = (boolean) field.get(gr);
                if (!res) field.set(gr, true);
            } catch (Exception e) {
                throw new RuntimeException("[CalamityAPI] Failed to force render", e);
            }
        }
    }

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			if (mc == null) mc = Minecraft.getInstance();
			if (gr == null) gr = mc.gameRenderer;
		
			if (!canRun(mc.player)) return;
		
			if (forceEnabled) setRenderTrueIfNot();
		
			if (gr.currentEffect() == null) {
				rebuildChainIfNeeded(true);
			} else if (effect != null) {
				rebuildChainIfNeeded(false);
			}
		}
	}

    public static void reload() {
    	gr.shutdownEffect();
    }

	private static void rebuildChainIfNeeded(boolean forceRebuild) {
	    boolean needsRebuild = forceRebuild;
	
	    for (Map.Entry<String, AdvancedPostPass> entry : active.entrySet()) {
	        AdvancedPostPass pass = entry.getValue();
	        boolean shouldEnable = pass.func().apply(pass);
	        boolean last = lastState.getOrDefault(pass.shader(), false);
	        if (shouldEnable != last) {
	            needsRebuild = true;
	        }
	        lastState.put(pass.shader(), shouldEnable);
	    }
	
	    long now = System.currentTimeMillis();
	
	    if (needsRebuild) {
	        if (forceRebuild || now - lastRebuild >= REBUILD_COOLDOWN_MS) {
	            doRebuild();
	            lastRebuild = now;
	            pendingRebuild = false;
	        } else {
	            pendingRebuild = true;
	        }
	    } else if (pendingRebuild && now - lastRebuild >= REBUILD_COOLDOWN_MS) {
	        doRebuild();
	        lastRebuild = now;
	        pendingRebuild = false;
	    }
	}
	
	private static void doRebuild() {
	    gr.shutdownEffect();
	    gr.loadEffect(coreShader);
	    PostChain chain = gr.currentEffect();
	    effect = new AdvancedEffectInstance(chain);
	
	    for (Map.Entry<String, AdvancedPostPass> entry : registered.entrySet()) {
	        if (lastState.getOrDefault(entry.getKey(), false)) {
	            active.put(entry.getKey(), effect.Add(entry.getKey(), entry.getValue().func(), entry.getValue().updateable()));
	        }
	    }
	
	    effect.End(endProgram);
	    chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
	}
}
