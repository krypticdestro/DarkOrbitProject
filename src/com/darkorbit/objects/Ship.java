package com.darkorbit.objects;

public class Ship {
	private short shipID;
	private int shipHealth, shipShield, shieldAbsorb, shipSpeed, batteries, rockets, maxCargo, minDamage, maxDamage, experience, honor, credits, uridium;
	
	public Ship(short shipID, int shipHealth, int shipShield, int shieldAbsorb, int shipSpeed, int batteries, int rockets, int maxCargo, 
	int minDamage, int maxDamage, int experience, int honor, int credits, int uridium) {
		
		this.shipID = shipID;
		this.shipHealth = shipHealth;
		this.shipShield = shipShield;
		this.shieldAbsorb = shieldAbsorb;
		this.shipSpeed = shipSpeed;
		this.batteries = batteries;
		this.rockets = rockets;
		this.maxCargo = maxCargo;
		this.minDamage = minDamage;
		this.maxDamage = maxDamage;
		this.experience = experience;
		this.honor = honor;
		this.credits = credits;
		this.uridium = uridium;
	}
	
	/* get methods */
		
		public short getShipID() {
			return shipID;
		}
	
		public int getShipHealth() {
			return shipHealth;
		}
		
		public int getShipShield(){
			return shipShield;
		}
		
		public int getShieldAbsorb(){
			return shieldAbsorb;
		}
		
		public int getShipSpeed() {
			return shipSpeed;
		}
		
		public int getBatteries() {
			return batteries;
		}
		
		public int getRockets() {
			return rockets;
		}
		
		public int getMaxCargo() {
			return maxCargo;
		}
		
		public int getMinDamage(){
			return minDamage;
		}
		
		public int getMaxDamage(){
			return maxDamage;
		}
		
		public int getExpercience(){
			return experience;
		}
		
		public int getHonor(){
			return honor;
		}
		
		public int getCredits(){
			return credits;
		}
		
		public int getUridium(){
			return uridium;
		}
	/* @end */
		
	
}
