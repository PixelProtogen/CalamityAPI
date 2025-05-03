package net.nebula.calamity_api;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Mth;

import net.nebula.calamity_api.client.ShaderCore;

import net.nebula.calamity_api.math.CalamityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CommonSetup {

	@SubscribeEvent
	public static void onSetup(ShaderCore.REGISTER event) {
		
//		event.register("calamity_api:advanced/black_hole", effect -> {
//			Vector3f pos = CalamityUtils.worldToScreenPoint(new Vector3f(0.5F,5.5F,0.5F),32F);
//		    float Depth = CalamityUtils.depthToValue(pos.z,32F,0F);
//		    effect.effect().safeGetUniform("Data").set(pos.x,pos.y, (1F - Depth) * 0.5F);
//		    return Depth > 0;
//		}, true);
//
//		event.register("calamity_api:simple/color_clamp", effect -> {
//			effect.effect().safeGetUniform("Data").set(0.08F,0.91F,0F);
//		    return CalamityUtils.inScreenBounds(new Vector3f(0.5F,5.5F,0.5F));
//		}, true);
//
//		event.register("calamity_api:advanced/cracks", effect -> {
//			effect.sampler("HeightmapSampler",new ResourceLocation("calamity_api","textures/noise_heightmap.png"));
//			return true;
//		}, false);
//
		event.register("calamity_api:advanced/black_hole", effect -> {
			Vector3f pos = CalamityUtils.worldToScreenPoint(new Vector3f(0.5F, 5.5F, 0.5F), 64F);
			if (pos.z <= 0) return false;
		
			float screenSize = CalamityUtils.worldSpaceSize(pos.z, 1.0F);
			effect.effect().safeGetUniform("Data").set(pos.x, pos.y, screenSize);
			return true;
		}, true);
		
	}
}

