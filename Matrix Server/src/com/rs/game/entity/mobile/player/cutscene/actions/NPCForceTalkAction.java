package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.ForceTalk;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;

public class NPCForceTalkAction extends CutsceneAction {

	private String text;

	public NPCForceTalkAction(int cachedObjectIndex, String text, int actionDelay) {
		super(cachedObjectIndex, actionDelay);
		this.text = text;
	}

	@Override
	public void process(Player player, Object[] cache) {
		NPC npc = (NPC) cache[getCachedObjectIndex()];
		npc.setNextForceTalk(new ForceTalk(text));
	}

}
