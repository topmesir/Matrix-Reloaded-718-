package com.rs.game.entity.mobile.player.dialogue;

import java.util.HashMap;
import com.rs.game.entity.mobile.player.cutscene.CutscenesHandler;
import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public final class DialogueHandler {
	
	private final static String PATH = CutscenesHandler.class.getPackage().toString().replace("package ", "") + ".impl";

	private static final HashMap<Object, Class<Dialogue>> handledDialogues = new HashMap<Object, Class<Dialogue>>();
	
	public static final void init() {
		try {
			@SuppressWarnings("unchecked")
			Class<Dialogue>[] dialogues = (Class<Dialogue>[]) Utilities.getAllClasses(PATH);
			for (Class<Dialogue> c : dialogues) {
				if (Dialogue.class.isAssignableFrom(c)) {
					handledDialogues.put(c.getSimpleName(), c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final void reload() {
		handledDialogues.clear();
		init();
	}

	public static final Dialogue getDialogue(Object key) {
		if (key instanceof Dialogue)
			return (Dialogue) key;
		Class<Dialogue> classD = handledDialogues.get(key);
		if (classD == null)
			return null;
		try {
			return classD.newInstance();
		} catch (Throwable e) {
			Logger.handle(e);
		}
		return null;
	}

	private DialogueHandler() {

	}
}