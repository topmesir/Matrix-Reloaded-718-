package com.rs.networking.decoders.world.handlers;

import com.rs.Settings;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.npc.NPCSpawns;
import com.rs.game.entity.mobile.player.EventOnDestination;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.Rank;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.handlers.option.NPCOptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionRepository;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.NPCOptionType;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;

public class NPCHandler {

	public static void handleNPCOption1(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		player.stopAll(false);
		if (forceRun) {
			player.setRun(forceRun);
		}
		player.setEventOnDestination(new EventOnDestination(npc, new Runnable() {

			@Override
			public void run() {
				npc.resetWalkSteps();
				player.faceEntity(npc);
				if (!player.getControlerManager().processNPCClick1(npc)) {
					return;
				}
				npc.faceEntity(player);
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, npc.getId());
				if(handler != null) {
					NPCOptionHandler handlerr = (NPCOptionHandler) handler;
					handlerr.execute(NPCOptionType.OPTION_1, player, npc);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
					if (Settings.DEBUG) {
						System.out.println("cliked 1 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
					}
				}
			}
		}, npc.getSize()));
	}

	public static void handleNPCOption2(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		player.stopAll(false);
		if (forceRun) {
			player.setRun(forceRun);
		}
		player.setEventOnDestination(new EventOnDestination(npc, new Runnable() {
			
			@Override
			public void run() {
				npc.resetWalkSteps();
				player.faceEntity(npc);
				npc.faceEntity(player);
				if (!player.getControlerManager().processNPCClick2(npc)) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, npc.getId());
				if(handler != null) {
					NPCOptionHandler handlerr = (NPCOptionHandler) handler;
					handlerr.execute(NPCOptionType.OPTION_2, player, npc);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
					if (Settings.DEBUG) {
						System.out.println("cliked 2 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
					}
				}
			}
		}, npc.getSize()));
	}

	public static void handleNPCOption3(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		player.stopAll(false);
		if (forceRun) {
			player.setRun(forceRun);
		}
		player.setEventOnDestination(new EventOnDestination(npc, new Runnable() {
			
			@Override
			public void run() {
				npc.resetWalkSteps();
				if (!player.getControlerManager().processNPCClick3(npc)) {
					return;
				}
				player.faceEntity(npc);
				npc.faceEntity(player);
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, npc.getId());
				if(handler != null) {
					NPCOptionHandler handlerr = (NPCOptionHandler) handler;
					handlerr.execute(NPCOptionType.OPTION_3, player, npc);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
					if (Settings.DEBUG) {
						System.out.println("cliked 3 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
					}
				}
			}
		}, npc.getSize()));
	}
	
	public static void handleExamine(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		if (forceRun) {
			player.setRun(forceRun);
		}
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		if (player.getRank().equals(Rank.ADMIN)) {
			player.getPackets().sendGameMessage("NPC - [id=" + npc.getId() + ", loc=[" + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane() + "]].");
		}
		player.getPackets().sendNPCMessage(0, npc, "It's a " + npc.getDefinitions().name + ".");
		if (player.isSpawnsMode()) {
			try {
				if (NPCSpawns.removeSpawn(npc)) {
					player.getPackets().sendGameMessage("Removed spawn!");
					return;
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			player.getPackets().sendGameMessage("Failed removing spawn!");
		}
		if (Settings.DEBUG)
			Logger.log("NPCHandler", "examined npc: " + npcIndex + ", " + npc.getId());
	}
}
