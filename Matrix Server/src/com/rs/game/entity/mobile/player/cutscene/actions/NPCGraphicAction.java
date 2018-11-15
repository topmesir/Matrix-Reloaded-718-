package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;

public class NPCGraphicAction extends CutsceneAction {

	private Graphics gfx;

	public NPCGraphicAction(int cachedObjectIndex, Graphics gfx, int actionDelay) {
		super(cachedObjectIndex, actionDelay);
		this.gfx = gfx;
	}

	@Override
	public void process(Player player, Object[] cache) {
		NPC npc = (NPC) cache[getCachedObjectIndex()];
		npc.setNextGraphics(gfx);
	}

}
