package com.rs.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.rs.Launcher;
import com.rs.Settings;
import com.rs.engine.GameEngine;
import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.item.FloorItem;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.item.ItemConstants;
import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.MobileEntityList;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.npc.NPCOverrideRepository;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.Rank;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.OwnedObjectManager;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.game.entity.mobile.player.controller.impl.Wilderness;
import com.rs.utilities.AntiFlood;
import com.rs.utilities.IPBanL;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public final class World {

	public static int exiting_delay;
	public static long exiting_start;

	private static final MobileEntityList<Player> players = new MobileEntityList<Player>(Settings.PLAYERS_LIMIT);
	public static final AtomicLong keyMaker = new AtomicLong();
	public static final Map<String, OwnedObjectManager> ownedObjects = new ConcurrentHashMap<String, OwnedObjectManager>();
	private static final MobileEntityList<NPC> npcs = new MobileEntityList<NPC>(Settings.NPCS_LIMIT);
	private static final Map<Integer, Region> regions = Collections.synchronizedMap(new HashMap<Integer, Region>());

	public static final void init() {
		addRestoreRunEnergyTask();
		addDrainPrayerTask();
		addRestoreHitPointsTask();
		addRestoreSkillsTask();
		addRestoreSpecialAttackTask();
		addOwnedObjectsTask();
	}

	private static void addOwnedObjectsTask() {
		GameEngine.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					OwnedObjectManager.processAll();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private static final void addRestoreSpecialAttackTask() {

		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning())
							continue;
						player.getContent().get(ContentType.COMBATDEF).restoreSpecialAttack();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 30000);
	}

	private static final void addRestoreRunEnergyTask() {
		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning() || (player.getSkills().getLevel(SkillType.AGILITY) < 70))
							continue;
						player.restoreRunEnergy();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 1000);
	}

	private static final void addDrainPrayerTask() {
		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning())
							continue;
						player.getContent().get(ContentType.PRAYER).processPrayerDrain();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 600);
	}

	private static final void addRestoreHitPointsTask() {
		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || player.isDead() || !player.isRunning())
							continue;
						player.restoreHitPoints();
					}
					for (NPC npc : npcs) {
						if (npc == null || npc.isDead() || npc.hasFinished())
							continue;
						npc.restoreHitPoints();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 6000);
	}

	private static final void addRestoreSkillsTask() {
		GameEngine.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					for (Player player : getPlayers()) {
						if (player == null || !player.isRunning())
							continue;
						int ammountTimes = player.getContent().get(ContentType.PRAYER).usingPrayer(0, 8) ? 2 : 1;
						if (player.isResting())
							ammountTimes += 1;
						boolean berserker = player.getContent().get(ContentType.PRAYER).usingPrayer(1, 5);
						for (SkillType skill : SkillType.values()) {
							if (skill == SkillType.SUMMONING || skill == SkillType.SHARED)
								continue;
							for (int time = 0; time < ammountTimes; time++) {
								int currentLevel = player.getSkills().getLevel(skill);
								int normalLevel = player.getSkills().getLevelForXp(skill);
								if (currentLevel > normalLevel) {
									if (skill == SkillType.ATTACK || skill == SkillType.STRENGTH || skill == SkillType.DEFENCE || skill == SkillType.RANGE || skill == SkillType.MAGIC) {
										if (berserker && Utilities.getRandom(100) <= 15)
											continue;
									}
									player.getSkills().setLevelBoost(skill, currentLevel - 1);
								} else if (currentLevel < normalLevel)
									player.getSkills().setLevelBoost(skill, currentLevel + 1);
								else
									break;
							}
						}
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 30000);

	}

	public static final Map<Integer, Region> getRegions() {
		// synchronized (lock) {
		return regions;
		// }
	}

	public static final Region getRegion(int id) {
		return getRegion(id, false);
	}

	public static final Region getRegion(int id, boolean load) {
		// synchronized (lock) {
		Region region = regions.get(id);
		if (region == null) {
			region = new Region(id);
			regions.put(id, region);
		}
		if (load)
			region.checkLoadMap();
		return region;
		// }
	}

	public static final void addPlayer(Player player) {
		players.add(player);
		AntiFlood.add(player.getSession().getIP());
	}

	public static void removePlayer(Player player) {
		players.remove(player);
		AntiFlood.remove(player.getSession().getIP());
	}

	public static final void addNPC(NPC npc) {
		npcs.add(npc);
	}

	public static final void removeNPC(NPC npc) {
		npcs.remove(npc);
	}

	public static final NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		if(NPCOverrideRepository.getNPC(id) != null) {
			try {
				Class<?>[] constructor = new Class[] {
						int.class, WorldTile.class, int.class, boolean.class, boolean.class
				};
				return NPCOverrideRepository.getNPC(id).getConstructor(constructor).newInstance(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public static final NPC spawnNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		return spawnNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}

	/*
	 * check if the entity region changed because moved or teled then we update
	 * it
	 */
	public static final void updateEntityRegion(MobileEntity entity) {
		if (entity.hasFinished()) {
			if (entity instanceof Player)
				getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
			else
				getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
			return;
		}
		int regionId = entity.getRegionId();
		if (entity.getLastRegionId() != regionId) { // map region entity at
			// changed
			if (entity instanceof Player) {
				if (entity.getLastRegionId() > 0)
					getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
				Region region = getRegion(regionId);
				region.addPlayerIndex(entity.getIndex());
				Player player = (Player) entity;
				int musicId = region.getMusicId();
				if (musicId != -1)
					player.getContent().get(ContentType.MUSIC).checkMusic(musicId);
				player.getControlerManager().moved();
				if (player.hasStarted())
					checkControlersAtMove(player);
			} else {
				if (entity.getLastRegionId() > 0)
					getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
				getRegion(regionId).addNPCIndex(entity.getIndex());
			}
			entity.checkMultiArea();
			entity.setLastRegionId(regionId);
		} else {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				player.getControlerManager().moved();
				if (player.hasStarted())
					checkControlersAtMove(player);
			}
			entity.checkMultiArea();
		}
	}

	private static void checkControlersAtMove(Player player) {

	}

	/*
	 * checks clip
	 */
	public static boolean canMoveNPC(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if (getMask(plane, tileX, tileY) != 0)
					return false;
		return true;
	}

	/*
	 * checks clip
	 */
	public static boolean isNotCliped(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if ((getMask(plane, tileX, tileY) & 2097152) != 0)
					return false;
		return true;
	}

	public static int getMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMask(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static void setMask(int plane, int x, int y, int mask) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
	}

	public static int getRotation(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return 0;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipedOnlyMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMaskClipedOnly(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final boolean checkProjectileStep(int plane, int x, int y, int dir, int size) {
		int xOffset = Utilities.DIRECTION_DELTA_X[dir];
		int yOffset = Utilities.DIRECTION_DELTA_Y[dir];
		if (size == 1) {
			int mask = getClipedOnlyMask(plane, x + Utilities.DIRECTION_DELTA_X[dir], y + Utilities.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0)
				return (mask & 0x42240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (mask & 0x60240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (mask & 0x40a40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (mask & 0x48240000) == 0;
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			if (xOffset == -1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
			if (xOffset == 1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
			if (xOffset == -1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
			if (xOffset == 1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0 || (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0 || (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			}
		}
		return true;
	}

	public static final boolean checkWalkStep(int plane, int x, int y, int dir, int size) {
		int xOffset = Utilities.DIRECTION_DELTA_X[dir];
		int yOffset = Utilities.DIRECTION_DELTA_Y[dir];
		int rotation = getRotation(plane, x + xOffset, y + yOffset);
		if (rotation != 0) {
			for (int rotate = 0; rotate < (4 - rotation); rotate++) {
				int fakeChunckX = xOffset;
				int fakeChunckY = yOffset;
				xOffset = fakeChunckY;
				yOffset = 0 - fakeChunckX;
			}
		}

		if (size == 1) {
			int mask = getMask(plane, x + Utilities.DIRECTION_DELTA_X[dir], y + Utilities.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0)
				return (mask & 0x42240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (mask & 0x60240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (mask & 0x40a40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (mask & 0x48240000) == 0;
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0 && (getMask(plane, x - 1, y) & 0x42240000) == 0 && (getMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0 && (getMask(plane, x + 1, y) & 0x60240000) == 0 && (getMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0 && (getMask(plane, x - 1, y) & 0x42240000) == 0 && (getMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0 && (getMask(plane, x + 1, y) & 0x60240000) == 0 && (getMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0)
				return (getMask(plane, x - 1, y) & 0x43a40000) == 0 && (getMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (getMask(plane, x + 2, y) & 0x60e40000) == 0 && (getMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (getMask(plane, x, y - 1) & 0x43a40000) == 0 && (getMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (getMask(plane, x, y + 2) & 0x4e240000) == 0 && (getMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			if (xOffset == -1 && yOffset == -1)
				return (getMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getMask(plane, x, y - 1) & 0x63e40000) == 0;
			if (xOffset == 1 && yOffset == -1)
				return (getMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getMask(plane, x + 2, y) & 0x78e40000) == 0;
			if (xOffset == -1 && yOffset == 1)
				return (getMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getMask(plane, x, y + 2) & 0x7e240000) == 0;
			if (xOffset == 1 && yOffset == 1)
				return (getMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getMask(plane, x - 1, y) & 0x43a40000) != 0 || (getMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getMask(plane, x + size, y) & 0x60e40000) != 0 || (getMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getMask(plane, x, y - 1) & 0x43a40000) != 0 || (getMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getMask(plane, x, y + size) & 0x4e240000) != 0 || (getMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getMask(plane, x + size, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getMask(plane, x - 1, y + size) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getMask(plane, x + size, y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			}
		}
		return true;
	}

	public static final boolean containsPlayer(String username) {
		for (Player p2 : players) {
			if (p2 == null)
				continue;
			if (p2.getUsername().equals(username))
				return true;
		}
		return false;
	}

	public static Player getPlayer(String username) {
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getUsername().equals(username))
				return player;
		}
		return null;
	}

	public static final Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utilities.formatPlayerNameForDisplay(username);
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getUsername().equalsIgnoreCase(formatedUsername) || player.getDisplayName().equalsIgnoreCase(formatedUsername))
				return player;
		}
		return null;
	}

	public static final MobileEntityList<Player> getPlayers() {
		return players;
	}

	public static final MobileEntityList<NPC> getNPCs() {
		return npcs;
	}

	private World() {

	}

	public static final void safeShutdown(final boolean restart, int delay) {
		if (exiting_start != 0) {
			return;
		}
		exiting_start = Utilities.currentTimeMillis();
		exiting_delay = delay;
		for (Player player : World.getPlayers()) {
			if (player == null || !player.hasStarted() || player.hasFinished()) {
				continue;
			}
			player.getPackets().sendSystemUpdate(delay);
		}
		GameEngine.slowExecutor.schedule(new Runnable() {

			@Override
			public void run() {
				try {
					for (Player player : World.getPlayers()) {
						if (player == null || !player.hasStarted()) {
							continue;
						}
						player.realFinish();
					}
					IPBanL.save();
					if (restart) {
						Launcher.restart();
					} else {
						Launcher.shutdown();
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, delay, TimeUnit.SECONDS);
	}

	/*
	 * by default doesnt changeClipData
	 */
	public static final void spawnTemporaryObject(final GameObject object, long time) {
		spawnTemporaryObject(object, time, false);
	}

	public static final void spawnTemporaryObject(final GameObject object, long time, final boolean clip) {
		final int regionId = object.getRegionId();
		GameObject realMapObject = getRegion(regionId).getRealObject(object);
		// remakes object, has to be done because on static region coords arent
		// same of real
		final GameObject realObject = realMapObject == null ? null : new GameObject(realMapObject.getId(), realMapObject.getType(), realMapObject.getRotation(), object.getX(), object.getY(), object.getPlane());
		spawnObject(object, clip);
		final int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		final int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		if (realObject != null && clip)
			getRegion(regionId).removeMapObject(realObject, baseLocalX, baseLocalY);
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeObject(object);
					if (clip) {
						getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
						if (realObject != null) {
							int baseLocalX = object.getX() - ((regionId >> 8) * 64);
							int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
							getRegion(regionId).addMapObject(realObject, baseLocalX, baseLocalY);
						}
					}
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
							continue;
						if (realObject != null)
							p2.getPackets().sendSpawnedObject(realObject);
						else
							p2.getPackets().sendDestroyObject(object);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, time, TimeUnit.MILLISECONDS);
	}

	public static final boolean isSpawnedObject(GameObject object) {
		final int regionId = object.getRegionId();
		GameObject spawnedObject = getRegion(regionId).getSpawnedObject(object);
		if (spawnedObject != null && object.getId() == spawnedObject.getId())
			return true;
		return false;
	}

	public static final boolean removeTemporaryObject(final GameObject object, long time, final boolean clip) {
		final int regionId = object.getRegionId();
		// remakes object, has to be done because on static region coords arent
		// same of real
		final GameObject realObject = object == null ? null : new GameObject(object.getId(), object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
		removeObject(object, clip);
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeRemovedObject(object);
					if (clip) {
						int baseLocalX = object.getX() - ((regionId >> 8) * 64);
						int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
						getRegion(regionId).addMapObject(realObject, baseLocalX, baseLocalY);
					}
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
							continue;
						p2.getPackets().sendSpawnedObject(realObject);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, time, TimeUnit.MILLISECONDS);

		return true;
	}

	public static final void removeObject(GameObject object, boolean clip) {
		int regionId = object.getRegionId();
		getRegion(regionId).addRemovedObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (players) {
			for (Player p2 : players) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendDestroyObject(object);
			}
		}
	}

	public static final GameObject getObject(WorldTile tile) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return getRegion(regionId).getObject(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final GameObject getObject(WorldTile tile, int type) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return getRegion(regionId).getObject(tile.getPlane(), baseLocalX, baseLocalY, type);
	}

	public static final void spawnObject(GameObject object, boolean clip) {
		int regionId = object.getRegionId();
		getRegion(regionId).addObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			getRegion(regionId).addMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (players) {
			for (Player p2 : players) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendSpawnedObject(object);
			}
		}
	}

	public static final void addGroundItem(final Item item, final WorldTile tile) {
		final FloorItem floorItem = new FloorItem(item, tile, null, false, false);
		final Region region = getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		int regionId = tile.getRegionId();
		for (Player player : players) {
			if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId))
				continue;
			player.getPackets().sendGroundItem(floorItem);
		}
	}

	public static final void addGroundItem(final Item item, final WorldTile tile, final Player owner/*
	 * null
	 * for
	 * default
	 */, final boolean underGrave, long hiddenTime/*
	 * default
	 * 3
	 * minutes
	 */, boolean invisible) {
		addGroundItem(item, tile, owner, underGrave, hiddenTime, invisible, false, 180);
	}

	public static final void addGroundItem(final Item item, final WorldTile tile, final Player owner/*
	 * null
	 * for
	 * default
	 */, final boolean underGrave, long hiddenTime/*
	 * default
	 * 3
	 * minutes
	 */, boolean invisible, boolean intoGold) {
		addGroundItem(item, tile, owner, underGrave, hiddenTime, invisible, intoGold, 180);
	}

	public static final void addGroundItem(final Item item, final WorldTile tile, final Player owner/*
	 * null
	 * for
	 * default
	 */, final boolean underGrave, long hiddenTime/*
	 * default
	 * 3
	 * minutes
	 */, boolean invisible, boolean intoGold, final int publicTime) {
		if (intoGold) {
			if (!ItemConstants.isTradeable(item)) {
				int price = item.getDefinitions().getValue();
				if (price <= 0)
					return;
				item.setId(995);
				item.setAmount(price);
			}
		}
		final FloorItem floorItem = new FloorItem(item, tile, owner, owner == null ? false : underGrave, invisible);
		final Region region = getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		if (invisible && hiddenTime != -1) {
			if (owner != null)
				owner.getPackets().sendGroundItem(floorItem);
			GameEngine.slowExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						if (!region.forceGetFloorItems().contains(floorItem))
							return;
						int regionId = tile.getRegionId();
						if (underGrave || !ItemConstants.isTradeable(floorItem) || item.getName().contains("Dr nabanik")) {
							region.forceGetFloorItems().remove(floorItem);
							if (owner != null) {
								if (owner.getMapRegionsIds().contains(regionId) && owner.getPlane() == tile.getPlane())
									owner.getPackets().sendRemoveGroundItem(floorItem);
							}
							return;
						}

						floorItem.setInvisible(false);
						for (Player player : players) {
							if (player == null || player == owner || !player.hasStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId))
								continue;
							player.getPackets().sendGroundItem(floorItem);
						}
						removeGroundItem(floorItem, publicTime);
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			}, hiddenTime, TimeUnit.SECONDS);
			return;
		}
		int regionId = tile.getRegionId();
		for (Player player : players) {
			if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId))
				continue;
			player.getPackets().sendGroundItem(floorItem);
		}
		removeGroundItem(floorItem, publicTime);
	}

	public static final void updateGroundItem(Item item, final WorldTile tile, final Player owner) {
		final FloorItem floorItem = World.getRegion(tile.getRegionId()).getGroundItem(item.getId(), tile, owner);
		if (floorItem == null) {
			addGroundItem(item, tile, owner, false, 360, true);
			return;
		}
		floorItem.setAmount(floorItem.getAmount() + item.getAmount());
		owner.getPackets().sendRemoveGroundItem(floorItem);
		owner.getPackets().sendGroundItem(floorItem);

	}

	private static final void removeGroundItem(final FloorItem floorItem, long publicTime) {
		if (publicTime < 0) {
			return;
		}
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					int regionId = floorItem.getTile().getRegionId();
					Region region = getRegion(regionId);
					if (!region.forceGetFloorItems().contains(floorItem))
						return;
					region.forceGetFloorItems().remove(floorItem);
					for (Player player : World.getPlayers()) {
						if (player == null || !player.hasStarted() || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId))
							continue;
						player.getPackets().sendRemoveGroundItem(floorItem);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, publicTime, TimeUnit.SECONDS);
	}

	public static final boolean removeGroundItem(Player player, FloorItem floorItem) {
		return removeGroundItem(player, floorItem, true);
	}

	public static final boolean removeGroundItem(Player player, FloorItem floorItem, boolean add) {
		int regionId = floorItem.getTile().getRegionId();
		Region region = getRegion(regionId);
		if (!region.forceGetFloorItems().contains(floorItem))
			return false;
		if (player.getContent().get(ContentType.INVENTORY).getFreeSlots() == 0)
			return false;
		region.forceGetFloorItems().remove(floorItem);
		if (add)
			player.getContent().get(ContentType.INVENTORY).addItem(floorItem.getId(), floorItem.getAmount());
		if (floorItem.isInvisible() || floorItem.isGrave()) {
			player.getPackets().sendRemoveGroundItem(floorItem);
			return true;
		} else {
			for (Player p2 : World.getPlayers()) {
				if (p2 == null || !p2.hasStarted() || p2.hasFinished() || p2.getPlane() != floorItem.getTile().getPlane() || !p2.getMapRegionsIds().contains(regionId))
					continue;
				p2.getPackets().sendRemoveGroundItem(floorItem);
			}
			return true;
		}
	}

	public static final void sendObjectAnimation(GameObject object, Animation animation) {
		sendObjectAnimation(null, object, animation);
	}

	public static final void sendObjectAnimation(MobileEntity creator, GameObject object, Animation animation) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object))
					continue;
				player.getPackets().sendObjectAnimation(object, animation);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(object))
						continue;
					player.getPackets().sendObjectAnimation(object, animation);
				}
			}
		}
	}

	public static final void sendGraphics(MobileEntity creator, Graphics graphics, WorldTile tile) {
		if (creator == null) {
			for (Player player : World.getPlayers()) {
				if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile))
					continue;
				player.getPackets().sendGraphics(graphics, tile);
			}
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.hasStarted() || player.hasFinished() || !player.withinDistance(tile))
						continue;
					player.getPackets().sendGraphics(graphics, tile);
				}
			}
		}
	}

	public static final void sendProjectile(MobileEntity shooter, WorldTile startTile, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(WorldTile shooter, MobileEntity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : receiver.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(MobileEntity shooter, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
			}
		}
	}

	public static final void sendProjectile(MobileEntity shooter, MobileEntity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.hasStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				int size = shooter.getSize();
				player.getPackets().sendProjectile(receiver, new WorldTile(shooter.getCoordFaceX(size), shooter.getCoordFaceY(size), shooter.getPlane()), receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, size);
			}
		}
	}

	public static final boolean isMultiArea(WorldTile tile) {
		return false;
	}

	public static final boolean isPvpArea(WorldTile tile) {
		return Wilderness.isAtWild(tile);
	}

	public static void destroySpawnedObject(GameObject object, boolean clip) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		GameObject realMapObject = getRegion(regionId).getRealObject(object);

		World.getRegion(regionId).removeObject(object);
		if (clip)
			World.getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
				continue;
			if (realMapObject != null)
				p2.getPackets().sendSpawnedObject(realMapObject);
			else
				p2.getPackets().sendDestroyObject(object);
		}
	}

	public static void destroySpawnedObject(GameObject object) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		World.getRegion(regionId).removeObject(object);
		World.getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.hasStarted() || p2.hasFinished() || !p2.getMapRegionsIds().contains(regionId))
				continue;
			p2.getPackets().sendDestroyObject(object);
		}
	}

	public static final void spawnTempGroundObject(final GameObject object, final int replaceId, long time) {
		final int regionId = object.getRegionId();
		GameObject realMapObject = getRegion(regionId).getRealObject(object);
		final GameObject realObject = realMapObject == null ? null : new GameObject(realMapObject.getId(), realMapObject.getType(), realMapObject.getRotation(), object.getX(), object.getY(), object.getPlane());
		spawnObject(object, false);
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					getRegion(regionId).removeObject(object);
					addGroundItem(new Item(replaceId), object, null, false, 180, false);
					for (Player p2 : players) {
						if (p2 == null || !p2.hasStarted() || p2.hasFinished() || p2.getPlane() != object.getPlane() || !p2.getMapRegionsIds().contains(regionId))
							continue;
						if (realObject != null)
							p2.getPackets().sendSpawnedObject(realObject);
						else
							p2.getPackets().sendDestroyObject(object);
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, time, TimeUnit.MILLISECONDS);
	}

	public static void sendWorldMessage(String message, boolean forStaff) {
		for (Player p : World.getPlayers()) {
			if (p == null || !p.isRunning() || p.isYellOff() || (forStaff && p.getRank().equals(Rank.PLAYER)))
				continue;
			p.getPackets().sendGameMessage(message);
		}
	}

	public static final void sendProjectile(GameObject object, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startOffset) {
		for (Player pl : players) {
			if (pl == null || !pl.withinDistance(object, 20))
				continue;
			pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
		}
	}

}
