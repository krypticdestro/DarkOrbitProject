package com.darkorbit.objects;

public class Shield extends Object {
	private int id;
	private String lootID;
	
	public Shield(int id, String lootID) {
		this.id = id;
		this.lootID = lootID;
	}
	
	public String getLootID() {
		return lootID;
	}

	public void setLootID(String lootID) {
		this.lootID = lootID;
	}

	public int getValue() {
		/**
		 * Numero de cada tipo de escudo
		 * 
		 * B02 => 10.000 / 80%
		 * B01 => 4.000 / 70%
		 * A03 => 5.000 / 60%
		 * A02 => 2.000 / 50%
		 * A01 => 1.000 / 40%
		 */
		int value = 0;
		switch(lootID) {
			case "sg3n-b02":
				value = 10000;
				break;
			case "sg3n-b01":
				value = 4000;
				break;
			case "sg3n-a03":
				value = 5000;
				break;
			case "sg3n-a02":
				value = 2000;
				break;
			case "sg3n-a01":
				value = 1000;
				break;
			default:
				value = 0;
		}
		
		return value;
	}
	
	public int getID() { 
		return id;
	}
}
