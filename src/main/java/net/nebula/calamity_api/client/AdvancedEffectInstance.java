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
    private String last = "";
    private final PostChain MainChain;
    private final PostPass MainPass;

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

    public AdvancedEffectInstance(PostChain chain) {
        this.MainChain = chain;
        this.MainPass = getPasses(chain).get(0);
        this.last = "swap";
    }

	private RenderTarget next() {
	    int number = last != null && last.length() > 4 ? last.substring(4).matches("\\d+") ? Integer.parseInt(last.substring(4)) + 1 : 1 : 1;
	    String target = "auto" + number;
	    MainChain.addTempTarget(target, MainPass.inTarget.width, MainPass.inTarget.height);
	    last = target;
	    return MainChain.getTempTarget(target);
	}

    public void End(String endType) {
        try {
            this.MainChain.addPass(endType, this.MainChain.getTempTarget(last), this.MainPass.inTarget);
        } catch (IOException e) {
            throw new RuntimeException("[CalamityAPI] Unable to finalize shader", e);
        }
    }

		@Nullable
		@SuppressWarnings("unchecked")
		public AdvancedPostPass Add(String data,Function<AdvancedPostPass, Boolean> func,boolean updateable) {
			try {
				PostPass effect = this.MainChain.addPass(data,this.MainChain.getTempTarget(this.last),next());
				AdvancedPostPass advPass = new AdvancedPostPass(data,effect,func,updateable);
				func.apply(advPass);
				this.passes.add(this.passes.size(),advPass);
				return advPass;
			} catch (IOException e) {
				throw new RuntimeException("[CalamityAPI] failed to add shader", e);
			}
		}
}