package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class TopRightGameFrameInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(548, 746);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId, int slotId, int slotId2) {
		if ((interfaceId == 548 && componentId == 148)
				|| (interfaceId == 746 && componentId == 199)) {
			if (player.getInterfaceManager().containsScreenInter()
					|| player.getInterfaceManager()
					.containsInventoryInter()) {
				// TODO cant open sound
				player.getPackets()
				.sendGameMessage(
						"Please finish what you're doing before opening the world map.");
				return;
			}
			// world map open
			player.getPackets().sendWindowsPane(755, 0);
			int posHash = player.getX() << 14 | player.getY();
			player.getPackets().sendGlobalConfig(622, posHash); // map open
			// center
			// pos
			player.getPackets().sendGlobalConfig(674, posHash); // player
			// position
		} else if ((interfaceId == 746 && componentId == 207) || (interfaceId == 548 && componentId == 159)) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				if (player.getInterfaceManager().containsScreenInter()) {
					player.getPackets()
					.sendGameMessage(
							"Please finish what you're doing before opening the price checker.");
					return;
				}
				player.stopAll();
				player.getContent().get(ContentType.PRICE_CHECK).openPriceCheck();
			}
		}
	}

}
