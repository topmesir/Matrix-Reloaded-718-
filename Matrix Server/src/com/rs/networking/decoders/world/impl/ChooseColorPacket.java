package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ChooseColorPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				97
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		@SuppressWarnings("unused")
		int colorId = stream.readUnsignedShort();
	}

}
