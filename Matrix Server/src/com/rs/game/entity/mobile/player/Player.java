package com.rs.game.entity.mobile.player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.engine.GameEngine;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.item.FloorItem;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.Animation;
import com.rs.game.entity.mobile.ForceTalk;
import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.Hit;
import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.Hit.HitLook;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.content.AbstractContentManager;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.FriendsChatManager;
import com.rs.game.entity.mobile.player.content.global.InterfaceManager;
import com.rs.game.entity.mobile.player.content.global.LocalNPCUpdate;
import com.rs.game.entity.mobile.player.content.global.LocalPlayerUpdate;
import com.rs.game.entity.mobile.player.content.global.OwnedObjectManager;
import com.rs.game.entity.mobile.player.content.type.CombatDefinitions;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.game.entity.mobile.player.content.type.skills.Skills;
import com.rs.game.entity.mobile.player.controller.ControllerManager;
import com.rs.game.entity.mobile.player.cutscene.CutscenesManager;
import com.rs.game.entity.mobile.player.dialogue.DialogueManager;
import com.rs.game.world.World;
import com.rs.networking.Session;
import com.rs.networking.decoders.world.LogicPacket;
import com.rs.networking.decoders.world.PublicChatMessage;
import com.rs.networking.decoders.world.QuickChatMessage;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.encoders.WorldPacketsEncoder;
import com.rs.networking.io.InputStream;
import com.rs.utilities.IsaacKeyPair;
import com.rs.utilities.Logger;
import com.rs.utilities.SerializableFilesManager;
import com.rs.utilities.Utilities;

public class Player extends MobileEntity {

	public static final int TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;

	private static final long serialVersionUID = 2011932556974180375L;

	// transient stuff
	private transient String username;
	private transient Session session;
	private transient boolean clientLoadedMapRegion;
	private transient int displayMode;
	private transient int screenWidth;
	private transient int screenHeight;
	private transient InterfaceManager interfaceManager;
	private transient DialogueManager dialogueManager;
	private transient CutscenesManager cutscenesManager;
	private transient EventOnDestination eventOnDestination;
	private transient FriendsChatManager currentFriendChat;
	private transient IsaacKeyPair isaacKeyPair;
	public transient int chatType;
	
	// used for packets logic
	private transient ConcurrentLinkedQueue<LogicPacket> packetQueue;

	// used for update
	private transient LocalPlayerUpdate localPlayerUpdate;
	private transient LocalNPCUpdate localNPCUpdate;

	private int temporaryMovementType;
	private boolean updateMovementType;

	// player stages
	private transient boolean started;
	private transient boolean running;

	private transient long packetsDecoderPing;
	private transient boolean resting;
	private transient boolean canPvp;
	private transient boolean cantTrade;
	private transient long lockDelay; 
	private transient long foodDelay;
	private transient long potDelay;
	private transient long boneDelay;
	private transient Runnable closeInterfacesEvent;
	private transient long lastPublicMessage;
	private transient List<Integer> switchItemCache;
	private transient boolean disableEquip;
	private transient boolean spawnsMode;
	private transient boolean invulnerable;
	private transient double hpBoostMultiplier;
	private transient boolean largeSceneView;

	// interface

	// saving stuff
	private String password;
	private String displayName;
	private String lastIP;
	private Rank rank;
	private AbstractContentManager content;
	private ControllerManager controlerManager;
	private byte runEnergy;
	private boolean allowChatEffects;
	private boolean mouseButtons;
	private int privateChatSetup;
	private int friendChatSetup;
	private int skullDelay;
	private int skullId;
	private boolean forceNextMapLoadRefresh;
	private long poisonImmune;
	private long fireImmune;
	private int[] pouches;
	private long displayTime;
	private long muted;
	private long banned;
	private boolean permBanned;
	private boolean filterGame;
	private boolean xpLocked;
	private boolean yellOff;
	
	// game bar status
	private int publicStatus;
	private int clanStatus;
	private int tradeStatus;
	private int assistStatus;

	private String lastMsg;

	// Used for storing recent ips and password
	private ArrayList<String> passwordList = new ArrayList<String>();
	private ArrayList<String> ipList = new ArrayList<String>();

	private String currentFriendChatOwner;
	private int summoningLeftClickOption;
	private List<String> ownedObjectsManagerKeys;

	private String yellColor = "ff0000";

	public Player(String password) {
		super(Settings.START_PLAYER_LOCATION);
		setHitpoints(Settings.START_PLAYER_HITPOINTS);
		this.password = password;
		controlerManager = new ControllerManager();
		setContent(new AbstractContentManager());
		runEnergy = 100;
		allowChatEffects = true;
		mouseButtons = true;
		pouches = new int[4];
		ownedObjectsManagerKeys = new LinkedList<String>();
		passwordList = new ArrayList<String>();
		ipList = new ArrayList<String>();
		setRank(Rank.PLAYER);
	}

