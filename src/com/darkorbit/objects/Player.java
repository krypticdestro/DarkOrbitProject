package com.darkorbit.objects;

import java.util.List;

import com.darkorbit.attack.LaserSystem;
import com.darkorbit.mysql.QueryManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.systems.MovementSystem;
import com.darkorbit.utils.Extra;
import com.darkorbit.utils.Vector;


/**
 * Player class
 * @author Borja
 *
 */
public class Player {
	private int playerID, health, level, rank, rings, clanID, configNum, targetID, selectedAmmo;
	private String userName;
	private short shipID, factionID, mapID;
	private Vector position;
	private boolean moving, isPremium, isJumping, isPlayer, isAttacking;
	private long experience, credits, uridium, honor;
	private double jackpot;
	
	private Settings playerSettings;
	private Ship playerShip;
	private MovementSystem movementSystem;
	private LaserSystem laserSystem;
	private Ammunition ammo;
	private Rockets rockets;
	private Clan clan;
	private Equipment config1, config2, activeConfig;
	
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
		this.isJumping = false;
		this.isPlayer = false;
		this.isAttacking = false;
		
		this.playerShip = GameManager.getShip(shipID);
		this.ammo = QueryManager.loadAmmunition(playerID);
		this.rockets = QueryManager.loadRockets(playerID);
		
		/*if(GameManager.dronesBought.containsKey(playerID)) {
			this.drones = GameManager.getDrones(playerID);
			
		} else {
			this.drones = QueryManager.loadDrones(playerID);
		}*/
		
		this.config1 = QueryManager.loadEquipment(playerID, 1);
		this.config2 = QueryManager.loadEquipment(playerID, 2);
		this.configNum = 1;
		
		if(hasClan()) {
			this.clan = QueryManager.loadClan(clanID);
		} else {
			//Empty clan
			this.clan = new Clan(0, "");
		}
		
		this.selectedAmmo = 1;
		
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
		
		public List<Drone> getDrones() { return activeConfig.getDrones(); }
		
		public Rockets getRocket() { return rockets; }
		
		public boolean isPremium() { return isPremium; }
		
		public boolean isAttacking() { return isAttacking; }
		
		public long getExperience() { return experience; }
		
		public long getCredits() { return credits; }
		
		public long getUridium() { return uridium; }
		
		public long getHonor() { return honor; }
		
		public int getLevel() { return level; }
		
		public double getJackpot() { return jackpot; }
		
		public int getRank() { return rank; }
		
		public int getRings() { return rings; }
		
		public Clan clan() { return clan; }
		
		public boolean isJumping() { return isJumping; }
		
		public Equipment getConfig(int num) {
			if(num == 1) {
				return config1;
			} else {
				return config2;
			}
		}
		
		public Equipment activeConfig() { return activeConfig; }
		
		public int configNum() { return configNum; }
		
		public boolean isPlayer() { return isPlayer; }
		
		public int getTargetID() { return targetID; }
		
		public LaserSystem laserSystem() { return laserSystem; }
		
		public int selectedAmmo() { return selectedAmmo; }
	/* @end */
		
	/* set methods */
		public void isMoving(boolean m) { moving = m; }
		
		public void isJumping(boolean j) { isJumping = j; }
		
		public void isAttacking(boolean a) { isAttacking = a; }
		
		public void isPlayer(boolean p) { isPlayer = p; }
		
		public void selectedAmmo(int s) { selectedAmmo = s; }
		
		public void setTarget(int targetID) {
			this.targetID = targetID;
		}
		
		public void setPosition(Vector p) { position = p; }
		
		public void setMapID(short m) { mapID = m; }
		
		/*public void addDrone(int position, Drone newDrone) {
			drones[position] = newDrone;
		}
		
		/*
		 * Para que cuando se compra uno el cambio en el cliente sea instantaneo
		 *
		public void updateDrones() {
			if(GameManager.dronesBought.containsKey(playerID)) {
				this.drones = GameManager.getDrones(playerID);
				
			} else {
				this.drones = QueryManager.loadDrones(playerID);
			}
		}*/
		
		public void setConfig(int configNum, Equipment config) {
			if(configNum == 1) {
				config1 = config;
			} else if(configNum == 2) {
				config2 = config;
			}
		}
		
		public void activeConfig(int i) {
			configNum = i;
			if(i == 1) {
				activeConfig = config1;
			} else {
				activeConfig = config2;
			}
		}
		
		public void setSystems() {
			/*
			 * Porque el constructor del movementHelper necesita que el usuario este online en el connectionManager
			 * asi que hago un metodo para desde el connectionManager iniciarlo cuando quiera...
			 */
			movementSystem = new MovementSystem(playerID);
			laserSystem = new LaserSystem(playerID);
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
		
		//Comprueba el rango respecto a un portal
		public boolean isInRange(Portal p) {
			//Actualiza la posicion del jugador..
			movementSystem.position();
			
			double finalX = position.getX() - p.getPosition().getX();
			double finalY = position.getY() - p.getPosition().getY();
			
			double distance = Math.sqrt(finalX * finalX + finalY * finalY);
			
			if(distance > p.getRange()) {
				return false;
			} else {
				return true;
			}
		}

		//Comprueba el rango respecto a un portal
		public boolean isInRange(Player p) {
			double range = 600;
			//Actualiza la posicion del jugador y del target
			movementSystem.position();
			p.movement().position();
			
			double finalX = position.getX() - p.getPosition().getX();
			double finalY = position.getY() - p.getPosition().getY();
			
			double distance = Math.sqrt(finalX * finalX + finalY * finalY);
			
			if(distance > range) {
				return false;
			} else {
				return true;
			}
		}
		
		//Pinta el rango de ataque
		public void drawRange() {
			Extra.drawRange(mapID, position.getX(), position.getY(), 600);
		}
}