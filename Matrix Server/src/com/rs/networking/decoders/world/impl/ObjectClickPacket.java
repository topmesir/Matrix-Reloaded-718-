package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.ObjectHandler;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ObjectClickPacket extends IncomingPacket {
	
	private final static int OBJECT_CLICK1_PACKET = 26;
	private final static int OBJECT_CLICK2_PACKET = 59;
	private final static int OBJECT_CLICK3_PACKET = 40;
	private final static int OBJECT_CLICK4_PACKET = 23;
	private final static int OBJECT_CLICK5_PACKET = 80;
	private final static int OBJECT_EXAMINE_PACKET = 25;
	
	
	public int[] packetIds() {
		return new int[] {
				26, 59, 40, 23, 80, 25
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		switch(packetId) {
		case OBJECT_CLICK1_PACKET:
			ObjectHandler.handleOption(player, stream, 1);
			break;
		case OBJECT_CLICK2_PACKET:
			ObjectHandler.handleOption(player, stream, 2);
			break;
		case OBJECT_CLICK3_PACKET:
			ObjectHandler.handleOption(player, stream, 3);
			break;
		case OBJECT_CLICK4_PACKET:
			ObjectHandler.handleOption(player, stream, 4);
			break;
		case OBJECT_CLICK5_PACKET:
			ObjectHandler.handleOption(player, stream, 5);
			break;
		case OBJECT_EXAMINE_PACKET:
			ObjectHandler.handleOption(player, stream, -1);
			break;
		}
	}

}
