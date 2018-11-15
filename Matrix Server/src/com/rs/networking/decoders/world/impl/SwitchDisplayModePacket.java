package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class SwitchDisplayModePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				98
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		int displayMode = stream.readUnsignedByte();
		player.setScreenWidth(stream.readUnsignedShort());
		player.setScreenHeight(stream.readUnsignedShort());
		@SuppressWarnings("unused")
		boolean switchScreenMode = stream.readUnsignedByte() == 1;
		if (!player.hasStarted() || player.hasFinished() || displayMode == player.getDisplayMode() || !player.getInterfaceManager().containsInterface(742))
			return;
		player.setDisplayMode(displayMode);
		player.getInterfaceManager().removeAll();
		player.getInterfaceManager().sendInterfaces();
		player.getInterfaceManager().sendInterface(742);
	}

}
