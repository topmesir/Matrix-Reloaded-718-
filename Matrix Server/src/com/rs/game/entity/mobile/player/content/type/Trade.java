package com.rs.game.entity.mobile.player.content.type;

import com.rs.engine.cache.loaders.ItemDefinitions;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemConstants;
import com.rs.game.entity.item.ItemExamines;
import com.rs.game.entity.item.ItemsContainer;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.AbstractContent;
import com.rs.game.entity.mobile.player.content.ContentType;

@SuppressWarnings("serial")
public class Trade extends AbstractContent {

	private transient Player target;
	private transient ItemsContainer<Item> items;
	private transient boolean tradeModified;
	private transient boolean accepted;

	public Trade() {
		items = new ItemsContainer<Item>(28, false);
	}

	public void openTrade(Player target) {
		synchronized (this) {
			synchronized (target.getContent().get(ContentType.TRADE)) {
				this.target = target;
				player.getPackets().sendIComponentText(335, 17, "Trading With: " + target.getDisplayName());
				player.getPackets().sendGlobalString(203, target.getDisplayName());
				sendInterItems();
				sendOptions();
				sendTradeModified();
				refreshFreeInventorySlots();
				refreshTradeWealth();
				refreshStageMessage(true);
				player.getInterfaceManager().sendInterface(335);
				player.getInterfaceManager().sendInventoryInterface(336);
				player.setCloseInterfacesEvent(new Runnable() {
					
					@Override
					public void run() {
						closeTrade(CloseTradeStage.CANCEL);
					}
				});
			}
		}
	}

