package com.rs.networking.decoders.world.handlers.option.impl.onitem;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.ItemOnItemHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;

public class ExampleItemOnItemHandler extends ItemOnItemHandler {

	@Override
	public void execute(Player player, Item item, Item usedOn) {
		
	}

	/**
	 * @return used on ids
	 */
	@Override
	public int[] getIds() {
		return toArr(-1);
	}

	@Override
	public OptionType getType() {
		return OptionType.ITEM_ON_ITEM;
	}

	/**
	 * @return used with ids
	 */
	@Override
	public int[] getUsedWithItemIds() {
		return toArr(-1);
	}

}
