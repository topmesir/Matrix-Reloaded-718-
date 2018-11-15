package com.rs.networking.decoders.world;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.io.InputStream;

/**
 * TODO: File Header
 * @author Abysa/Dido#4821 4 Dec 2016
 */
public abstract class IncomingPacket {
	
	public abstract int[] packetIds();
	
	public abstract void processPacket(Player player, InputStream stream, int packetId);
	
}
