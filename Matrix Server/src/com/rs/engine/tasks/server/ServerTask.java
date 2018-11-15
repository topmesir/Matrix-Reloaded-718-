package com.rs.engine.tasks.server;

public abstract class ServerTask {

	public enum TickType {
		MAIN,
		SECONDARY;
	}

	private final int delay;
	private final boolean immediate;
	private int countdown;
	private boolean running = true;

	private TickType tickType;

	public ServerTask() {
		this(1);
	}

	public ServerTask(boolean immediate) {
		this(1, immediate, TickType.MAIN);
	}

	public ServerTask(int delay) {
		this(delay, false, TickType.MAIN);
	}

	public ServerTask(int delay, boolean immediate) {
		this(delay, immediate, TickType.MAIN);
	}

	public ServerTask(int delay, boolean immediate, TickType type) {
		checkDelay(delay);
		this.delay = delay;
		countdown = delay;
		this.immediate = immediate;
		tickType = type;
	}

	public ServerTask(TickType tick) {
		this(1, false, tick);
	}

	private void checkDelay(int delay) {
		if (delay <= 0)
			throw new IllegalArgumentException("Delay must be positive.");
	}

	private void checkStopped() {
		if (!running)
			System.out.println("The error is in method checkStopped l82 Task.java..");
		throw new IllegalStateException();
	}

	public void delay(int delay) {
		checkDelay(delay);
		delay = 0;
	}

	public abstract void execute();

	public boolean immediate() {
		return immediate;
	}

	public boolean run() {
		if (running && --countdown == 0) {
			execute();
			countdown = delay;
		}
		return running;
	}

	public boolean running() {
		return running;
	}

	public void stop() {
		checkStopped();
		running = false;
	}

	public boolean stopped() {
		return !running;
	}

	public TickType tickType() {
		return tickType;
	}
}
