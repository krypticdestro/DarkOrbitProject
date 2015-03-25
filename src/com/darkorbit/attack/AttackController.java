package com.darkorbit.attack;

import com.darkorbit.net.ConnectionManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.net.Global;
import com.darkorbit.objects.Player;

public abstract class AttackController extends Global implements Runnable {
	private ConnectionManager playerCM;
	protected Player player;
	private Thread thread;
	
	public AttackController(int playerID) {
		this.playerCM = GameManager.getConnectionManager(playerID);
		this.player = playerCM.player();
		this.thread = new Thread(this, "AttackSystem Thread player" + player.getPlayerID());
		thread.start();
	}

	public synchronized void startAttack() {
		//Comprueba si el jugador esta atacando para despertar al hilo
        if(player.isAttacking())
            notify();
    }
	
	public void run() {
		/*
		 * Usado para poder hacer thread.stop(); de forma segura
		 */
		Thread currentThread = Thread.currentThread();
		
		while(currentThread == thread) {
	        try {
				//'suspendo' el hilo mientras el jugador no ataque
	            synchronized(this) {
	                while (!player.isAttacking())
	                    wait();
	            }
	            
	            doDamage();
		        Thread.sleep(1000);
	        } catch (InterruptedException e){
	        }
		}
		
		//Esta aqui para que al cerrar el hilo vuelva a ser falso
		player.isAttacking(false);
	}
	
	protected ConnectionManager getPlayerCM() { return playerCM; }
	
	public synchronized void close() {
		player.isAttacking(true);
		notifyAll();
		thread = null;
	}
	
	public abstract void doDamage();
}