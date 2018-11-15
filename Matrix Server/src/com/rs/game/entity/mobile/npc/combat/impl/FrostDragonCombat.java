package com.rs.game.entity.mobile.npc.combat.impl;

import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.npc.combat.CombatScript;
import com.rs.game.entity.mobile.npc.combat.NPCCombatDefinitions;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.CombatDefinitions;
import com.rs.game.world.World;
import com.rs.utilities.Utilities;

public class FrostDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Frost dragon" };
	}

	@Override
	public int attack(NPC npc, MobileEntity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		int damage;
		switch (Utilities.getRandom(3)) {
		case 0: // Melee
			if (npc.withinDistance(target, 3)) {
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, damage));
			} else {
				damage = Utilities.getRandom(650);
				if (CombatDefinitions.hasAntiFireShield(target)  || (player != null
						&& (player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17) || player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (CombatDefinitions.hasAntiFireShield(target)  ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!CombatDefinitions.hasAntiFireShield(target)  || !player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17)
						|| !player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7))
						&& player.getFireImmune() > Utilities.currentTimeMillis()) {
					damage = Utilities.getRandom(164);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13155));
				World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			}
			break;
		case 1: // Dragon breath
			if (npc.withinDistance(target, 3)) {
				damage = Utilities.getRandom(650);
				if (CombatDefinitions.hasAntiFireShield(target)  || (player != null
						&& (player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17) || player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (CombatDefinitions.hasAntiFireShield(target)  ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!CombatDefinitions.hasAntiFireShield(target)  || !player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17)
						|| !player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7))
						&& player.getFireImmune() > Utilities.currentTimeMillis()) {
					damage = Utilities.getRandom(164);
					player.getPackets().sendGameMessage(
							"Your potion fully protects you from the heat of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13152));
				npc.setNextGraphics(new Graphics(2465));
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			} else {
				damage = Utilities.getRandom(650);
				if (CombatDefinitions.hasAntiFireShield(target) || (player != null
						&& (player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17) || player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (CombatDefinitions.hasAntiFireShield(target)  ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!CombatDefinitions.hasAntiFireShield(target)  || !player.getContent().get(ContentType.PRAYER).usingPrayer(0, 17)
						|| !player.getContent().get(ContentType.PRAYER).usingPrayer(1, 7))
						&& player.getFireImmune() > Utilities.currentTimeMillis()) {
					damage = Utilities.getRandom(164);
					player.getPackets().sendGameMessage(
							"Your potion fully protects you from the heat of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13155));
				World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			}
			break;
		case 2: // Range
			damage = Utilities.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 2707, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 3: // Ice arrows range
			damage = Utilities.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 369, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 4: // Orb crap
			break;
		}
		return defs.getAttackDelay();
	}

}