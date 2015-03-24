package com.darkorbit.objects;

public class Drone {
	private int droneID, droneLevel, dronePacket;
	private String droneKind;
	private Equipment EQ, EQ2;

	public Drone(int droneID, int droneLevel, String droneKind) {
		this.droneID = droneID;
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
		
		this.EQ = null;
		this.EQ2 = null;
	}
	
	/* get methods */
		
		public int getDroneID() { return droneID; }
	
		public int getDroneLevel() {
			return droneLevel;
		}
		
		public String getDoneKind() {
			return droneKind;
		}
		
		public int getDronePacket() {
			return dronePacket;
		}
	/* @end */
		
	/* set methods */
		
		public void setEQ(int configNum, int currentShield, int B02, int B01, int A03, int A02, int A01, int G3N79, int G3N69, int G3N33, int G3N32,
				int G3N20, int G3N10, int LF3, int LF2, int MP1, int LF1) {
			
			if(configNum == 1) {
				EQ = new Equipment(currentShield, B02, B01, A03, A02, A01, G3N79, G3N69, G3N33, G3N32, G3N20, G3N10, LF3, LF2, MP1, LF1);
			} else if(configNum == 2) {
				EQ2 = new Equipment(currentShield, B02, B01, A03, A02, A01, G3N79, G3N69, G3N33, G3N32, G3N20, G3N10, LF3, LF2, MP1, LF1);
			}
		}
}
