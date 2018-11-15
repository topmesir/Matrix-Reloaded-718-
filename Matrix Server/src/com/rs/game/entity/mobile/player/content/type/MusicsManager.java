package com.rs.game.entity.mobile.player.content.type;

import java.util.ArrayList;

import com.rs.engine.cache.loaders.ClientScriptMapDefinitions;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.AbstractContent;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.world.Region;
import com.rs.game.world.World;
import com.rs.utilities.MusicHints;
import com.rs.utilities.Utilities;

public final class MusicsManager extends AbstractContent {

	private static final long serialVersionUID = 1020415702861567375L;

	private static final int[] CONFIG_IDS = new int[] { 20, 21, 22, 23, 24, 25, 298, 311, 346, 414, 464, 598, 662, 721, 906, 1009, 1104, 1136, 1180, 1202, 1381, 1394, 1434, 1596, 1618, 1619, 1620, 1865, 1864, 2246, 2019, -1, 2430, 2559 };
	private static final int[] PLAY_LIST_CONFIG_IDS = new int[] { 1621, 1622, 1623, 1624, 1625, 1626 };

	private transient int playingMusic;
	private transient long playingMusicDelay;
	private transient boolean settedMusic;
	private ArrayList<Integer> unlockedMusics;
	private ArrayList<Integer> playList;

	private transient boolean playListOn;
	private transient int nextPlayListMusic;
	private transient boolean shuffleOn;

	public MusicsManager() {
		playingMusic = World.getRegion(player.getRegionId()).getMusicId();
		unlockedMusics = new ArrayList<Integer>();
		playList = new ArrayList<Integer>(12);
		// auto unlocked musics
		unlockedMusics.add(62);
		unlockedMusics.add(400);
		unlockedMusics.add(16);
		unlockedMusics.add(466);
		unlockedMusics.add(321);
		unlockedMusics.add(547);
		unlockedMusics.add(621);
		unlockedMusics.add(207);
		unlockedMusics.add(401);
		unlockedMusics.add(147);
		unlockedMusics.add(457);
		unlockedMusics.add(552);
		unlockedMusics.add(858);
	}

	public void passMusics(Player p) {
		for (int musicId : p.getContent().get(ContentType.MUSIC).unlockedMusics) {
			if (!unlockedMusics.contains(musicId))
				unlockedMusics.add(musicId);
		}
	}

	public boolean hasMusic(int id) {
		return unlockedMusics.contains(id);
	}

	public void switchShuffleOn() {
		if (shuffleOn) {
			playListOn = false;
			refreshPlayListConfigs();
		}
		shuffleOn = !shuffleOn;
	}

	public void switchPlayListOn() {
		if (playListOn) {
			playListOn = false;
			shuffleOn = false;
			refreshPlayListConfigs();
		} else {
			playListOn = true;
			nextPlayListMusic = 0;
			replayMusic();
		}
	}

	public void clearPlayList() {
		if (playList.isEmpty())
			return;
		playList.clear();
		refreshPlayListConfigs();
	}

	public void addPlayingMusicToPlayList() {
		addToPlayList((int) ClientScriptMapDefinitions.getMap(1351).getKeyForValue(playingMusic));
	}

	public void addToPlayList(int musicIndex) {
		if (playList.size() == 12)
			return;
		int musicId = ClientScriptMapDefinitions.getMap(1351).getIntValue(musicIndex);
		if (musicId != -1 && unlockedMusics.contains(musicId) && !playList.contains(musicId)) {
			playList.add(musicId);
			if (playListOn)
				switchPlayListOn();
			else
				refreshPlayListConfigs();
		}
	}

	public void removeFromPlayList(int musicIndex) {
		Integer musicId = ClientScriptMapDefinitions.getMap(1351).getIntValue(musicIndex);
		if (musicId != -1 && unlockedMusics.contains(musicId) && playList.contains(musicId)) {
			playList.remove(musicId);
			if (playListOn)
				switchPlayListOn();
			else
				refreshPlayListConfigs();
		}
	}

	public void refreshPlayListConfigs() {
		int[] configValues = new int[PLAY_LIST_CONFIG_IDS.length];
		for (int i = 0; i < configValues.length; i++)
			configValues[i] = -1;
		for (int i = 0; i < playList.size(); i += 2) {
			Integer musicId1 = playList.get(i);
			Integer musicId2 = (i + 1) >= playList.size() ? null : playList.get(i + 1);
			if (musicId1 == null && musicId2 == null)
				break;
			int musicIndex = (int) ClientScriptMapDefinitions.getMap(1351).getKeyForValue(musicId1);
			int configValue;
			if (musicId2 != null) {
				int musicIndex2 = (int) ClientScriptMapDefinitions.getMap(1351).getKeyForValue(musicId2);
				configValue = musicIndex | musicIndex2 << 15;
			} else
				configValue = musicIndex | -1 << 15;
			configValues[i / 2] = configValue;
		}
		for (int i = 0; i < PLAY_LIST_CONFIG_IDS.length; i++)
			if (PLAY_LIST_CONFIG_IDS[i] == -1)
				player.getPackets().sendConfig(PLAY_LIST_CONFIG_IDS[i], configValues[i]);
	}

