package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class KickFriendChatPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				74
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		player.setLastPublicMessage(Utilities.currentTimeMillis() + 1000); // avoids
		// message
		// appearing
		player.kickPlayerFromFriendsChannel(stream.readString());
	}

}
