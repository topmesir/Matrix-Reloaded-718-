package com.rs.engine.tasks.server;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.rs.engine.tasks.server.ServerTask.TickType;
import com.rs.engine.thread.factory.FastThreadFactory;
import com.rs.utilities.Logger;

public final class ServerTaskEngine {

	private static final int MAIN_TICK = 600;
	private static final int SECONDARY_TICK = 410;

	private final ScheduledExecutorService main_service = Executors
			.newSingleThreadScheduledExecutor(new FastThreadFactory());
	private final ScheduledExecutorService secondary_service = Executors
			.newSingleThreadScheduledExecutor(new FastThreadFactory());

	private final List<ServerTask> main_tasks = new ArrayList<>();
	private final List<ServerTask> secondary_tasks = new ArrayList<>();

	private final Queue<ServerTask> main_newTasks = new ArrayDeque<>();
	private final Queue<ServerTask> secondary_newTasks = new ArrayDeque<>();

	public ServerTaskEngine() {

		main_service.scheduleAtFixedRate(() -> tick(TickType.MAIN), 0, MAIN_TICK, TimeUnit.MILLISECONDS);

		secondary_service.scheduleAtFixedRate(() -> tick(TickType.SECONDARY), 0, SECONDARY_TICK, TimeUnit.MILLISECONDS);

	}

	public void addTask(final ServerTask task) {

		if (task == null)
			return;

		schedule(task);
	}

	public void addTasks(ServerTask[] tasks) {
		for (ServerTask task : tasks)
			addTask(task);
	}

	public ServerTaskEngine schedule(final ServerTask task) {

		if (task.immediate()) {

			final ScheduledExecutorService service = task.tickType() == TickType.MAIN ? main_service
					: secondary_service;

			service.execute(() -> task.execute());

		}

		final Queue<ServerTask> newTasks = task.tickType() == TickType.MAIN ? main_newTasks : secondary_newTasks;

		synchronized (newTasks) {
			newTasks.add(task);
		}

		return this;

	}

	public void shutdown() {
		main_service.shutdown();
		secondary_service.shutdown();
	}

	public void tick(TickType type) {

		final Queue<ServerTask> newTasks = type == TickType.MAIN ? main_newTasks : secondary_newTasks;
		final List<ServerTask> tasks = type == TickType.MAIN ? main_tasks : secondary_tasks;

		synchronized (newTasks) {
			ServerTask task;
			while ((task = newTasks.poll()) != null)
				tasks.add(task);
		}

		for (Iterator<ServerTask> it = tasks.iterator(); it.hasNext();) {
			final ServerTask task = it.next();
			try {
				if (!task.run())
					it.remove();
			} catch (Throwable t) {
				Logger.log("Error", "Exception during task execution.");
				Logger.handle(t);
			}
		}

	}

}
