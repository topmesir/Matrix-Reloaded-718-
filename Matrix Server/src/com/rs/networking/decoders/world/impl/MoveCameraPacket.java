package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class MoveCameraPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				103
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		stream.readUnsignedShort();
		stream.readUnsignedShort();
	}

}
