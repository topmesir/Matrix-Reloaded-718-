package com.rs.game.entity.mobile.npc.combat;

import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.utilities.MapAreas;
import com.rs.utilities.Utilities;

public final class NPCCombat {

	private NPC npc;
	private int combatDelay;
	private MobileEntity target;

	public NPCCombat(NPC npc) {
		this.npc = npc;
	}

	public int getCombatDelay() {
		return combatDelay;
	}

	/*
	 * returns if under combat
	 */
	public boolean process() {
		if (combatDelay > 0)
			combatDelay--;
		if (target != null) {
			if (!checkAll()) {
				removeTarget();
				return false;
			}
			if (combatDelay <= 0)
				combatDelay = combatAttack();
			return true;
		}
		return false;
	}

	/*
	 * return combatDelay
	 */
	private int combatAttack() {
		MobileEntity target = this.target; // prevents multithread issues
		if (target == null)
			return 0;
		// if hes frooze not gonna attack
		if (npc.getFreezeDelay() >= Utilities.currentTimeMillis())
			return 0;
		// check if close to target, if not let it just walk and dont attack
		// this gameticket
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = defs.getAttackStyle();
		int maxDistance = attackStyle == NPCCombatDefinitions.MELEE || attackStyle == NPCCombatDefinitions.SPECIAL2 ? 0 : 7;
		if (!npc.clipedProjectile(target, maxDistance == 0))
			return 0;
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
			return 0;
		}
		addAttackedByDelay(target);
		return CombatScriptsHandler.specialAttack(npc, target);
	}

	protected void doDefenceEmote(MobileEntity target) {
		
		  if (target.getNextAnimation() != null) // if has att emote already
		  return;
		 
		target.setNextAnimationNoPriority(new Animation(target.getDefenceEmote()));
	}

	public MobileEntity getTarget() {
		return target;
	}

	public void addAttackedByDelay(MobileEntity target) { // prevents multithread
													// issues
		target.setAttackedBy(npc);
		target.setAttackedByDelay(Utilities.currentTimeMillis() + npc.getCombatDefinitions().getAttackDelay() * 600 + 600); // 8seconds
	}

	public void setTarget(MobileEntity target) {
		this.target = target;
		npc.setNextFaceEntity(target);
		if (!checkAll()) {
			removeTarget();
			return;
		}
	}

	public boolean checkAll() {
		MobileEntity target = this.target; // prevents multithread issues
		if (target == null)
			return false;
		if (npc.isDead() || npc.hasFinished() || npc.isForceWalking() || target.isDead() || target.hasFinished() || npc.getPlane() != target.getPlane())
			return false;
		if (npc.getFreezeDelay() >= Utilities.currentTimeMillis())
			return true; // if freeze cant move ofc
		int distanceX = npc.getX() - npc.getRespawnTile().getX();
		int distanceY = npc.getY() - npc.getRespawnTile().getY();
		int size = npc.getSize();
		int maxDistance;
		if (!npc.isNoDistanceCheck() && !npc.isCantFollowUnderCombat()) {
			maxDistance = 32;

				if (npc.getMapAreaNameHash() != -1) {
					// if out his area
					if (!MapAreas.isAtArea(npc.getMapAreaNameHash(), npc) || (!npc.canBeAttackFromOutOfArea() && !MapAreas.isAtArea(npc.getMapAreaNameHash(), target))) {
						npc.forceWalkRespawnTile();
						return false;
					}
				} else if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
					// if more than 64 distance from respawn place
					npc.forceWalkRespawnTile();
					return false;
				}
			maxDistance = 16;
			distanceX = target.getX() - npc.getX();
			distanceY = target.getY() - npc.getY();
			if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance)
				return false; // if target distance higher 16
		} else {
			distanceX = target.getX() - npc.getX();
			distanceY = target.getY() - npc.getY();
		}
		// checks for no multi area :)
			if (!npc.isForceMultiAttacked()) {
				if (!target.isAtMultiArea() || !npc.isAtMultiArea()) {
					if (npc.getAttackedBy() != target && npc.getAttackedByDelay() > Utilities.currentTimeMillis())
						return false;
					if (target.getAttackedBy() != npc && target.getAttackedByDelay() > Utilities.currentTimeMillis())
						return false;
				}
			}
		if (!npc.isCantFollowUnderCombat()) {
			// if is under
			int targetSize = target.getSize();
			if (distanceX < size && distanceX > -targetSize && distanceY < size && distanceY > -targetSize && !target.hasWalkSteps()) {

				/*
				 * System.out.println(size + maxDistance); System.out.println(-1
				 * - maxDistance);
				 */
				npc.resetWalkSteps();
				if (!npc.addWalkSteps(target.getX() + 1, npc.getY())) {
					npc.resetWalkSteps();
					if (!npc.addWalkSteps(target.getX() - size, npc.getY())) {
						npc.resetWalkSteps();
						if (!npc.addWalkSteps(npc.getX(), target.getY() + 1)) {
							npc.resetWalkSteps();
							if (!npc.addWalkSteps(npc.getX(), target.getY() - size)) {
								return true;
							}
						}
					}
				}
				return true;
			}
			if (npc.getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE && targetSize == 1 && size == 1 && Math.abs(npc.getX() - target.getX()) == 1 && Math.abs(npc.getY() - target.getY()) == 1 && !target.hasWalkSteps()) {

				if (!npc.addWalkSteps(target.getX(), npc.getY(), 1))
					npc.addWalkSteps(npc.getX(), target.getY(), 1);
				return true;
			}

			int attackStyle = npc.getCombatDefinitions().getAttackStyle();
				maxDistance = npc.isForceFollowClose() ? 0 : (attackStyle == NPCCombatDefinitions.MELEE || attackStyle == NPCCombatDefinitions.SPECIAL2) ? 0 : 7;
				npc.resetWalkSteps();
				// is far from target, moves to it till can attack
				if ((!npc.clipedProjectile(target, maxDistance == 0)) || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
					if (!npc.addWalkStepsInteract(target.getX(), target.getY(), 2, size, true) && combatDelay < 3)
						combatDelay = 3;
					return true;
			}
		}
		return true;
	}

	public void addCombatDelay(int delay) {
		combatDelay += delay;
	}

	public void setCombatDelay(int delay) {
		combatDelay = delay;
	}

	public boolean underCombat() {
		return target != null;
	}

	public void removeTarget() {
		this.target = null;
		npc.setNextFaceEntity(null);
	}

	public void reset() {
		combatDelay = 0;
		target = null;
	}

}
