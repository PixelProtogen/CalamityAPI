package net.nebula.calamity_api;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import net.nebula.calamity_api.client.ShaderCore;

import net.nebula.calamity_api.math.CalamityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.ibm.icu.impl.Pair;
import net.minecraft.client.CameraType;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CommonSetup {
	static int id0 = 0;

	@SubscribeEvent
	public static void onSetup(ShaderCore.REGISTER event) {
//
//		event.register("calamity_api:simple/color_clamp", effect -> {
//			effect.effect().safeGetUniform("Data").set(0.08F,0.91F,0F);
//		    return CalamityUtils.inScreenBounds(new Vector3f(0.5F,5.5F,0.5F));
//		}, true, event.toList(ShaderCore.EditType.ALL));

//		id0 = event.register("calamity_api:advanced/cracks", effect -> {
//			effect.sampler("HeightmapSampler",new ResourceLocation("calamity_api","textures/noise_heightmap.png"));
//			return true;
//		}, false, event.toList(ShaderCore.EditType.ALL));

		event.register("calamity_api:advanced/black_hole", effect -> {
			//if (CalamityUtils.getCameraType() == CameraType.THIRD_PERSON_FRONT) return false;
			Vector3f at = new Vector3f(0.5F, 5.5F, 0.5F);
			Vector3f pos = CalamityUtils.worldToScreenPoint(at, 64F);
			if (pos.z <= 0 || !CalamityUtils.inView(at)) return false;
		
			float screenSize = CalamityUtils.worldSpaceSize(pos.z, 1.0F);
			effect.effect().safeGetUniform("Data").set(pos.x, pos.y, screenSize);
			return true;
		}, true, event.toList(ShaderCore.EditType.ALL));
		
	}

	@SubscribeEvent
	public static void onPost(ShaderCore.POST event) {
//		event.iterate(effect -> {
//			System.out.println("Working shader id ["+effect.first+"] PATH ["+effect.second+"]");
//			return null;
//		});
//		boolean worked = ShaderCore.editShader(id0,effect -> {
//			effect.sampler("HeightmapSampler",new ResourceLocation("calamity_api","textures/cracks_heightmap.png"));
//			return true;
//		}); 
		//ShaderCore.forceShaderUpdate(id0);
//		boolean worked = ShaderCore.editShaderToggle(id0,false);
//		ShaderCore.reload();
//		System.out.println("Success: "+worked);
	}
}

