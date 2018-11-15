package com.rs.game.entity.mobile.player.cutscene;

import com.rs.game.entity.mobile.player.Player;

public final class CutscenesManager {

	private Player player;
	private Cutscene cutscene;

	/*
	 * cutscene play stuff
	 */

	public CutscenesManager(Player player) {
		this.player = player;
	}

	public void process() {
		if (cutscene == null)
			return;
		if (cutscene.process(player))
			return;
		cutscene = null;
	}

	public void logout() {
		if (hasCutscene())
			cutscene.logout(player);
	}

	public boolean hasCutscene() {
		return cutscene != null;
	}

	public boolean play(Object key) {
		if (hasCutscene()) {
			return false;
		}
		Cutscene cutscene = (Cutscene) (key instanceof Cutscene ? key : CutscenesHandler.getCutscene(key));
		if (cutscene == null) {
			return false;
		}
		cutscene.createCache(player);
		this.cutscene = cutscene;
		return true;
	}

}
