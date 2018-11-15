package com.rs.networking.decoders.world.handlers.button.impl;

import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.networking.decoders.world.WorldPacketsDecoder;
import com.rs.networking.decoders.world.handlers.button.InterfaceButtonHandler;

public class MusicInterface extends InterfaceButtonHandler {

	@Override
	public int[] interfaceIds() {
		return toArr(187);
	}

	@Override
	public void executeButton(Player player, int packetId, int interfaceHash, int interfaceId, int componentId,
			int slotId, int slotId2) {
		if (componentId == 1) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				player.getContent().get(ContentType.MUSIC).playAnotherMusic(slotId / 2);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.getContent().get(ContentType.MUSIC).sendHint(slotId / 2);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.getContent().get(ContentType.MUSIC).addToPlayList(slotId / 2);
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
				player.getContent().get(ContentType.MUSIC).removeFromPlayList(slotId / 2);
		} else if (componentId == 4)
			player.getContent().get(ContentType.MUSIC).addPlayingMusicToPlayList();
		else if (componentId == 10)
			player.getContent().get(ContentType.MUSIC).switchPlayListOn();
		else if (componentId == 11)
			player.getContent().get(ContentType.MUSIC).clearPlayList();
		else if (componentId == 13)
			player.getContent().get(ContentType.MUSIC).switchShuffleOn();	
	}

}
