package com.rs.engine.tasks.server;

import com.rs.engine.tasks.server.impl.CleanMemoryTask;
import com.rs.engine.tasks.server.impl.ServerSaveTask;

/**
 *
 * @author Abysa/Dido#4821
 *
 */

public final class ServerTaskRepository {

	private static ServerTaskRepository INSTANCE;
	private ServerTask[] tasks;

	public ServerTaskRepository() {
		tasks = new ServerTask[] { new CleanMemoryTask(), new ServerSaveTask()};
	}

	public ServerTask[] tasks() {
		return tasks;
	}

	public static ServerTaskRepository get() {
		if (INSTANCE == null)
			INSTANCE = new ServerTaskRepository();
		return INSTANCE;
	}

}