	public void removeItem(final int slot, int amount) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getContent().get(ContentType.TRADE)) {
				Item item = items.get(slot);
				if (item == null) {
					return;
				}
				Item[] itemsBefore = items.getItemsCopy();
				int maxAmount = items.getNumberOf(item);
				if (amount < maxAmount) {
					item = new Item(item.getId(), amount);
				} else {
					item = new Item(item.getId(), maxAmount);
				}
				items.remove(slot, item);
				player.getContent().get(ContentType.INVENTORY).addItem(item);
				refreshItems(itemsBefore);
				cancelAccepted();
				setTradeModified(true);
			}
		}
	}

	public void sendFlash(int slot) {
		player.getPackets().sendInterFlashScript(335, 33, 4, 7, slot);
		target.getPackets().sendInterFlashScript(335, 36, 4, 7, slot);
	}

	public void cancelAccepted() {
		boolean canceled = false;
		if (accepted) {
			accepted = false;
			canceled = true;
		}
		if (target.getContent().get(ContentType.TRADE).accepted) {
			target.getContent().get(ContentType.TRADE).accepted = false;
			canceled = true;
		}
		if (canceled) {
			refreshBothStageMessage(canceled);
		}
	}

	public void addItem(int slot, int amount) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getContent().get(ContentType.TRADE)) {
				Item item = player.getContent().get(ContentType.INVENTORY).getItem(slot);
				if (item == null) {
					return;
				}
				if (!ItemConstants.isTradeable(item)) {
					player.getPackets().sendGameMessage("That item isn't tradeable.");
					return;
				}
				Item[] itemsBefore = items.getItemsCopy();
				int maxAmount = player.getContent().get(ContentType.INVENTORY).getItems().getNumberOf(item);
				if (amount < maxAmount) {
					item = new Item(item.getId(), amount);
				} else {
					item = new Item(item.getId(), maxAmount);
				}
				items.add(item);
				player.getContent().get(ContentType.INVENTORY).deleteItem(slot, item);
				refreshItems(itemsBefore);
				cancelAccepted();
			}
		}
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = items.getItems()[index];
			if (itemsBefore[index] != item) {
				if (itemsBefore[index] != null && (item == null || item.getId() != itemsBefore[index].getId() || item.getAmount() < itemsBefore[index].getAmount())) {
					sendFlash(index);
				}
				changedSlots[count++] = index;
			}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
		refreshFreeInventorySlots();
		refreshTradeWealth();
	}

	public void sendOptions() {
		player.getPackets().sendInterSetItemsOptionsScript(336, 0, 93, 4, 7, "Offer", "Offer-5", "Offer-10", "Offer-All", "Offer-X", "Value<col=FF9040>", "Lend");
		player.getPackets().sendIComponentSettings(336, 0, 0, 27, 1278);
		player.getPackets().sendInterSetItemsOptionsScript(335, 32, 90, 4, 7, "Remove", "Remove-5", "Remove-10", "Remove-All", "Remove-X", "Value");
		player.getPackets().sendIComponentSettings(335, 32, 0, 27, 1150);
		player.getPackets().sendInterSetItemsOptionsScript(335, 35, 90, true, 4, 7, "Value");
		player.getPackets().sendIComponentSettings(335, 35, 0, 27, 1026);
	}

	public boolean isTrading() {
		return target != null;
	}

	public void setTradeModified(boolean modified) {
		if (modified == tradeModified) {
			return;
		}
		tradeModified = modified;
		sendTradeModified();
	}

	public void sendInterItems() {
		player.getPackets().sendItems(90, items);
		target.getPackets().sendItems(90, true, items);
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(90, items, slots);
		target.getPackets().sendUpdateItems(90, true, items.getItems(), slots);
	}

	public void accept(boolean firstStage) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getContent().get(ContentType.TRADE)) {
				if (target.getContent().get(ContentType.TRADE).accepted) {
					if (firstStage) {
						if (nextStage()) {
							target.getContent().get(ContentType.TRADE).nextStage();
						}
					} else {
						player.setCloseInterfacesEvent(null);
						player.closeInterfaces();
						closeTrade(CloseTradeStage.DONE);
					}
					return;
				}
				accepted = true;
				refreshBothStageMessage(firstStage);
			}
		}
	}

	public void sendValue(int slot, boolean traders) {
		if (!isTrading()) {
			return;
		}
		Item item = traders ? target.getContent().get(ContentType.TRADE).items.get(slot) : items.get(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		int price = ItemDefinitions.getItemDefinitions(item.getId()).getValue();
		player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + price + " coins.");
	}

	public void sendValue(int slot) {
		Item item = player.getContent().get(ContentType.INVENTORY).getItem(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		int price = ItemDefinitions.getItemDefinitions(item.getId()).getValue();
		player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + price + " coins.");
	}

	public void sendExamine(int slot, boolean traders) {
		if (!isTrading()) {
			return;
		}
		Item item = traders ? target.getContent().get(ContentType.TRADE).items.get(slot) : items.get(slot);
		if (item == null) {
			return;
		}
		player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
	}

	public boolean nextStage() {
		if (!isTrading()) {
			return false;
		}
		if (player.getContent().get(ContentType.INVENTORY).getItems().getUsedSlots() + target.getContent().get(ContentType.TRADE).items.getUsedSlots() > 28) {
			player.setCloseInterfacesEvent(null);
			player.closeInterfaces();
			closeTrade(CloseTradeStage.NO_SPACE);
			return false;
		}
		accepted = false;
		player.getInterfaceManager().sendInterface(334);
		player.getInterfaceManager().closeInventoryInterface();
		player.getPackets().sendHideIComponent(334, 55, !(tradeModified || target.getContent().get(ContentType.TRADE).tradeModified));
		refreshBothStageMessage(false);
		return true;
	}

	public void refreshBothStageMessage(boolean firstStage) {
		refreshStageMessage(firstStage);
		target.getContent().get(ContentType.TRADE).refreshStageMessage(firstStage);
	}

	public void refreshStageMessage(boolean firstStage) {
		player.getPackets().sendIComponentText(firstStage ? 335 : 334, firstStage ? 39 : 34, getAcceptMessage(firstStage));
	}

	public String getAcceptMessage(boolean firstStage) {
		if (accepted) {
			return "Waiting for other player...";
		}
		if (target.getContent().get(ContentType.TRADE).accepted) {
			return "Other player has accepted.";
		}
		return firstStage ? "" : "Are you sure you want to make this trade?";
	}

	public void sendTradeModified() {
		player.getPackets().sendConfig(1042, tradeModified ? 1 : 0);
		target.getPackets().sendConfig(1043, tradeModified ? 1 : 0);
	}

	public void refreshTradeWealth() {
		int wealth = getTradeWealth();
		player.getPackets().sendGlobalConfig(729, wealth);
		target.getPackets().sendGlobalConfig(697, wealth);
	}

	public void refreshFreeInventorySlots() {
		int freeSlots = player.getContent().get(ContentType.INVENTORY).getFreeSlots();
		target.getPackets().sendIComponentText(335, 23, "has " + (freeSlots == 0 ? "no" : freeSlots) + " free" + "<br>inventory slots");
	}

	public int getTradeWealth() {
		int wealth = 0;
		for (Item item : items.getItems()) {
			if (item == null) {
				continue;
			}
			wealth += ItemDefinitions.getItemDefinitions(item.getId()).getValue() * item.getAmount();
		}
		return wealth;
	}

	private static enum CloseTradeStage {
		CANCEL, NO_SPACE, DONE
	}

	public void closeTrade(CloseTradeStage stage) {
		synchronized (this) {
			synchronized (target.getContent().get(ContentType.TRADE)) {
				Player oldTarget = target;
				target = null;
				tradeModified = false;
				accepted = false;
				if (CloseTradeStage.DONE != stage) {
					player.getContent().get(ContentType.INVENTORY).getItems().addAll(items);
					player.getContent().get(ContentType.INVENTORY).init();
					items.clear();
				} else {
					player.getPackets().sendGameMessage("Accepted trade.");
					player.getContent().get(ContentType.INVENTORY).getItems().addAll(oldTarget.getContent().get(ContentType.TRADE).items);
					player.getContent().get(ContentType.INVENTORY).init();
					oldTarget.getContent().get(ContentType.TRADE).items.clear();
				}
				if (oldTarget.getContent().get(ContentType.TRADE).isTrading()) {
					oldTarget.setCloseInterfacesEvent(null);
					oldTarget.closeInterfaces();
					oldTarget.getContent().get(ContentType.TRADE).closeTrade(stage);
					if (CloseTradeStage.CANCEL == stage) {
						oldTarget.getPackets().sendGameMessage("<col=ff0000>Other player declined trade!");
					} else if (CloseTradeStage.NO_SPACE == stage) {
						player.getPackets().sendGameMessage("You don't have enough space in your inventory for this trade.");
						oldTarget.getPackets().sendGameMessage("Other player doesn't have enough space in their inventory for this trade.");
					}
				}
			}
		}
	}
}
