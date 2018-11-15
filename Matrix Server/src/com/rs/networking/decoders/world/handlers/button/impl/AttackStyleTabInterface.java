package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class AttackStyleTabInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(884);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 4) {
			int weaponId = player.getContent().get(ContentType.EQUIPMENT).getWeaponId();
			if (player.hasInstantSpecial(weaponId)) {
				player.performInstantSpecial(weaponId);
				return;
			}
			ButtonClickHandler.submitSpecialRequest(player);
		} else if (componentId >= 7 && componentId <= 10)
			player.getContent().get(ContentType.COMBATDEF).setAttackStyle(componentId - 7);
		else if (componentId == 11)
			player.getContent().get(ContentType.COMBATDEF).switchAutoRelatie();
	}

}
