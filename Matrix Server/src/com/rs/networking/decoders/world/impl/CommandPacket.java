package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.command.CommandFactory;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class CommandPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				60
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.isRunning())
			return;
		@SuppressWarnings("unused")
		boolean clientCommand = stream.readUnsignedByte() == 1;
		@SuppressWarnings("unused")
		boolean unknown = stream.readUnsignedByte() == 1;
		String command = stream.readString();
		CommandFactory.executeCommand(player, command.split(" "));
	}

}
