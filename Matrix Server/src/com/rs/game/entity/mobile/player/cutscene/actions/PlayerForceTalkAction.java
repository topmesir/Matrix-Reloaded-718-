package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.ForceTalk;
import com.rs.game.entity.mobile.player.Player;

public class PlayerForceTalkAction extends CutsceneAction {

	private String text;

	public PlayerForceTalkAction(String text, int actionDelay) {
		super(-1, actionDelay);
		this.text = text;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextForceTalk(new ForceTalk(text));
	}

}
