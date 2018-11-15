package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.Magic;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class SpellBookInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(192, 193, 430);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		switch(interfaceId) {
		case 192:
			if (componentId == 2)
				player.getContent().get(ContentType.COMBATDEF).switchDefensiveCasting();
			else if (componentId == 7)
				player.getContent().get(ContentType.COMBATDEF).switchShowCombatSpells();
			else if (componentId == 9)
				player.getContent().get(ContentType.COMBATDEF).switchShowTeleportSkillSpells();
			else if (componentId == 11)
				player.getContent().get(ContentType.COMBATDEF).switchShowMiscallaneousSpells();
			else if (componentId == 13)
				player.getContent().get(ContentType.COMBATDEF).switchShowSkillSpells();
			else if (componentId >= 15 & componentId <= 17)
				player.getContent().get(ContentType.COMBATDEF)
				.setSortSpellBook(componentId - 15);
			else
				Magic.processNormalSpell(player, componentId, packetId);
			break;
		case 193:
			if (componentId == 5)
				player.getContent().get(ContentType.COMBATDEF).switchShowCombatSpells();
			else if (componentId == 7)
				player.getContent().get(ContentType.COMBATDEF).switchShowTeleportSkillSpells();
			else if (componentId >= 9 && componentId <= 11)
				player.getContent().get(ContentType.COMBATDEF).setSortSpellBook(componentId - 9);
			else if (componentId == 18)
				player.getContent().get(ContentType.COMBATDEF).switchDefensiveCasting();
			else
				Magic.processAncientSpell(player, componentId, packetId);
			break;
		case 430:
			if (componentId == 5)
				player.getContent().get(ContentType.COMBATDEF).switchShowCombatSpells();
			else if (componentId == 7)
				player.getContent().get(ContentType.COMBATDEF).switchShowTeleportSkillSpells();
			else if (componentId == 9)
				player.getContent().get(ContentType.COMBATDEF).switchShowMiscallaneousSpells();
			else if (componentId >= 11 & componentId <= 13)
				player.getContent().get(ContentType.COMBATDEF)
				.setSortSpellBook(componentId - 11);
			else if (componentId == 20)
				player.getContent().get(ContentType.COMBATDEF).switchDefensiveCasting();
			else
				Magic.processLunarSpell(player, componentId, packetId);
			break;
		}
	}

}
