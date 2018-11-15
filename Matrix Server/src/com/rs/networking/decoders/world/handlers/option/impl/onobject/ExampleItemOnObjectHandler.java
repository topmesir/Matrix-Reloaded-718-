package com.rs.networking.decoders.world.handlers.option.impl.onobject;

import com.rs.game.entity.GameObject;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.ItemOnObjectHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;

public class ExampleItemOnObjectHandler extends ItemOnObjectHandler {

	@Override
	public void execute(Player player, Item item, GameObject object) {
		
	}

	@Override
	public int[] getUsedWithItemIds() {
		return toArr(-1);
	}

	@Override
	public int[] getIds() {
		return toArr(-1);
	}

	@Override
	public OptionType getType() {
		return OptionType.ITEM_ON_OBJECT;
	}

}
