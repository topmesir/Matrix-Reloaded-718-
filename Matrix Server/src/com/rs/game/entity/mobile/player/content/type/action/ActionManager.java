package com.rs.game.entity.mobile.player.content.type.action;

import com.rs.game.entity.mobile.player.content.AbstractContent;

@SuppressWarnings("serial")
public class ActionManager extends AbstractContent {

	private transient Action action;
	private transient int actionDelay;

	public void process() {
		if (action != null) {
			if (player.isDead()) {
				forceStop();
			} else if (!action.process(player)) {
				forceStop();
			}
		}
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		if (action == null)
			return;
		int delay = action.processWithDelay(player);
		if (delay == -1) {
			forceStop();
			return;
		}
		actionDelay += delay;
	}

	public boolean setAction(Action action) {
		forceStop();
		if (!action.start(player))
			return false;
		this.action = action;
		return true;
	}

	public void forceStop() {
		if (action == null)
			return;
		action.stop(player);
		action = null;
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public void addActionDelay(int skillDelay) {
		this.actionDelay += skillDelay;
	}

	public void setActionDelay(int skillDelay) {
		this.actionDelay = skillDelay;
	}

	public boolean actionIsRunning() {
		return action != null;
	}

	public Action getAction() {
		return action;
	}
}
