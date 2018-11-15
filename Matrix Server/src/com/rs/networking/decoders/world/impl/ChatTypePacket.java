package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ChatTypePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				83
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		player.chatType = stream.readUnsignedByte();
	}

}
