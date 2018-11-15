package com.rs.networking.decoders.world.handlers.option.impl.inventory;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.InventoryOptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.InventoryOptionType;

public class ExampleInventoryOption extends InventoryOptionHandler {

	@Override
	public void execute(InventoryOptionType type, Player player, int slotId, int itemId, Item item) {
		
	}

	/**
	 *@return itemIds for this handler
	 */
	@Override
	public int[] getIds() {
		return toArr(-1);
	}

	@Override
	public OptionType getType() {
		return OptionType.INVENTORY;
	}

}
