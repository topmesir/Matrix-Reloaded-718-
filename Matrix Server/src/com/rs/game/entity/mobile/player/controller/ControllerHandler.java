package com.rs.game.entity.mobile.player.controller;

import java.util.HashMap;

import com.rs.utilities.Logger;
import com.rs.utilities.Utilities;

public class ControllerHandler {
	
	private final static String PATH = ControllerHandler.class.getPackage().toString().replace("package ", "") + ".impl";

	private static final HashMap<Object, Class<Controller>> handledControlers = new HashMap<Object, Class<Controller>>();

	public static final void init() {
		try {
			@SuppressWarnings("unchecked")
			Class<Controller>[] controllers = (Class<Controller>[]) Utilities.getAllClasses(PATH);
			for (Class<Controller> c : controllers) {
				if (Controller.class.isAssignableFrom(c)) {
					handledControlers.put(c.getSimpleName(), c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final void reload() {
		handledControlers.clear();
		init();
	}

	public static final Controller getControler(Object key) {
		if (key instanceof Controller)
			return (Controller) key;
		Class<Controller> classC = handledControlers.get(key);
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
