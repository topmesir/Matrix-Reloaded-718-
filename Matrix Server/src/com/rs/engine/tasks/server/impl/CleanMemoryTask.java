package com.rs.engine.tasks.server.impl;

import com.alex.store.Index;
import com.rs.engine.GameEngine;
import com.rs.engine.cache.Cache;
import com.rs.engine.cache.loaders.ItemDefinitions;
import com.rs.engine.cache.loaders.NPCDefinitions;
import com.rs.engine.cache.loaders.ObjectDefinitions;
import com.rs.engine.tasks.server.ServerTask;
import com.rs.game.world.Region;
import com.rs.game.world.World;
import com.rs.utilities.Logger;

public class CleanMemoryTask extends ServerTask {

	private final static int TICK_DELAY = 100 * 30; // 30 Minutes

	public CleanMemoryTask() {
		super(TICK_DELAY, false, TickType.MAIN);
	}

	@Override
	public void execute() {
		try {
			Logger.log("ServerTasks", "Cleaning System Memory...");
				ItemDefinitions.clearItemsDefinitions();
				NPCDefinitions.clearNPCDefinitions();
				ObjectDefinitions.clearObjectDefinitions();
				for (Region region : World.getRegions().values())
					region.removeMapFromMemory();
			for (Index index : Cache.STORE.getIndexes())
				index.resetCachedFiles();
			GameEngine.timer.purge();
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

