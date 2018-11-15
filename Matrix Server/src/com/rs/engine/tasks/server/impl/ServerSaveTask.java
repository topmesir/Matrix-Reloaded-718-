package com.rs.engine.tasks.server.impl;

import com.rs.Launcher;
import com.rs.engine.tasks.server.ServerTask;

public class ServerSaveTask extends ServerTask {
	
	private final static int TICK_DELAY = 100 * 3; // 3 Minutes

	public ServerSaveTask() {
		super(TICK_DELAY, false, TickType.MAIN);
	}
	
	@Override
	public void execute() {
		Launcher.saveFiles();
	}

}
