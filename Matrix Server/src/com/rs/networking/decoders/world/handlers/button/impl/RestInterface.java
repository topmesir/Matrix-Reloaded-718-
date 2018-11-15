package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.action.impl.Rest;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;
import com.rs.utilities.Utilities;

public class RestInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(750);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 4) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				player.toogleRun(player.isResting() ? false : true);
				if (player.isResting())
					player.stopAll();
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				if (player.isResting()) {
					player.stopAll();
					return;
				}
				long currentTime = Utilities.currentTimeMillis();
				if (player.getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= currentTime) {
					player.getPackets().sendGameMessage(
							"You can't rest while perfoming an emote.");
					return;
				}
				if (player.getLockDelay() >= currentTime) {
					player.getPackets().sendGameMessage(
							"You can't rest while perfoming an action.");
					return;
				}
				player.stopAll();
				player.getContent().get(ContentType.ACTION).setAction(new Rest());
			}
		}
	}

}
