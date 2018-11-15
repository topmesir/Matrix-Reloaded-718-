package com.rs.game.entity.mobile.npc.combat;

import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.mobile.Hit;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.Hit.HitLook;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.CombatDefinitions;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.utilities.Utilities;

public abstract class CombatScript {

	/*
	 * Returns ids and names
	 */
	public abstract Object[] getKeys();

	/*
	 * Returns Move Delay
	 */
	public abstract int attack(NPC npc, MobileEntity target);

	public static void delayHit(NPC npc, int delay, final MobileEntity target, final Hit... hits) {
		npc.getCombat().addAttackedByDelay(target);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				for (Hit hit : hits) {
					NPC npc = (NPC) hit.getSource();
					if (npc.isDead() || npc.hasFinished() || target.isDead() || target.hasFinished())
						return;
					target.applyHit(hit);
					npc.getCombat().doDefenceEmote(target);
					/*
					 * if (!(npc instanceof Nex) && hit.getLook() ==
					 * HitLook.MAGIC_DAMAGE && hit.getDamage() == 0)
					 * target.setNextGraphics(new Graphics(85, 0, 100));
					 */
					if (target instanceof Player) {
						Player p2 = (Player) target;
						p2.closeInterfaces();
						if (p2.getContent().get(ContentType.COMBATDEF).isAutoRelatie() && !p2.getContent().get(ContentType.ACTION).actionIsRunning() && !p2.hasWalkSteps())
							p2.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(npc));
					} else {
						NPC n = (NPC) target;
						if (!n.isUnderCombat() || n.canBeAttackedByAutoRelatie())
							n.setTarget(npc);
					}

				}
			}

		}, delay);
	}

	public static Hit getRangeHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.RANGE_DAMAGE);
	}

	public static Hit getMagicHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MAGIC_DAMAGE);
	}

	public static Hit getRegularHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.REGULAR_DAMAGE);
	}

	public static Hit getMeleeHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MELEE_DAMAGE);
	}

	public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, MobileEntity target) {
		int[] bonuses = npc.getBonuses();
		double att = bonuses == null ? 0 : attackStyle == NPCCombatDefinitions.RANGE ? bonuses[CombatDefinitions.RANGE_ATTACK] : attackStyle == NPCCombatDefinitions.MAGE ? bonuses[CombatDefinitions.MAGIC_ATTACK] : bonuses[CombatDefinitions.STAB_ATTACK];
		double def;
		if (target instanceof Player) {
			Player p2 = (Player) target;
			def = p2.getSkills().getLevel(SkillType.DEFENCE) + (2 * p2.getContent().get(ContentType.COMBATDEF).getBonuses()[attackStyle == NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF : attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF]);
			def *= p2.getContent().get(ContentType.PRAYER).getDefenceMultiplier();
		} else {
			NPC n = (NPC) target;
			def = n.getBonuses() == null ? 0 : n.getBonuses()[attackStyle == NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF : attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF : CombatDefinitions.STAB_DEF];
			def *= 2;
		}
		double prob = att / def;
		if (prob > 0.90) // max, 90% prob hit so even lvl 138 can miss at lvl 3
			prob = 0.90;
		else if (prob < 0.05) // minimun 5% so even lvl 3 can hit lvl 138
			prob = 0.05;
		if (prob < Math.random())
			return 0;
		return Utilities.getRandom(maxHit);
	}

}
