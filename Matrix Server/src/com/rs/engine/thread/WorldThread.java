package com.rs.engine.thread;

import com.rs.Settings;
import com.rs.engine.GameEngine;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.MobileEntityList;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.world.World;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public final class WorldThread extends Thread {
	
	public static long LAST_CYCLE_CTM;

	public WorldThread() {
		setPriority(Thread.MAX_PRIORITY);
		setName("World Thread");
	}
	
	

	@Override
	public final void run() {
		while (!GameEngine.shutdown) {
			long currentTime = Utilities.currentTimeMillis();
			try {
				WorldTasksManager.processTasks();
				
				MobileEntityList<NPC> npcs = World.getNPCs();
				MobileEntityList<Player> players = World.getPlayers();
				
				fastLoop(players, (MobileSyncAction<Player>) (e) -> {
					if (currentTime - e.getPacketsDecoderPing() > Settings.MAX_PACKETS_DECODER_PING_DELAY
							&& e.getSession().getChannel().isOpen())
						e.getSession().getChannel().close();
					e.processEntity();
				});
				fastLoop(npcs, (MobileSyncAction<NPC>) (e) -> {
					e.processEntity();
				});
				fastLoop(players, (MobileSyncAction<Player>) (e) -> {
					e.getPackets().sendLocalPlayersUpdate();
					e.getPackets().sendLocalNPCsUpdate();
				});
				fastLoop(players, (MobileSyncAction<Player>) (e) -> {
					e.resetMasks();
				});
				fastLoop(npcs, (MobileSyncAction<NPC>) (e) -> {
					e.resetMasks();
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			// System.out.println(" ,TOTAL:
			// "+(Utils.currentTimeMillis()-currentTime));
			LAST_CYCLE_CTM = Utilities.currentTimeMillis();
			long sleepTime = Settings.WORLD_CYCLE_TIME + (currentTime - LAST_CYCLE_CTM);
			if (sleepTime <= 0)
				continue;
			try {
				Thread.sleep(sleepTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Iterates through the {@link EntityList} and performs a
	 * {@link MobileSyncAction} on each {@link Entity} in the
	 * {@link EntityList}.
	 *
	 * @param entityList
	 *            The {@link EntityList} to be used in the Loop
	 * @param action
	 *            The {@link MobileSyncAction} to be used in the Loop
	 */
	private static <T extends MobileEntity> void fastLoop(MobileEntityList<T> entityList, MobileSyncAction<T> action) {
		try {
			for (T e : entityList) {
				if (e == null || (e.hasFinished() || !e.hasStarted()))
					continue;
				action.execute(e);
			}
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

}