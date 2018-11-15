package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class SettingsTabInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(261);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (player.getInterfaceManager().containsInventoryInter())
			return;
		if (componentId == 22) {
			if (player.getInterfaceManager().containsScreenInter()) {
				player.getPackets()
				.sendGameMessage(
						"Please close the interface you have open before setting your graphic options.");
				return;
			}
			player.stopAll();
			player.getInterfaceManager().sendInterface(742);
		} else if (componentId == 12)
			player.switchAllowChatEffects();
		else if (componentId == 13) { //chat setup
			player.getInterfaceManager().sendSettings(982);
		} else if (componentId == 14)
			player.switchMouseButtons();
		else if (componentId == 24) //audio options
			player.getInterfaceManager().sendSettings(429);
	}

}
