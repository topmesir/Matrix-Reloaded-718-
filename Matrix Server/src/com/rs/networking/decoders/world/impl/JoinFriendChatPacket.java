package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.global.FriendsChatManager;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class JoinFriendChatPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				36
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted())
			return;
		FriendsChatManager.joinChat(stream.readString(), player);
	}

}
