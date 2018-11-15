package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class AttackNPCPacket extends IncomingPacket {
	
	
	public int[] packetIds() {
		return new int[] {
				20
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getLockDelay() > Utilities.currentTimeMillis()) {
			return;
		}
		int npcIndex = stream.readUnsignedShort128();
		boolean forceRun = stream.read128Byte() == 1;
		if (forceRun)
			player.setRun(forceRun);
		NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || !npc.getDefinitions().hasAttackOption()) {
			return;
		}
		if (!player.getControlerManager().canAttack(npc)) {
			return;
		}
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
		player.stopAll(false);
		player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(npc));
	}

}
