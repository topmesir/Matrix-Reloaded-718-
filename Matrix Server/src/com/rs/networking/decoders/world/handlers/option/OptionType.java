package com.rs.networking.decoders.world.handlers.option;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public enum OptionType {
	NPC, 			// USES NPC ID
	OBJECT,			// USES Object ID
	INVENTORY,		// USES Item ID
	ITEM_ON_ITEM, 	// USES Used On item id
	ITEM_ON_NPC,	// USES NPC ID
	ITEM_ON_OBJECT; // USES Object ID
	
	public enum ObjectOptionType {
		OPTION_1, OPTION_2, OPTION_3, OPTION_4, OPTION_5;
	}
	public enum NPCOptionType {
		OPTION_1, OPTION_2, OPTION_3;
	}
	public enum InventoryOptionType {
		OPTION_1, OPTION_3, OPTION_4, OPTION_5, OPTION_6;
	}
}
