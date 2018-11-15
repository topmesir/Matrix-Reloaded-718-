package com.rs.game.entity.mobile.player.command;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.Rank;
import com.rs.utilities.Utilities;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:26:02 PM
 */
public class CommandFactory {

	private static Map<String, AbstractCommand> commands = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static void populateCommands() {
		String fileLoc = "bin/com/rs/game/player/command/impl";
		String packageDir = "com.rs.game.player.command.impl";
		try {
			@SuppressWarnings("rawtypes")
			List<Class> files = Utilities.findClasses(new File(fileLoc) , packageDir);
			for (Class<AbstractCommand> c : files) {
				if (AbstractCommand.class.isAssignableFrom(c)) {
					commands.put(c.getSimpleName().toLowerCase(), c.newInstance());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void executeCommand(Player player, String[] args) {
		AbstractCommand skeleton;
		if((skeleton = commands.get(args[0].toLowerCase())) != null) {
			for(Rank rank : skeleton.getRank()) {
				if(player.getRank() == rank) {
					skeleton.getCommand().execute(player, args);
					return;
				}
			}
			player.getPackets().sendGameMessage("Insufficient rank.");
		} else 
			player.getPackets().sendGameMessage("Command does not exist.");
	}

}
