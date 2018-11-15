package com.rs.networking.decoders.world.handlers.option;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public abstract class ItemOnItemHandler extends OptionHandler {

	public abstract void execute(final Player player, final Item item, final Item usedOn);
	
	public abstract int[] getUsedWithItemIds();
	
}
