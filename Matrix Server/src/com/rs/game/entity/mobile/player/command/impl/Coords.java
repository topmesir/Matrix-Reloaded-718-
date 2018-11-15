package com.rs.game.entity.mobile.player.command.impl;

import com.rs.game.entity.mobile.player.Rank;
import com.rs.game.entity.mobile.player.command.AbstractCommand;
import com.rs.game.entity.mobile.player.command.FunctionalCommand;

public class Coords extends AbstractCommand {

	@Override
	public Rank[] getRank() {
		return setRanks(Rank.ADMIN);
	}

	@Override
	public FunctionalCommand getCommand() {
		return (player, args) -> {
			player.getPackets().sendGameMessage("Coordinates: " + player.getX() + ", " + player.getY() + ", " + player.getPlane());
		};
	}

}
