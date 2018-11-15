package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.command.CommandFactory;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.PublicChatMessage;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;
import com.rs.utilities.huffman.Huffman;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ChatPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				53
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		if (player.getLastPublicMessage() > Utilities.currentTimeMillis())
			return;
		player.setLastPublicMessage(Utilities.currentTimeMillis() + 300);
		int colorEffect = stream.readUnsignedByte();
		int moveEffect = stream.readUnsignedByte();
		String message = Huffman.readEncryptedMessage(200, stream);
		if (message == null || message.replaceAll(" ", "").equals(""))
			return;
		if (message.startsWith("::") || message.startsWith(";;")) {
			CommandFactory.executeCommand(player, message.replace("::", "").replace(";;", "").split(" "));
			return;
		}
		if (player.getMuted() > Utilities.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You temporary muted. Recheck in 48 hours.");
			return;
		}
		int effects = (colorEffect << 8) | (moveEffect & 0xff);
		if (player.chatType == 1)
			player.sendFriendsChannelMessage(Utilities.fixChatMessage(message));
		else
			player.sendPublicChatMessage(new PublicChatMessage(Utilities.fixChatMessage(message), effects));
		player.setLastMsg(message);
		if (Settings.DEBUG)
			Logger.log(this, "Chat type: " + player.chatType);
	}

}
