package com.rs.game.entity.mobile.player.content.type.action;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;

public abstract class Action {

	public abstract boolean start(Player player);

	public abstract boolean process(Player player);

	public abstract int processWithDelay(Player player);

	public abstract void stop(Player player);

	protected final void setActionDelay(Player player, int delay) {
		player.getContent().get(ContentType.ACTION).setActionDelay(delay);
	}
}
