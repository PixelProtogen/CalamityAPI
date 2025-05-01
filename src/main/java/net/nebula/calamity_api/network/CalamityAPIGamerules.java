package net.nebula.calamity_api.network;

import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.GameRules;

import net.nebula.calamity_api.CalamityApiMod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CalamityAPIGamerules {
    public static final GameRules.Key<GameRules.BooleanValue> FORCE_SHADER_RENDER = GameRules.register("forceShaderRender", GameRules.Category.MISC, GameRules.BooleanValue.create(true, (server, rule) -> {
        CalamityApiMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new ForceShaderPacket(rule.get()));
    }));
}