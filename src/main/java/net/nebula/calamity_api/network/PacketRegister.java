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
