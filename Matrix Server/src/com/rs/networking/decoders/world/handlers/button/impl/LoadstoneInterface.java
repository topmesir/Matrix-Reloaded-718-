package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.WorldTile;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.action.impl.HomeTeleport;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class LoadstoneInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(1092);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		player.stopAll();
		WorldTile destTile = null;
		switch(componentId) {
		case 47:
			destTile = HomeTeleport.LUMBRIDGE_LODE_STONE;
			break;
		case 42:
			destTile = HomeTeleport.BURTHORPE_LODE_STONE;
			break;
		case 39:
			destTile = HomeTeleport.LUNAR_ISLE_LODE_STONE;
			break;
		case 7:
			destTile = HomeTeleport.BANDIT_CAMP_LODE_STONE;
			break;	
		case 50:
			destTile = HomeTeleport.TAVERLY_LODE_STONE;
			break;
		case 40:
			destTile = HomeTeleport.ALKARID_LODE_STONE;
			break;
		case 51:
			destTile = HomeTeleport.VARROCK_LODE_STONE;
			break;
		case 45:
			destTile = HomeTeleport.EDGEVILLE_LODE_STONE;
			break;
		case 46:
			destTile = HomeTeleport.FALADOR_LODE_STONE;
			break;
		case 48:
			destTile = HomeTeleport.PORT_SARIM_LODE_STONE;
			break;
		case 44:
			destTile = HomeTeleport.DRAYNOR_VILLAGE_LODE_STONE;
			break;
		case 41:
			destTile = HomeTeleport.ARDOUGNE_LODE_STONE;
			break;
		case 43:
			destTile = HomeTeleport.CATHERBAY_LODE_STONE;
			break;
		case 52:
			destTile = HomeTeleport.YANILLE_LODE_STONE;
			break;
		case 49:
			destTile = HomeTeleport.SEERS_VILLAGE_LODE_STONE;
			break;
		}
		if(destTile != null) 
			player.getContent().get(ContentType.ACTION).setAction(new HomeTeleport(destTile));
	}

}
