package net.nebula.calamity_api.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import net.nebula.calamity_api.client.ShaderCore;

public class ForceShaderPacket {
    private final boolean forceShader;

    public ForceShaderPacket(boolean forceShader) {
        this.forceShader = forceShader;
    }

    public static void encode(ForceShaderPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.forceShader);
    }

    public static ForceShaderPacket decode(FriendlyByteBuf buf) {
        return new ForceShaderPacket(buf.readBoolean());
    }

    public static void handle(ForceShaderPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
        	if (msg != null) {
        		boolean forceShaderVal = msg.forceShader;
            	ShaderCore.setForced(forceShaderVal);
        	}
        });
        context.setPacketHandled(true);
    }
}
