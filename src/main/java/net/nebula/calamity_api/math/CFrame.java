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

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.ForgeHooksClient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.player.AbstractClientPlayer;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.client.player.LocalPlayer;

@OnlyIn(Dist.CLIENT)
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

	private Vec2 getScreenSize() {
		Minecraft mc = Minecraft.getInstance();
		return new Vec2(mc.getWindow().getWidth(),mc.getWindow().getHeight());
	}
	
	@OnlyIn(Dist.CLIENT)
	public Vector3f worldToScreenPoint(Vector3f worldPos, float maxDistance,float FOV) {
		Vec2 screenSize = getScreenSize();
		float aspectRatio = screenSize.x / screenSize.y;
		float fovY = (float) Math.toRadians(FOV);
		float nearPlane = 0.1f;
	
		Vector3f toPoint = new Vector3f(worldPos);
		toPoint.sub(this.position);
	
		float camX = toPoint.dot(this.leftVector);
		float camY = toPoint.dot(this.upVector);
		float camZ = toPoint.dot(this.lookVector);
	
		if (camZ <= nearPlane || camZ > maxDistance) {
			return new Vector3f(0F, 0F, -1F);
		}
	
		float fovX = 2.0f * (float) Math.atan(Math.tan(fovY / 2.0f) * aspectRatio);
	
		float projX = (camX / camZ) / (float) Math.tan(fovX / 2.0f);
		float projY = (camY / camZ) / (float) Math.tan(fovY / 2.0f);
	
		float screenX = (1.0f - projX) * 0.5f;
		float screenY = (projY + 1.0f) * 0.5f;
	
		return new Vector3f(screenX, screenY, camZ);
	}

	public static Vector3f Vec3ToVec3f(Vec3 v3) {
		return new Vector3f((float) v3.x,(float) v3.y,(float) v3.z);
	}

	public static CFrame Camera(Camera camera) {
		return new CFrame(Vec3ToVec3f(camera.getPosition()),camera.getUpVector(),camera.getLookVector(),camera.getLeftVector());
	}
}