package com.rs.networking.decoders.world.handlers.button;

import java.util.HashMap;
import java.util.TimerTask;

import com.rs.Settings;
import com.rs.engine.GameEngine;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemConstants;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.CombatDefinitions;
import com.rs.game.entity.mobile.player.content.type.container.Equipment;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.game.entity.mobile.player.content.type.skills.Skills;
import com.rs.game.entity.mobile.player.dialogue.impl.SkillsDialogue;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public class ButtonClickHandler {

	public static void handleButtons(final Player player, InputStream stream,
			int packetId) {
		int interfaceHash = stream.readIntV2();
		int interfaceId = interfaceHash >> 16;
		final int componentId = interfaceHash - (interfaceId << 16);
		final int slotId2 = stream.readUnsignedShort128();
		final int slotId = stream.readUnsignedShortLE128();
		if (Utilities.getInterfaceDefinitionsSize() <= interfaceId) {
			return;
		}
		if (player.isDead()
				|| !player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (componentId != 65535
				&& Utilities.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
			return;
		}
		if (!player.getControlerManager().processButtonClick(interfaceId,
				componentId, slotId, packetId))
			return;
		
		InterfaceButtonHandler button = ButtonRepository.getButtonHandler(interfaceId);
		if(button != null)
			button.executeButton(player, packetId, interfaceHash, interfaceId, componentId, slotId, slotId2);
		else 
			switch(interfaceId) {
			case 916:
				SkillsDialogue.handleSetQuantityButtons(player, componentId);
				break;
			case 429:
				if (componentId == 18)
					player.getInterfaceManager().sendSettings();
			case 982:
				if (componentId == 5)
					player.getInterfaceManager().sendSettings();
				else if (componentId == 41)
					player.setPrivateChatSetup(player.getPrivateChatSetup() == 0 ? 1
							: 0);
				else if (componentId >= 49 && componentId <= 66)
					player.setPrivateChatSetup(componentId - 48);
				else if (componentId >= 72 && componentId <= 91)
					player.setFriendChatSetup(componentId - 72);
					break;
			case 1218:
				if((componentId >= 33 && componentId <= 55) || componentId == 120 || componentId == 151 || componentId == 189)
					player.getPackets().sendInterface(false, 1218, 1, 1217);
				break;
			case 742:
			case 743:
			case 741:
				if (componentId == 46 || componentId == 20 || componentId == 9)
					player.stopAll();
				break;
			case 749:
				if (componentId == 4) {
					if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) // activate
						player.getContent().get(ContentType.PRAYER).switchQuickPrayers();
					else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) // switch
						player.getContent().get(ContentType.PRAYER).switchSettingQuickPrayer();
				}
				break;
			case 767:
				if (componentId == 10)
					player.getContent().get(ContentType.BANK).openBank();
				break;
			case 755:
				if (componentId == 44)
					player.getPackets().sendWindowsPane(
							player.getInterfaceManager().hasRezizableScreen() ? 746
									: 548, 2);
				else if (componentId == 42) {
					player.getContent().get(ContentType.HINTICON).removeAll();//TODO find hintIcon index
					player.getPackets().sendConfig(1159, 1);
				}
				break;
			case 1056:
				if (componentId == 173)
					player.getInterfaceManager().sendInterface(917);
				break;
			case 1108:
			case 1109:
				player.getContent().get(ContentType.FRIENDSLIST).handleFriendChatButtons(interfaceId,
						componentId, packetId);
				break;
			case 1709:
				player.closeInterfaces(); 
				break;
			}
		
		if (Settings.DEBUG)
			Logger.log("ButtonClickHandler", "InterfaceId " + interfaceId
					+ ", componentId " + componentId + ", slotId " + slotId
					+ ", slotId2 " + slotId2 + ", PacketId: " + packetId);
	}

	public static void sendRemove(Player player, int slotId) {
		if (slotId >= 15)
			return;
		player.stopAll(false, false);
		Item item = player.getContent().get(ContentType.EQUIPMENT).getItem(slotId);
		if (item == null
				|| !player.getContent().get(ContentType.INVENTORY).addItem(item.getId(),
						item.getAmount()))
			return;
		player.getContent().get(ContentType.EQUIPMENT).getItems().set(slotId, null);
		player.getContent().get(ContentType.EQUIPMENT).refresh(slotId);
		player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		if (slotId == 3)
			player.getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(0);
	}

	public static boolean sendWear(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead())
			return false;
		player.stopAll(false, false);
		Item item = player.getContent().get(ContentType.INVENTORY).getItem(slotId);
		if (item == null || item.getId() != itemId)
			return false;
		if (item.getDefinitions().isNoted()
				|| !item.getDefinitions().isWearItem(player.getContent().get(ContentType.APPEARANCE).isMale())) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		if(!ItemConstants.canWear(item, player))
			return true;
		boolean isTwoHandedWeapon = targetSlot == 3
				&& Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getContent().get(ContentType.INVENTORY).hasFreeSlots()
				&& player.getContent().get(ContentType.EQUIPMENT).hasShield()) {
			player.getPackets().sendGameMessage(
					"Not enough free space in your inventory.");
			return true;
		}
		HashMap<SkillType, Integer> requiriments = item.getDefinitions()
				.getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (SkillType skillId : requiriments.keySet()) {
				if (skillId != SkillType.SHARED)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments) {
						player.getPackets()
						.sendGameMessage(
								"You are not high enough level to use this item.");
					}
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId.getSkillId()].toLowerCase();
					player.getPackets().sendGameMessage(
							"You need to have a"
									+ (name.startsWith("a") ? "n" : "") + " "
									+ name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return true;
		if (!player.getControlerManager().canEquip(targetSlot, itemId))
			return false;
		player.stopAll(false, false);
		player.getContent().get(ContentType.INVENTORY).deleteItem(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getContent().get(ContentType.EQUIPMENT).getItem(5) != null) {
				if (!player.getContent().get(ContentType.INVENTORY).addItem(
						player.getContent().get(ContentType.EQUIPMENT).getItem(5).getId(),
						player.getContent().get(ContentType.EQUIPMENT).getItem(5).getAmount())) {
					player.getContent().get(ContentType.INVENTORY).getItems().set(slotId, item);
					player.getContent().get(ContentType.INVENTORY).refresh(slotId);
					return true;
				}
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getContent().get(ContentType.EQUIPMENT).getItem(3) != null
					&& Equipment.isTwoHandedWeapon(player.getContent().get(ContentType.EQUIPMENT)
							.getItem(3))) {
				if (!player.getContent().get(ContentType.INVENTORY).addItem(
						player.getContent().get(ContentType.EQUIPMENT).getItem(3).getId(),
						player.getContent().get(ContentType.EQUIPMENT).getItem(3).getAmount())) {
					player.getContent().get(ContentType.INVENTORY).getItems().set(slotId, item);
					player.getContent().get(ContentType.INVENTORY).refresh(slotId);
					return true;
				}
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(3, null);
			}

		}
		if (player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot) != null
				&& (itemId != player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot).getId() || !item
				.getDefinitions().isStackable())) {
			if (player.getContent().get(ContentType.INVENTORY).getItems().get(slotId) == null) {
				player.getContent().get(ContentType.INVENTORY)
				.getItems()
				.set(slotId,
						new Item(player.getContent().get(ContentType.EQUIPMENT)
								.getItem(targetSlot).getId(), player
								.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot)
								.getAmount()));
				player.getContent().get(ContentType.INVENTORY).refresh(slotId);
			} else
				player.getContent().get(ContentType.INVENTORY).addItem(
						new Item(player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot)
								.getId(), player.getContent().get(ContentType.EQUIPMENT)
								.getItem(targetSlot).getAmount()));
			player.getContent().get(ContentType.EQUIPMENT).getItems().set(targetSlot, null);
		}
		if(targetSlot == Equipment.SLOT_AURA)
			player.getContent().get(ContentType.AURAS).removeAura();
		int oldAmt = 0;
		if (player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot) != null) {
			oldAmt = player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getContent().get(ContentType.EQUIPMENT).getItems().set(targetSlot, item2);
		player.getContent().get(ContentType.EQUIPMENT).refresh(targetSlot,
				targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
		player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		player.getPackets().sendSound(2240, 0, 1);
		if (targetSlot == 3)
			player.getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(0);
		player.getContent().get(ContentType.CHARGES).wear(targetSlot);
		return true;
	}

	public static boolean sendWear2(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead())
			return false;
		player.stopAll(false, false);
		Item item = player.getContent().get(ContentType.INVENTORY).getItem(slotId);
		if (item == null || item.getId() != itemId)
			return false;
		if (item.getDefinitions().isNoted()
				|| !item.getDefinitions().isWearItem(player.getContent().get(ContentType.APPEARANCE).isMale()) && itemId != 4084) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if(itemId == 4084)
			targetSlot = 3;
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		if(!ItemConstants.canWear(item, player))
			return false;
		boolean isTwoHandedWeapon = targetSlot == 3
				&& Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getContent().get(ContentType.INVENTORY).hasFreeSlots()
				&& player.getContent().get(ContentType.EQUIPMENT).hasShield()) {
			player.getPackets().sendGameMessage(
					"Not enough free space in your inventory.");
			return false;
		}
		HashMap<SkillType, Integer> requiriments = item.getDefinitions()
				.getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (SkillType skillId : requiriments.keySet()) {
				if (skillId != SkillType.SHARED)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments)
						player.getPackets()
						.sendGameMessage(
								"You are not high enough level to use this item.");
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId.getSkillId()].toLowerCase();
					player.getPackets().sendGameMessage(
							"You need to have a"
									+ (name.startsWith("a") ? "n" : "") + " "
									+ name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return false;
		if (!player.getControlerManager().canEquip(targetSlot, itemId))
			return false;
		player.getContent().get(ContentType.INVENTORY).getItems().remove(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getContent().get(ContentType.EQUIPMENT).getItem(5) != null) {
				if (!player.getContent().get(ContentType.INVENTORY).getItems()
						.add(player.getContent().get(ContentType.EQUIPMENT).getItem(5))) {
					player.getContent().get(ContentType.INVENTORY).getItems().set(slotId, item);
					return false;
				}
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getContent().get(ContentType.EQUIPMENT).getItem(3) != null
					&& Equipment.isTwoHandedWeapon(player.getContent().get(ContentType.EQUIPMENT)
							.getItem(3))) {
				if (!player.getContent().get(ContentType.INVENTORY).getItems()
						.add(player.getContent().get(ContentType.EQUIPMENT).getItem(3))) {
					player.getContent().get(ContentType.INVENTORY).getItems().set(slotId, item);
					return false;
				}
				player.getContent().get(ContentType.EQUIPMENT).getItems().set(3, null);
			}

		}
		if (player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot) != null
				&& (itemId != player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot).getId() || !item
				.getDefinitions().isStackable())) {
			if (player.getContent().get(ContentType.INVENTORY).getItems().get(slotId) == null) {
				player.getContent().get(ContentType.INVENTORY)
				.getItems()
				.set(slotId,
						new Item(player.getContent().get(ContentType.EQUIPMENT)
								.getItem(targetSlot).getId(), player
								.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot)
								.getAmount()));
			} else
				player.getContent().get(ContentType.INVENTORY)
				.getItems()
				.add(new Item(player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot)
						.getId(), player.getContent().get(ContentType.EQUIPMENT)
						.getItem(targetSlot).getAmount()));
			player.getContent().get(ContentType.EQUIPMENT).getItems().set(targetSlot, null);
		}
		if(targetSlot == Equipment.SLOT_AURA)
			player.getContent().get(ContentType.AURAS).removeAura();
		int oldAmt = 0;
		if (player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot) != null) {
			oldAmt = player.getContent().get(ContentType.EQUIPMENT).getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getContent().get(ContentType.EQUIPMENT).getItems().set(targetSlot, item2);
		player.getContent().get(ContentType.EQUIPMENT).refresh(targetSlot,
				targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
		if (targetSlot == 3)
			player.getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(0);
		player.getContent().get(ContentType.CHARGES).wear(targetSlot);
		return true;
	}

	public static void submitSpecialRequest(final Player player) {
		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							player.getContent().get(ContentType.COMBATDEF).switchUsingSpecialAttack();
						}
					}, 0);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 200);
	}

	public static void sendWear(Player player, int[] slotIds) {
		if (player.hasFinished() || player.isDead())
			return;
		boolean worn = false;
		Item[] copy = player.getContent().get(ContentType.INVENTORY).getItems().getItemsCopy();
		for (int slotId : slotIds) {
			Item item = player.getContent().get(ContentType.INVENTORY).getItem(slotId);
			if (item == null)
				continue;
			if (sendWear2(player, slotId, item.getId()))
				worn = true;
		}
		player.getContent().get(ContentType.INVENTORY).refreshItems(copy);
		if (worn) {
			player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
			player.getPackets().sendSound(2240, 0, 1);
		}
	}

	public static void openEquipmentBonuses(final Player player, boolean banking) {
		player.stopAll();
		player.getInterfaceManager().sendInventoryInterface(670);
		player.getInterfaceManager().sendInterface(667);
		player.getPackets().sendConfigByFile(4894, banking ? 1 : 0);
		player.getPackets().sendItems(93,
				player.getContent().get(ContentType.INVENTORY).getItems());
		player.getPackets().sendInterSetItemsOptionsScript(670, 0, 93,
				4, 7, "Equip", "Compare", "Stats", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(670, 0, 0,
				27, 0, 1, 2, 3);
		player.getPackets().sendIComponentSettings(667, 14, 0, 13, 1030);
		refreshEquipBonuses(player);
		if(banking) {
			player.getTemporaryAttributtes().put("Banking", Boolean.TRUE);
			player.setCloseInterfacesEvent(new Runnable() {
				@Override
				public void run() {
					player.getTemporaryAttributtes().remove("Banking");
				}

			});
		}
	}

	public static void refreshEquipBonuses(Player player) {
		player.getPackets().sendIComponentText(667, 28,
				"Stab: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[0]);
		player.getPackets().sendIComponentText(667, 29,
				"Slash: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[1]);
		player.getPackets().sendIComponentText(667, 30,
				"Crush: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[2]);
		player.getPackets().sendIComponentText(667, 31,
				"Magic: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[3]);
		player.getPackets().sendIComponentText(667, 32,
				"Range: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[4]);
		player.getPackets().sendIComponentText(667, 33,
				"Stab: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[5]);
		player.getPackets().sendIComponentText(667, 34,
				"Slash: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[6]);
		player.getPackets().sendIComponentText(667, 35,
				"Crush: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[7]);
		player.getPackets().sendIComponentText(667, 36,
				"Magic: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[8]);
		player.getPackets().sendIComponentText(667, 37,
				"Range: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[9]);
		player.getPackets().sendIComponentText(667, 38,
				"Summoning: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[10]);
		player.getPackets().sendIComponentText(667, 39, 
				"Absorb Melee: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_MELEE_BONUS] + "%");
		player.getPackets().sendIComponentText(667, 40,
				"Absorb Magic: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_MAGE_BONUS] + "%");
		player.getPackets().sendIComponentText(667, 41,
				"Absorb Ranged: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_RANGE_BONUS]+ "%");
		player.getPackets().sendIComponentText(667, 42,
				"Strength: " + player.getContent().get(ContentType.COMBATDEF).getBonuses()[14]);
		player.getPackets().sendIComponentText(667, 43,
				"Ranged Str: " + player.getContent().get(ContentType.COMBATDEF).getBonuses()[15]);
		player.getPackets().sendIComponentText(667, 44,
				"Prayer: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[16]);
		player.getPackets().sendIComponentText(667,45,"Magic Damage: +" + player.getContent().get(ContentType.COMBATDEF).getBonuses()[17] + "%");
	}
}