	public void init(Session session, String username, int displayMode, int screenWidth, int screenHeight, IsaacKeyPair isaacKeyPair) {
		this.session = session;
		this.username = username;
		this.displayMode = displayMode;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isaacKeyPair = isaacKeyPair;
		interfaceManager = new InterfaceManager(this);
		dialogueManager = new DialogueManager(this);
		localPlayerUpdate = new LocalPlayerUpdate(this);
		localNPCUpdate = new LocalNPCUpdate(this);
		cutscenesManager = new CutscenesManager(this);
		getContent().initialize(this);
		controlerManager.setPlayer(this);
		setDirection(Utilities.getFaceDirection(0, -1));
		temporaryMovementType = -1;
		packetQueue = new ConcurrentLinkedQueue<LogicPacket>();
		switchItemCache = Collections.synchronizedList(new ArrayList<Integer>());
		initEntity();
		packetsDecoderPing = Utilities.currentTimeMillis();
		World.addPlayer(this);
		World.updateEntityRegion(this);
		if (Settings.DEBUG)
			Logger.log(this, "Initiated player: " + username + ", pass: " + password);

		// Do not delete >.>, useful for security purpose. this wont waste that
		// much space..
		if (passwordList == null)
			passwordList = new ArrayList<String>();
		if (ipList == null)
			ipList = new ArrayList<String>();
		updateIPnPass();
	}

	public void setWildernessSkull() {
		skullDelay = 3000; // 30minutes
		skullId = 0;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
	}

	public void setFightPitsSkull() {
		skullDelay = Integer.MAX_VALUE; // infinite
		skullId = 1;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
	}

	public void setSkullInfiniteDelay(int skullId) {
		skullDelay = Integer.MAX_VALUE; // infinite
		this.skullId = skullId;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
	}

	public void removeSkull() {
		skullDelay = -1;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
	}

	public boolean hasSkull() {
		return skullDelay > 0;
	}

	public int setSkullDelay(int delay) {
		return this.skullDelay = delay;
	}

