package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class InventoryInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(670);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 0) {
			if (slotId >= player.getContent().get(ContentType.INVENTORY).getItemsContainerSize())
				return;
			Item item = player.getContent().get(ContentType.INVENTORY).getItem(slotId);
			if (item == null)
				return;
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				if (ButtonClickHandler.sendWear(player, slotId, item.getId()))
					ButtonClickHandler.refreshEquipBonuses(player);
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
		}
	}

}
