package net.nebula.calamity_api.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerFOV {

    private static float currentEffectiveFov = 0.0f;

    public static float getCurrentFov() {
        return currentEffectiveFov;
    }

    @SubscribeEvent
    public static void onFovUpdate(ComputeFovModifierEvent event) {
	    float baseFov = (float) Minecraft.getInstance().options.fov().get();
	    currentEffectiveFov = baseFov * event.getNewFovModifier();
    }
}
