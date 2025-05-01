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

@OnlyIn(Dist.CLIENT)
public class CalamityUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static float depthToValue(float depth, float maxDepth, float minValue) {
	    if (depth >= maxDepth || depth <= 0) return -1F;
	    return Math.max(depth,minValue) / maxDepth;
	}
	
	public static Vector3f worldToScreenPoint(Vector3f worldPos, float MaxDist) {
		Minecraft mc = Minecraft.getInstance();
		CFrame cf = CFrame.Camera(mc.gameRenderer.getMainCamera());
		Vector3f result = cf.worldToScreenPoint(worldPos,MaxDist);
		return result;
	}

	public static Vector3f worldToScreenPoint(BlockPos pos, float MaxDist) {
		return worldToScreenPoint( new Vector3f( (float) pos.getX(),(float) pos.getY(),(float) pos.getZ()) , MaxDist );
	}
}
