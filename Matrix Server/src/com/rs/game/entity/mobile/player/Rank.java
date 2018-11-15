package com.rs.game.entity.mobile.player;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 11:08:16 PM
 */
public enum Rank {
	PLAYER(0, ""),
	MOD(1, "Moderator"),
	ADMIN(2, "Administrator");
	
	private final int iconId;
	private final String title;
	
	private Rank(int iconId, String title) {
		this.iconId = iconId;
		this.title = title;
	}

	public int getIconId() {
		return iconId;
	}

	public String getTitle() {
		return title;
	}

}
