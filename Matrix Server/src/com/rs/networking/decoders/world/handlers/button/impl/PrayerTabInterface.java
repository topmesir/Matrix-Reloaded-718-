package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class PrayerTabInterface extends InterfaceButtonHandler{

	@Override
	public int[] interfaceIds() {
		return toArr(271);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (componentId == 8 || componentId == 42)
					player.getContent().get(ContentType.PRAYER).switchPrayer(slotId);

				else if (componentId == 43
						&& player.getContent().get(ContentType.PRAYER).isUsingQuickPrayer())
					player.getContent().get(ContentType.PRAYER).switchSettingQuickPrayer();
			}
		});
	}

}
