package com.rs.game.entity.mobile.player.cutscene;

import java.util.HashMap;

import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public class CutscenesHandler {
	
	private final static String PATH = CutscenesHandler.class.getPackage().toString().replace("package ", "") + ".impl";


	private static final HashMap<Object, Class<Cutscene>> handledCutscenes = new HashMap<Object, Class<Cutscene>>();

	@SuppressWarnings("unchecked")
	public static final void init() {
		try {
			Class<Cutscene>[] cutscenes = (Class<Cutscene>[]) Utilities.getAllClasses(PATH);
			for (Class<Cutscene> c : cutscenes) {
				if (Cutscene.class.isAssignableFrom(c)) {
					handledCutscenes.put(c.getSimpleName(), c);
				}
			}
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

	public static final void reload() {
		handledCutscenes.clear();
		init();
	}

	public static final Cutscene getCutscene(Object key) {
		Class<Cutscene> classC = handledCutscenes.get(key);
		if (classC == null)
			return null;
		try {
			return classC.newInstance();
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}
}
