package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.EmotesManager;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class EmotesInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(464, 590);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if ((interfaceId == 590 && componentId == 8) || interfaceId == 464) {
			player.getContent().get(ContentType.EMOTES).useBookEmote(interfaceId == 464 ? componentId : EmotesManager.getId(slotId, packetId));
		}
	}

}
