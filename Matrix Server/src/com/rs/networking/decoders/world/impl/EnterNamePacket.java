package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class EnterNamePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				29
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.isRunning() || player.isDead())
			return;
		String value = stream.readString();
		if (value.equals(""))
			return;
		if (player.getInterfaceManager().containsInterface(1108))
			player.getContent().get(ContentType.FRIENDSLIST).setChatPrefix(value);
		else if (player.getTemporaryAttributtes().get("yellcolor") == Boolean.TRUE) {
			if (value.length() != 6) {
				player.getDialogueManager().startDialogue("SimpleMessage", "The HEX yell color you wanted to pick cannot be longer and shorter then 6.");
			} else if (Utilities.containsInvalidCharacter(value) || value.contains("_")) {
				player.getDialogueManager().startDialogue("SimpleMessage", "The requested yell color can only contain numeric and regular characters.");
			} else {
				player.setYellColor(value);
				player.getDialogueManager().startDialogue("SimpleMessage", "Your yell color has been changed to <col=" + player.getYellColor() + ">" + player.getYellColor() + "</col>.");
			}
			player.getTemporaryAttributtes().put("yellcolor", Boolean.FALSE);
		} else if (player.getTemporaryAttributtes().get("view_name") == Boolean.TRUE) {
			player.getTemporaryAttributtes().remove("view_name");
			Player other = World.getPlayerByDisplayName(value);
			if (other == null) {
				player.getPackets().sendGameMessage("Couldn't find player.");
				return;
			}
		} 
	}

}
