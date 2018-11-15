package com.rs.game.entity.mobile.player.content.global;

import java.util.Iterator;
import com.rs.engine.tasks.WorldTask;
import com.rs.engine.tasks.WorldTasksManager;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.world.World;
import com.rs.utilities.Utilities;

public class OwnedObjectManager {

	private Player player;
	private GameObject[] objects;
	private int count;
	private long cycleTime;
	private long lifeTime;
	private String managerKey;

	public static void processAll() {
		for (OwnedObjectManager object : World.ownedObjects.values())
			object.process();
	}

	public static boolean isPlayerObject(Player player, GameObject object) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext();) {
			OwnedObjectManager manager = World.ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getX() == object.getX() && manager.getCurrentObject().getY() == object.getY() && manager.getCurrentObject().getPlane() == object.getPlane() && manager.getCurrentObject().getId() == object.getId())
				return true;
		}
		return false;
	}

	public static interface ConvertEvent {

		public boolean canConvert(Player player);

	}

	public static boolean convertIntoObject(GameObject object, GameObject toObject, ConvertEvent event) {
		for (OwnedObjectManager manager : World.ownedObjects.values()) {
			if (manager.getCurrentObject().getX() == toObject.getX() && manager.getCurrentObject().getY() == toObject.getY() && manager.getCurrentObject().getPlane() == toObject.getPlane() && manager.getCurrentObject().getId() == object.getId()) {
				if (event != null && !event.canConvert(manager.player))
					return false;
				manager.convertIntoObject(toObject);
				return true;
			}
		}
		return false;
	}

	public static boolean removeObject(Player player, GameObject object) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext();) {
			final OwnedObjectManager manager = World.ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getX() == object.getX() && manager.getCurrentObject().getY() == object.getY() && manager.getCurrentObject().getPlane() == object.getPlane() && manager.getCurrentObject().getId() == object.getId()) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						manager.delete();
					}
				});
				return true;
			}
		}
		return false;
	}

	public static void linkKeys(Player player) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext();) {
			OwnedObjectManager manager = World.ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			manager.player = player;
		}
	}

	public static void addOwnedObjectManager(Player player, GameObject[] objects, long cycleTime) {
		new OwnedObjectManager(player, objects, cycleTime);
	}

	private OwnedObjectManager(Player player, GameObject[] objects, long cycleTime) {
		managerKey = player.getUsername() + "_" + World.keyMaker.getAndIncrement();
		this.cycleTime = cycleTime;
		this.objects = objects;
		this.player = player;
		spawnObject();
		player.getOwnedObjectManagerKeys().add(managerKey);
		World.ownedObjects.put(managerKey, this);
	}

	public static int getObjectsforValue(Player player, int objectId) {
		int count = 0;
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext();) {
			OwnedObjectManager manager = World.ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getId() == objectId)
				count++;
		}
		return count;
	}

	public void reset() {
		for (OwnedObjectManager object : World.ownedObjects.values())
			object.delete();
	}

	public void resetLifeTime() {
		this.lifeTime = Utilities.currentTimeMillis() + cycleTime;
	}

	public boolean forceMoveNextStage() {
		if (count != -1)
			destroyObject(objects[count]);
		count++;
		if (count == objects.length) {
			remove();
			return false;
		}
		spawnObject();
		return true;
	}

	private void spawnObject() {
		World.spawnObject(objects[count], true);
		resetLifeTime();
	}

	public void convertIntoObject(GameObject object) {
		destroyObject(objects[count]);
		objects[count] = object;
		spawnObject();
	}

	private void remove() {
		World.ownedObjects.remove(managerKey);
		if (player != null)
			player.getOwnedObjectManagerKeys().remove(managerKey);
	}

	public void delete() {
		destroyObject(objects[count]);
		remove();
	}

	public void process() {
		if (Utilities.currentTimeMillis() > lifeTime)
			forceMoveNextStage();
	}

	public GameObject getCurrentObject() {
		return objects[count];
	}

	public void destroyObject(GameObject object) {
		World.destroySpawnedObject(object);
	}

}
