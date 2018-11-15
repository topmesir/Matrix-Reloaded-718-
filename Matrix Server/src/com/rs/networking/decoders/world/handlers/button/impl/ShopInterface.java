package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.shop.Shop;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class ShopInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(620, 449, 621);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		switch(interfaceId) {
		case 620:
			if (componentId == 25) {
				Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
				if (shop == null)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					shop.sendInfo(player, slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					shop.buy(player, slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					shop.buy(player, slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					shop.buy(player, slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
					shop.buy(player, slotId, 50);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					shop.buy(player, slotId, 500);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					shop.sendExamine(player, slotId);
			}
			break;
		case 621:
			if (componentId == 0) {

				if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
				else {
					Shop shop = (Shop) player.getTemporaryAttributtes().get(
							"Shop");
					if (shop == null)
						return;
					if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
						shop.sendValue(player, slotId);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
						shop.sell(player, slotId, 1);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
						shop.sell(player, slotId, 5);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
						shop.sell(player, slotId, 10);
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
						shop.sell(player, slotId, 50);
				}
			}
			break;
		case 449:
			if (componentId == 1) {
				Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
				if (shop == null)
					return;
				shop.sendInventory(player);
			} else if (componentId == 21) {
				Shop shop = (Shop) player.getTemporaryAttributtes().get("Shop");
				if (shop == null)
					return;
				Integer slot = (Integer) player.getTemporaryAttributtes().get(
						"ShopSelectedSlot");
				if (slot == null)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					shop.buy(player, slot, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					shop.buy(player, slot, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					shop.buy(player, slot, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					shop.buy(player, slot, 50);

			}
			break;
		}
	}

}
