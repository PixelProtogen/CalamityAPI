package net.nebula.calamity_api.network;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.nebula.calamity_api.network.ForceShaderPacket;
import net.nebula.calamity_api.CalamityApiMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacketRegister {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		CalamityApiMod.addNetworkMessage(ForceShaderPacket.class, ForceShaderPacket::encode, ForceShaderPacket::decode, ForceShaderPacket::handle);
	}
}
