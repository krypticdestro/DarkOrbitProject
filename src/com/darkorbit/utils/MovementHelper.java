package com.darkorbit.utils;

import java.util.Calendar;

import com.darkorbit.net.GameManager;
import com.darkorbit.net.Global;
import com.darkorbit.objects.Player;

public class MovementHelper extends Global {
	private final double vtRel = 0.84412; //Relacion velocidad juego original
	
	private Vector destination, oldPosition, direction;
	private double time, distance, timeRemaining;
	private long lastMove, timeElapsed;
	private Player player;
	
	public MovementHelper(int playerID) {
		this.player = GameManager.getConnectionManager(playerID).player();
	}
	
	/**
	 * Mueve la nave del jugador
	 * @param p Paquete de movimiento
	 */
	public void moveShip(String[] p) {
		//	1|destX|destY|currX|currY
		destination = new Vector(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
		
		oldPosition = new Vector(Integer.parseInt(p[3]), Integer.parseInt(p[4]));
		
		int vel = player.getShip().getShipSpeed(); //Tener en cuenta que luego la velocidad aumentara con el equipamiento
				
		direction = new Vector(destination.getX() - oldPosition.getX(), destination.getY() - oldPosition.getY());
		
		distance = oldPosition.distanceTo(destination);
		
		time = (distance / (vel * vtRel)) * 1000;
		
		if((oldPosition.getX() != destination.getX()) || (oldPosition.getY() != destination.getY())) {
			player.isMoving(true);
		} else {
			player.isMoving(false);
		}
		
		lastMove = Calendar.getInstance().getTimeInMillis();
		
		
		
		String movePacket = "0|1|" + player.getPlayerID() + "|" + destination.getX() + "|" + destination.getY() + "|" + time;
		sendToMap(player.getMapID(), movePacket);
	}
	
	/**
	 * Devuelve la posicion del usuario
	 * @return
	 */
	public Vector position() {
		timeElapsed = Calendar.getInstance().getTimeInMillis() - lastMove;
		
		if(player.isMoving()) {
			if(timeElapsed < time) {
				//Usuario se esta moviendo, devuelve position
				player.isMoving(true);
				
				timeRemaining = time - timeElapsed;
				
				return new Vector(oldPosition.getX() + (direction.getX() * (timeElapsed / time)), oldPosition.getY() + (direction.getY() * (timeElapsed / time)));
			} else {
				//ya ha llegado...
				player.isMoving(false);
				player.setPosition(new Vector(destination.getX(), destination.getY()));
				
				return player.getPosition();
			}
		} else {
			return player.getPosition();
		}
	}
	
	public double timeRemaining() {
		return timeRemaining;
	}
	
	public Vector destination() {
		return destination;
	}
}
