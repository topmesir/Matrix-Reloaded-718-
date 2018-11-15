package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.container.Inventory;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class SwitchInterfaceItemPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				76
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		stream.readShortLE128();
		int fromInterfaceHash = stream.readIntV1();
		int toInterfaceHash = stream.readInt();
		int fromSlot = stream.readUnsignedShort();
		int toSlot = stream.readUnsignedShortLE128();
		stream.readUnsignedShortLE();

		int toInterfaceId = toInterfaceHash >> 16;
		int toComponentId = toInterfaceHash - (toInterfaceId << 16);
		int fromInterfaceId = fromInterfaceHash >> 16;
		int fromComponentId = fromInterfaceHash - (fromInterfaceId << 16);

		if (Utilities.getInterfaceDefinitionsSize() <= fromInterfaceId || Utilities.getInterfaceDefinitionsSize() <= toInterfaceId)
			return;
		if (!player.getInterfaceManager().containsInterface(fromInterfaceId) || !player.getInterfaceManager().containsInterface(toInterfaceId))
			return;
		if (fromComponentId != -1 && Utilities.getInterfaceDefinitionsComponentsSize(fromInterfaceId) <= fromComponentId)
			return;
		if (toComponentId != -1 && Utilities.getInterfaceDefinitionsComponentsSize(toInterfaceId) <= toComponentId)
			return;
		if (fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 0 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 0) {
			toSlot -= 28;
			if (toSlot < 0 || toSlot >= player.getContent().get(ContentType.INVENTORY).getItemsContainerSize() || fromSlot >= player.getContent().get(ContentType.INVENTORY).getItemsContainerSize())
				return;
			player.getContent().get(ContentType.INVENTORY).switchItem(fromSlot, toSlot);
		} else if (fromInterfaceId == 763 && fromComponentId == 0 && toInterfaceId == 763 && toComponentId == 0) {
			if (toSlot >= player.getContent().get(ContentType.INVENTORY).getItemsContainerSize() || fromSlot >= player.getContent().get(ContentType.INVENTORY).getItemsContainerSize())
				return;
			player.getContent().get(ContentType.INVENTORY).switchItem(fromSlot, toSlot);
		} else if (fromInterfaceId == 762 && toInterfaceId == 762) {
			player.getContent().get(ContentType.BANK).switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
		}
		if (Settings.DEBUG)
			System.out.println("Switch item " + fromInterfaceId + ", " + fromSlot + ", " + toSlot);
	}

}
