package com.rs.networking.decoders.world.handlers;

import com.rs.Settings;
import com.rs.engine.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.EventOnDestination;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.controller.impl.Wilderness;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.handlers.option.ItemOnObjectHandler;
import com.rs.networking.decoders.world.handlers.option.ObjectOptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionRepository;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.ObjectOptionType;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public final class ObjectHandler {

	public static void handleOption(final Player player, InputStream stream, int option) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		long currentTime = Utilities.currentTimeMillis();
		if (player.getLockDelay() >= currentTime || player.getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= currentTime) {
			return;
		}
		boolean forceRun = stream.readUnsignedByte128() == 1;
		final int id = stream.readIntLE();
		int x = stream.readUnsignedShortLE();
		int y = stream.readUnsignedShortLE128();
		int rotation = 0;
		if (player.isAtDynamicRegion()) {
			rotation = World.getRotation(player.getPlane(), x, y);
			if (rotation == 1) {
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
				y += defs.getSizeY() - 1;
			} else if (rotation == 2) {
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
				x += defs.getSizeY() - 1;
			}
		}
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId)) {
			return;
		}
		GameObject mapObject = World.getRegion(regionId).getObject(id, tile);
		if (mapObject == null || mapObject.getId() != id) {
			return;
		}
		if (player.isAtDynamicRegion() && World.getRotation(player.getPlane(), x, y) != 0) {
			ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
			if (defs.getSizeX() > 1 || defs.getSizeY() > 1) {
				for (int xs = 0; xs < defs.getSizeX() + 1 && (mapObject == null || mapObject.getId() != id); xs++) {
					for (int ys = 0; ys < defs.getSizeY() + 1 && (mapObject == null || mapObject.getId() != id); ys++) {
						tile.setLocation(x + xs, y + ys, tile.getPlane());
						mapObject = World.getRegion(regionId).getObject(id, tile);
					}
				}
			}
			if (mapObject == null || mapObject.getId() != id) {
				return;
			}
		}
		final GameObject object = !player.isAtDynamicRegion() ? mapObject : new GameObject(id, mapObject.getType(), (mapObject.getRotation() + rotation % 4), x, y, player.getPlane());
		player.stopAll(false);
		if (forceRun) {
			player.setRun(forceRun);
		}
		switch (option) {
		case 1:
			handleObjectOption1(player, object);
			break;
		case 2:
			handleObjectOption2(player, object);
			break;
		case 3:
			handleObjectOption3(player, object);
			break;
		case 4:
			handleObjectOption4(player, object);
			break;
		case 5:
			handleObjectOption5(player, object);
			break;
		case -1:
			handleObjectExamineOption(player, object);
			break;
		}
	}

	private static void handleObjectOption1(final Player player, final GameObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick1(object)) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId() + object.getRegionId());
				if(handler != null) {
					ObjectOptionHandler handlerr = (ObjectOptionHandler) handler;
						handlerr.execute(ObjectOptionType.OPTION_1, player, object);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					Logger.log("ObjectHandler", "clicked 1 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name);
				}
			}
		}, objectDef.getSizeX(), Wilderness.isDitch(id) ? 4 : objectDef.getSizeY(), object.getRotation()));
	}

	private static void handleObjectOption2(final Player player, final GameObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick2(object)) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId());
				if(handler != null) {
					ObjectOptionHandler handlerr = (ObjectOptionHandler) handler;
					handlerr.execute(ObjectOptionType.OPTION_2, player, object);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					Logger.log("ObjectHandler", "clicked 2 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane());
				}
			}
		}, objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()));
	}

	private static void handleObjectOption3(final Player player, final GameObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick3(object)) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId());
				if(handler != null) {
					ObjectOptionHandler handlerr = (ObjectOptionHandler) handler;
					handlerr.execute(ObjectOptionType.OPTION_3, player, object);
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					Logger.log("ObjectHandler", "cliked 3 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
				}
			}
		}, objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()));
	}

	private static void handleObjectOption4(final Player player, final GameObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick4(object)) {
					return;
				}

				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId());
				if(handler != null) {
					ObjectOptionHandler handlerr = (ObjectOptionHandler) handler;
					handlerr.execute(ObjectOptionType.OPTION_4, player, object);
				}else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					Logger.log("ObjectHandler", "cliked 4 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
				}
			}
		}, objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()));
	}

	private static void handleObjectOption5(final Player player, final GameObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.stopAll();
				player.faceObject(object);
				if (!player.getControlerManager().processObjectClick5(object)) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId());
				if(handler != null) {
					ObjectOptionHandler handlerr = (ObjectOptionHandler) handler;
					handlerr.execute(ObjectOptionType.OPTION_5, player, object);
				}else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					Logger.log("ObjectHandler", "cliked 5 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
				}
			}
		}, objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()));
	}

	private static void handleObjectExamineOption(final Player player, final GameObject object) {
		player.getPackets().sendGameMessage("It's an " + object.getDefinitions().name + ".");
		if (Settings.DEBUG) {
			Logger.log("ObjectHandler", "examined object id : " + object.getId() + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name);
		}
	}

	public static void handleItemOnObject(final Player player, final GameObject object, final int interfaceId, final Item item) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		player.setEventOnDestination(new EventOnDestination(object, new Runnable() {
			
			@Override
			public void run() {
				player.faceObject(object);
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.NPC, object.getId());
				if(handler != null) {
					ItemOnObjectHandler handlerr = (ItemOnObjectHandler) handler;
					for(int id : handlerr.getUsedWithItemIds()) {
						if(item.getId() == id)
							handlerr.execute(player, item, object);
					}
				} else {
					player.getPackets().sendGameMessage("Nothing interesting happens.");
				}
				if (Settings.DEBUG) {
					System.out.println("Item on object: " + object.getId());
				}
			}
		}, objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()));
	}
	
	private ObjectHandler() {

	}
}
