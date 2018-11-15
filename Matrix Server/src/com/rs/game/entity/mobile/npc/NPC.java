package com.rs.game.entity.mobile.npc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.engine.GameEngine;
import com.rs.engine.cache.loaders.ItemDefinitions;
import com.rs.engine.cache.loaders.NPCDefinitions;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.WorldTile;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.Hit;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.Hit.HitLook;
import com.rs.game.entity.mobile.npc.combat.NPCBonuses;
import com.rs.game.entity.mobile.npc.combat.NPCCombat;
import com.rs.game.entity.mobile.npc.combat.NPCCombatDefinitions;
import com.rs.game.entity.mobile.npc.combat.NPCCombatDefinitionsL;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.controller.impl.Wilderness;
import com.rs.game.world.World;
import com.rs.utilities.Logger;
import com.rs.utilities.MapAreas;
import com.rs.utilities.Utilities;

public class NPC extends MobileEntity implements Serializable {

	private static final long serialVersionUID = -4794678936277614443L;

	private int id;
	private WorldTile respawnTile;
	private int mapAreaNameHash;
	private boolean canBeAttackFromOutOfArea;
	private boolean randomwalk;
	private int[] bonuses; // 0 stab, 1 slash, 2 crush,3 mage, 4 range, 5 stab
	// def, blahblah till 9
	private boolean spawned;
	private transient NPCCombat combat;
	public WorldTile forceWalk;

	private long lastAttackedByTarget;
	private boolean cantInteract;
	private int capDamage;
	private int lureDelay;
	private boolean cantFollowUnderCombat;
	private boolean forceAgressive;
	private int forceTargetDistance;
	private boolean forceFollowClose;
	private boolean forceMultiAttacked;
	private boolean noDistanceCheck;

	// npc masks
	private transient Transformation nextTransformation;
	// name changing masks
	private String name;
	private transient boolean changedName;
	private int combatLevel;
	private transient boolean changedCombatLevel;
	private transient boolean locked;

	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		this(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}
	
	public int[] getNPCIds() {
		return null;
	}

