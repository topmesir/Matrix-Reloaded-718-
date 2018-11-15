package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class TradeScreenInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(335, 336, 334);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		switch(interfaceId) {
		case 335:
			if(componentId == 18)
				player.getContent().get(ContentType.TRADE).accept(true);
			else if(componentId == 20) 
				player.closeInterfaces();
			else if(componentId == 32) {
				if(packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.TRADE).removeItem(slotId, 1);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.TRADE).removeItem(slotId, 5);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.TRADE).removeItem(slotId, 10);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.TRADE).removeItem(slotId, Integer.MAX_VALUE);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("trade_item_X_Slot",
							slotId);
					player.getTemporaryAttributtes().put("trade_isRemove", Boolean.TRUE);
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				}else if(packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.TRADE).sendValue(slotId, false);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getContent().get(ContentType.TRADE).sendExamine(slotId, false);
			}else if(componentId == 35) {
				if(packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.TRADE).sendValue(slotId, true);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getContent().get(ContentType.TRADE).sendExamine(slotId, true);
			}
			break;
		case 334:
			if(componentId == 22)
				player.closeInterfaces();
			else if (componentId == 21)
				player.getContent().get(ContentType.TRADE).accept(false);
			break;
		case 336:
			if(componentId == 0) {
				if(packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getContent().get(ContentType.TRADE).addItem(slotId, 1);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getContent().get(ContentType.TRADE).addItem(slotId, 5);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getContent().get(ContentType.TRADE).addItem(slotId, 10);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getContent().get(ContentType.TRADE).addItem(slotId, Integer.MAX_VALUE);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getTemporaryAttributtes().put("trade_item_X_Slot", slotId);
					player.getTemporaryAttributtes().remove("trade_isRemove");
					player.getPackets().sendRunScript(108,
							new Object[] { "Enter Amount:" });
				}else if(packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET)
					player.getContent().get(ContentType.TRADE).sendValue(slotId);
				else if(packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
					player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
			}
			break;
		}
	}

}
