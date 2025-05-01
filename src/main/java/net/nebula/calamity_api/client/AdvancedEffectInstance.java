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
package net.nebula.calamity_api.client;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;

import java.util.List;
import net.minecraft.client.renderer.EffectInstance;
import java.lang.reflect.Field;
import javax.annotation.Nullable;
import java.io.IOException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancedEffectInstance {
		private final List<AdvancedPostPass> passes = Lists.newArrayList();
		private String last = "swap";

		@SuppressWarnings("unchecked")
		private static List<PostPass> getPasses(PostChain chain) {
		    try {
		        Field field = PostChain.class.getDeclaredField("passes");
		        field.setAccessible(true);
		        return (List<PostPass>) field.get(chain);
		    } catch (Exception e) {
		        throw new RuntimeException("[CalamityAPI] Failed to access passes", e);
		    }
		}

		private PostChain MainChain;
		private PostPass MainPass;

		public AdvancedEffectInstance(PostChain chain) {
			this.MainChain = chain;
			this.MainPass = getPasses(chain).get(0);
		}

		public void Update(boolean forceUpdateAll) {
			for ( AdvancedPostPass pass : this.passes) {
				if (pass.updateable() == true || forceUpdateAll == true) {
					pass.release();
					boolean result = pass.func().apply(pass);
					pass.effect().safeGetUniform("isEnabled").set(result ? 1.0F : 0.0F);
				}
			}
		}

		private RenderTarget next() {
		    int number = 0;
		    if (this.last.startsWith("swap")) {
		        String suffix = this.last.substring(4);
		        if (!suffix.isEmpty()) {
		            try {
		                number = Integer.parseInt(suffix);
		            } catch (NumberFormatException e) {
		                number = 0;
		            }
		        }
		    }
		    number++;
		    String target = "swap" + number;
		    this.MainChain.addTempTarget(target, this.MainPass.inTarget.width, this.MainPass.inTarget.height);
		    this.last = target;
		    return this.MainChain.getTempTarget(target);
		}


		public void End(String endType) {
			try {
				this.MainChain.addPass(endType,this.MainChain.getTempTarget(last),this.MainPass.inTarget);
				Update(true);
			} catch (IOException e) {
				throw new RuntimeException("[CalamityAPI] unable to finalize shader", e);
			}
		}

		@Nullable
		public EffectInstance Add(String data,Function<AdvancedPostPass, Boolean> func,boolean updateable) {
			try {
				PostPass effect = this.MainChain.addPass(data,this.MainChain.getTempTarget(this.last),next());
				AdvancedPostPass advPass = new AdvancedPostPass(data,effect,func,updateable);
				func.apply(advPass);
				this.passes.add(this.passes.size(),advPass);
				return effect.getEffect();
			} catch (IOException e) {
				throw new RuntimeException("[CalamityAPI] failed to add shader", e);
			}
		}
}
