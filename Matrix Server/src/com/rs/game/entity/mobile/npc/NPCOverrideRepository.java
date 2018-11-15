package com.rs.game.entity.mobile.npc;

import java.util.HashMap;
import java.util.Map;

import com.rs.utilities.Utilities;

public class NPCOverrideRepository {
	private static final String BASE_DIR = NPCOverrideRepository.class.getPackage().toString().replace("package ", "");

	public static Map<Integer, Class<NPC>> NPCS = new HashMap<>(); 

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, Class<NPC>> populateNPCTable() {
		HashMap<Integer, Class<NPC>> npcs = new HashMap<>();
		try {
			Class<NPC>[] npcArray = (Class<NPC>[]) Utilities.getAllClasses(BASE_DIR + ".impl");
			for(Class<NPC> npc : npcArray) {
				if(npc.getCanonicalName() == null)
					continue;
				Class<NPC> npcClass = (Class<NPC>) Class.forName(npc.getCanonicalName());
				NPC npcInstance = (NPC) npcClass.newInstance();
					if(npcInstance.getNPCIds() == null)
						continue;
					if(npcInstance.getNPCIds().length > 1) {
						for(int id : npcInstance.getNPCIds()) {
							if(id != -1)
								npcs.put(id, npcClass);
						}
						continue;
					}
					if(npcInstance.getNPCIds()[0] != -1)
						npcs.put(npcInstance.getNPCIds()[0], npcClass);
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return npcs;
	}

	public static Class<NPC> getNPC(int npcId) {
		Class<NPC> npc = NPCS.get(npcId);
		if(npc != null)
			return npc;
		return null;
	}
}
