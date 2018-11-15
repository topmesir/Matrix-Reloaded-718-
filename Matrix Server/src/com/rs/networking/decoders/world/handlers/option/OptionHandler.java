package com.rs.networking.decoders.world.handlers.option;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public abstract class OptionHandler {
	/**
	 * NPC type USES NPC ID
	 * OBJECT type USES Object ID
	 * INVENTORY type USES Item ID
	 * ITEM_ON_ITEM type USES Used On item id
	 * ITEM_ON_NPC type USES NPC ID
	 * ITEM_ON_OBJECT type USES Object ID
	 * @return ids for OptionHandler to catch
	 */
	public abstract int[] getIds();
	
	public abstract OptionType getType();
	
	public int[] toArr(int... ids) {
		return ids;
	}

}
