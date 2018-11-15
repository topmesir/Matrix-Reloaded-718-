package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class EnterIntegerPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				81
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.isRunning() || player.isDead())
			return;
		int value = stream.readInt();
		if ((player.getInterfaceManager().containsInterface(762) && player.getInterfaceManager().containsInterface(763)) || player.getInterfaceManager().containsInterface(11)) {
			if (value < 0)
				return;
			Integer bank_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("bank_item_X_Slot");
			if (bank_item_X_Slot == null)
				return;
			player.getContent().get(ContentType.BANK).setLastX(value);
			player.getContent().get(ContentType.BANK).refreshLastX();
			if (player.getTemporaryAttributtes().remove("bank_isWithdraw") != null)
				player.getContent().get(ContentType.BANK).withdrawItem(bank_item_X_Slot, value);
			else
				player.getContent().get(ContentType.BANK).depositItem(bank_item_X_Slot, value, player.getInterfaceManager().containsInterface(11) ? false : true);
		} else if (player.getInterfaceManager().containsInterface(206) && player.getInterfaceManager().containsInterface(207)) {
			if (value < 0)
				return;
			Integer pc_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("pc_item_X_Slot");
			if (pc_item_X_Slot == null)
				return;
			if (player.getTemporaryAttributtes().remove("pc_isRemove") != null)
				player.getContent().get(ContentType.PRICE_CHECK).removeItem(pc_item_X_Slot, value);
			else
				player.getContent().get(ContentType.PRICE_CHECK).addItem(pc_item_X_Slot, value);
		} else if (player.getInterfaceManager().containsInterface(335) && player.getInterfaceManager().containsInterface(336)) {
			if (value < 0)
				return;
			Integer trade_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("trade_item_X_Slot");
			if (trade_item_X_Slot == null)
				return;
			if (player.getTemporaryAttributtes().remove("trade_isRemove") != null)
				player.getContent().get(ContentType.TRADE).removeItem(trade_item_X_Slot, value);
			else
				player.getContent().get(ContentType.TRADE).addItem(trade_item_X_Slot, value);
		}
	}

}
