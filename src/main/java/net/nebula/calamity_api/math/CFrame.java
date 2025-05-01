/**
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside net.nebula.calamity_api as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
*/
package net.nebula.calamity_api.math;

import net.minecraft.client.Camera;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import javax.annotation.Nullable;

public class CFrame {
	private Vector3f position;
	private Vector3f upVector;
	private Vector3f lookVector;
	private Vector3f leftVector;

	public CFrame(Vector3f position, Vector3f upVector, Vector3f lookVector, Vector3f leftVector) {
		this.position = position;
		this.upVector = upVector;
		this.lookVector = lookVector;
		this.leftVector = leftVector;
	}

	@OnlyIn(Dist.CLIENT)
	private Vec2 getScreenSize() {
		Minecraft mc = Minecraft.getInstance();
		return new Vec2(mc.getWindow().getWidth(),mc.getWindow().getHeight());
	}

	@OnlyIn(Dist.CLIENT)
	private int getFOV() {
		return Minecraft.getInstance().options.fov().get();
	}

	@OnlyIn(Dist.CLIENT)
	public Vector3f worldToScreenPoint(Vector3f worldPos, float maxDistance) {
		Vec2 screenSize = getScreenSize();
		float fovDegrees = (float) getFOV();
		float aspectRatio = screenSize.x / screenSize.y;
		float fovRadians = (float) Math.toRadians(fovDegrees);
		float nearPlane = 0.1f;
		
		Vector3f toPoint = new Vector3f(worldPos);
		toPoint.sub(this.position);
	
		float camX = toPoint.dot(this.leftVector);
		float camY = toPoint.dot(this.upVector  );
		float camZ = toPoint.dot(this.lookVector);
	
		if (camZ <= nearPlane || camZ > maxDistance ) {
			return new Vector3f(0F,0F,-1F);
		}
	
		float scale = (float) (1.0 / Math.tan(fovRadians / 2.0));
		float projX = (camX / camZ) * scale / aspectRatio;
		float projY = (camY / camZ) * scale;
		
		float screenX = (1.0f - projX) * 0.5f;
		float screenY = (projY + 1.0f) * 0.5f;
	
		return new Vector3f(screenX, screenY, camZ);
	}

	private static Vector3f Vec3ToVec3f(Vec3 v3) {
		return new Vector3f((float) v3.x,(float) v3.y,(float) v3.z);
	}

	public static CFrame Camera(Camera camera) {
		return new CFrame(Vec3ToVec3f(camera.getPosition()),camera.getUpVector(),camera.getLookVector(),camera.getLeftVector());
	}
}
