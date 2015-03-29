package com.darkorbit.objects;

import java.util.List;

public class Equipment {
	private Generators generators;
	private Weapons weapons;
	private List<Drone> drones;
	private int currentShield;
	
	public Equipment(Generators generators, Weapons weapons, List<Drone> drones, int currentShield) {
		this.generators = generators;
		this.weapons = weapons;
		this.drones = drones;
		this.currentShield = currentShield;
	}

	public List<Drone> getDrones() {
		return drones;
	}

	public void setDrones(List<Drone> drones) {
		this.drones = drones;
	}

	public Generators getGenerators() {
		return generators;
	}

	public Weapons getWeapons() {
		return weapons;
	}
	
	public void setGenerators(Generators generators) {
		this.generators = generators;
	}

	public void setWeapons(Weapons weapons) {
		this.weapons = weapons;
	}
	
	public int getCurrentShield() {
		return currentShield;
	}

	public void setCurrentShield(int currentShield) {
		this.currentShield = currentShield;
	}
	
	public int getShield() {
		int shield = 0;
		if(drones.size() > 0) {
			for(Drone d : drones) {
				for(Shield s : d.getShields()) {
					shield += s.getValue();
				}
			}
		}
		
		for(Shield s : generators.getShields()) {
			shield += s.getValue();
		}
		
		return shield;
	}
	
	public int getSpeed() {
		int speed = 0;
		for(Engine e : generators.getEngines()) {
			speed += e.getValue();
		}
		
		return speed;
	}
	
	public int getDamage() {
		int damage = 0;
		if(drones.size() > 0) {
			for(Drone d : drones) {
				for(Laser l : d.getLasers()) {
					damage += l.getValue();
				}
			}
		}
		
		for(Laser l : weapons.getLasers()) {
			damage += l.getValue();
		}
		
		return damage;
	}
}
