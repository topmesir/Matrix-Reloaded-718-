package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class ChatBoxInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(751);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 26) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setPrivateStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setPrivateStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setPrivateStatus(2);
		} else if (componentId == 32) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.setFilterGame(false);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.setFilterGame(true);
		} else if (componentId == 29) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.setPublicStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.setPublicStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.setPublicStatus(2);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
				player.setPublicStatus(3);
		}else if (componentId == 0) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setFriendsChatStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setFriendsChatStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.getContent().get(ContentType.FRIENDSLIST).setFriendsChatStatus(2);
		} else if (componentId == 23) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.setClanStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.setClanStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.setClanStatus(2);
		} else if (componentId == 20) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.setTradeStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.setTradeStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.setTradeStatus(2);
		} else if (componentId == 17) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.setAssistStatus(0);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.setAssistStatus(1);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.setAssistStatus(2);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
				//ASSIST XP Earned/Time
			}
		}
	}

}
