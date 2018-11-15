package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class SkillMenuInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(499);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		int skillMenu = -1;
		if (player.getTemporaryAttributtes().get("skillMenu") != null)
			skillMenu = (Integer) player.getTemporaryAttributtes().get(
					"skillMenu");
		if(componentId >= 10 && componentId <= 25) 
			player.getPackets().sendConfig(965, ((componentId - 10) * 1024) + skillMenu);
		else if (componentId == 29) 
			// close inter
			player.stopAll();

	}

}
