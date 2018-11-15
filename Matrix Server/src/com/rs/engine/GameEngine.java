package com.rs.engine;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.rs.engine.thread.WorldThread;
import com.rs.engine.thread.factory.DecoderThreadFactory;
import com.rs.engine.thread.factory.SlowThreadFactory;

public final class GameEngine {

	public static volatile boolean shutdown;
	public static WorldThread worldThread;
	public static ExecutorService serverWorkerChannelExecutor;
	public static ExecutorService serverBossChannelExecutor;
	public static Timer timer;
	public static ScheduledExecutorService slowExecutor;
	public static int serverWorkersCount;

	public static void init() {
		worldThread = new WorldThread();
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		serverWorkersCount = availableProcessors >= 6 ? availableProcessors - (availableProcessors >= 12 ? 7 : 5) : 1;
		serverWorkerChannelExecutor = availableProcessors >= 6 ? Executors.newFixedThreadPool(availableProcessors - (availableProcessors >= 12 ? 7 : 5), new DecoderThreadFactory()) : Executors.newSingleThreadExecutor(new DecoderThreadFactory());
		serverBossChannelExecutor = Executors.newSingleThreadExecutor(new DecoderThreadFactory());
		timer = new Timer("Fast Executor");
		slowExecutor = availableProcessors >= 6 ? Executors.newScheduledThreadPool(availableProcessors >= 12 ? 4 : 2, new SlowThreadFactory()) : Executors.newSingleThreadScheduledExecutor(new SlowThreadFactory());
		worldThread.start();
	}

	public static void shutdown() {
		serverWorkerChannelExecutor.shutdown();
		serverBossChannelExecutor.shutdown();
		timer.cancel();
		slowExecutor.shutdown();
		shutdown = true;
	}

	private GameEngine() {

	}
}
