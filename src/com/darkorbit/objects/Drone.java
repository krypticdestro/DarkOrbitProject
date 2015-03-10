package com.darkorbit.objects;

public class Drone {
	private int droneLevel, dronePacket;
	private String droneKind;
	
	public Drone(int droneLevel, String droneKind) {
		this.droneLevel = droneLevel;
		this.droneKind = droneKind;
		
		String[] arrayKind = droneKind.split("_");
		
		/*
		 * 10 -> flax (hercules)
		 * 20 -> iris
		 * 30 -> iris (hercules)
		 * 40 -> iris (havok)
		 * 50 -> zeus
		 * 70 -> apis
		 */
		switch(arrayKind[1]) {
			case "iris":
				//20 es el codigo del iris
				dronePacket = 20 + droneLevel;
				break;
				
			case "flax":
				//Flax hercules, no se el pacquete del normal :/
				dronePacket = 10 + droneLevel;
				break;
				
		}
	}
	
	/* get methods */
		public int getDroneLevel() {
			return droneLevel;
		}
		
		public String getDoneKind() {
			return droneKind;
		}
		
		public int getDronePacket() {
			return dronePacket;
		}
		
}
