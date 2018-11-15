package com.rs.game.entity.mobile.player.content.type;

import com.rs.engine.cache.loaders.ItemDefinitions;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemConstants;
import com.rs.game.entity.item.ItemsContainer;
import com.rs.game.entity.mobile.player.content.AbstractContent;
import com.rs.game.entity.mobile.player.content.ContentType;

//Transient Content Class (Doesnt Save)
@SuppressWarnings("serial")
public class PriceCheckManager extends AbstractContent {
	
	private transient ItemsContainer<Item> pcInv;

	public PriceCheckManager() {
		pcInv = new ItemsContainer<Item>(28, false);
	}

	public void openPriceCheck() {
		player.getInterfaceManager().sendInterface(206);
		player.getInterfaceManager().sendInventoryInterface(207);
		sendInterItems();
		sendOptions();
		player.getPackets().sendGlobalConfig(728, 0);
		for (int i = 0; i < pcInv.getSize(); i++) {
			player.getPackets().sendGlobalConfig(700 + i, 0);
		}
		player.setCloseInterfacesEvent(new Runnable() {
			
			@Override
			public void run() {
				player.getContent().get(ContentType.INVENTORY).getItems().addAll(pcInv);
				player.getContent().get(ContentType.INVENTORY).init();
				pcInv.clear();
			}
		});
	}

	public int getSlotId(int clickSlotId) {
		return clickSlotId / 2;
	}

	public void removeItem(int clickSlotId, int amount) {
		int slot = getSlotId(clickSlotId);
		Item item = pcInv.get(slot);
		if (item == null) {
			return;
		}
		Item[] itemsBefore = pcInv.getItemsCopy();
		int maxAmount = pcInv.getNumberOf(item);
		if (amount < maxAmount) {
			item = new Item(item.getId(), amount);
		} else {
			item = new Item(item.getId(), maxAmount);
		}
		pcInv.remove(slot, item);
		player.getContent().get(ContentType.INVENTORY).addItem(item);
		refreshItems(itemsBefore);
	}

	public void addItem(int slot, int amount) {
		Item item = player.getContent().get(ContentType.INVENTORY).getItem(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		Item[] itemsBefore = pcInv.getItemsCopy();
		int maxAmount = player.getContent().get(ContentType.INVENTORY).getItems().getNumberOf(item);
		if (amount < maxAmount) {
			item = new Item(item.getId(), amount);
		} else {
			item = new Item(item.getId(), maxAmount);
		}
		pcInv.add(item);
		player.getContent().get(ContentType.INVENTORY).deleteItem(slot, item);
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		int totalPrice = 0;
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = pcInv.getItems()[index];
			if (item != null) {
				totalPrice += ItemDefinitions.getItemDefinitions(item.getId()).getValue() * item.getAmount();
			}
			if (itemsBefore[index] != item) {
				changedSlots[count++] = index;
				player.getPackets().sendGlobalConfig(700 + index, item == null ? 0 : ItemDefinitions.getItemDefinitions(item.getId()).getValue());
			}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
		player.getPackets().sendGlobalConfig(728, totalPrice);
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(90, pcInv, slots);
	}

	public void sendOptions() {
		player.getPackets().sendUnlockIComponentOptionSlots(206, 15, 0, 54, 0, 1, 2, 3, 4, 5, 6);
		player.getPackets().sendUnlockIComponentOptionSlots(207, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(207, 0, 93, 4, 7, "Add", "Add-5", "Add-10", "Add-All", "Add-X", "Examine");
	}

	public void sendInterItems() {
		player.getPackets().sendItems(90, pcInv);
	}
}
