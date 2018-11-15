package com.rs.game.entity.mobile.player.command.impl;

import com.rs.game.WorldTile;
import com.rs.game.entity.mobile.player.Rank;
import com.rs.game.entity.mobile.player.command.AbstractCommand;
import com.rs.game.entity.mobile.player.command.FunctionalCommand;

public class Tele extends AbstractCommand {

	@Override
	public Rank[] getRank() {
		return setRanks(Rank.ADMIN);
	}

	@Override
	public FunctionalCommand getCommand() {
		return (player, args) -> {
			args = args[1].split(",");
			int plane = Integer.valueOf(args[0]);
			int x = Integer.valueOf(args[1]) << 6 | Integer.valueOf(args[3]);
			int y = Integer.valueOf(args[2]) << 6 | Integer.valueOf(args[4]);
			player.setNextWorldTile(new WorldTile(x, y, plane));
		};
	}

}
