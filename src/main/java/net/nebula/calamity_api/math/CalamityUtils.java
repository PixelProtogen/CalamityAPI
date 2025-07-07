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
import net.minecraft.world.level.Level;
import net.minecraft.client.CameraType;

@OnlyIn(Dist.CLIENT)
public class CalamityUtils {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static float worldSpaceSize(float depth, float size) {
		Minecraft mc = Minecraft.getInstance();
		Vec2 screenSize = new Vec2(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		float fovDegrees = PlayerFOV.getCurrentFov();
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

	public static boolean inView(Vector3f worldPos) {
	    float step = 0.1f;
	    Minecraft mc = Minecraft.getInstance();
	    Vec3 eyeVec = mc.player.getEyePosition(mc.getPartialTick());
    	Vector3f eyePos = new Vector3f((float) eyeVec.x, (float) eyeVec.y, (float) eyeVec.z);
	    Level lvl = (Level) mc.player.level();
	    
	    Vector3f direction = new Vector3f();
	    worldPos.sub(eyePos, direction);
	    float distance = direction.length();
	    direction.normalize();
	    
	    
	    Vector3f current = new Vector3f(eyePos);
	
	    for (float traveled = 0; traveled < distance; traveled += step) {
	        int x = (int)Math.floor(current.x);
	        int y = (int)Math.floor(current.y);
	        int z = (int)Math.floor(current.z);
	        
	        if (lvl.getBlockState(BlockPos.containing(x, y, z)).canOcclude()) {
	            return false;
	        }
	
	        current.fma(step, direction);
	    }
	
	    return true;
	}

	public static CameraType getCameraType() {
		return Minecraft.getInstance().options.getCameraType();
	}

	public static Vector3f worldToScreenPoint(BlockPos pos, float MaxDist) {
		return worldToScreenPoint( new Vector3f( (float) pos.getX(),(float) pos.getY(),(float) pos.getZ()) , MaxDist );
	}
}
