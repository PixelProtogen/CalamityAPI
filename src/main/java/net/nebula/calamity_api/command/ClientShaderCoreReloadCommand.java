
package net.nebula.calamity_api.command;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.commands.Commands;
import net.nebula.calamity_api.client.ShaderCore;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientShaderCoreReloadCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("ClientShaderCoreReload").executes(arguments -> { 
			ShaderCore.reload(); 
			Player plr = (Player) arguments.getSource().getEntity();
			plr.displayClientMessage(Component.literal("ShaderCore reloaded!").withStyle(ChatFormatting.GOLD), false);
			return 0; 
		}));
	}
}
