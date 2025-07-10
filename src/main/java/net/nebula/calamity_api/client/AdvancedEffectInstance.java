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

	private RenderTarget mainTarget;
    private int X;
    private int Y;

	/*
	 * This works fine i guess
	 */
    @SuppressWarnings("unchecked")
    private static List<PostPass> getPasses(PostChain chain) {
        try {
            Field field = PostChain.class.getDeclaredField("passes");
            field.setAccessible(true);
            return (List<PostPass>) field.get(chain);
        } catch (Exception e) {
            System.out.println("[CalamityAPI] Failed to access passes");
            return null;
        }
    }

    public AdvancedEffectInstance(PostChain chain,RenderTarget mainTarget,int X,int Y) {
        this.MainChain = chain;
        
        this.last = "swap";
        this.mainTarget = mainTarget;
        this.X = X;
        this.Y = Y;
    }

	/*
	 * Creates new target for each Pass (effect)
	 */
	private RenderTarget next() {
	    int number = last != null && last.length() > 4 ? last.substring(4).matches("\\d+") ? Integer.parseInt(last.substring(4)) + 1 : 1 : 1;
	    String target = "auto" + number;
	    MainChain.addTempTarget(target, this.X,this.Y);
	    last = target;
	    return MainChain.getTempTarget(target);
	}

	/*
	 * Finalizes Effect Instance
	 */
    public void End(String endType) {
        try {
            this.MainChain.addPass(endType, this.MainChain.getTempTarget(last), this.mainTarget);
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