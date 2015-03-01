package com.darkorbit.objects;

public class Ship {
	private short shipID;
	private int shipHealth, shipSpeed, batteries, rockets, maxCargo;
	
	public Ship(short shipID, int shipHealth, int shipSpeed, int batteries, int rockets, int maxCargo) {
		this.shipID = shipID;
		this.shipHealth = shipHealth;
		this.shipSpeed = shipSpeed;
		this.batteries = batteries;
		this.rockets = rockets;
		this.maxCargo = maxCargo;
	}
	
	/* get methods */
		
		public short getShipID() {
			return shipID;
		}
	
		public int getShipHealth() {
			return shipHealth;
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
	/* @end */
		
	
}
