package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class DialogueContinuePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				72
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		int interfaceHash = stream.readInt();
		int junk = stream.readShort128();
		int interfaceId = interfaceHash >> 16;
		int buttonId = (interfaceHash & 0xFF);
		if (Utilities.getInterfaceDefinitionsSize() <= interfaceId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			return;
		}
		if (!player.isRunning() || !player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (Settings.DEBUG)
			Logger.log(this, "Dialogue: " + interfaceId + ", " + buttonId + ", " + junk);
		int componentId = interfaceHash - (interfaceId << 16);
		player.getDialogueManager().continueDialogue(interfaceId, componentId);
	}

}
