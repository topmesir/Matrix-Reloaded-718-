package com.rs.networking.decoders.world.impl;

import com.rs.game.entity.mobile.npc.NPC;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerCombat;
import com.rs.game.entity.mobile.player.content.type.action.impl.PlayerFollow;
import com.rs.game.world.World;
import com.rs.networking.decoders.world.IncomingPacket;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * Debug packet
 * @author Abysa/Dido#4821 5 Dec 2016
 */
public class PlayerOptionPacket extends IncomingPacket {
	
	private final static int PLAYER_OPTION_1_PACKET = 42;
	private final static int PLAYER_OPTION_2_PACKET = 46;
	private final static int PLAYER_OPTION_4_PACKET = 17;
	
	public int[] packetIds() {
		return new int[] {
				42, 46, 17
		};
	}
	
	
	public void processPacket(Player player, InputStream stream, int packetId) {
		switch(packetId) {
		case PLAYER_OPTION_1_PACKET:
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			@SuppressWarnings("unused")
			boolean unknown = stream.readByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.getLockDelay() > Utilities.currentTimeMillis() || !player.getControlerManager().canPlayerOption1(p2))
				return;
			if (!player.isCanPvp())
				return;
			if (!player.getControlerManager().canAttack(p2))
				return;

			if (!player.isCanPvp() || !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
				return;
			}
			if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
				if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utilities.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You are already in combat.");
					return;
				}
				if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utilities.currentTimeMillis()) {
					if (p2.getAttackedBy() instanceof NPC) {
						p2.setAttackedBy(player); // changes enemy to player,
						// player has priority over
						// npc on single areas
					} else {
						player.getPackets().sendGameMessage("That player is already in combat.");
						return;
					}
				}
			}
			player.stopAll(false);
			player.getContent().get(ContentType.ACTION).setAction(new PlayerCombat(p2));
			break;
		case PLAYER_OPTION_2_PACKET:
			if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead())
				return;
			@SuppressWarnings("unused")
			boolean unknown1 = stream.readByte() == 1;
			int playerIndex1 = stream.readUnsignedShortLE128();
			Player p21 = World.getPlayers().get(playerIndex1);
			if (p21 == null || p21.isDead() || p21.hasFinished() || !player.getMapRegionsIds().contains(p21.getRegionId()))
				return;
			if (player.getLockDelay() > Utilities.currentTimeMillis())
				return;
			player.stopAll(false);
			player.getContent().get(ContentType.ACTION).setAction(new PlayerFollow(p21));
			break;
		case PLAYER_OPTION_4_PACKET:
			@SuppressWarnings("unused")
			boolean unknown11 = stream.readByte() == 1;
			int playerIndex11 = stream.readUnsignedShortLE128();
			Player p211 = World.getPlayers().get(playerIndex11);
			if (p211 == null || p211.isDead() || p211.hasFinished() || !player.getMapRegionsIds().contains(p211.getRegionId()))
				return;
			if (player.getLockDelay() > Utilities.currentTimeMillis())
				return;
			player.stopAll(false);
			if (player.isCantTrade()) {
				player.getPackets().sendGameMessage("You are busy.");
				return;
			}
			if (p211.getInterfaceManager().containsScreenInter() || p211.isCantTrade()) {
				player.getPackets().sendGameMessage("The other player is busy.");
				return;
			}
			if (!p211.withinDistance(player, 14)) {
				player.getPackets().sendGameMessage("Unable to find target " + p211.getDisplayName());
				return;
			}

			if (p211.getTemporaryAttributtes().get("TradeTarget") == player) {
				p211.getTemporaryAttributtes().remove("TradeTarget");
				player.getContent().get(ContentType.TRADE).openTrade(p211);
				p211.getContent().get(ContentType.TRADE).openTrade(player);
				return;
			}
			player.getTemporaryAttributtes().put("TradeTarget", p211);
			player.getPackets().sendGameMessage("Sending " + p211.getDisplayName() + " a request...");
			p211.getPackets().sendTradeRequestMessage(player);
			break;
		}
	}

}
