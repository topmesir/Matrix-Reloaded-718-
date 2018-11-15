package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.NPCHandler;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class NPCClickPaket extends IncomingPacket {
	
	private final static int NPC_CLICK1_PACKET = 31;
	private final static int NPC_CLICK2_PACKET = 101;
	private final static int NPC_CLICK3_PACKET = 34;
	private static final int NPC_EXAMINE_PACKET = 9;
	
	public int[] packetIds() {
		return new int[] {
				31, 101, 34
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		switch(packetId) {
		case NPC_CLICK1_PACKET:
			NPCHandler.handleNPCOption1(player, stream);
			break;
		case NPC_CLICK2_PACKET:
			NPCHandler.handleNPCOption2(player, stream);
			break;
		case NPC_CLICK3_PACKET:
			NPCHandler.handleNPCOption3(player, stream);
			break;
		case NPC_EXAMINE_PACKET:
			NPCHandler.handleExamine(player, stream);
			break;
		}
	}

}
