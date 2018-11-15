package com.rs.game.entity.mobile.player.command;

import com.rs.game.entity.mobile.player.Player;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:25:38 PM
 */
@FunctionalInterface
public interface FunctionalCommand {

	public void execute(Player player, String[] args);
	
}
