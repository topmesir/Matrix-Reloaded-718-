package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;

public class PlayerTransformAction extends CutsceneAction {

	private int npcId;

	public PlayerTransformAction(int npcId, int actionDelay) {
		super(-1, actionDelay);
		this.npcId = npcId;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.getContent().get(ContentType.APPEARANCE).transformIntoNPC(npcId);
	}

}
