package com.rs.game.entity;

import com.rs.engine.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldTile;

@SuppressWarnings("serial")
public class GameObject extends WorldTile {

	private int id;
	private int type;
	private int rotation;
	private int life;

	public GameObject(int id, int type, int rotation, WorldTile tile) {
		super(tile.getX(), tile.getY(), tile.getPlane());
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}
	
	public GameObject(int id, int x, int y, int plane) {
		super(x, y, plane);
		this.id = id;
		this.type = -1;
		this.rotation = -1;
		this.life = -1;
	}

	public GameObject(int id, int type, int rotation, int x, int y, int plane) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	public GameObject(int id, int type, int rotation, int x, int y, int plane, int life) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = life;
	}

	public GameObject(GameObject object) {
		super(object.getX(), object.getY(), object.getPlane());
		this.id = object.id;
		this.type = object.type;
		this.rotation = object.rotation;
		this.life = object.life;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public void decrementObjectLife() {
		this.life--;
	}

	public ObjectDefinitions getDefinitions() {
		return ObjectDefinitions.getObjectDefinitions(id);
	}
	
	public WorldTile getWorldTile() {
		return (WorldTile)this;
	}
}
