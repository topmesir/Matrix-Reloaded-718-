package com.rs.game.entity.mobile.player.controller.impl;

import com.rs.Settings;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.ForceMovement;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.game.entity.mobile.player.controller.Controller;
import com.rs.utilities.Utilities;

public class Wilderness extends Controller {

	private boolean showingSkull;

	@Override
	public void start() {
		checkBoosts(player);
	}

	public static void checkBoosts(Player player) {
		boolean changed = false;
		int level = player.getSkills().getLevelForXp(SkillType.ATTACK);
		int maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(SkillType.ATTACK)) {
			player.getSkills().setLevelBoost(SkillType.ATTACK, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(SkillType.STRENGTH);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(SkillType.STRENGTH)) {
			player.getSkills().setLevelBoost(SkillType.STRENGTH, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(SkillType.DEFENCE);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(SkillType.DEFENCE)) {
			player.getSkills().setLevelBoost(SkillType.DEFENCE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(SkillType.RANGE);
		maxLevel = (int) (level + 5 + (level * 0.1));
		if (maxLevel < player.getSkills().getLevel(SkillType.RANGE)) {
			player.getSkills().setLevelBoost(SkillType.RANGE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(SkillType.MAGIC);
		maxLevel = level + 5;
		if (maxLevel < player.getSkills().getLevel(SkillType.MAGIC)) {
			player.getSkills().setLevelBoost(SkillType.MAGIC, maxLevel);
			changed = true;
		}
		if (changed)
			player.getPackets().sendGameMessage("Your extreme potion bonus has been reduced.");
	}

	@Override
	public boolean login() {
		moved();
		return false;
	}

	@Override
	public boolean keepCombating(MobileEntity target) {
		if (target instanceof NPC)
			return true;
		if (!canAttack(target))
			return false;
		if (target.getAttackedBy() != player && player.getAttackedBy() != target)
			player.setWildernessSkull();
		if (player.getContent().get(ContentType.COMBATDEF).getSpellId() <= 0 && Utilities.inCircle(new WorldTile(3105, 3933, 0), target, 24)) {
			player.getPackets().sendGameMessage("You can only use magic in the arena.");
			return false;
		}
		return true;
	}

	@Override
	public boolean canAttack(MobileEntity target) {
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (player.isCanPvp() && !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("That player is not in the wilderness.");
				return false;
			}
			if (canHit(target))
				return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean canHit(MobileEntity target) {
		if (target instanceof NPC)
			return true;
		Player p2 = (Player) target;
		if (Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) > getWildLevel())
			return false;
		return true;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		if (getWildLevel() > 20) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utilities.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;

	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		if (getWildLevel() > 20) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		if (player.getTeleBlockDelay() > Utilities.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		if (player.getTeleBlockDelay() > Utilities.currentTimeMillis()) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	public void showSkull() {
		player.getInterfaceManager().sendTab(player.getInterfaceManager().hasRezizableScreen() ? 11 : 0, 381);
	}

	public static boolean isDitch(int id) {
		return id >= 1440 && id <= 1444 || id >= 65076 && id <= 65087;
	}

	@Override
	public boolean processObjectClick1(final GameObject object) {
		if (isDitch(object.getId())) {
			player.lock();
			player.setNextAnimation(new Animation(6132));
			final WorldTile toTile = new WorldTile(object.getRotation() == 1 || object.getRotation() == 3 ? object.getX() + 2 : player.getX(), object.getRotation() == 0 || object.getRotation() == 2 ? object.getY() - 1 : player.getY(), object.getPlane());

			player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2, object.getRotation() == 0 || object.getRotation() == 2 ? ForceMovement.SOUTH : ForceMovement.EAST));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(toTile);
					player.faceObject(object);
					removeIcon();
					removeControler();
					player.resetReceivedDamage();
					player.unlock();
				}
			}, 2);
			return false;
		} else if (object.getId() == 2557 || object.getId() == 65717) {
			player.getPackets().sendGameMessage("It seems it is locked, maybe you should try something else.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(final GameObject object) {
		return true;
	}

	@Override
	public void sendInterfaces() {
		if (isAtWild(player))
			showSkull();
	}

	@Override
	public boolean sendDeath() {

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.removeDamage(player);
					}
					player.sendItemsOnDeath(killer);
					player.getContent().get(ContentType.EQUIPMENT).init();
					player.getContent().get(ContentType.INVENTORY).init();
					player.reset();
					player.setNextWorldTile(new WorldTile(Settings.RESPAWN_PLAYER_LOCATION));
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					removeIcon();
					removeControler();
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void moved() {
		boolean isAtWild = isAtWild(player);
		boolean isAtWildSafe = isAtWildSafe();
		if (!showingSkull && isAtWild && !isAtWildSafe) {
			showingSkull = true;
			player.setCanPvp(true);
			showSkull();
			player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		} else if (showingSkull && (isAtWildSafe || !isAtWild)) {
			removeIcon();
		} else if (!isAtWildSafe && !isAtWild) {
			player.setCanPvp(false);
			removeIcon();
			removeControler();
		}
	}

	public void removeIcon() {
		if (showingSkull) {
			showingSkull = false;
			player.setCanPvp(false);
			player.getPackets().closeInterface(player.getInterfaceManager().hasRezizableScreen() ? 11 : 0);
			player.getContent().get(ContentType.APPEARANCE).generateAppearenceData();
			player.getContent().get(ContentType.EQUIPMENT).refresh(null);
		}
	}

	@Override
	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void forceClose() {
		removeIcon();
	}

	public static final boolean isAtWild(WorldTile tile) {// TODO fix this
		return (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175) // fortihrny
																											// dungeon
				|| (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() >= 3525 && tile.getY() <= 4000) || (tile.getX() >= 3264 && tile.getX() <= 3279 && tile.getY() >= 3279 && tile.getY() <= 3672) || (tile.getX() >= 2756 && tile.getX() <= 2875 && tile.getY() >= 5512 && tile.getY() <= 5627) || (tile.getX() >= 3158 && tile.getX() <= 3181 && tile.getY() >= 3679 && tile.getY() <= 3697) || (tile.getX() >= 3280 && tile.getX() <= 3183 && tile.getY() >= 3885 && tile.getY() <= 3888) || (tile.getX() >= 3012 && tile.getX() <= 3059 && tile.getY() >= 10303 && tile.getY() <= 10351);
	}

	public boolean isAtWildSafe() {
		return (player.getX() >= 2940 && player.getX() <= 3395 && player.getY() <= 3524 && player.getY() >= 3523);
	}

	public int getWildLevel() {
		if (player.getY() > 9900)
			return (player.getY() - 9912) / 8 + 1;
		return (player.getY() - 3520) / 8 + 1;
	}

}
