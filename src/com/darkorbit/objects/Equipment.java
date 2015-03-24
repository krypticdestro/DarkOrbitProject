package com.darkorbit.objects;

public class Equipment {
	private int currentShield;
	/**
	 * Numero de cada tipo de escudo
	 * 
	 * B02 => 10.000 / 80%
	 * B01 => 4.000 / 70%
	 * A03 => 5.000 / 60%
	 * A02 => 2.000 / 50%
	 * A01 => 1.000 / 40%
	 */
	private int numB02, numB01, numA03, numA02, numA01, numG3N79, numG3N69, numG3N33, numG3N32, numG3N20, numG3N10;
	/**
	 * Daño de los laseres
	 * 
	 * LF3 => 150
	 * LF2 => 100
	 * MP1 => 60
	 * LF1 => 40
	 */
	private int numLF3, numLF2, numMP1, numLF1;
	
	public Equipment(int currentShield, int B02, int B01, int A03, int A02, int A01, int G3N79, int G3N69, int G3N33, int G3N32,
			int G3N20, int G3N10, int LF3, int LF2, int MP1, int LF1) {
		
		this.currentShield = currentShield;
		this.numB02 = B02;
		this.numB01 = B01;
		this.numA03 = A03;
		this.numA02 = A02;
		this.numA01 = A01;
		this.numG3N79 = G3N79;
		this.numG3N69 = G3N69;
		this.numG3N33 = G3N33;
		this.numG3N32 = G3N32;
		this.numG3N20 = G3N20;
		this.numG3N10 = G3N10;
		this.numLF3 = LF3;
		this.numLF2 = LF2;
		this.numMP1 = MP1;
		this.numLF1 = LF1;
	}
	
	/* get methods */
		
		public int getShield() {
			int maxShield = (numB02 * 10000) + (numB01 * 4000) + (numA03 * 5000) + (numA02 * 2000) + (numA01 * 1000);
			if(maxShield <= 0) {
				/*
				 * Hago esto porque si el escudo es 0 se ve toda la barra de escudo llena, aunque sea 0 de 0 => 0|0
				 */
				return 1;
			} else {
				return maxShield;
			}
		}
		
		public int getSpeed() {
			return (numG3N79 * 10) + (numG3N69 * 7) + (numG3N33 * 5) + (numG3N32 * 4) + (numG3N20 * 3) + (numG3N10 * 2);
		}
		
		public int getDamage() {
			return (numLF3 * 150) + (numLF2 * 100) + (numMP1 * 60) + (numLF1 * 40);
		}
		
		public int getCurrentShield() { return currentShield; }
	/* @end */
		
	/* set methods */
		public void setCurrentShield(int s) { currentShield = s; }
		
}
