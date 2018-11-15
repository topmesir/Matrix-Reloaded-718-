package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class PriceCheckInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(206, 207);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		switch(interfaceId) {
		case 206:
			if (componentId == 15) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).removeItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).removeItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).removeItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).removeItem(slotId,
							Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("pc_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().put("pc_isRemove",
							Boolean.TRUE);
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				}
			}
			break;
		case 207:
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).addItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).addItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).addItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.PRICE_CHECK).addItem(slotId,
							Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("pc_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().remove("pc_isRemove");
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
			}
			break;
		}
	}

}