	public void refreshListConfigs() {
		int[] configValues = new int[CONFIG_IDS.length];
		for (int musicId : unlockedMusics) {
			int musicIndex = (int) ClientScriptMapDefinitions.getMap(1351).getKeyForValue(musicId);
			if (musicIndex == -1)
				continue;
			int index = getConfigIndex(musicIndex);
			if (index >= CONFIG_IDS.length)
				continue;
			configValues[index] |= 1 << (musicIndex - (index * 32));
		}
		for (int i = 0; i < CONFIG_IDS.length; i++) {
			if (CONFIG_IDS[i] != -1 && configValues[i] != 0)
				player.getPackets().sendConfig(CONFIG_IDS[i], configValues[i]);
		}
	}

	public void addMusic(int musicId) {
		unlockedMusics.add(musicId);
		refreshListConfigs();
	}

	public int getConfigIndex(int musicId) {
		return (musicId + 1) / 32;
	}

	public void unlockMusicPlayer() {
		player.getPackets().sendUnlockIComponentOptionSlots(187, 1, 0, CONFIG_IDS.length * 64, 0, 1, 2, 3);
	}

	public void init() {
		// unlock music inter all options
		if (playingMusic >= 0)
			playMusic(playingMusic);
		refreshListConfigs();
		refreshPlayListConfigs();
	}

	public boolean musicEnded() {
		return playingMusic != -2 && playingMusicDelay + (180000) < Utilities.currentTimeMillis();
	}

	public void replayMusic() {
		if (playListOn && playList.size() > 0) {
			if (shuffleOn)
				playingMusic = playList.get(Utilities.getRandom(playList.size() - 1));
			else {
				if (nextPlayListMusic >= playList.size())
					nextPlayListMusic = 0;
				playingMusic = playList.get(nextPlayListMusic++);
			}
		} else if (unlockedMusics.size() > 0) // random music
			playingMusic = unlockedMusics.get(Utilities.getRandom(unlockedMusics.size() - 1));
		playMusic(playingMusic);
	}

	public void checkMusic(int requestMusicId) {
		if (playListOn || settedMusic && playingMusicDelay + (180000) >= Utilities.currentTimeMillis())
			return;
		settedMusic = false;
		if (playingMusic != requestMusicId)
			playMusic(requestMusicId);
	}

	public void forcePlayMusic(int musicId) {
		settedMusic = true;
		playMusic(musicId);
	}

	public void reset() {
		settedMusic = false;
		checkMusic(World.getRegion(player.getRegionId()).getMusicId());
	}

	public void sendHint(int musicIndex) {
		int musicId = ClientScriptMapDefinitions.getMap(1351).getIntValue(musicIndex);
		if (musicId != -1) {
			player.getPackets().sendGameMessage("This track " + (unlockedMusics.contains(musicId) ? "was unlocked" : "unlocks") + " " + MusicHints.getHint(musicId));
		}
	}

	public void playAnotherMusic(int musicIndex) {
		int musicId = ClientScriptMapDefinitions.getMap(1351).getIntValue(musicIndex);
		if (musicId != -1 && unlockedMusics.contains(musicId)) {
			settedMusic = true;
			if (playListOn)
				switchPlayListOn();
			playMusic(musicId);
		}

	}

	public void playMusic(int musicId) {
		if (!player.hasStarted())
			return;
		playingMusicDelay = Utilities.currentTimeMillis();
		if (musicId == -2) {
			playingMusic = musicId;
			player.getPackets().sendMusic(-1);
			player.getPackets().sendIComponentText(187, 4, "");
			return;
		}
		player.getPackets().sendMusic(musicId, playingMusic == -1 ? 0 : 100, 255);
		playingMusic = musicId;
		int musicIndex = (int) ClientScriptMapDefinitions.getMap(1351).getKeyForValue(musicId);
		if (musicIndex != -1) {
			String musicName = ClientScriptMapDefinitions.getMap(1345).getStringValue(musicIndex);
			if (musicName.equals(" "))
				musicName = Region.getMusicName1(player.getRegionId());
			player.getPackets().sendIComponentText(187, 4, musicName != null ? musicName : "");
			if (!unlockedMusics.contains(musicId)) {
				addMusic(musicId);
				if (musicName != null)
					player.getPackets().sendGameMessage("<col=ff0000>You have unlocked a new music track: " + musicName + ".");
			}
		}
	}

}
