package com.rs.game.entity.mobile.npc.combat;

import java.util.HashMap;

import com.rs.game.entity.mobile.MobileEntity;
import com.rs.game.entity.mobile.npc.NPC;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public class CombatScriptsHandler {
	
	private final static String PATH = CombatScriptsHandler.class.getPackage().toString().replace("package ", "") + ".impl";

	private static final HashMap<Object, CombatScript> cachedCombatScripts = new HashMap<Object, CombatScript>();
	private static final CombatScript DEFAULT_SCRIPT = new Default();

	@SuppressWarnings("rawtypes")
	public static final void init() {
		try {
			Class[] classes = Utilities.getAllClasses(PATH);
			for (Class c : classes) {
				if (c.isAnonymousClass()) // next
					continue;
				Object o = c.newInstance();
				if (!(o instanceof CombatScript))
					continue;
				CombatScript script = (CombatScript) o;
				for (Object key : script.getKeys())
					cachedCombatScripts.put(key, script);
			}
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public static int specialAttack(final NPC npc, final MobileEntity target) {
		CombatScript script = cachedCombatScripts.get(npc.getId());
		if (script == null) {
			script = cachedCombatScripts.get(npc.getDefinitions().name);
			if (script == null)
				script = DEFAULT_SCRIPT;
		}
		return script.attack(npc, target);
	}
}
