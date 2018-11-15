package com.rs.game.entity.mobile.npc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.engine.GameEngine;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.WorldTile;
import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.npc.combat.NPCCombatDefinitions;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.world.World;

@SuppressWarnings("serial")
public class Man extends NPC {

	public Man(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public ArrayList<MobileEntity> getPossibleTargets() {
		ArrayList<MobileEntity> possibleTarget = new ArrayList<MobileEntity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int npcIndex : playerIndexes) {
					Player player = World.getPlayers().get(npcIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
							|| !player.withinDistance(this, 64)
							|| ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this
									&& player.getAttackedByDelay() > System.currentTimeMillis())
							|| !clipedProjectile(player, false))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}

	/*
	 * gotta override else setRespawnTask override doesnt work
	 */
	@Override
	public void sendDeath(MobileEntity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		getMostDamageReceivedSourcePlayer();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					drop();
					reset();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		final NPC npc = this;
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				setFinished(false);
				World.addNPC(npc);
				npc.setLastRegionId(0);
				World.updateEntityRegion(npc);
				loadMapRegions();
				checkMultiArea();
			}
		}, getCombatDefinitions().getRespawnDelay() * 600, TimeUnit.MILLISECONDS);
	}

	@Override
	public int[] getNPCIds() {
		return new int[] {
				1
		};
	}
	
}