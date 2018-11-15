package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemExamines;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class BankInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(762, 763, 11, 667);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		switch(interfaceId) {
		case 762:
			if (componentId == 15)
				player.getContent().get(ContentType.BANK).switchInsertItems();
			else if (componentId == 19)
				player.getContent().get(ContentType.BANK).switchWithdrawNotes();
			else if (componentId == 33)
				player.getContent().get(ContentType.BANK).depositAllInventory(true);
			else if (componentId == 37)
				player.getContent().get(ContentType.BANK).depositAllEquipment(true);
			else if (componentId == 46) {
				player.closeInterfaces();
				player.getInterfaceManager().sendInterface(767);
				player.setCloseInterfacesEvent(new Runnable() {
					@Override
					public void run() {
						player.getContent().get(ContentType.BANK).openBank();
					}
				});
			} else if (componentId >= 46 && componentId <= 64) {
				int tabId = 9 - ((componentId - 46) / 2);
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.BANK).setCurrentTab(tabId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.BANK).collapse(tabId);
			} else if (componentId == 95) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.BANK).withdrawItem(slotId, 1);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.BANK).withdrawItem(slotId, 5);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.BANK).withdrawItem(slotId, 10);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.BANK).withdrawLastAmount(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().put("bank_isWithdraw",
							Boolean.TRUE);
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.BANK).withdrawItem(slotId, Integer.MAX_VALUE);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET)
					player.getContent().get(ContentType.BANK).withdrawItemButOne(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getContent().get(ContentType.BANK).sendExamine(slotId);

			} else if (componentId == 119) {
				ButtonClickHandler.openEquipmentBonuses(player, true);
			}
			break;
		case 763:
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 1, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 5, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 10, true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.BANK).depositLastAmount(slotId);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().remove("bank_isWithdraw");
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, Integer.MAX_VALUE,
							true);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
			}
			break;
		case 11:
			if (componentId == 17) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 1, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 5, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, 10, false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.BANK).depositItem(slotId, Integer.MAX_VALUE,
							false);
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("bank_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().remove("bank_isWithdraw");
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
			} else if (componentId == 18)
				player.getContent().get(ContentType.BANK).depositAllInventory(false);
			else if (componentId == 20)
				player.getContent().get(ContentType.BANK).depositAllEquipment(false);
			break;
		case 667:
			if (componentId == 14) {
				if (slotId >= 14)
					return;
				Item item = player.getContent().get(ContentType.EQUIPMENT).getItem(slotId);
				if (item == null)
					return;
				if (packetId == 3)
					player.getPackets().sendGameMessage(
							ItemExamines.getExamine(item));
				else if (packetId == 216) {
					ButtonClickHandler.sendRemove(player, slotId);
					ButtonClickHandler.refreshEquipBonuses(player);
				}
			} else if (componentId == 46 && player.getTemporaryAttributtes().remove("Banking") != null) {
				player.getContent().get(ContentType.BANK).openBank();
			}
			break;
		}
	}

}
