package com.rs.networking.decoders.world.handlers;

import java.util.List;

import com.rs.Settings;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.engine.thread.WorldThread;
import com.rs.game.WorldTile;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.EventOnDestination;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.container.Inventory;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.decoders.world.handlers.option.InventoryOptionHandler;
import com.rs.networking.decoders.world.handlers.option.ItemOnItemHandler;
import com.rs.networking.decoders.world.handlers.option.ItemOnNPCHandler;
import com.rs.networking.decoders.world.handlers.option.OptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionRepository;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.InventoryOptionType;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public class InventoryHandler {
	
	public static void handleItemOption1(Player player, final int slotId, final int itemId, Item item) {
		OptionHandler handler = OptionRepository.getOptionHandler(OptionType.OBJECT, itemId);
		if(handler != null) {
			InventoryOptionHandler handlerr = (InventoryOptionHandler) handler;
			handlerr.execute(InventoryOptionType.OPTION_1, player, slotId, itemId, item);
		}
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 1");
		}
	}

	public static void handleItemOption2(final Player player, final int slotId, final int itemId, Item item) {
		if (player.isEquipDisabled()) {
			return;
		}
		long passedTime = Utilities.currentTimeMillis()
				- WorldThread.LAST_CYCLE_CTM;
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				List<Integer> slots = player.getSwitchItemCache();
				int[] slot = new int[slots.size()];
				for (int i = 0; i < slot.length; i++)
					slot[i] = slots.get(i);
				player.getSwitchItemCache().clear();
				ButtonClickHandler.sendWear(player, slot);
				player.stopAll(false, true, false);
			}
		}, passedTime >= 600 ? 0 : passedTime > 330 ? 1 : 0);
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 2");
		}
	}
	
	public static void handleItemOption3(Player player, int slotId, int itemId, Item item) {
		OptionHandler handler = OptionRepository.getOptionHandler(OptionType.OBJECT, itemId);
		if(handler != null) {
			InventoryOptionHandler handlerr = (InventoryOptionHandler) handler;
			handlerr.execute(InventoryOptionType.OPTION_3, player, slotId, itemId, item);
		}
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 3");
		}
	}
	
	public static void handleItemOption4(Player player, int slotId, int itemId, Item item) {
		OptionHandler handler = OptionRepository.getOptionHandler(OptionType.OBJECT, itemId);
		if(handler != null) {
			InventoryOptionHandler handlerr = (InventoryOptionHandler) handler;
			handlerr.execute(InventoryOptionType.OPTION_4, player, slotId, itemId, item);
		}
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 4");
		}
	}

	public static void handleItemOption5(Player player, int slotId, int itemId, Item item) {
		OptionHandler handler = OptionRepository.getOptionHandler(OptionType.OBJECT, itemId);
		if(handler != null) {
			InventoryOptionHandler handlerr = (InventoryOptionHandler) handler;
			handlerr.execute(InventoryOptionType.OPTION_5, player, slotId, itemId, item);
		}
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 5");
		}
	}
	
	public static void handleItemOption6(Player player, int slotId, int itemId, Item item) {
		OptionHandler handler = OptionRepository.getOptionHandler(OptionType.OBJECT, itemId);
		if(handler != null) {
			InventoryOptionHandler handlerr = (InventoryOptionHandler) handler;
			handlerr.execute(InventoryOptionType.OPTION_6, player, slotId, itemId, item);
		}
		if (Settings.DEBUG) {
			Logger.log("InventoryHandler", "Option 6");
		}
	}
	
	public static void handleItemOption7(Player player, int slotId, int itemId, Item item) {
		long time = Utilities.currentTimeMillis();
		if (player.getLockDelay() >= time || player.getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= time) {
			return;
		}
		if (!player.getControlerManager().canDropItem(item)) {
			return;
		}
		player.stopAll(false);
		if (item.getDefinitions().isOverSized()) {
			player.getPackets().sendGameMessage("The item appears to be oversized.");
			player.getContent().get(ContentType.INVENTORY).deleteItem(item);
			return;
		}
		if (item.getDefinitions().isDestroyItem()) {
			player.getDialogueManager().startDialogue("DestroyItemOption", slotId, item);
			return;
		}
		player.getContent().get(ContentType.INVENTORY).deleteItem(slotId, item);
		if (player.getContent().get(ContentType.CHARGES).degradeCompletly(item)) {
			return;
		}
		player.getPackets().sendSound(2739, 0, 1);
		World.addGroundItem(item, new WorldTile(player), player, false, 180, true);
	}

	public static void handleInventoryItemExamine(Player player, int slotId, int itemId, Item item) {
		player.getContent().get(ContentType.INVENTORY).sendExamine(slotId);
	}

	public static void handleItemOnItem(final Player player, InputStream stream) {
		int itemUsedWithId = stream.readShort();
		int toSlot = stream.readShortLE128();
		int interfaceId = stream.readInt() >> 16;
		int interfaceId2 = stream.readInt() >> 16;
		int fromSlot = stream.readShort();
		int itemUsedId = stream.readShortLE128();
		if (interfaceId == Inventory.INVENTORY_INTERFACE && interfaceId == interfaceId2 && !player.getInterfaceManager().containsInventoryInter()) {
			if (toSlot >= 28 || fromSlot >= 28) {
				return;
			}
			Item usedWith = player.getContent().get(ContentType.INVENTORY).getItem(toSlot);
			Item itemUsed = player.getContent().get(ContentType.INVENTORY).getItem(fromSlot);
			if (itemUsed == null || usedWith == null || itemUsed.getId() != itemUsedId || usedWith.getId() != itemUsedWithId) {
				return;
			}
			player.stopAll();
			if (!player.getControlerManager().canUseItemOnItem(itemUsed, usedWith)) {
				return;
			}
			
			OptionHandler handler = OptionRepository.getOptionHandler(OptionType.ITEM_ON_ITEM, usedWith.getId());
			if(handler != null) {
				ItemOnItemHandler handlerr = (ItemOnItemHandler) handler;
				for(int id : handlerr.getUsedWithItemIds()) {
					if(id == usedWith.getId())
						handlerr.execute(player, usedWith, itemUsed);	
				}
			}

			if (Settings.DEBUG) {
				Logger.log("ItemHandler", "Used:" + itemUsed.getId() + ", With:" + usedWith.getId());
			}
		}
	}

	public static void handleItemOnNPC(final Player player, final NPC npc, final Item item) {
		if (item == null) {
			return;
		}
		player.setEventOnDestination(new EventOnDestination(npc, new Runnable() {
			
			@Override
			public void run() {
				if (!player.getContent().get(ContentType.INVENTORY).containsItem(item.getId(), item.getAmount())) {
					return;
				}
				
				OptionHandler handler = OptionRepository.getOptionHandler(OptionType.ITEM_ON_NPC, npc.getId());
				if(handler != null) {
					ItemOnNPCHandler handlerr = (ItemOnNPCHandler) handler;
					for(int id : handlerr.getUsedWithItemIds()) {
						if(item.getId() == id)
							handlerr.execute(player, item, npc);
					}
				}
				
				if (Settings.DEBUG) {
					Logger.log("ItemHandler", "Used:" + item.getId() + ", On NPC:" + npc.getId());
				}
			}
		}, npc.getSize()));
	}
}
