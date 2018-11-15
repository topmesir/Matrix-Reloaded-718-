package com.rs.game.entity.mobile.player.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.game.entity.mobile.player.Player;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:24:54 PM
 */
public class AbstractContentManager implements Serializable {

	/**
	 * The serialisation key of the {@link AbstractContentManager} class type.
	 */
	private static final long serialVersionUID = 5879190178405435243L;

	/**
	 * The contents map.
	 */
	private final Map<ContentType<? extends AbstractContent>, AbstractContent> contents = new HashMap<>();

	/**
	 * The owner {@link Player} object.
	 */
	private transient Player player;

	/**
	 * Initializes the content manager.
	 * 
	 * @param player
	 *            the owner player whose this manager is for.
	 */
	public void initialize(Player player) {
		this.setPlayer(player);
		for (ContentType<? extends AbstractContent> kind : ContentType.values()) {
			AbstractContent content = contents.get(kind);
			if (content == null) 
				contents.put(kind, content = kind.supply());
			
			content.initialize(getPlayer());
		}
	}

	/**
	 * Gets the {@link AbstractContent} object which is associated with the
	 * specified {@link ContentType kind}.
	 * 
	 * @param kind
	 *            the content type.
	 * @return the {@link AbstractContent} objected casted to the inferenced type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractContent> T get(ContentType<T> kind) {
		T content = (T) contents.get(kind);
		if (content == null) {
			throw new IllegalStateException("PANIC! Runtime changes");
		}
		return content;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}
