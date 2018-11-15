package com.rs.networking.decoders.world.impl;

import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.container.Inventory;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.ObjectHandler;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class ItemOnObjectPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				-1
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		boolean forceRun = stream.readByte128() == 1;
		int itemId = stream.readShortLE128();
		int y = stream.readShortLE128();
		int objectId = stream.readIntV2();
		int interfaceHash = stream.readInt();
		final int interfaceId = interfaceHash >> 16;
		int slot = stream.readShortLE();
		int x = stream.readShort128();
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utilities.currentTimeMillis();
		if (player.getLockDelay() >= currentTime || player.getContent().get(ContentType.EMOTES).getNextEmoteEnd() >= currentTime)
			return;
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		GameObject mapObject = World.getRegion(regionId).getObject(objectId, tile);
		if (mapObject == null || mapObject.getId() != objectId)
			return;
		final GameObject object = !player.isAtDynamicRegion() ? mapObject : new GameObject(objectId, mapObject.getType(), mapObject.getRotation(), x, y, player.getPlane());
		final Item item = player.getContent().get(ContentType.INVENTORY).getItem(slot);
		if (player.isDead() || Utilities.getInterfaceDefinitionsSize() <= interfaceId)
			return;
		if (player.getLockDelay() > Utilities.currentTimeMillis())
			return;
		if (!player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (item == null || item.getId() != itemId)
			return;
		player.stopAll(false); // false
		if (forceRun)
			player.setRun(forceRun);
		switch (interfaceId) {
		case Inventory.INVENTORY_INTERFACE: // inventory
			ObjectHandler.handleItemOnObject(player, object, interfaceId, item);
			break;
		}
	}

}
