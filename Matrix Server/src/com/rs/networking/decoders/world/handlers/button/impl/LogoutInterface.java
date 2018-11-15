package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class LogoutInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(182);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (player.getInterfaceManager().containsInventoryInter())
			return;
		if (componentId == 6 || componentId == 13)
			if (!player.hasFinished())
				player.logout();
	}

}
