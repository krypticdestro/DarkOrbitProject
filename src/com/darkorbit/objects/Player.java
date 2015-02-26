package com.darkorbit.objects;

public class Player {
	private int playerID;
	private long sessionID;
	private Settings playerSettings;
	
	/**
	 * Player constructor
	 */
	public Player(int playerID, Settings playerSettings) {
		this.playerID = playerID;
		this.playerSettings = playerSettings;
	}
	
	public int getPlayerID() {
		return playerID;
	}

	public Settings getSettings() {
		return playerSettings;
	}
}
