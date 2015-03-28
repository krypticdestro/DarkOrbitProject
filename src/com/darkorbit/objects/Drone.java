package com.darkorbit.objects;

import java.util.List;

public class Drone {
	private int id, level, dronePacket;
	private String type;
	private List<Shield> shields;
	private List<Laser> lasers;

	public Drone(int id, int level, String type, List<Shield> shields, List<Laser> lasers) {
		this.id = id;
		this.level = level;
		this.type = type;
		this.shields = shields;
		this.lasers = lasers;
		
		/*
		 * 10 -> flax (hercules)
		 * 20 -> iris
		 * 30 -> iris (hercules)
		 * 40 -> iris (havok)
		 * 50 -> zeus
		 * 70 -> apis
		 */
		switch(type) {
			case "iris":
				//20 es el codigo del iris
				dronePacket = 20 + level;
				break;
				
			case "flax":
				//Flax hercules, no se el pacquete del normal :/
				dronePacket = 10 + level;
				break;
				
		}
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getType() {
		return type;
	}

	public List<Shield> getShields() {
		return shields;
	}

	public List<Laser> getLasers() {
		return lasers;
	}
	
	public int getDronePacket() {
		return dronePacket;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setShields(List<Shield> shields) {
		this.shields = shields;
	}

	public void setLasers(List<Laser> lasers) {
		this.lasers = lasers;
	}
}
