package net.nebula.calamity_api.client;

import net.minecraft.client.renderer.PostPass;
import com.google.common.base.Function;
import net.minecraft.client.renderer.EffectInstance;
import javax.annotation.Nullable;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import java.util.List;
import java.util.ArrayList;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class AdvancedPostPass {
    private final String shader;
    private final @Nullable PostPass pass;
    private final Function<AdvancedPostPass, Boolean> func;
    private final boolean updateable;
    private final List<Integer> sampledTextures = new ArrayList<>();

	/*
	 * custom Pass instance to store data
	 */
    public AdvancedPostPass(String shader, @Nullable PostPass pass, Function<AdvancedPostPass, Boolean> func, boolean updateable) {
        this.shader = shader;
        this.pass = pass;
        this.func = func;
        this.updateable = updateable;
    }

	@Nullable
	public EffectInstance effect() {
	    return this.pass != null ? this.pass.getEffect() : null;
	}

	/*
	 * Loads Samplers from ResourceLocation. You technically can use safeGetUniform but whatever
	 */
	public int sampler(String sampler, ResourceLocation location) {
	    try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location);
	         NativeImage image = NativeImage.read(stream)) {
	
	        final int textureId = TextureUtil.generateTextureId();
	        TextureUtil.prepareImage(textureId, image.getWidth(), image.getHeight());

			image.flipY();
	        image.upload(0, 0, 0, false);
	
		    this.effect().setSampler(sampler, () -> textureId);
		    this.sampledTextures.add(textureId);
		    return textureId;
	
	    } catch (IOException e) {
	        e.printStackTrace();
	        return -1;
	    }
	}

	/*
	 * Do not fire unless you know what youre doing
	 * Unbinds loaded sampler textures
	 */
    public void release() {
        for (int textureId : this.sampledTextures) {
            TextureUtil.releaseTextureId(textureId);
        }
        this.sampledTextures.clear();
    }

    public String shader() { return shader; }
    public PostPass pass() { return pass; }
    public Function<AdvancedPostPass, Boolean> func() { return func; }
    public boolean updateable() { return updateable; }
}