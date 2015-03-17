package com.darkorbit.objects;

public class Equipment {
	/**
	 * Numero de cada tipo de escudo
	 * 
	 * B02 => 10.000 / 80%
	 * B01 => 4.000 / 70%
	 * A03 => 5.000 / 60%
	 * A02 => 2.000 / 50%
	 * A01 => 1.000 / 40%
	 */
	private int numB02, numB01, numA03, numA02, numA01;
	
	public Equipment(int B02, int B01, int A03, int A02, int A01) {
		this.numB02 = B02;
		this.numB01 = B01;
		this.numA03 = A03;
		this.numA02 = A02;
		this.numA01 = A01;
	}
	
	/* get methods */
		public int getShield() {
			return (numB02 * 10000) + (numB01 * 4000) + (numA03 * 5000) + (numA02 * 2000) + (numA01 * 1000);   
		}
		
		
}
