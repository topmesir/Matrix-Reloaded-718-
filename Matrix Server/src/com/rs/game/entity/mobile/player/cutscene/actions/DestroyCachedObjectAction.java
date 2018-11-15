package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.cutscene.Cutscene;

public class DestroyCachedObjectAction extends CutsceneAction {

	public DestroyCachedObjectAction(int cachedObjectIndex, int actionDelay) {
		super(cachedObjectIndex, actionDelay);
	}

	@Override
	public void process(Player player, Object[] cache) {
		Cutscene scene = (Cutscene) cache[0];
		scene.destroyCache(cache[getCachedObjectIndex()]);
	}

}
