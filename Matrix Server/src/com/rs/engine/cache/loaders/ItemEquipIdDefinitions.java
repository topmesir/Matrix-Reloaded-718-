package com.rs.engine.cache.loaders;

import java.util.HashMap;

import com.rs.utilities.Utilities;

public final class ItemEquipIdDefinitions {

	private static final HashMap<Integer, Integer> equipIds = new HashMap<Integer, Integer>();

	public static final void init() {
		int equipId = 0;
		for (int itemId = 0; itemId < Utilities.getItemDefinitionsSize(); itemId++) {
			ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
			if (def.getMaleWornModelId1() >= 0 || def.getFemaleWornModelId1() >= 0)
				equipIds.put(itemId, equipId++);
		}
	}

	public static int getEquipId(int itemId) {
		Integer equipId = equipIds.get(itemId);
		return equipId == null ? -1 : equipId;
	}

	private ItemEquipIdDefinitions() {

	}
}
