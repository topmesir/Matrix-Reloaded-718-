package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.entity.item.Item;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.Magic;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.entity.mobile.player.content.type.container.Inventory;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.decoders.world.handlers.InventoryHandler;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class InterfaceOnNPCPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				66
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		if (player.getLockDelay() > Utilities.currentTimeMillis())
			return;
		@SuppressWarnings("unused")
		boolean unknown = stream.readByte() == 1;
		int interfaceHash = stream.readInt();
		int npcIndex = stream.readUnsignedShortLE();
		int interfaceSlot = stream.readUnsignedShortLE128();
		@SuppressWarnings("unused")
		int junk2 = stream.readUnsignedShortLE();
		int interfaceId = interfaceHash >> 16;
		int componentId = interfaceHash - (interfaceId << 16);
		if (Utilities.getInterfaceDefinitionsSize() <= interfaceId)
			return;
		if (!player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (componentId == 65535)
			componentId = -1;
		if (componentId != -1 && Utilities.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
			return;
		NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		player.stopAll(false);
		if (interfaceId != Inventory.INVENTORY_INTERFACE) {
			if (!npc.getDefinitions().hasAttackOption()) {
				player.getPackets().sendGameMessage("You can't attack this npc.");
				return;
			}
		}
		switch (interfaceId) {
		case Inventory.INVENTORY_INTERFACE:
			Item item = player.getContent().get(ContentType.INVENTORY).getItem(interfaceSlot);
			if (item == null || !player.getControlerManager().processItemOnNPC(npc, item))
				return;
			InventoryHandler.handleItemOnNPC(player, npc, item);
			break;
		case 193:
			switch (componentId) {
			case 28:
			case 32:
			case 24:
			case 20:
			case 30:
			case 34:
			case 26:
			case 22:
			case 29:
			case 33:
			case 25:
			case 21:
			case 31:
			case 35:
			case 27:
			case 23:
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
					if (!player.getControlerManager().canAttack(npc))
						return;
					if (!npc.isForceMultiAttacked()) {
						if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You are already in combat.");
								return;
							}
							if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utilities.currentTimeMillis()) {
								player.getPackets().sendGameMessage("This npc is already in combat.");
								return;
							}
						}
					}
					player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(npc));
				}
				break;
			}
		case 192:
			switch (componentId) {
			case 25: // air strike
			case 28: // water strike
			case 30: // earth strike
			case 32: // fire strike
			case 34: // air bolt
			case 39: // water bolt
			case 42: // earth bolt
			case 45: // fire bolt
			case 49: // air blast
			case 52: // water blast
			case 58: // earth blast
			case 63: // fire blast
			case 70: // air wave
			case 73: // water wave
			case 77: // earth wave
			case 80: // fire wave
			case 84: // air surge
			case 87: // water surge
			case 89: // earth surge
			case 66: // Sara Strike
			case 67: // Guthix Claws
			case 68: // Flame of Zammy
			case 93:
			case 91: // fire surge
			case 99: // storm of Armadyl
			case 36: // bind
			case 55: // snare
			case 81: // entangle
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
					if (!player.getControlerManager().canAttack(npc))
						return;
					if (!npc.isForceMultiAttacked()) {
						if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You are already in combat.");
								return;
							}
							if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utilities.currentTimeMillis()) {
								player.getPackets().sendGameMessage("This npc is already in combat.");
								return;
							}
						}
					}
					player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(npc));
				}
				break;
			}
			break;
		}
		if (Settings.DEBUG)
			System.out.println("Spell:" + componentId);
	}

}
