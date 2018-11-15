package com.rs.game.entity.mobile.player.command;

import com.rs.game.entity.mobile.player.Rank;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:26:12 PM
 */
public abstract class AbstractCommand {

	public abstract Rank[] getRank();
	
	public abstract FunctionalCommand getCommand();
	
	public Rank[] setRanks(Rank... ranks) {
		return ranks;
	}
	
}
