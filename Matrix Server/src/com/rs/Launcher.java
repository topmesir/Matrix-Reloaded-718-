package com.rs;

import com.rs.engine.GameEngine;
import com.rs.engine.cache.Cache;
import com.rs.engine.cache.loaders.ItemEquipIdDefinitions;
import com.rs.engine.tasks.server.ServerTaskEngine;
import com.rs.engine.tasks.server.ServerTaskRepository;
import com.rs.game.entity.ObjectSpawns;
import com.rs.game.entity.item.ItemBonuses;
import com.rs.game.entity.item.ItemExamines;
import com.rs.game.entity.mobile.npc.NPCDrops;
import com.rs.game.entity.mobile.npc.NPCOverrideRepository;
import com.rs.game.entity.mobile.npc.NPCSpawns;
import com.rs.game.entity.mobile.npc.combat.CombatScriptsHandler;
import com.rs.game.entity.mobile.npc.combat.NPCBonuses;
import com.rs.game.entity.mobile.npc.combat.NPCCombatDefinitionsL;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.command.CommandFactory;
import com.rs.game.entity.mobile.player.controller.ControllerHandler;
import com.rs.game.entity.mobile.player.cutscene.CutscenesHandler;
import com.rs.game.entity.mobile.player.dialogue.DialogueHandler;
import com.rs.game.world.RegionBuilder;
import com.rs.game.world.World;
import com.rs.networking.ServerChannelHandler;
import com.rs.networking.decoders.world.handlers.button.ButtonRepository;
import com.rs.networking.decoders.world.handlers.option.OptionRepository;
import com.rs.utilities.IPBanL;
import com.rs.utilities.Logger;
import com.rs.utilities.MapArchiveKeys;
import com.rs.utilities.MapAreas;
import com.rs.utilities.MusicHints;
import com.rs.utilities.SerializableFilesManager;
import com.rs.utilities.Utilities;
import com.rs.utilities.huffman.Huffman;

public final class Launcher {

	public static void main(String[] args) throws Exception {
		long currentTime = Utilities.currentTimeMillis();
		Cache.init();
		ItemEquipIdDefinitions.init();
		Huffman.init();
		IPBanL.init();
		MapArchiveKeys.init();
		MapAreas.init();
		ObjectSpawns.init();
		NPCOverrideRepository.populateNPCTable();
		ButtonRepository.loadButtons();
		OptionRepository.loadOptions();
		NPCSpawns.init();
		NPCCombatDefinitionsL.init();
		NPCBonuses.init();
		NPCDrops.init();
		ItemExamines.init();
		ItemBonuses.init();
		MusicHints.init();
		CommandFactory.populateCommands();
		CombatScriptsHandler.init();
		DialogueHandler.init();
		ControllerHandler.init();
		CutscenesHandler.init();
		GameEngine.init();
		World.init();
		RegionBuilder.init();
		try {
			ServerChannelHandler.init();
		} catch (Throwable e) {
			Logger.handle(e);
			Logger.log("Launcher", "Failed initing Server Channel Handler. Shutting down...");
			System.exit(1);
			return;
		}
		Logger.log("Launcher", "Server took " + (Utilities.currentTimeMillis() - currentTime) + " milli seconds to launch.");
		new ServerTaskEngine().addTasks(ServerTaskRepository.get().tasks());
	}

	public static void saveFiles() {
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished()) {
				continue;
			}
			SerializableFilesManager.savePlayer(player);
		}
		IPBanL.save();
	}

	public static void shutdown() {
		try {
			closeServices();
		} finally {
			System.exit(0);
		}
	}

	public static void closeServices() {
		ServerChannelHandler.shutdown();
		GameEngine.shutdown();
	}

	public static void restart() {
		closeServices();
		System.gc();
		try {
			Runtime.getRuntime().exec("java -server -Xms2048m -Xmx20000m -cp bin;/data/libs/netty-3.2.7.Final.jar;/data/libs/FileStore.jar Launcher false false true false");
			System.exit(0);
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	private Launcher() {

	}
}
