package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.InventoryHandler;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ItemOnItemPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				3
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		InventoryHandler.handleItemOnItem(player, stream);
	}

}
