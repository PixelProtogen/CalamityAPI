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

import net.minecraft.client.renderer.GameRenderer;
import java.util.Map;
import java.util.HashMap;
import com.google.common.base.Function;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import java.lang.reflect.Field;

import net.nebula.calamity_api.network.CalamityAPIGamerules;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import java.util.List;
import java.util.ArrayList;
import com.ibm.icu.impl.Pair;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import com.mojang.blaze3d.pipeline.RenderTarget;

// Idk why i added to add some notes to stuff but yea

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShaderCore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Minecraft mc;
    private static GameRenderer gr;
	private static boolean firstRun = true;
    private static long lastRebuild = 0;
	private static final long REBUILD_COOLDOWN_MS = 100;
	private static boolean pendingRebuild = false;
    private static boolean isShaderProgramEnabled = true;
    private static boolean forceEnabled = true;
    @Nullable
    private static PostChain targetEffect;
    @Nullable
    private static AdvancedEffectInstance effect;
    private static final ResourceLocation coreShader = new ResourceLocation("calamity_api:shaders/post/blank.json");
	private static final String endProgram = "blit";
	public enum EditType { FIXED, UPDATEABLE, FUNCTION, TOGGLE, ALL, UPDT_FUNC, UPDT_TOGL, FUNC_TOGL; }
	private static int gameLoadedShaders = 0;
	private static final Map<Integer, EditType> shaderAccessibility = new HashMap<>();
    private static final Map<Integer, String> registeredShaderIds = new HashMap<>();
    private static final Map<Integer, AdvancedPostPass> registered = new HashMap<>();
    private static final Map<Integer, AdvancedPostPass> active = new HashMap<>();
    private static final Map<Integer, Boolean> lastState = new HashMap<>();
    private static final Map<Integer, Boolean> disabledShaders = new HashMap<>();

    private static boolean editTypeReq(EditType editType, EditType targetEditType) {
		return switch (editType) {
			case FIXED -> false;
			case UPDATEABLE -> targetEditType == EditType.UPDATEABLE || targetEditType == EditType.UPDT_FUNC || targetEditType == EditType.UPDT_TOGL || targetEditType == EditType.ALL;
			case FUNCTION -> targetEditType == EditType.FUNCTION || targetEditType == EditType.UPDT_FUNC || targetEditType == EditType.FUNC_TOGL || targetEditType == EditType.ALL;
			case TOGGLE -> targetEditType == EditType.TOGGLE || targetEditType == EditType.UPDT_TOGL || targetEditType == EditType.FUNC_TOGL || targetEditType == EditType.ALL;
			case UPDT_FUNC -> targetEditType == EditType.UPDATEABLE || targetEditType == EditType.FUNCTION || targetEditType == EditType.UPDT_FUNC || targetEditType == EditType.ALL;
			case UPDT_TOGL -> targetEditType == EditType.UPDATEABLE || targetEditType == EditType.TOGGLE || targetEditType == EditType.UPDT_TOGL || targetEditType == EditType.ALL;
			case FUNC_TOGL -> targetEditType == EditType.FUNCTION || targetEditType == EditType.TOGGLE || targetEditType == EditType.FUNC_TOGL || targetEditType == EditType.ALL;
			case ALL -> true;
		};
	}
	
    public static class REGISTER extends Event {
    	/*
    	 * Shader : PATH to your shader (program/shader.json) file
    	 * Context: The controlling function that sets variables and etc.
    	 * updateOnTick: Controls whatever your function should update after register or no
    	 * editType: Controls what part of shader can be changed after registry
    	 * toggleable: Controls if your shader should even be loaded or no
    	 */
        public static int register(String shader, Function<AdvancedPostPass, Boolean> context, boolean updateOnTick,@Nullable EditType editType, @Nullable Boolean toggleable) {
        	int id = gameLoadedShaders++;
        	editType = editType != null ? editType : EditType.FIXED;
            registered.put(id, new AdvancedPostPass(shader, null, context, updateOnTick));
            lastState.put(id, true);
            registeredShaderIds.put(id, shader);
            shaderAccessibility.put(id, editType);
            disabledShaders.put(id, toggleable != null ? !toggleable : false);
            
            LOGGER.info("[CalamityAPI] Registered shader {}", shader);
            return id;
        }
    }

	public static class POST extends Event {
	    private final Map<Integer, String> shaderList = new HashMap<>();
	
	    public POST(Map<Integer, String> shaders) {
	        this.shaderList.putAll(shaders); // safer: avoids overwriting field
	    }
	
	    /**
	     * Iterates over all shader entries and applies a function to each Pair<id, shader>
	     */
	    public void iterate(Function<Pair<Integer, String>, ?> action) {
	        for (Map.Entry<Integer, String> entry : shaderList.entrySet()) {
	            action.apply(Pair.of(entry.getKey(), entry.getValue()));
	        }
	    }
	
	    /**
	     * Gets the shader path associated with an ID
	     */
	    public String getShader(int id) {
	        return shaderList.get(id);
	    }
	
	    /**
	     * Returns a list of shader IDs that match the given shader name/path
	     */
	    public List<Integer> getShaderInts(String shaderType) {
	        List<Integer> matching = new ArrayList<>();
	        for (Map.Entry<Integer, String> entry : shaderList.entrySet()) {
	            if (entry.getValue().equals(shaderType)) {
	                matching.add(entry.getKey());
	            }
	        }
	        return matching;
	    }
	}


	/*
	 * Force Shader to update via its function
	 */
    public static boolean forceShaderUpdate(int ShaderId) {
    	AdvancedPostPass shader = active.get(ShaderId);
    	return shader != null ? shader.func().apply(shader) : false;
    }

	/*
	 * Edit Shader's Function
	 */
    public static boolean editShader(int ShaderId,Function<AdvancedPostPass, Boolean> context) {
    	EditType shaderType = shaderAccessibility.getOrDefault(ShaderId, EditType.FIXED);
    	if ( editTypeReq(shaderType, EditType.FUNCTION) == true ) {
    		AdvancedPostPass old = registered.get(ShaderId);
    		registered.put(ShaderId, new AdvancedPostPass(old.shader(), null, context, old.updateable()));
    		return true;
    	}
    	return false;
    }

	/*
	 * Edit Shader's Tick toggle
	 */
    public static boolean editShader(int ShaderId, boolean updateOnTick) {
    	EditType shaderType = shaderAccessibility.getOrDefault(ShaderId, EditType.FIXED);
    	if ( editTypeReq(shaderType, EditType.UPDATEABLE) == true ) {
    		AdvancedPostPass old = registered.get(ShaderId);
    		registered.put(ShaderId, new AdvancedPostPass(old.shader(), null, old.func(), updateOnTick));
    		return true;
    	}
    	return false;
    }

	/*
	 * Toggle Shader's Work
	 */
    public static boolean editShaderToggle(int ShaderId, boolean isEnabled) {
    	EditType shaderType = shaderAccessibility.getOrDefault(ShaderId, EditType.FIXED);
    	if ( editTypeReq(shaderType, EditType.TOGGLE) == true ) {
    		disabledShaders.put(ShaderId, !isEnabled);
    		return true;
    	}
    	return false;
    }
	
	public static boolean isShaderEnabled(int ShaderId) {
		return !disabledShaders.getOrDefault(ShaderId, true);
	}

	/*
	 * Fires register event on client setup
	 */
    @SubscribeEvent
    public static void onClientSetup(ClientPlayerNetworkEvent.LoggingIn event) {
        MinecraftForge.EVENT_BUS.post(new REGISTER());
        LOGGER.info("[CalamityAPI] Fired ShaderCore.REGISTER to allow shader registrations");
    }

	/*
	 * Safe check
	 */
    private static boolean canRun(Player player) {
        return mc != null && player != null && player.level().isClientSide() && gr != null;
    }

	/*
	 * Sets if effects should be forced (Usually controlled by gamerule)
	 */
    public static void setForced(boolean value) {
        forceEnabled = value;
    }

    public static void toggleRender() {
    	if (forceEnabled == true) {
    		isShaderProgramEnabled = true;
    	} else {
    		isShaderProgramEnabled = !isShaderProgramEnabled;
    	}
    }

    public static boolean getRenderState() {
    	return isShaderProgramEnabled;
    }

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
			if (mc == null) mc = Minecraft.getInstance();
			if (gr == null) gr = mc.gameRenderer;
		
			if (!canRun(mc.player)) return;
			
			//if (forceEnabled) setRenderTrueIfNot();
			
			if (firstRun) {
				MinecraftForge.EVENT_BUS.post(new POST(registeredShaderIds));
        		LOGGER.info("[CalamityAPI] Fired ShaderCore.POST to allow shader changes if needed");
        		firstRun = false;
			}
		
			if (targetEffect == null && isShaderProgramEnabled) {
				rebuildChainIfNeeded(true);
			} else if (targetEffect != null) {
				if (isShaderProgramEnabled) {
					targetEffect.process(event.getRenderTick());
					mc.getMainRenderTarget().bindWrite(true);
				} else {
					reload();
				}
				rebuildChainIfNeeded(false);
			}
		}
	}

	/*
	 * Forces minecraft to shutdown effect resulting in ShaderCore making new instance of effect
	 */
    public static void reload() {
      if (targetEffect != null) {
         targetEffect.close();
      }
      targetEffect = null;
    }

	/*
	 * Checks if effects need to be rebuilded (And also fires their function to set values)
	 */
	private static void rebuildChainIfNeeded(boolean forceRebuild) {
	    boolean needsRebuild = forceRebuild;
	
	    for (Map.Entry<Integer, AdvancedPostPass> entry : active.entrySet()) {
			int id = entry.getKey();
			if (disabledShaders.getOrDefault(id, false)) continue;

	    	AdvancedPostPass pass = entry.getValue();

			boolean shouldEnable = pass.func().apply(pass);
			boolean last = lastState.getOrDefault(id, false);
			
			if (shouldEnable != last && pass.updateable()) {
			    needsRebuild = true;
			}
			
			lastState.put(id, shouldEnable);
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

	private static PostChain makePostChain(RenderTarget mainRenderTarget) {
		try {
			return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mainRenderTarget, coreShader);
		} catch (IOException e) {
			throw new RuntimeException("[CalamityAPI] Failed to create PostChain", e);
		}
	}

	/*
	 * Rebuilds whole effect
	 */
	private static void doRebuild() {
	    if (gr == null || mc == null) return;

		RenderTarget mainRenderTarget = mc.getMainRenderTarget();
		int windowX = mc.getWindow().getWidth();
		int windowY = mc.getWindow().getHeight();
	    
	    PostChain chain = makePostChain(mainRenderTarget);
	    if (chain == null) return;
	    chain.resize(windowX, windowY);
	    effect = new AdvancedEffectInstance(chain,mainRenderTarget,windowX,windowY);
	    //active.clear();
	
	    for (Map.Entry<Integer, AdvancedPostPass> entry : registered.entrySet()) {
	        int id = entry.getKey();
	        boolean isDisabled = disabledShaders.getOrDefault(id, false);
	        boolean shouldRun = lastState.getOrDefault(id, false);
	
	        if (!isDisabled && shouldRun) {
	            AdvancedPostPass pass = entry.getValue();
	            active.put(id, effect.Add(registeredShaderIds.get(id), pass.func(), pass.updateable()));
	        }
	    }
	
	    effect.End(endProgram);
	    chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
	    targetEffect = chain;
	}
}
