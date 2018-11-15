package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.player.Player;

public class PlayerGraphicAction extends CutsceneAction {

	private Graphics gfx;

	public PlayerGraphicAction(Graphics gfx, int actionDelay) {
		super(-1, actionDelay);
		this.gfx = gfx;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextGraphics(gfx);
	}

}
