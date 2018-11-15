package com.rs.networking.decoders.world.impl;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.global.Magic;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class InterfaceOnPlayerPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				50
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
			return;
		if (player.getLockDelay() > Utilities.currentTimeMillis())
			return;
		@SuppressWarnings("unused")
		int junk1 = stream.readUnsignedShort();
		int playerIndex = stream.readUnsignedShort();
		int interfaceHash = stream.readIntV2();
		@SuppressWarnings("unused")
		int junk2 = stream.readUnsignedShortLE128();
		@SuppressWarnings("unused")
		boolean unknown = stream.read128Byte() == 1;
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
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
			return;
		player.stopAll(false);
		switch (interfaceId) {
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
					player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
					if (!player.getControlerManager().canAttack(p2))
						return;
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
						if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
							player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
							return;
						}
						if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utilities.currentTimeMillis()) {
							if (p2.getAttackedBy() instanceof NPC) {
								p2.setAttackedBy(player); // changes enemy
								// to player,
								// player has
								// priority over
								// npc on single
								// areas
							} else {
								player.getPackets().sendGameMessage("That player is already in combat.");
								return;
							}
						}
					}
					player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(p2));
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
			case 86: // teleblock
			case 84: // air surge
			case 87: // water surge
			case 89: // earth surge
			case 91: // fire surge
			case 99: // storm of armadyl
			case 36: // bind
			case 66: // Sara Strike
			case 67: // Guthix Claws
			case 68: // Flame of Zammy
			case 55: // snare
			case 81: // entangle
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
					if (!player.getControlerManager().canAttack(p2))
						return;
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
						if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
							player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
							return;
						}
						if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utilities.currentTimeMillis()) {
							if (p2.getAttackedBy() instanceof NPC) {
								p2.setAttackedBy(player); // changes enemy
								// to player,
								// player has
								// priority over
								// npc on single
								// areas
							} else {
								player.getPackets().sendGameMessage("That player is already in combat.");
								return;
							}
						}
					}
					player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(p2));
				}
				break;
			}
			break;
		}
		if (Settings.DEBUG)
			System.out.println("Spell:" + componentId);
	}

}
