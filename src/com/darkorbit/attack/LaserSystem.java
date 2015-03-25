package com.darkorbit.attack;

import java.net.Socket;
import java.util.Random;

import com.darkorbit.net.GameManager;
import com.darkorbit.net.Global;
import com.darkorbit.objects.Player;

public class LaserSystem extends AttackController {
	private Socket playerSocket, targetSocket;
	private Player target;
	private int minDamage, maxDamage;
	
	public LaserSystem(int playerID) {
		super(playerID);
		
		playerSocket = super.getPlayerCM().getSocket();
	}

	@Override
	public void doDamage() {
		/*
		 * El daño variara un 15%
		 */
		minDamage = (int) (player.activeConfig().getDamage() * 0.80);
		maxDamage = (int) (player.activeConfig().getDamage() * 1.20);
		
		if(player.isPlayer()) {
			//Comprueba que el target este online
			if(GameManager.isOnline(player.getTargetID())) {
				target = GameManager.getConnectionManager(player.getTargetID()).player();
				targetSocket = GameManager.getConnectionManager(player.getTargetID()).getSocket();
			} else { target = null; }
		} else {
			//el target es un NPC
		}
		
		if((target != null) && (target.getHealth() > 0)) {
			if(player.isInRange(target)) {
				//Comienza el ataque!
				int damage;
				if(maxDamage <= 0 || minDamage <= 0) {
					damage = 0;
				} else {
					damage = new Random().nextInt(maxDamage-minDamage) + minDamage;
				}
				
				//Multiplico el daño por la municion elegida, si es sab(5) x2
				if(player.selectedAmmo() == 5) {
					damage *= 2;
				} else {
					damage *= player.selectedAmmo();
				}
				
				//Selecciono el gráfico del laser
				int laserGfx = 0;
				switch(player.selectedAmmo()) {
					case 1:
						laserGfx = 1;
						break;
					case 2:
						laserGfx = 1;
						break;
					case 3:
						laserGfx = 2;
						break;
					case 4:
						laserGfx = 3;
						break;
					case 5:
						laserGfx = 4;
						break;
				}
				
				if(damage > 0) {
					//Envia el daño causado
					String showDMG = "0|Y|" + player.getPlayerID() + "|" + target.getPlayerID() + "|L|" + target.getHealth() + "|" + target.activeConfig().getCurrentShield() + "|" + damage + "|100|1";
					Global.sendPacket(playerSocket, showDMG);
					Global.sendPacket(targetSocket, showDMG);
					
					//Envia el laser
					String showLaser = "0|a|" + player.getPlayerID() + "|" + target.getPlayerID() + "|" + laserGfx + "|0|0";
					Global.sendPacket(playerSocket, showLaser);
					Global.sendPacket(targetSocket, showLaser);
				} else {
					//No tienes daño | laseres
					Global.sendPacket(playerSocket, "0|A|STM|no_lasers_on_board");
					player.isAttacking(false);
				}
			} else {
				//No esta en rango
				Global.sendPacket(playerSocket, "0|A|STM|outofrange");
			}
		}
	}
}
