package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;

public class NPCAnimationAction extends CutsceneAction {

	private Animation anim;

	public NPCAnimationAction(int cachedObjectIndex, Animation anim, int actionDelay) {
		super(cachedObjectIndex, actionDelay);
		this.anim = anim;
	}

	@Override
	public void process(Player player, Object[] cache) {
		NPC npc = (NPC) cache[getCachedObjectIndex()];
		npc.setNextAnimation(anim);
	}

}