	public void refreshSpawnedItems() {
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && this != item.getOwner() || item.getTile().getPlane() != getPlane())
					continue;
				getPackets().sendRemoveGroundItem(item);
			}
		}
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && this != item.getOwner() || item.getTile().getPlane() != getPlane())
					continue;
				getPackets().sendGroundItem(item);
			}
		}
	}

	public void refreshSpawnedObjects() {
		for (int regionId : getMapRegionsIds()) {
			List<GameObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
			if (spawnedObjects != null) {
				for (GameObject object : spawnedObjects)
					if (object.getPlane() == getPlane())
						getPackets().sendSpawnedObject(object);
			}
			List<GameObject> removedObjects = World.getRegion(regionId).getRemovedObjects();
			if (removedObjects != null) {
				for (GameObject object : removedObjects)
					if (object.getPlane() == getPlane())
						getPackets().sendDestroyObject(object);
			}
		}
	}

	// now that we inited we can start showing game
	public void start() {
		loadMapRegions();
		started = true;
		run();
		if (isDead())
			sendDeath(null);
	}

	public void stopAll() {
		stopAll(true);
	}

	public void stopAll(boolean stopWalk) {
		stopAll(stopWalk, true);
	}

	public void stopAll(boolean stopWalk, boolean stopInterface) {
		stopAll(stopWalk, stopInterface, true);
	}

	// as walk done clientsided
	public void stopAll(boolean stopWalk, boolean stopInterfaces, boolean stopActions) {
		eventOnDestination = null;
		if (stopInterfaces)
			closeInterfaces();
		if (stopWalk)
			resetWalkSteps();
		if (stopActions)
			getContent().get(ContentType.ACTION).forceStop();
		getContent().get(ContentType.COMBATDEF).resetSpells(false);
	}

	@Override
	public void reset(boolean attributes) {
		super.reset(attributes);
		refreshHitPoints();
		getContent().get(ContentType.HINTICON).removeAll();
		getContent().get(ContentType.SKILLS).restoreSkills();
		getContent().get(ContentType.COMBATDEF).resetSpecialAttack();
		getContent().get(ContentType.PRAYER).reset();
		getContent().get(ContentType.COMBATDEF).resetSpells(true);
		resting = false;
		skullDelay = 0;
		foodDelay = 0;
		potDelay = 0;
		poisonImmune = 0;
		fireImmune = 0;
		setRunEnergy(100);
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
	}

	@Override
	public void reset() {
		reset(true);
	}

	public void closeInterfaces() {
		if (interfaceManager.containsScreenInter())
			interfaceManager.closeScreenInterface();
		if (interfaceManager.containsInventoryInter())
			interfaceManager.closeInventoryInterface();
		dialogueManager.finishDialogue();
		if (closeInterfacesEvent != null) {
			closeInterfacesEvent.run();
			closeInterfacesEvent = null;
		}
	}

	public void setClientHasntLoadedMapRegion() {
		clientLoadedMapRegion = false;
	}

	@Override
	public void loadMapRegions() {
		boolean wasAtDynamicRegion = isAtDynamicRegion();
		super.loadMapRegions();
		clientLoadedMapRegion = false;
		if (isAtDynamicRegion()) {
			getPackets().sendDynamicMapRegion(!started);
			if (!wasAtDynamicRegion)
				localNPCUpdate.reset();
		} else {
			getPackets().sendMapRegion(!started);
			if (wasAtDynamicRegion)
				localNPCUpdate.reset();
		}
		forceNextMapLoadRefresh = false;
	}

	public void processLogicPackets() {
		LogicPacket packet;
		while ((packet = packetQueue.poll()) != null) {
			InputStream stream = new InputStream(packet.getData());
			getPacketsDecoder().requestProcess(true, packet.getId(), stream.getLength(), stream);
		}
	}
	
	public WorldPacketsDecoder getPacketsDecoder() {
		return ((WorldPacketsDecoder)getSession().getDecoder());
	}


	@Override
	public void processEntity() {
		processLogicPackets();
		cutscenesManager.process();
		
		if (eventOnDestination != null && eventOnDestination.processEvent(this))
			eventOnDestination = null;
		
		super.processEntity();
		if (getContent().get(ContentType.MUSIC).musicEnded())
			getContent().get(ContentType.MUSIC).replayMusic();
		if (hasSkull()) {
			skullDelay--;
			if (!hasSkull())
				getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		}
		getContent().get(ContentType.CHARGES).process();//you change all these to share process method u retard i want all content done like this and none works without  stp[ bro, just stfu
		getContent().get(ContentType.AURAS).process();
		getContent().get(ContentType.ACTION).process();
		//stop doing bs ho ldon u didnt fix what i have the shit still erros fixed what you h ave,what you had didnt suport type inference, JAVA DOESNT nigga ive done something similar and also, here is a screenshot to prove this method is  pos fsaibggo, faggot, it is impossible the way you DONE it, i told you , it is posisle only _if_ you store the freakin object in the other ones type ,show me
		getContent().get(ContentType.PRAYER).processPrayer();  //i want it like this, stop changing what i want, literally i asked for help to fix what i have lmfao, idc if u think its bad, i.  li, . o foxed  i fixed what you ahve tho, this is waht you had..
		controlerManager.process();

	}

	@Override
	public void processReceivedHits() {
		if (lockDelay > Utilities.currentTimeMillis())
			return;
		super.processReceivedHits();
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || temporaryMovementType != -1 || updateMovementType;
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		temporaryMovementType = -1;
		updateMovementType = false;
		if (!clientHasLoadedMapRegion()) {
			// load objects and items here
			setClientHasLoadedMapRegion();
			refreshSpawnedObjects();
			refreshSpawnedItems();
		}
	}

	public void toogleRun(boolean update) {
		super.setRun(!getRun());
		updateMovementType = true;
		if (update)
			sendRunButtonConfig();
	}

	public void setRunHidden(boolean run) {
		super.setRun(run);
		updateMovementType = true;
	}

	@Override
	public void setRun(boolean run) {
		if (run != getRun()) {
			super.setRun(run);
			updateMovementType = true;
			sendRunButtonConfig();
		}
	}

	public void sendRunButtonConfig() {
		getPackets().sendConfig(173, resting ? 3 : getRun() ? 1 : 0);
	}

	public void restoreRunEnergy() {
		if (getNextRunDirection() == -1 && runEnergy < 100) {
			runEnergy++;
			if (resting && runEnergy < 100)
				runEnergy++;
			getPackets().sendRunEnergy();
		}
	}

	public void run() {
		if (World.exiting_start != 0) {
			int delayPassed = (int) ((Utilities.currentTimeMillis() - World.exiting_start) / 1000);
			getPackets().sendSystemUpdate(World.exiting_delay - delayPassed);
		}
		lastIP = getSession().getIP();
		interfaceManager.sendInterfaces();
		getPackets().sendRunEnergy();
		refreshAllowChatEffects();
		refreshMouseButtons();
		refreshPrivateChatSetup();
		refreshOtherChatsSetup();
		sendRunButtonConfig();
		getPackets().sendGameMessage("Welcome to " + Settings.SERVER_NAME + ".");

		sendDefaultPlayersOptions();
		checkMultiArea();
		getContent().get(ContentType.INVENTORY).init();
		getContent().get(ContentType.EQUIPMENT).init();
		getContent().get(ContentType.COMBATDEF).init();
		getContent().get(ContentType.PRAYER).init();
		getContent().get(ContentType.FRIENDSLIST).init();
		refreshHitPoints();
		getContent().get(ContentType.PRAYER).refreshPrayerPoints();
		getPoison().refresh();
		getPackets().sendConfig(281, 1000); // unlock can't do this on tutorial
		getPackets().sendConfig(1160, -1); // unlock summoning orb
		getPackets().sendConfig(1159, 1);
		getPackets().sendGameBarStages();
		getContent().get(ContentType.MUSIC).init();
		getContent().get(ContentType.EMOTES).refreshListConfigs();
		if (currentFriendChatOwner != null) {
			FriendsChatManager.joinChat(currentFriendChatOwner, this);
			if (currentFriendChat == null) // failed
				currentFriendChatOwner = null;
		}
		running = true;
		updateMovementType = true;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		controlerManager.login(); // checks what to do on login after welcome
		OwnedObjectManager.linkKeys(this);
	}

	public void updateIPnPass() {
		if (getPasswordList().size() > 25)
			getPasswordList().clear();
		if (getIPList().size() > 50)
			getIPList().clear();
		if (!getPasswordList().contains(getPassword()))
			getPasswordList().add(getPassword());
		if (!getIPList().contains(getLastIP()))
			getIPList().add(getLastIP());
		return;
	}

	public void sendDefaultPlayersOptions() {
		getPackets().sendPlayerOption("Follow", 2, false);
		getPackets().sendPlayerOption("Trade with", 4, false);
		getPackets().sendPlayerOption("Req Assist", 5, false);
	}

	@Override
	public void checkMultiArea() {
		if (!started)
			return;
		boolean isAtMultiArea = isForceMultiArea() ? true : World.isMultiArea(this);
		if (isAtMultiArea && !isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 1);
		} else if (!isAtMultiArea && isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 0);
		}
	}

	public void logout() {
		if (!running) {
			return;
		}
		long currentTime = Utilities.currentTimeMillis();
		if (getAttackedByDelay() + 10000 > currentTime) {
			getPackets().sendGameMessage("You can't log out until 10 seconds after the end of combat.");
			return;
		}
		if (getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= currentTime) {
			getPackets().sendGameMessage("You can't log out while performing an emote.");
			return;
		}
		if (lockDelay >= currentTime) {
			getPackets().sendGameMessage("You can't log out while performing an action.");
			return;
		}
		getPackets().sendLogout();
		running = false;
	}

	public void forceLogout() {
		getPackets().sendLogout();
		running = false;
		realFinish();
	}

	private transient boolean finishing;

	@Override
	public void finish() {
		finish(0);
	}

	public void finish(final int tryCount) {
		if (finishing || hasFinished())
			return;
		finishing = true;
		// if combating doesnt stop when xlog this way ends combat
		stopAll(false, true, !(getContent().get(ContentType.ACTION).getAction() instanceof PlayerCombat));
		long currentTime = Utilities.currentTimeMillis();
		if ((getAttackedByDelay() + 10000 > currentTime && tryCount < 6) || getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= currentTime || lockDelay >= currentTime) {
			GameEngine.slowExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						packetsDecoderPing = Utilities.currentTimeMillis();
						finishing = false;
						finish(tryCount + 1);
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			}, 10, TimeUnit.SECONDS);
			return;
		}
		realFinish();
	}

	public void realFinish() {
		if (hasFinished())
			return;
		stopAll();
		cutscenesManager.logout();
		controlerManager.logout(); // checks what to do on before logout for
		// login
		running = false;
		getContent().get(ContentType.FRIENDSLIST).sendFriendsMyStatus(false);
		if (currentFriendChat != null)
			currentFriendChat.leaveChat(this, true);
		setFinished(true);
		session.setDecoder(-1);
		SerializableFilesManager.savePlayer(this);
		World.updateEntityRegion(this);
		World.removePlayer(this);
		if (Settings.DEBUG)
			Logger.log(this, "Finished Player: " + username + ", pass: " + password);
	}

	@Override
	public boolean restoreHitPoints() {
		boolean update = super.restoreHitPoints();
		if (update) {
			if (getContent().get(ContentType.PRAYER).usingPrayer(0, 9))
				super.restoreHitPoints();
			if (resting)
				super.restoreHitPoints();
			refreshHitPoints();
		}
		return update;
	}

	public void refreshHitPoints() {
		getPackets().sendConfigByFile(7198, getHitpoints());
	}

	@Override
	public void removeHitpoints(Hit hit) {
		super.removeHitpoints(hit);
		refreshHitPoints();
	}

	@Override
	public int getMaxHitpoints() {
		return getContent().get(ContentType.SKILLS).getSkill(SkillType.HITPOINTS).getLevel() * 10 + getContent().get(ContentType.EQUIPMENT).getEquipmentHpIncrease();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public ArrayList<String> getPasswordList() {
		return passwordList;
	}

	public ArrayList<String> getIPList() {
		return ipList;
	}


	public int getMessageIcon() {
		return rank.getIconId();
	}

	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	public boolean isRunning() {
		return running;
	}

	public String getDisplayName() {
		if (displayName != null)
			return displayName;
		return Utilities.formatPlayerNameForDisplay(username);
	}

	public boolean hasDisplayName() {
		return displayName != null;
	}
	
	public int getTemporaryMoveType() {
		return temporaryMovementType;
	}

	public void setTemporaryMoveType(int temporaryMovementType) {
		this.temporaryMovementType = temporaryMovementType;
	}

	public LocalPlayerUpdate getLocalPlayerUpdate() {
		return localPlayerUpdate;
	}

	public LocalNPCUpdate getLocalNPCUpdate() {
		return localNPCUpdate;
	}

	public int getDisplayMode() {
		return displayMode;
	}

	public InterfaceManager getInterfaceManager() {
		return interfaceManager;
	}

	public void setPacketsDecoderPing(long packetsDecoderPing) {
		this.packetsDecoderPing = packetsDecoderPing;
	}

	public long getPacketsDecoderPing() {
		return packetsDecoderPing;
	}

	public Session getSession() {
		return session;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public boolean clientHasLoadedMapRegion() {
		return clientLoadedMapRegion;
	}

	public void setClientHasLoadedMapRegion() {
		clientLoadedMapRegion = true;
	}

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}
	
	public byte getRunEnergy() {
		return runEnergy;
	}

	public void drainRunEnergy() {
		setRunEnergy(runEnergy - 1);
	}

	public void setRunEnergy(int runEnergy) {
		this.runEnergy = (byte) runEnergy;
		getPackets().sendRunEnergy();
	}

	public boolean isResting() {
		return resting;
	}

	public void setResting(boolean resting) {
		this.resting = resting;
		sendRunButtonConfig();
	}

	public void setEventOnDestination(EventOnDestination coordsEvent) {
		this.eventOnDestination = coordsEvent;
	}

	public DialogueManager getDialogueManager() {
		return dialogueManager;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	public void sendSoulSplit(final Hit hit, final MobileEntity user) {
		final Player target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		user.heal(hit.getDamage() / 5);
		getContent().get(ContentType.PRAYER).drainPrayer(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
			}
		}, 0);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		if (invulnerable) {
			hit.setDamage(0);
			return;
		}
		if (getContent().get(ContentType.AURAS).usingPenance()) {
			int amount = (int) (hit.getDamage() * 0.2);
			if (amount > 0)
				getContent().get(ContentType.PRAYER).restorePrayer(amount);
		}
		MobileEntity source = hit.getSource();
		if (source == null)
			return;
		getContent().get(ContentType.PRAYER).checkPray(source, hit);
		if (hit.getDamage() >= 200) {
			if (hit.getLook() == HitLook.MELEE_DAMAGE) {
				int reducedDamage = hit.getDamage() * getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_MELEE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
				int reducedDamage = hit.getDamage() * getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_RANGE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
				int reducedDamage = hit.getDamage() * getContent().get(ContentType.COMBATDEF).getBonuses()[CombatDefinitions.ABSORVE_MAGE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			}
		}
		int shieldId = getContent().get(ContentType.EQUIPMENT).getShieldId();
		if (shieldId == 13742) { // elsyian
			if (Utilities.getRandom(100) <= 70)
				hit.setDamage((int) (hit.getDamage() * 0.75));
		} else if (shieldId == 13740) { // divine
			int drain = (int) (Math.ceil(hit.getDamage() * 0.3) / 2);
			if (getContent().get(ContentType.PRAYER).getPrayerpoints() >= drain) {
				hit.setDamage((int) (hit.getDamage() * 0.70));
				getContent().get(ContentType.PRAYER).drainPrayer(drain);
			}
		}
		if (source instanceof Player) {
			final Player p2 = (Player) source;
			getContent().get(ContentType.PRAYER).checkTargetPray(p2, hit);
		} else {
			NPC n = (NPC) source;
			if (n.getId() == 13448)
				sendSoulSplit(hit, n);
		}
	}
	
	public Skills getSkills() {
		return getContent().get(ContentType.SKILLS);
	}

	@Override
	public void sendDeath(final MobileEntity source) {
		setNextAnimation(new Animation(-1));
		if (!controlerManager.sendDeath())
			return;
		lock(7);
		stopAll();
		getContent().get(ContentType.PRAYER).processPrayerOnDeath(source);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					getPackets().sendGameMessage("Oh dear, you have died.");
					if (source instanceof Player) {
						Player killer = (Player) source;
						killer.setAttackedByDelay(4);
					}
				} else if (loop == 3) {
					if(source instanceof Player)
						sendItemsOnDeath((Player)source);
					else 
						controlerManager.startControler("DeathEvent");
				} else if (loop == 4) {
					getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public void sendItemsOnDeath(Player killer) {
		if (rank.equals(Rank.ADMIN)) {
			return;
		}
		getContent().get(ContentType.CHARGES).die();
		getContent().get(ContentType.AURAS).removeAura();
		CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<Item>();
		for (int i = 0; i < 14; i++) {
			if (getContent().get(ContentType.EQUIPMENT).getItem(i) != null && getContent().get(ContentType.EQUIPMENT).getItem(i).getId() != -1 && getContent().get(ContentType.EQUIPMENT).getItem(i).getAmount() != -1) {
				containedItems.add(new Item(getContent().get(ContentType.EQUIPMENT).getItem(i).getId(), getContent().get(ContentType.EQUIPMENT).getItem(i).getAmount()));
			}
		}
		for (int i = 0; i < 28; i++) {
			if (getContent().get(ContentType.INVENTORY).getItem(i) != null && getContent().get(ContentType.INVENTORY).getItem(i).getId() != -1 && getContent().get(ContentType.INVENTORY).getItem(i).getAmount() != -1) {
				containedItems.add(new Item(getContent().get(ContentType.INVENTORY).getItem(i).getId(), getContent().get(ContentType.INVENTORY).getItem(i).getAmount()));
			}
		}
		if (containedItems.isEmpty()) {
			return;
		}
		int keptAmount = 0;
		keptAmount = hasSkull() ? 0 : 3;
		if (getContent().get(ContentType.PRAYER).usingPrayer(0, 10) || getContent().get(ContentType.PRAYER).usingPrayer(1, 0)) {
			keptAmount++;
		}
		CopyOnWriteArrayList<Item> keptItems = new CopyOnWriteArrayList<Item>();
		Item lastItem = new Item(1, 1);
		for (int i = 0; i < keptAmount; i++) {
			for (Item item : containedItems) {
				int price = item.getDefinitions().getValue();
				if (price >= lastItem.getDefinitions().getValue()) {
					lastItem = item;
				}
			}
			keptItems.add(lastItem);
			containedItems.remove(lastItem);
			lastItem = new Item(1, 1);
		}
		getContent().get(ContentType.INVENTORY).reset();
		getContent().get(ContentType.EQUIPMENT).reset();
		for (Item item : keptItems) {
			getContent().get(ContentType.INVENTORY).addItem(item);
		}
		for (Item item : containedItems) {
			World.addGroundItem(item, getLastWorldTile(), killer == null ? this : killer, false, 180, true, true);
		}
	}

	@Override
	public int getSize() {
		return getContent().get(ContentType.APPEARANCE).getSize();
	}

	public boolean isCanPvp() {
		return canPvp;
	}

	public void setCanPvp(boolean canPvp) {
		this.canPvp = canPvp;
		getContent().get(ContentType.APPEARANCE).generateAppearenceData();
		getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		getPackets().sendPlayerUnderNPCPriority(canPvp);
	}

	public long getLockDelay() {
		return lockDelay;
	}

	public boolean isLocked() {
		return lockDelay >= Utilities.currentTimeMillis();
	}

	public void lock() {
		lockDelay = Long.MAX_VALUE;
	}

	public void lock(long time) {
		lockDelay = Utilities.currentTimeMillis() + (time * 600);
	}

	public void unlock() {
		lockDelay = 0;
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay) {
		useStairs(emoteId, dest, useDelay, totalDelay, null);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay, final String message) {
		stopAll();
		lock(totalDelay);
		if (emoteId != -1)
			setNextAnimation(new Animation(emoteId));
		if (useDelay == 0)
			setNextWorldTile(dest);
		else {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (isDead())
						return;
					setNextWorldTile(dest);
					if (message != null)
						getPackets().sendGameMessage(message);
				}
			}, useDelay - 1);
		}
	}
	
	public ControllerManager getControlerManager() {
		return controlerManager;
	}

	public void switchMouseButtons() {
		mouseButtons = !mouseButtons;
		refreshMouseButtons();
	}

	public void switchAllowChatEffects() {
		allowChatEffects = !allowChatEffects;
		refreshAllowChatEffects();
	}

	public void refreshAllowChatEffects() {
		getPackets().sendConfig(171, allowChatEffects ? 0 : 1);
	}

	public void refreshMouseButtons() {
		getPackets().sendConfig(170, mouseButtons ? 0 : 1);
	}

	public void refreshPrivateChatSetup() {
		getPackets().sendConfig(287, privateChatSetup);
	}

	public void refreshOtherChatsSetup() {
		int value = friendChatSetup << 6;
		getPackets().sendConfig(1438, value);
	}

	public void setPrivateChatSetup(int privateChatSetup) {
		this.privateChatSetup = privateChatSetup;
	}

	public void setFriendChatSetup(int friendChatSetup) {
		this.friendChatSetup = friendChatSetup;
	}

	public int getPrivateChatSetup() {
		return privateChatSetup;
	}

	public boolean isForceNextMapLoadRefresh() {
		return forceNextMapLoadRefresh;
	}

	public void setForceNextMapLoadRefresh(boolean forceNextMapLoadRefresh) {
		this.forceNextMapLoadRefresh = forceNextMapLoadRefresh;
	}

	/*
	 * do not use this, only used by pm
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void addPotDelay(long time) {
		potDelay = time + Utilities.currentTimeMillis();
	}

	public long getPotDelay() {
		return potDelay;
	}

	public void addFoodDelay(long time) {
		foodDelay = time + Utilities.currentTimeMillis();
	}

	public long getFoodDelay() {
		return foodDelay;
	}

	public long getBoneDelay() {
		return boneDelay;
	}

	public void addBoneDelay(long time) {
		boneDelay = time + Utilities.currentTimeMillis();
	}

	public void addPoisonImmune(long time) {
		poisonImmune = time + Utilities.currentTimeMillis();
		getPoison().reset();
	}

	public long getPoisonImmune() {
		return poisonImmune;
	}

	public void addFireImmune(long time) {
		fireImmune = time + Utilities.currentTimeMillis();
	}

	public long getFireImmune() {
		return fireImmune;
	}

	@Override
	public void heal(int ammount, int extra) {
		super.heal(ammount, extra);
		refreshHitPoints();
	}

	public void setCloseInterfacesEvent(Runnable closeInterfacesEvent) {
		this.closeInterfacesEvent = closeInterfacesEvent;
	}

	public long getMuted() {
		return muted;
	}

	public void setMuted(long muted) {
		this.muted = muted;
	}

	public boolean isPermBanned() {
		return permBanned;
	}

	public void setPermBanned(boolean permBanned) {
		this.permBanned = permBanned;
	}

	public long getBanned() {
		return banned;
	}

	public void setBanned(long banned) {
		this.banned = banned;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}

	public int[] getPouches() {
		return pouches;
	}

	public String getLastIP() {
		return lastIP;
	}

	public String getLastHostname() {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(getLastIP());
			String hostname = addr.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isUpdateMovementType() {
		return updateMovementType;
	}

	public long getLastPublicMessage() {
		return lastPublicMessage;
	}

	public void setLastPublicMessage(long lastPublicMessage) {
		this.lastPublicMessage = lastPublicMessage;
	}

	public CutscenesManager getCutscenesManager() {
		return cutscenesManager;
	}

	public void kickPlayerFromFriendsChannel(String name) {
		if (currentFriendChat == null)
			return;
		currentFriendChat.kickPlayerFromChat(this, name);
	}

	public void sendFriendsChannelMessage(String message) {
		if (currentFriendChat == null)
			return;
		currentFriendChat.sendMessage(this, message);
	}

	public void sendFriendsChannelQuickMessage(QuickChatMessage message) {
		if (currentFriendChat == null)
			return;
		currentFriendChat.sendQuickMessage(this, message);
	}

	public void sendPublicChatMessage(PublicChatMessage message) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player p = World.getPlayers().get(playerIndex);
				if (p == null || !p.hasStarted() || p.hasFinished() || p.getLocalPlayerUpdate().getLocalPlayers()[getIndex()] == null)
					continue;
				p.getPackets().sendPublicMessage(this, message);
			}
		}
	}
	
	public void setSkullId(int skullId) {
		this.skullId = skullId;
	}

	public int getSkullId() {
		return skullId;
	}

	public boolean isFilterGame() {
		return filterGame;
	}

	public void setFilterGame(boolean filterGame) {
		this.filterGame = filterGame;
	}

	public void addLogicPacketToQueue(LogicPacket packet) {
		for (LogicPacket p : packetQueue) {
			if (p.getId() == packet.getId()) {
				packetQueue.remove(p);
				break;
			}
		}
		packetQueue.add(packet);
	}

	public void setTeleBlockDelay(long teleDelay) {
		getTemporaryAttributtes().put("TeleBlocked", teleDelay + Utilities.currentTimeMillis());
	}

	public long getTeleBlockDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("TeleBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public void setPrayerDelay(long teleDelay) {
		getTemporaryAttributtes().put("PrayerBlocked", teleDelay + Utilities.currentTimeMillis());
		getContent().get(ContentType.PRAYER).closeAllPrayers();
	}

	public long getPrayerDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("PrayerBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public FriendsChatManager getCurrentFriendChat() {
		return currentFriendChat;
	}

	public void setCurrentFriendChat(FriendsChatManager currentFriendChat) {
		this.currentFriendChat = currentFriendChat;
	}

	public String getCurrentFriendChatOwner() {
		return currentFriendChatOwner;
	}

	public void setCurrentFriendChatOwner(String currentFriendChatOwner) {
		this.currentFriendChatOwner = currentFriendChatOwner;
	}

	public int getSummoningLeftClickOption() {
		return summoningLeftClickOption;
	}

	public void setSummoningLeftClickOption(int summoningLeftClickOption) {
		this.summoningLeftClickOption = summoningLeftClickOption;
	}

	public List<Integer> getSwitchItemCache() {
		return switchItemCache;
	}

	public int getMovementType() {
		if (getTemporaryMoveType() != -1)
			return getTemporaryMoveType();
		return getRun() ? RUN_MOVE_TYPE : WALK_MOVE_TYPE;
	}

	public List<String> getOwnedObjectManagerKeys() {
		if (ownedObjectsManagerKeys == null) // temporary
			ownedObjectsManagerKeys = new LinkedList<String>();
		return ownedObjectsManagerKeys;
	}

	public boolean hasInstantSpecial(final int weaponId) {
		switch (weaponId) {
		case 4153:
		case 15486:
		case 22207:
		case 22209:
		case 22211:
		case 22213:
		case 1377:
		case 13472:
		case 35:// Excalibur
		case 8280:
		case 14632:
			return true;
		default:
			return false;
		}
	}

	public void performInstantSpecial(final int weaponId) {
		int specAmt = PlayerCombat.getSpecialAmmount(weaponId);
		if (getContent().get(ContentType.COMBATDEF).hasRingOfVigour())
			specAmt *= 0.9;
		if (getContent().get(ContentType.COMBATDEF).getSpecialAttackPercentage() < specAmt) {
			getPackets().sendGameMessage("You don't have enough power left.");
			getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(0);
			return;
		}
		switch (weaponId) {
		case 4153:
			getContent().get(ContentType.COMBATDEF).setInstantAttack(true);
			getContent().get(ContentType.COMBATDEF).switchUsingSpecialAttack();
			MobileEntity target = (MobileEntity) getTemporaryAttributtes().get("last_target");
			if (target != null && target.getTemporaryAttributtes().get("last_attacker") == this) {
				if (!(getContent().get(ContentType.ACTION).getAction() instanceof PlayerCombat) || ((PlayerCombat) getContent().get(ContentType.ACTION).getAction()).getTarget() != target) {
					getContent().get(ContentType.ACTION).setAction(new PlayerCombat(target));
				}
			}
			break;
		case 1377:
		case 13472:
			setNextAnimation(new Animation(1056));
			setNextGraphics(new Graphics(246));
			setNextForceTalk(new ForceTalk("Raarrrrrgggggghhhhhhh!"));
			int defence = (int) (getSkills().getLevelForXp(SkillType.DEFENCE) * 0.90D);
			int attack = (int) (getSkills().getLevelForXp(SkillType.ATTACK) * 0.90D);
			int range = (int) (getSkills().getLevelForXp(SkillType.RANGE) * 0.90D);
			int magic = (int) (getSkills().getLevelForXp(SkillType.MAGIC) * 0.90D);
			int strength = (int) (getSkills().getLevelForXp(SkillType.STRENGTH) * 1.2D);
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.DEFENCE, defence);
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.ATTACK, attack);
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.RANGE, range);
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.MAGIC, magic);
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.STRENGTH, strength);
			getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(specAmt);
			break;
		case 35:// Excalibur
		case 8280:
		case 14632:
			setNextAnimation(new Animation(1168));
			setNextGraphics(new Graphics(247));
			setNextForceTalk(new ForceTalk("For " + Settings.SERVER_NAME + "!"));
			final boolean enhanced = weaponId == 14632;
			getContent().get(ContentType.SKILLS).setLevelBoost(SkillType.DEFENCE, enhanced ? (int) (getSkills().getLevelForXp(SkillType.DEFENCE) * 1.15D) : (getSkills().getLevel(SkillType.DEFENCE) + 8));
			WorldTasksManager.schedule(new WorldTask() {
				int count = 5;

				@Override
				public void run() {
					if (isDead() || hasFinished() || getHitpoints() >= getMaxHitpoints()) {
						stop();
						return;
					}
					heal(enhanced ? 80 : 40);
					if (count-- == 0) {
						stop();
						return;
					}
				}
			}, 4, 2);
			getContent().get(ContentType.COMBATDEF).desecreaseSpecialAttack(specAmt);
			break;
		}
	}

	public void setDisableEquip(boolean equip) {
		disableEquip = equip;
	}

	public boolean isEquipDisabled() {
		return disableEquip;
	}

	public void addDisplayTime(long i) {
		this.displayTime = i + Utilities.currentTimeMillis();
	}

	public long getDisplayTime() {
		return displayTime;
	}

	public int getPublicStatus() {
		return publicStatus;
	}

	public void setPublicStatus(int publicStatus) {
		this.publicStatus = publicStatus;
	}

	public int getClanStatus() {
		return clanStatus;
	}

	public void setClanStatus(int clanStatus) {
		this.clanStatus = clanStatus;
	}

	public int getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(int tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public int getAssistStatus() {
		return assistStatus;
	}

	public void setAssistStatus(int assistStatus) {
		this.assistStatus = assistStatus;
	}

	public boolean isSpawnsMode() {
		return spawnsMode;
	}

	public void setSpawnsMode(boolean spawnsMode) {
		this.spawnsMode = spawnsMode;
	}

	public IsaacKeyPair getIsaacKeyPair() {
		return isaacKeyPair;
	}

	public boolean isCantTrade() {
		return cantTrade;
	}

	public void setCantTrade(boolean canTrade) {
		this.cantTrade = canTrade;
	}

	public String getYellColor() {
		return yellColor;
	}

	public void setYellColor(String yellColor) {
		this.yellColor = yellColor;
	}

	public boolean isXpLocked() {
		return xpLocked;
	}

	public void setXpLocked(boolean locked) {
		this.xpLocked = locked;
	}

	public boolean isYellOff() {
		return yellOff;
	}

	public void setYellOff(boolean yellOff) {
		this.yellOff = yellOff;
	}

	public void setInvulnerable(boolean invulnerable) {
		this.invulnerable = invulnerable;
	}

	public double getHpBoostMultiplier() {
		return hpBoostMultiplier;
	}

	public void setHpBoostMultiplier(double hpBoostMultiplier) {
		this.hpBoostMultiplier = hpBoostMultiplier;
	}

	public boolean hasLargeSceneView() {
		return largeSceneView;
	}

	public void setLargeSceneView(boolean largeSceneView) {
		this.largeSceneView = largeSceneView;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public AbstractContentManager getContent() {
		return content;
	}

	public void setContent(AbstractContentManager content) {
		this.content = content;
	}
}
