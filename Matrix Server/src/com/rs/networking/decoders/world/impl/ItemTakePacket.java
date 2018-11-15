package com.rs.networking.decoders.world.impl;

import com.rs.game.WorldTile;
import com.rs.game.entity.item.FloorItem;
import com.rs.game.entity.mobile.player.EventOnDestination;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ItemTakePacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				57
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utilities.currentTimeMillis();
		if (player.getLockDelay() > currentTime)
			// || player.getFreezeDelay() >= currentTime)
			return;
		int y = stream.readUnsignedShort();
		int x = stream.readUnsignedShortLE();
		final int id = stream.readUnsignedShort();
		boolean forceRun = stream.read128Byte() == 1;
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
		if (item == null)
			return;
		player.stopAll(false);
		if (forceRun)
			player.setRun(forceRun);
		player.setEventOnDestination(new EventOnDestination(tile, new Runnable() {
			@Override
			public void run() {
				final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
				if (item == null)
					return;
				 player.setNextFaceWorldTile(tile);
				player.addWalkSteps(tile.getX(), tile.getY(), 1);
				World.removeGroundItem(player, item);
			}
		}, 1, 1));
	}

}
