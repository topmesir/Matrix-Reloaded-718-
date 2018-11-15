package com.rs.networking.decoders.world.handlers.option;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.OptionType.InventoryOptionType;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public abstract class InventoryOptionHandler extends OptionHandler {

	public abstract void execute(final InventoryOptionType type, final Player player, final int slotId, final int itemId, final Item item);
	
}
