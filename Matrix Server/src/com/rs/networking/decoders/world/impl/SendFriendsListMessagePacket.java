package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;
import com.rs.utilities.huffman.Huffman;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class SendFriendsListMessagePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				82
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		if (player.getMuted() > Utilities.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You temporary muted. Recheck in 48 hours.");
			return;
		}
		String username = stream.readString();
		Player p2 = World.getPlayerByDisplayName(username);
		if (p2 == null)
			return;

		player.getContent().get(ContentType.FRIENDSLIST).sendMessage(p2, Utilities.fixChatMessage(Huffman.readEncryptedMessage(150, stream)));
	}

}
