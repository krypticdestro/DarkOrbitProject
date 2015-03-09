package com.darkorbit.objects;

import com.darkorbit.mysql.QueryManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.utils.MovementHelper;
import com.darkorbit.utils.Vector;

public class Player {
	private int playerID, health;
	private String userName;
	private short shipID, factionID, mapID;
	private Vector position;
	private boolean moving;
	
	private Settings playerSettings;
	private Ship playerShip;
	private MovementHelper movementHelper;
	private Ammunition ammo;
	private Drone[] drones;
	
	/**
	 * Player constructor
	 */
	public Player(int playerID, Settings playerSettings, String userName, short shipID, short factionID, short mapID, Vector position, int health) {
		this.playerID = playerID;
		this.playerSettings = playerSettings;
		this.userName = userName;
		this.shipID = shipID;
		this.factionID = factionID;
		this.mapID = mapID;
		this.position = position;
		this.health = health;
		
		this.moving = false;
		
		this.playerShip = GameManager.getShip(shipID);
		this.ammo = QueryManager.loadAmmunition(playerID);
		this.drones = QueryManager.loadDrones(playerID);
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
		
		public short getMapID() {
			return mapID;
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
		
		public Vector getPosition() {
			return position;
		}
		
		public boolean isMoving() {
			return moving;
		}
		
		public MovementHelper movement() {
			return movementHelper;
		}
		
		public int getHealth() {
			return health;
		}
		
		public Ammunition getAmmo() {
			return ammo;
		}
		
		public Drone[] getDrones() {
			return drones;
		}
		
	/* @end */
		
	/* set methods */
		public void isMoving(boolean m) {
			moving = m;
		}
		
		public void setPosition(Vector p) {
			position = p;
		}
		
		public void setMovementHelper() {
			/*
			 * Porque el constructor del movementHelper necesita que el usuario este online en el connectionManager
			 * asi que hago un metodo para desde el connectionManager iniciarlo cuando quiera...
			 */
			movementHelper = new MovementHelper(playerID);
		}
		
		public void setHealth(int h) {
			health = h;
		}
}