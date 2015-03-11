package com.darkorbit.objects;

import com.darkorbit.mysql.QueryManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.systems.MovementSystem;
import com.darkorbit.utils.Vector;


/**
 * Player class
 * @author Borja
 *
 */
public class Player {
	private int playerID, health, level, rank, rings, clanID;
	private String userName;
	private short shipID, factionID, mapID;
	private Vector position;
	private boolean moving, isPremium;
	private long experience, credits, uridium, honor;
	private double jackpot;
	
	private Settings playerSettings;
	private Ship playerShip;
	private MovementSystem movementSystem;
	private Ammunition ammo;
	private Rockets rockets;
	private Drone[] drones;
	private Clan	clan;
	
	/**
	 * Player constructor
	 */
	public Player(int playerID, Settings playerSettings, String userName, short shipID, short factionID, short mapID, Vector position, int health,
			boolean isPremium, long experience, long credits, long uridium, long honor, int level, double jackpot, int rank, int rings, int clanID
			) {
		
		this.playerID = playerID;
		this.playerSettings = playerSettings;
		this.userName = userName;
		this.shipID = shipID;
		this.factionID = factionID;
		this.mapID = mapID;
		this.position = position;
		this.health = health;
		this.isPremium = isPremium;
		this.experience = experience;
		this.credits = credits;
		this.uridium = uridium;
		this.honor = honor;
		this.level = level;
		this.jackpot = jackpot;
		this.rank = rank;
		this.rings = rings;
		this.clanID = clanID;
		
		this.moving = false;
		
		this.playerShip = GameManager.getShip(shipID);
		this.ammo = QueryManager.loadAmmunition(playerID);
		this.rockets = QueryManager.loadRockets(playerID);
		this.drones = QueryManager.loadDrones(playerID);
		
		if(hasClan()) {
			this.clan = QueryManager.loadClan(clanID);
		} else {
			//Empty clan
			this.clan = new Clan(0, "");
		}
	}
	
	
	/* get methods  Self-explained*/
	
		public int getPlayerID() { return playerID; }

		public short getShipID() { return shipID; }
		
		public short getFactionID() { return factionID; }
		
		public short getMapID() { return mapID; }
		
		public String getUserName() { return userName; }
		
		public Settings getSettings() { return playerSettings; }
		
		public Ship getShip() { return playerShip; }
		
		public Vector getPosition() { return position; }
		
		public boolean isMoving() { return moving; }
		
		public MovementSystem movement() { return movementSystem; }
		
		public int getHealth() { return health; }
		
		public Ammunition getAmmo() { return ammo; }
		
		public Drone[] getDrones() { return drones; }
		
		public Rockets getRocket() { return rockets; }
		
		public boolean isPremium() { return isPremium; }
		
		public long getExperience() { return experience; }
		
		public long getCredits() { return credits; }
		
		public long getUridium() { return uridium; }
		
		public long getHonor() { return honor; }
		
		public int getLevel() { return level; }
		
		public double getJackpot() { return jackpot; }
		
		public int getRank() { return rank; }
		
		public int getRings() { return rings; }
		
		public Clan clan() { return clan; }
		
		
	/* @end */
		
	/* set methods */
		public void isMoving(boolean m) { moving = m; }
		
		public void setPosition(Vector p) { position = p; }
		
		public void setMovementSystem() {
			/*
			 * Porque el constructor del movementHelper necesita que el usuario este online en el connectionManager
			 * asi que hago un metodo para desde el connectionManager iniciarlo cuando quiera...
			 */
			movementSystem = new MovementSystem(playerID);
		}
		
		public void setHealth(int h) { health = h; }
		
	/* @end */
		
		//Comprueba si el usuario tiene clan
		private boolean hasClan() {
			if(this.clanID > 0) {
				return true;
			} else {
				return false;
			}
		}
}