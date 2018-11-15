package com.rs.game.entity.mobile.player.content.type;

import java.util.HashMap;

import com.rs.engine.cache.loaders.ItemDefinitions;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemConstants;
import com.rs.game.entity.mobile.player.content.AbstractContent;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.utilities.Utilities;

public class ChargesManager extends AbstractContent {

	private static final long serialVersionUID = -5978513415281726450L;

	private HashMap<Integer, Integer> charges;

	public ChargesManager() {
		charges = new HashMap<Integer, Integer>();
	}

	public void process() {
		Item[] items = player.getContent().get(ContentType.EQUIPMENT).getItems().getItems();
		for (int slot = 0; slot < items.length; slot++) {
			Item item = items[slot];
			if (item == null)
				continue;
			if (player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
				int newId = ItemConstants.getDegradeItemWhenCombating(item.getId());
				if (newId != -1) {
					item.setId(newId);
					player.getContent().get(ContentType.EQUIPMENT).refresh(slot);
					player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
					player.getPackets().sendGameMessage("Your " + item.getDefinitions().getName() + " degraded.");
				}
			}
			int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
			if (defaultCharges == -1)
				continue;
			if (ItemConstants.itemDegradesWhileWearing(item.getId()))
				degrade(item.getId(), defaultCharges, slot);
			else if (player.getAttackedByDelay() > Utilities.currentTimeMillis())
				degrade(item.getId(), defaultCharges, slot);
		}
	}

	public void die() {
		Item[] equipItems = player.getContent().get(ContentType.EQUIPMENT).getItems().getItems();
		for (int slot = 0; slot < equipItems.length; slot++) {
			if (equipItems[slot] != null && degradeCompletly(equipItems[slot]))
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(slot, null);
		}
		Item[] invItems = player.getContent().get(ContentType.INVENTORY).getItems().getItems();
		for (int slot = 0; slot < invItems.length; slot++) {
			if (invItems[slot] != null && degradeCompletly(invItems[slot]))
				player.getContent().get(ContentType.INVENTORY).getItems().set(slot, null);
		}
	}

	/*
	 * return disapear;
	 */
	public boolean degradeCompletly(Item item) {
		int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
		if (defaultCharges == -1)
			return false;
		while (true) {
			if (ItemConstants.itemDegradesWhileWearing(item.getId()) || ItemConstants.itemDegradesWhileCombating(item.getId())) {
				charges.remove(item.getId());
				int newId = ItemConstants.getItemDegrade(item.getId());
				if (newId == -1)
					return ItemConstants.getItemDefaultCharges(item.getId()) == -1 ? false : true;
				item.setId(newId);
			} else {
				int newId = ItemConstants.getItemDegrade(item.getId());
				if (newId != -1) {
					charges.remove(item.getId());
					item.setId(newId);
				}
				break;
			}
		}
		return false;
	}

	public void wear(int slot) {
		Item item = player.getContent().get(ContentType.EQUIPMENT).getItems().get(slot);
		if (item == null)
			return;
		int newId = ItemConstants.getDegradeItemWhenWear(item.getId());
		if (newId == -1)
			return;
		player.getContent().get(ContentType.EQUIPMENT).getItems().set(slot, new Item(newId, 1));
		player.getContent().get(ContentType.EQUIPMENT).refresh(slot);
		player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		player.getPackets().sendGameMessage("Your " + item.getDefinitions().getName() + " degraded.");
	}

	private void degrade(int itemId, int defaultCharges, int slot) {
		Integer c = charges.remove(itemId);
		if (c == null)
			c = defaultCharges;
		else {
			c--;
			if (c == 0) {
				int newId = ItemConstants.getItemDegrade(itemId);
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(slot, newId != -1 ? new Item(newId, 1) : null);
				if (newId == -1)
					player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(itemId).getName() + " became into dust.");
				else
					player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(itemId).getName() + " degraded.");
				player.getContent().get(ContentType.EQUIPMENT).refresh(slot);
				player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
				return;
			}
		}
		charges.put(itemId, c);
	}

}
