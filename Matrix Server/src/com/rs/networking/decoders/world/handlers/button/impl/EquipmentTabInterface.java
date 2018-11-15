package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.WorldTile;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.Magic;
import com.rs.game.entity.mobile.player.content.type.container.Equipment;
import com.rs.game.entity.mobile.player.dialogue.impl.Transportation;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.ButtonClickHandler;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class EquipmentTabInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(387);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (player.getInterfaceManager().containsInventoryInter())
			return;
		if (componentId == 6) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				int hatId = player.getContent().get(ContentType.EQUIPMENT).getHatId();
				if(hatId == 24437 || hatId == 24439 || hatId == 24440 || hatId == 24441) {
					player.getDialogueManager().startDialogue("FlamingSkull", player.getContent().get(ContentType.EQUIPMENT).getItem(Equipment.SLOT_HAT), -1);
					return;
				}
			}else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				ButtonClickHandler.sendRemove(player, Equipment.SLOT_HAT);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				player.getContent().get(ContentType.EQUIPMENT).sendExamine(Equipment.SLOT_HAT);
		} else if (componentId == 9) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				ButtonClickHandler.sendRemove(player, Equipment.SLOT_CAPE);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				player.getContent().get(ContentType.EQUIPMENT).sendExamine(Equipment.SLOT_CAPE);
		} else if (componentId == 12) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				int amuletId = player.getContent().get(ContentType.EQUIPMENT).getAmuletId();
				if (amuletId <= 1712 && amuletId >= 1706
						|| amuletId >= 10354 && amuletId <= 10361) {
					if (Magic.sendItemTeleportSpell(player, true,
							Transportation.EMOTE, Transportation.GFX, 4,
							new WorldTile(3087, 3496, 0))) {
						Item amulet = player.getContent().get(ContentType.EQUIPMENT).getItem(
								Equipment.SLOT_AMULET);
						if (amulet != null) {
							amulet.setId(amulet.getId() - 2);
							player.getContent().get(ContentType.EQUIPMENT).refresh(
									Equipment.SLOT_AMULET);
						}
					}
				} else if (amuletId == 1704 || amuletId == 10352)
					player.getPackets()
					.sendGameMessage(
							"The amulet has ran out of charges. You need to recharge it if you wish it use it once more.");
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
				int amuletId = player.getContent().get(ContentType.EQUIPMENT).getAmuletId();
				if (amuletId <= 1712 && amuletId >= 1706
						|| amuletId >= 10354 && amuletId <= 10361) {
					if (Magic.sendItemTeleportSpell(player, true,
							Transportation.EMOTE, Transportation.GFX, 4,
							new WorldTile(2918, 3176, 0))) {
						Item amulet = player.getContent().get(ContentType.EQUIPMENT).getItem(
								Equipment.SLOT_AMULET);
						if (amulet != null) {
							amulet.setId(amulet.getId() - 2);
							player.getContent().get(ContentType.EQUIPMENT).refresh(
									Equipment.SLOT_AMULET);
						}
					}
				}
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				int amuletId = player.getContent().get(ContentType.EQUIPMENT).getAmuletId();
				if (amuletId <= 1712 && amuletId >= 1706
						|| amuletId >= 10354 && amuletId <= 10361) {
					if (Magic.sendItemTeleportSpell(player, true,
							Transportation.EMOTE, Transportation.GFX, 4,
							new WorldTile(3105, 3251, 0))) {
						Item amulet = player.getContent().get(ContentType.EQUIPMENT).getItem(
								Equipment.SLOT_AMULET);
						if (amulet != null) {
							amulet.setId(amulet.getId() - 2);
							player.getContent().get(ContentType.EQUIPMENT).refresh(
									Equipment.SLOT_AMULET);
						}
					}
				}
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
				int amuletId = player.getContent().get(ContentType.EQUIPMENT).getAmuletId();
				if (amuletId <= 1712 && amuletId >= 1706
						|| amuletId >= 10354 && amuletId <= 10361) {
					if (Magic.sendItemTeleportSpell(player, true,
							Transportation.EMOTE, Transportation.GFX, 4,
							new WorldTile(3293, 3163, 0))) {
						Item amulet = player.getContent().get(ContentType.EQUIPMENT).getItem(
								Equipment.SLOT_AMULET);
						if (amulet != null) {
							amulet.setId(amulet.getId() - 2);
							player.getContent().get(ContentType.EQUIPMENT).refresh(
									Equipment.SLOT_AMULET);
						}
					}
				}
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				ButtonClickHandler.sendRemove(player, Equipment.SLOT_AMULET);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				player.getContent().get(ContentType.EQUIPMENT).sendExamine(Equipment.SLOT_AMULET);
		} else if (componentId == 15) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
				int weaponId = player.getContent().get(ContentType.EQUIPMENT).getWeaponId();
				if(weaponId == 15484) 
					player.getInterfaceManager().gazeOrbOfOculus();
			}else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				ButtonClickHandler.sendRemove(player, Equipment.SLOT_WEAPON);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				player.getContent().get(ContentType.EQUIPMENT).sendExamine(Equipment.SLOT_WEAPON);
		} else if (componentId == 18)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_CHEST);
		else if (componentId == 21)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_SHIELD);
		else if (componentId == 24)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_LEGS);
		else if (componentId == 27)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_HANDS);
		else if (componentId == 30)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_FEET);
		else if (componentId == 33)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_RING);
		else if (componentId == 36)
			ButtonClickHandler.sendRemove(player, Equipment.SLOT_ARROWS);
		else if (componentId == 45) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				ButtonClickHandler.sendRemove(player, Equipment.SLOT_AURA);
				player.getContent().get(ContentType.AURAS).removeAura();
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET)
				player.getContent().get(ContentType.EQUIPMENT).sendExamine(Equipment.SLOT_AURA);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.getContent().get(ContentType.AURAS).activate();
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.getContent().get(ContentType.AURAS).sendAuraRemainingTime();
		} else if (componentId == 40) {
			player.stopAll();
			player.getInterfaceManager().sendInterface(17);
		} else if (componentId == 37) {
			ButtonClickHandler.openEquipmentBonuses(player, false);
		}
	}

}
