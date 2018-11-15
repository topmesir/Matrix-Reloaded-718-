package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.InventoryHandler;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class InventoryOptionInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(679);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 0) {
			if (slotId > 27
					|| player.getInterfaceManager()
					.containsInventoryInter())
				return;
			Item item = player.getContent().get(ContentType.INVENTORY).getItem(slotId);
			if (item == null || item.getId() != slotId2)
				return;
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				InventoryHandler.handleItemOption1(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				InventoryHandler.handleItemOption2(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				InventoryHandler.handleItemOption3(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				InventoryHandler.handleItemOption4(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET)
				InventoryHandler.handleItemOption5(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
				InventoryHandler.handleItemOption6(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET)
				InventoryHandler.handleItemOption7(player, slotId,
						slotId2, item);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				InventoryHandler.handleInventoryItemExamine(player, slotId,
						slotId2, item);
		}
	}

}
