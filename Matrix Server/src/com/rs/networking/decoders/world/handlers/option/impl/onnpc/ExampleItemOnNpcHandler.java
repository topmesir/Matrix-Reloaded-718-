package com.rs.networking.decoders.world.handlers.option.impl.onnpc;

import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.ItemOnNPCHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;

public class ExampleItemOnNpcHandler extends ItemOnNPCHandler {

	@Override
	public void execute(Player player, Item item, NPC npc) {
		
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
		return OptionType.ITEM_ON_NPC;
	}

}
