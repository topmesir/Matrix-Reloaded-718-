/**
 * 
 */
package com.rs.game.entity.mobile.player.content.global;

import java.util.HashMap;

/**
 * @author Abysa/Dido#4821
 * Nov 12, 2018 | 7:35:13 PM
 */
public class FriendsChatContainer {
	
	private static HashMap<String, FriendsChatManager> cachedFriendChats;
	
	static {
		cachedFriendChats = new HashMap<String, FriendsChatManager>();
	}

	/**
	 * @return the cachedFriendChats
	 */
	public static HashMap<String, FriendsChatManager> getCached() {
		return cachedFriendChats;
	}

}
