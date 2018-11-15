package com.rs.game.entity.mobile.player;

import com.rs.game.WorldTile;

public final class EventOnDestination {

	private WorldTile destination;
	private Runnable event;
	private int sizeX;
	private int sizeY;

	public EventOnDestination(WorldTile destination, Runnable event, int sizeX, int sizeY, int rotation) {
		this.destination = destination;
		this.event = event;
		if (rotation == 1 || rotation == 3) {
			this.sizeX = sizeY;
			this.sizeY = sizeX;
		} else {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}
	}

	public EventOnDestination(WorldTile destination, Runnable event, int sizeX, int sizeY) {
		this(destination, event, sizeX, sizeY, -1);
	}

	public EventOnDestination(WorldTile destination, Runnable event, int size) {
		this(destination, event, size, size);
	}

	/*
	 * returns if done
	 */
	public boolean processEvent(Player player) {
		if (player.getPlane() != destination.getPlane())
			return true;
		int distanceX = player.getX() - destination.getX();
		int distanceY = player.getY() - destination.getY();
		if (distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1)
			return cantReach(player);
		if (player.hasWalkSteps())
			player.resetWalkSteps();
		event.run();
		return true;
	}

	public boolean cantReach(Player player) {
		if (!player.hasWalkSteps() && player.getNextWalkDirection() == -1) {
			player.getPackets().sendGameMessage("You can't reach that.");
			return true;
		}
		return false;
	}
}