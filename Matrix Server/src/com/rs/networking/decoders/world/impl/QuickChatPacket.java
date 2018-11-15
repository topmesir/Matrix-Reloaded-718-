package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.QuickChatMessage;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class QuickChatPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				86, 0
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if(packetId == 86) {
		if (!player.hasStarted())
			return;
		if (player.getLastPublicMessage() > Utilities.currentTimeMillis())
			return;
		player.setLastPublicMessage(Utilities.currentTimeMillis() + 300);
		// just tells you which client script created packet
		@SuppressWarnings("unused")
		boolean secondClientScript = stream.readByte() == 1;// script 5059
		// or 5061
		int fileId = stream.readUnsignedShort();
		byte[] data = null;
		if (stream.getLength() > 3) {
			data = new byte[stream.getLength() - 3];
			stream.readBytes(data);
		}
		data = Utilities.completeQuickMessage(player, fileId, data);
		if (player.chatType == 0)
			player.sendPublicChatMessage(new QuickChatMessage(fileId, data));
		else if (player.chatType == 1)
			player.sendFriendsChannelQuickMessage(new QuickChatMessage(fileId, data));
		else if (Settings.DEBUG)
			Logger.log(this, "Unknown chat type: " + player.chatType);
		} else if(packetId == 0) {
			if (!player.hasStarted())
				return;
			String username = stream.readString();
			int fileId = stream.readUnsignedShort();
			byte[] data = null;
			if (stream.getLength() > 3 + username.length()) {
				data = new byte[stream.getLength() - (3 + username.length())];
				stream.readBytes(data);
			}
			data = Utilities.completeQuickMessage(player, fileId, data);
			Player p2 = World.getPlayerByDisplayName(username);
			if (p2 == null)
				return;
			player.getContent().get(ContentType.FRIENDSLIST).sendQuickChatMessage(p2, new QuickChatMessage(fileId, data));
		}
	}

}
