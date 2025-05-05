package net.nebula.calamity_api.math;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import com.mojang.blaze3d.vertex.PoseStack;

import net.nebula.calamity_api.math.CFrame;
import net.minecraft.world.phys.Vec2;

import net.nebula.calamity_api.client.PlayerFOV;

@OnlyIn(Dist.CLIENT)
public class CalamityUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static float worldSpaceSize(float depth, float size) {
		Minecraft mc = Minecraft.getInstance();
		Vec2 screenSize = new Vec2(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		float fovDegrees = mc.options.fov().get();
		float fovRadians = (float) Math.toRadians(fovDegrees);
	
		float pixelsPerUnit = screenSize.y / (2.0f * depth * (float)Math.tan(fovRadians / 2.0));
		
		float screenSizePixels = size * pixelsPerUnit;
		return screenSizePixels / screenSize.y;
	}
	
	public static Vector3f worldToScreenPoint(Vector3f worldPos, float MaxDist) {
		Minecraft mc = Minecraft.getInstance();
		Camera cam = mc.gameRenderer.getMainCamera();
		CFrame cf = CFrame.Camera(cam);
		Vector3f result = cf.worldToScreenPoint(worldPos,MaxDist,PlayerFOV.getCurrentFov());
		return result;
	}

	public static boolean inScreenBounds(Vector3f worldPos) {
		Vector3f result = worldToScreenPoint(worldPos,2048F);
		return result.z > -1F;
	}

	public static Vector3f worldToScreenPoint(BlockPos pos, float MaxDist) {
		return worldToScreenPoint( new Vector3f( (float) pos.getX(),(float) pos.getY(),(float) pos.getZ()) , MaxDist );
	}
}
