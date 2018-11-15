package com.rs.networking.decoders.world.handlers.option.impl.npc;

import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.NPCOptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.NPCOptionType;

public class ExampleNPCOption extends NPCOptionHandler {

	@Override
	public void execute(NPCOptionType type, Player player, NPC npc) {
		
	}

	/**
	 * @return npcIds for this handler
	 */
	@Override
	public int[] getIds() {
		return toArr(-1);
	}

	@Override
	public OptionType getType() {
		return OptionType.NPC;
	}

}
