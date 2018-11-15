package com.rs.networking.decoders.world;

public class PublicChatMessage {

	private String message;
	private int effects;

	public PublicChatMessage(String message, int effects) {
		this.message = message;
		this.effects = effects;
	}

	public String getMessage() {
		return message;
	}

	public int getEffects() {
		return effects;
	}

}
