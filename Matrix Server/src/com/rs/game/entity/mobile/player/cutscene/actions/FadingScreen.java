package com.rs.game.entity.mobile.player.cutscene.actions;

import java.util.TimerTask;

import com.rs.engine.GameEngine;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.mobile.player.Player;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public final class FadingScreen {

	private FadingScreen() {

	}

	public static void fade(final Player player, final Runnable event) {
		unfade(player, fade(player), event);
	}

	public static void unfade(final Player player, long startTime, final Runnable event) {
		long leftTime = 2500 - (Utilities.currentTimeMillis() - startTime);
		if (leftTime > 0) {
			GameEngine.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						unfade(player, event);
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}

			}, leftTime);
		} else
			unfade(player, event);
	}

	public static void unfade(final Player player, Runnable event) {
		event.run();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getInterfaceManager().sendFadingInterface(170);
				GameEngine.timer.schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							player.getInterfaceManager().closeFadingInterface();
						} catch (Throwable e) {
							Logger.handle(e);
						}
					}
				}, 2000);
			}

		});
	}

	public static long fade(Player player) {
		player.getInterfaceManager().sendFadingInterface(115);
		return Utilities.currentTimeMillis();
	}

}
