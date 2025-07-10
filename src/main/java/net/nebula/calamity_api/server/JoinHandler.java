package net.nebula.calamity_api.server;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

import net.nebula.calamity_api.network.CalamityAPIGamerules;
import net.nebula.calamity_api.CalamityApiMod;

import net.nebula.calamity_api.network.ForceShaderPacket;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JoinHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean value = player.level().getGameRules().getBoolean(CalamityAPIGamerules.FORCE_SHADER_RENDER);
        
        CalamityApiMod.PACKET_HANDLER.send(
            PacketDistributor.PLAYER.with(() -> player),
            new ForceShaderPacket(value)
        );
    }
}
