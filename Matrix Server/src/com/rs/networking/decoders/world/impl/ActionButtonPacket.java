package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ActionButtonPacket extends IncomingPacket {
	
	public int[] packetIds() {
		return new int[] {
				14, 67, 5, 55, 68, 90, 6, 32, 27, 96
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		ButtonClickHandler.handleButtons(player, stream, packetId);
	}

}
