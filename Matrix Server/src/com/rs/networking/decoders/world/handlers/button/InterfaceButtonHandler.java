package com.rs.networking.decoders.world.handlers.button;

import com.rs.game.entity.mobile.player.Player;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public abstract class InterfaceButtonHandler {
	
	public abstract int[] interfaceIds();
	
	public abstract void executeButton(final Player player, final int packetId, final int interfaceHash, final int interfaceId, final int componentId, final int slotId, final int slotId2);

	
	public int[] toArr(int... ids) {
		return ids;
	}
	
}
