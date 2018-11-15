package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class RemoveFriendPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				24
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		player.getContent().get(ContentType.FRIENDSLIST).removeFriend(stream.readString());
	}

}
