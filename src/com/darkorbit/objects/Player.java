package com.darkorbit.objects;

import com.darkorbit.net.GameManager;

public class Player {
	private int playerID;
	private String userName;
	private short shipID, factionID;
	
	private Settings playerSettings;
	private Ship playerShip;
	
	/**
	 * Player constructor
	 */
	public Player(int playerID, Settings playerSettings, String userName, short shipID, short factionID) {
		this.playerID = playerID;
		this.playerSettings = playerSettings;
		this.userName = userName;
		this.shipID = shipID;
		this.factionID = factionID;
		
		this.playerShip = GameManager.getShip(shipID);
	}
	
	
	/* get methods */
	
		public int getPlayerID() {
			return playerID;
		}

		public short getShipID() {
			return shipID;
		}
		
		public short getFactionID() {
			return factionID;
		}
		
		public String getUserName() {
			return userName;
		}
		
		public Settings getSettings() {
			return playerSettings;
		}
		
		public Ship getShip() {
			return playerShip;
		}
		
	/* @end */
}