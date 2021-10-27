package twopiradians.blockArmor.packet;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import twopiradians.blockArmor.client.key.KeyActivateSetEffect;

public class CActivateSetEffectPacket {

	private boolean isKeyPressed;
	private UUID player;

	public CActivateSetEffectPacket() {}

	public CActivateSetEffectPacket(boolean isKeyPressed, UUID player) {
		this.isKeyPressed = isKeyPressed;
		this.player = player;
	}

	public static void encode(CActivateSetEffectPacket packet, FriendlyByteBuf buf) {
		buf.writeBoolean(packet.isKeyPressed);
		buf.writeUtf(packet.player.toString());
	}

	public static CActivateSetEffectPacket decode(FriendlyByteBuf buf) {		
		boolean isKeyPressed = buf.readBoolean();
		UUID player = UUID.fromString(buf.readUtf(32767));
		return new CActivateSetEffectPacket(isKeyPressed, player);
	}

	public static class Handler  {

		public static void handle(CActivateSetEffectPacket packet, Supplier<NetworkEvent.Context> ctx) {
			KeyActivateSetEffect.isKeyDown.put(packet.player, packet.isKeyPressed);
			ctx.get().setPacketHandled(true);
		}
	}
}