package com.rs.game.entity.mobile.player.content;

import java.io.Serializable;

import com.rs.game.entity.mobile.player.Player;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:25:06 PM
 */
public abstract class AbstractContent implements Serializable {

	/**
	 * The serialization key of the {@link AbstractContent} class type.
	 */
	private static final long serialVersionUID = 8287330789352770679L;

	/**
	 * The current owner {@link Player} object.
	 */
	protected transient Player player;

	/**
	 * Initializes the current content session.
	 * 
	 * @param player
	 *            the session owner player whose this content is for.
	 */
	void initialize(Player player) {
		this.player = player;
	}

	/**
	 * Gets the current session {@link Player} object.
	 * 
	 * @return the owner {@link Player} object.
	 */
	public Player getPlayer() {
		return player;
	}
}