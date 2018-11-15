package com.rs.game.entity.mobile.player.cutscene.actions;

import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.player.Player;

public class PlayerAnimationAction extends CutsceneAction {

	private Animation anim;

	public PlayerAnimationAction(Animation anim, int actionDelay) {
		super(-1, actionDelay);
		this.anim = anim;
	}

	@Override
	public void process(Player player, Object[] cache) {
		player.setNextAnimation(anim);
	}

}
