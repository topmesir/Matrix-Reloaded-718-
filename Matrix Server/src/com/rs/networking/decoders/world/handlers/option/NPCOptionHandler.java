package com.rs.networking.decoders.world.handlers.option;

import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.OptionType.NPCOptionType;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public abstract class NPCOptionHandler extends OptionHandler {

	public abstract void execute(final NPCOptionType type, final Player player, final NPC npc);
	
}
