package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class WalkingPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				8, 58
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utilities.currentTimeMillis();
		if (player.getLockDelay() > currentTime)
			return;
		if (player.getFreezeDelay() >= currentTime) {
			player.getPackets().sendGameMessage("A magical force prevents you from moving.");
			return;
		}
		int length = stream.getLength();
		int baseX = stream.readUnsignedShort128();
		boolean forceRun = stream.readUnsigned128Byte() == 1;
		int baseY = stream.readUnsignedShort128();
		int steps = (length - 5) / 2;
		if (steps > 25)
			steps = 25;
		player.stopAll();
		if (forceRun)
			player.setRun(forceRun);
		for (int step = 0; step < steps; step++)
			if (!player.addWalkSteps(baseX + stream.readUnsignedByte(), baseY + stream.readUnsignedByte(), 25, true))
				break;
	}

}
