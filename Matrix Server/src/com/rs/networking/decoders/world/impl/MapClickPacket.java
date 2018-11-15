package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class MapClickPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				38
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		int coordinateHash = stream.readInt();
		int x = coordinateHash >> 14;
		int y = coordinateHash & 0x3fff;
		int plane = coordinateHash >> 28;
		Integer hash = (Integer) player.getTemporaryAttributtes().get("worldHash");
		if (hash == null || coordinateHash != hash)
			player.getTemporaryAttributtes().put("worldHash", coordinateHash);
		else {
			player.getTemporaryAttributtes().remove("worldHash");
			player.getContent().get(ContentType.HINTICON).addHintIcon(x, y, plane, 20, 0, 2, -1, true);
			player.getPackets().sendConfig(1159, coordinateHash);
		}
	}

}