	/*
	 * creates and adds npc
	 */
	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(tile);
		this.id = id;
		this.respawnTile = new WorldTile(tile);
		this.mapAreaNameHash = mapAreaNameHash;
		this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
		this.setSpawned(spawned);
		combatLevel = -1;
		setHitpoints(getMaxHitpoints());
		setDirection(getRespawnDirection());
		setRandomWalk((getDefinitions().walkMask & 0x2) != 0 || forceRandomWalk(id));
		bonuses = NPCBonuses.getBonuses(id);
		combat = new NPCCombat(this);
		capDamage = -1;
		lureDelay = 12000;
		// npc is inited on creating instance
		initEntity();
		World.addNPC(this);
		World.updateEntityRegion(this);
		// npc is started on creating instance
		loadMapRegions();
		checkMultiArea();
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || nextTransformation != null || changedCombatLevel || changedName;
	}

	public void transformIntoNPC(int id) {
		setNPC(id);
		nextTransformation = new Transformation(id);
	}

	public void setNPC(int id) {
		this.id = id;
		bonuses = NPCBonuses.getBonuses(id);
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		nextTransformation = null;
		changedCombatLevel = false;
		changedName = false;
	}

	public int getMapAreaNameHash() {
		return mapAreaNameHash;
	}

	public void setCanBeAttackFromOutOfArea(boolean b) {
		canBeAttackFromOutOfArea = b;
	}

	public boolean canBeAttackFromOutOfArea() {
		return canBeAttackFromOutOfArea;
	}

	public NPCDefinitions getDefinitions() {
		return NPCDefinitions.getNPCDefinitions(id);
	}

	public NPCCombatDefinitions getCombatDefinitions() {
		return NPCCombatDefinitionsL.getNPCCombatDefinitions(id);
	}

	@Override
	public int getMaxHitpoints() {
		return getCombatDefinitions().getHitpoints();
	}

	public int getId() {
		return id;
	}

	public void processNPC() {
		if (isDead() || locked)
			return;
		if (!combat.process()) { // if not under combat
			if (!isForceWalking()) {// combat still processed for attack delay
				// go down
				// random walk
				if (!cantInteract) {
					if (!checkAgressivity()) {
						if (getFreezeDelay() < Utilities.currentTimeMillis()) {
							if (((hasRandomWalk()) && World.getRotation(getPlane(), getX(), getY()) == 0) // temporary
									// fix
									&& Math.random() * 1000.0 < 100.0) {
								int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
								int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
								resetWalkSteps();
								if (getMapAreaNameHash() != -1) {
									if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
										forceWalkRespawnTile();
										return;
									}
									addWalkSteps(getX() + moveX, getY() + moveY, 5);
								} else
									addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 5);
							}
						}
					}
				}
			}
		}
		if (isForceWalking()) {
			if (getFreezeDelay() < Utilities.currentTimeMillis()) {
				if (getX() != forceWalk.getX() || getY() != forceWalk.getY()) {
					if (!hasWalkSteps())
						addWalkSteps(forceWalk.getX(), forceWalk.getY(), getSize(), true);
					if (!hasWalkSteps()) { // failing finding route
						setNextWorldTile(new WorldTile(forceWalk)); // force
						// tele
						// to
						// the
						// forcewalk
						// place
						forceWalk = null; // so ofc reached forcewalk place
					}
				} else
					// walked till forcewalk place
					forceWalk = null;
			}
		}
	}

	@Override
	public void processEntity() {
		super.processEntity();
		processNPC();
	}

	public int getRespawnDirection() {
		NPCDefinitions definitions = getDefinitions();
		if (definitions.anInt853 << 32 != 0 && definitions.respawnDirection > 0 && definitions.respawnDirection <= 8)
			return (4 + definitions.respawnDirection) << 11;
		return 0;
	}

	/*
	 * forces npc to random walk even if cache says no, used because of fake
	 * cache information
	 */
	private static boolean forceRandomWalk(int npcId) {
		switch (npcId) {
		case 11226:
			return true;
		case 3341:
		case 3342:
		case 3343:
			return true;
		default:
			return false;
		}
	}

	public void sendSoulSplit(final Hit hit, final MobileEntity user) {
		final NPC target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		user.heal(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
			}
		}, 1);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (capDamage != -1 && hit.getDamage() > capDamage)
			hit.setDamage(capDamage);
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		MobileEntity source = hit.getSource();
		if (source == null)
			return;
		if (source instanceof Player) {
			final Player p2 = (Player) source;
			if (p2.getContent().get(ContentType.PRAYER).hasPrayersOn()) {
				if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 18))
					sendSoulSplit(hit, p2);
				if (hit.getDamage() == 0)
					return;
				if (!p2.getContent().get(ContentType.PRAYER).isBoostedLeech()) {
					if (hit.getLook() == HitLook.MELEE_DAMAGE) {
						if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 19)) {
							p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
							return;
						} else if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 1)) { // sap
							// att
							if (Utilities.getRandom(4) == 0) {
								if (p2.getContent().get(ContentType.PRAYER).reachedMax(0)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(0);
									p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2214));
								p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
								World.sendProjectile(p2, this, 2215, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2216));
									}
								}, 1);
								return;
							}
						} else {
							if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 10)) {
								if (Utilities.getRandom(7) == 0) {
									if (p2.getContent().get(ContentType.PRAYER).reachedMax(3)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(3);
										p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
									World.sendProjectile(p2, this, 2231, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2232));
										}
									}, 1);
									return;
								}
							}
							if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 14)) {
								if (Utilities.getRandom(7) == 0) {
									if (p2.getContent().get(ContentType.PRAYER).reachedMax(7)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(7);
										p2.getPackets().sendGameMessage("Your curse drains Strength from the enemy, boosting your Strength.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
									World.sendProjectile(p2, this, 2248, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2250));
										}
									}, 1);
									return;
								}
							}

						}
					}
					if (hit.getLook() == HitLook.RANGE_DAMAGE) {
						if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 2)) { // sap range
							if (Utilities.getRandom(4) == 0) {
								if (p2.getContent().get(ContentType.PRAYER).reachedMax(1)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(1);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2217));
								p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
								World.sendProjectile(p2, this, 2218, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2219));
									}
								}, 1);
								return;
							}
						} else if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 11)) {
							if (Utilities.getRandom(7) == 0) {
								if (p2.getContent().get(ContentType.PRAYER).reachedMax(4)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(4);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
								World.sendProjectile(p2, this, 2236, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2238));
									}
								});
								return;
							}
						}
					}
					if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
						if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 3)) { // sap mage
							if (Utilities.getRandom(4) == 0) {
								if (p2.getContent().get(ContentType.PRAYER).reachedMax(2)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(2);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2220));
								p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
								World.sendProjectile(p2, this, 2221, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2222));
									}
								}, 1);
								return;
							}
						} else if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 12)) {
							if (Utilities.getRandom(7) == 0) {
								if (p2.getContent().get(ContentType.PRAYER).reachedMax(5)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(5);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
								World.sendProjectile(p2, this, 2240, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2242));
									}
								}, 1);
								return;
							}
						}
					}

					// overall

					if (p2.getContent().get(ContentType.PRAYER).usingPrayer(1, 13)) { // leech defence
						if (Utilities.getRandom(10) == 0) {
							if (p2.getContent().get(ContentType.PRAYER).reachedMax(6)) {
								p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
							} else {
								p2.getContent().get(ContentType.PRAYER).increaseLeechBonus(6);
								p2.getPackets().sendGameMessage("Your curse drains Defence from the enemy, boosting your Defence.", true);
							}
							p2.setNextAnimation(new Animation(12575));
							p2.getContent().get(ContentType.PRAYER).setBoostedLeech(true);
							World.sendProjectile(p2, this, 2244, 35, 35, 20, 5, 0, 0);
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									setNextGraphics(new Graphics(2246));
								}
							}, 1);
							return;
						}
					}
				}
			}
		}

	}

	@Override
	public void reset() {
		super.reset();
		setDirection(getRespawnDirection());
		combat.reset();
		bonuses = NPCBonuses.getBonuses(id); // back to real bonuses
		forceWalk = null;
	}

	@Override
	public void finish() {
		if (hasFinished())
			return;
		setFinished(true);
		World.updateEntityRegion(this);
		World.removeNPC(this);
	}

	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(respawnTile);
			finish();
		}
		GameEngine.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					spawn();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, getCombatDefinitions().getRespawnDelay() * 600, TimeUnit.MILLISECONDS);
	}

	public void deserialize() {
		if (combat == null)
			combat = new NPCCombat(this);
		spawn();
	}

	public void spawn() {
		setFinished(false);
		World.addNPC(this);
		setLastRegionId(0);
		World.updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

	public NPCCombat getCombat() {
		return combat;
	}

	@Override
	public void sendDeath(MobileEntity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		combat.removeTarget();
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
					setLocation(respawnTile);
					finish();
					if (!isSpawned())
						setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public void drop() {
		try {
			Drop[] drops = NPCDrops.getDrops(id);
			if (drops == null)
				return;
			Player killer = getMostDamageReceivedSourcePlayer();
			if (killer == null)
				return;
			Drop[] possibleDrops = new Drop[drops.length];
			int possibleDropsCount = 0;
			for (Drop drop : drops) {
				if (drop.getRate() == 100)
					sendDrop(killer, drop);
				else {
					if ((Utilities.getRandomDouble(99) + 1) <= drop.getRate() * 1.5)
						possibleDrops[possibleDropsCount++] = drop;
				}
			}
			if (possibleDropsCount > 0)
				sendDrop(killer, possibleDrops[Utilities.getRandom(possibleDropsCount - 1)]);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	public void sendDrop(Player player, Drop drop) {
		int size = getSize();
		Item item = ItemDefinitions.getItemDefinitions(drop.getItemId()).isStackable() ? new Item(drop.getItemId(), (drop.getMinAmount() * Settings.DROP_RATE) + Utilities.getRandom(drop.getExtraAmount() * Settings.DROP_RATE)) : new Item(drop.getItemId(), drop.getMinAmount() + Utilities.getRandom(drop.getExtraAmount()));
		World.addGroundItem(item, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()), player, false, 180, true);
	}

	@Override
	public int getSize() {
		return getDefinitions().size;
	}

	public int getMaxHit() {
		return getCombatDefinitions().getMaxHit();
	}

	public int[] getBonuses() {
		return bonuses;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0;
	}

	public WorldTile getRespawnTile() {
		return respawnTile;
	}

	public boolean isUnderCombat() {
		return combat.underCombat();
	}

	@Override
	public void setAttackedBy(MobileEntity target) {
		super.setAttackedBy(target);
		if (target == combat.getTarget())
			lastAttackedByTarget = Utilities.currentTimeMillis();
	}

	public boolean canBeAttackedByAutoRelatie() {
		return Utilities.currentTimeMillis() - lastAttackedByTarget > lureDelay;
	}

	public boolean isForceWalking() {
		return forceWalk != null;
	}

	public void setTarget(MobileEntity entity) {
		if (isForceWalking()) // if force walk not gonna get target
			return;
		combat.setTarget(entity);
		lastAttackedByTarget = Utilities.currentTimeMillis();
	}

	public void removeTarget() {
		if (combat.getTarget() == null)
			return;
		combat.removeTarget();
	}

	public void forceWalkRespawnTile() {
		setForceWalk(respawnTile);
	}

	public void setForceWalk(WorldTile tile) {
		resetWalkSteps();
		forceWalk = tile;
	}

	public boolean hasForceWalk() {
		return forceWalk != null;
	}

	public ArrayList<MobileEntity> getPossibleTargets() {
		ArrayList<MobileEntity> possibleTarget = new ArrayList<MobileEntity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || !player.withinDistance(this, forceTargetDistance > 0 ? forceTargetDistance : (getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE ? 4 : getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.SPECIAL ? 64 : 8)) || (!forceMultiAttacked && (!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this && (player.getAttackedByDelay() > Utilities.currentTimeMillis() || player.getFindTargetDelay() > Utilities.currentTimeMillis())) || !clipedProjectile(player, false) || (!forceAgressive && !Wilderness.isAtWild(this) && player.getSkills().getCombatLevelWithSummoning() >= getCombatLevel() * 2))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}

	public boolean checkAgressivity() {
		// if(!(Wilderness.isAtWild(this) &&
		// getDefinitions().hasAttackOption())) {
		if (!forceAgressive) {
			NPCCombatDefinitions defs = getCombatDefinitions();
			if (defs.getAgressivenessType() == NPCCombatDefinitions.PASSIVE)
				return false;
		}
		// }
		ArrayList<MobileEntity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			MobileEntity target = possibleTarget.get(Utilities.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utilities.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public boolean isCantInteract() {
		return cantInteract;
	}

	public void setCantInteract(boolean cantInteract) {
		this.cantInteract = cantInteract;
		if (cantInteract)
			combat.reset();
	}

	public int getCapDamage() {
		return capDamage;
	}

	public void setCapDamage(int capDamage) {
		this.capDamage = capDamage;
	}

	public int getLureDelay() {
		return lureDelay;
	}

	public void setLureDelay(int lureDelay) {
		this.lureDelay = lureDelay;
	}

	public boolean isCantFollowUnderCombat() {
		return cantFollowUnderCombat;
	}

	public void setCantFollowUnderCombat(boolean canFollowUnderCombat) {
		this.cantFollowUnderCombat = canFollowUnderCombat;
	}

	public Transformation getNextTransformation() {
		return nextTransformation;
	}

	@Override
	public String toString() {
		return getDefinitions().name + " - " + id + " - " + getX() + " " + getY() + " " + getPlane();
	}

	public boolean isForceAgressive() {
		return forceAgressive;
	}

	public void setForceAgressive(boolean forceAgressive) {
		this.forceAgressive = forceAgressive;
	}

	public int getForceTargetDistance() {
		return forceTargetDistance;
	}

	public void setForceTargetDistance(int forceTargetDistance) {
		this.forceTargetDistance = forceTargetDistance;
	}

	public boolean isForceFollowClose() {
		return forceFollowClose;
	}

	public void setForceFollowClose(boolean forceFollowClose) {
		this.forceFollowClose = forceFollowClose;
	}

	public boolean isForceMultiAttacked() {
		return forceMultiAttacked;
	}

	public void setForceMultiAttacked(boolean forceMultiAttacked) {
		this.forceMultiAttacked = forceMultiAttacked;
	}

	public boolean hasRandomWalk() {
		return randomwalk;
	}

	public void setRandomWalk(boolean forceRandomWalk) {
		this.randomwalk = forceRandomWalk;
	}

	public String getCustomName() {
		return name;
	}

	public void setName(String string) {
		this.name = getDefinitions().name.equals(string) ? null : string;
		changedName = true;
	}

	public int getCustomCombatLevel() {
		return combatLevel;
	}

	public int getCombatLevel() {
		return combatLevel >= 0 ? combatLevel : getDefinitions().combatLevel;
	}

	public String getName() {
		return name != null ? name : getDefinitions().name;
	}

	public void setCombatLevel(int level) {
		combatLevel = getDefinitions().combatLevel == level ? -1 : level;
		changedCombatLevel = true;
	}

	public boolean hasChangedName() {
		return changedName;
	}

	public boolean hasChangedCombatLevel() {
		return changedCombatLevel;
	}

	public WorldTile getMiddleWorldTile() {
		int size = getSize();
		return new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}

	public boolean isSpawned() {
		return spawned;
	}

	public void setSpawned(boolean spawned) {
		this.spawned = spawned;
	}

	public boolean isNoDistanceCheck() {
		return noDistanceCheck;
	}

	public void setNoDistanceCheck(boolean noDistanceCheck) {
		this.noDistanceCheck = noDistanceCheck;
	}

	public boolean withinDistance(Player tile, int distance) {
		return super.withinDistance(tile, distance);
	}

	/**
	 * Gets the locked.
	 * 
	 * @return The locked.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets the locked.
	 * 
	 * @param locked
	 *            The locked to set.
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public boolean hasStarted() {
		return true;
	}
}
