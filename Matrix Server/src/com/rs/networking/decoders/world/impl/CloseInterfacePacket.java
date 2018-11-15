package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class CloseInterfacePacket extends IncomingPacket {


	public int[] packetIds() {
		return new int[] {
				54
		};
	}


	public void processPacket(Player player, InputStream stream, int packetId) {
		if (player.hasStarted() && !player.hasFinished() && !player.isRunning()) { // used
			// for
			// old
			// welcome
			// screen
			player.run();
			return;
		}
		player.stopAll();
	}

}
