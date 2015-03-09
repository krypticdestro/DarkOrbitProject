package com.darkorbit.assemblies;

import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;

import com.darkorbit.main.Launcher;
import com.darkorbit.mysql.QueryManager;
import com.darkorbit.net.ConnectionManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.net.Global;
import com.darkorbit.objects.Drone;
import com.darkorbit.objects.Player;
import com.darkorbit.utils.Console;


public class LoginAssembly extends Global {
	private Socket userSocket;
	private int playerID;
	//private long sessionID; no usado aun
	private Player player;
	
	public LoginAssembly(Socket userSocket) {
		this.userSocket = userSocket;
	}
	
	/**
	 * Comprueba si el usuario se puede conectar
	 * @param p Login packet
	 * @return
	 */
	public boolean requestLogin(String[] p) {
		try {
			
			this.playerID = Integer.parseInt(p[1]);
			//this.sessionID = Long.parseLong(p[2]); no usado
			player = QueryManager.loadAccount(playerID);
			
		} catch(Exception e) {
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		//Si la cuenta existe
		if(!player.equals(null)) {

			//Pierde la conexion y se reconecta antes del timeOut 
			if(GameManager.isOnline(player.getPlayerID())) {
				
				try {
					//Cierran los sockets antiguos que tenia abiertos y el timeout
					GameManager.getConnectionManager(player.getPlayerID()).cancelTimeOut();
					GameManager.getConnectionManager(player.getPlayerID()).closeConnection();
					
					//login normal
					startLogin();
					Console.out("Player " + player.getPlayerID() + " reconnected!");
					
					return true;
					
				} catch (IOException e) {
					//Error cerrando los sockets...
					if(Launcher.developmentMode) {
						e.printStackTrace();
					}
					
					return false;
				}
			} else {
				//El timeOut cierra los sockets solito... | Login normal
				startLogin();
				Console.out("Player " + player.getPlayerID() + " connected!");
				return true;
			}
			
		} else {
			//La cuenta no ha podido ser cargada -> no hay login
			return false;
		}
	}
	
	/**
	 * Returns the player object
	 */
	public Player getPlayer() {
		return player;
	}
	
	private void startLogin() {
		//si el login va bien, se mandan los paquetes necesarios..
		setSettings();
		setPlayer();
		//setDrones();
		setAmmunition();
		checkPlayerPosition();
		loadUsers();
		sendMyShip();
	}
	
	/* Login functions */
		
		//Manda las opciones del cliente
		private void setSettings() {
			//Envia al cliente las opciones del juego
			
			sendPacket(userSocket, "0|A|SET|"+ player.getSettings().SET);
			sendPacket(userSocket, "0|7|MINIMAP_SCALE,1|"+player.getSettings().MINIMAP_SCALE);
			sendPacket(userSocket, "0|7|DISPLAY_PLAYER_NAMES|"+ player.getSettings().DISPLAY_PLAYER_NAMES);
			sendPacket(userSocket, "0|7|DISPLAY_CHAT|"+ player.getSettings().DISPLAY_CHAT);
			sendPacket(userSocket, "0|7|PLAY_MUSIC|"+ player.getSettings().PLAY_MUSIC);
			sendPacket(userSocket, "0|7|PLAY_SFX|"+ player.getSettings().PLAY_SFX);
			sendPacket(userSocket, "0|7|BAR_STATUS|"+ player.getSettings().BAR_STATUS);
			sendPacket(userSocket, "0|7|WINDOW_SETTINGS,3|"+ player.getSettings().WINDOW_SETTINGS);
			sendPacket(userSocket, "0|7|AUTO_REFINEMENT|"+ player.getSettings().AUTO_REFINEMENT);
			sendPacket(userSocket, "0|7|QUICKSLOT_STOP_ATTACK|"+ player.getSettings().QUICKSLOT_STOP_ATTACK);
			sendPacket(userSocket, "0|7|DOUBLECLICK_ATTACK|"+ player.getSettings().DOUBLECLICK_ATTACK);
			sendPacket(userSocket, "0|7|AUTO_START|"+ player.getSettings().AUTO_START);
			sendPacket(userSocket, "0|7|DISPLAY_NOTIFICATIONS|"+ player.getSettings().DISPLAY_NOTIFICATIONS);
			sendPacket(userSocket, "0|7|SHOW_DRONES|"+ player.getSettings().SHOW_DRONES);
		}
		
		//Informacion básica del jugador
		private void setPlayer() {
			//0|I|playerID|username|shipID|maxSpeed|shield|maxShield|health|maxHealth|cargo|maxCargo|user.x|user.y|mapId|factionId|clanId|shipAmmo|shipRockets|expansion|premium|exp|honor|level|credits|uridium|jackpot|rank|clanTag|ggates|0|cloaked
			String loginPacket = "0|I|" + player.getPlayerID() + "|" + player.getUserName() + "|" + player.getShipID() + "|" + player.getShip().getShipSpeed() + "|5|10|" + player.getHealth() + "|" + player.getShip().getShipHealth() + "|0|" + player.getShip().getMaxCargo() + "|" + player.getPosition().getX() + "|" + player.getPosition().getY() + "|" + player.getMapID() + "|" + player.getFactionID() + "|0|" + player.getShip().getBatteries() + "|" + player.getShip().getRockets() + "|3|1|1|2|3|124|412312|3|21|CLANTAG|0|0|0";
			sendPacket(userSocket, loginPacket);
		}
		
		
		@SuppressWarnings("unused")
		private void setDrones() {
			Drone[] playerDrones = player.getDrones();
			String packet = "";
			
			//El array tiene 8 posiciones, pero pueden ser null... asi que busco cuantos drones de verdad hay
			int numDrones = 0;
			
			for(int i=0; i<playerDrones.length; i++) {
				if(!(playerDrones[i] == null)) {
					numDrones++;
				}
			}
			
			/*
			 * 0|n|d|5|3/2-96-96,2/4-26-26-26-26,3/2-86-86 -> 8 drones
			 * 0|n|d|5|3/1-96,2/4-26-26-26-26,3/1-86 -> 6 drones
			 * 0|n|d|5|1/4-26-26-26-26 -> 4 drones
			 */
			
			//Para cambiar la posicion del grupo de drones...
			if(numDrones <= 4) {
				packet += "1/" + numDrones;
			}
			
			
			//Recorro el array..
			for(Drone d : playerDrones) {
				if(!(d == null)) {

					if(numDrones > 4) {
						switch(numDrones) {
							case 5:
								packet += "";
								break;
						}
					} else {
						packet += "-" + d.getDronePacket();
					}
					
					
					
					numDrones--;
				}
			}
			
			sendPacket(userSocket, "0|n|d|" + player.getPlayerID() + "|" + packet);
		}
			
		
		//Carga la munición
		private void setAmmunition() {
			// 0|B|x1|x2|x3|x4|sab|rsb
	        sendPacket(userSocket, "0|B|" + player.getAmmo().getLcb10() + "|" + player.getAmmo().getMcb25() + "|" + player.getAmmo().getMcb50() + "|" + player.getAmmo().getUcb100() + "|0");
		}
		
		//Actualiza la posicion de los usuarios para no verlos en su posicion inicial...
		private void checkPlayerPosition() {
			for(Entry<Integer, ConnectionManager> u : GameManager.onlinePlayers.entrySet()) {
				
				if(u.getValue().player().getMapID() == player.getMapID()) {
					//Actualizo la posicion y el moving para falsear el movimiento mas abajo
					u.getValue().player().setPosition(u.getValue().player().movement().position());
				}
			}
		}
				
		//Los usuarios del mismo mapa
		private void loadUsers() {
			//Carga las naves de los jugadores del mismo mapID (cambiar en un futuro para el rango del minimapa)
			for(Entry<Integer, ConnectionManager> u : GameManager.onlinePlayers.entrySet()) {
				
				//Si el mapa es el mismo que el "mio" y el playerID diferente
				if((u.getValue().player().getMapID() == player.getMapID()) && (u.getValue().player().getPlayerID() != player.getPlayerID())) {
					
					//0|C|USERID|SHIPID|EXPANSION|CLANTAG|USERNAME|X|Y|FactionId|CLANID|RANK|WARNICON|CLANDIPLOMACY|GALAXYGATES|NPC|CLOACK
					String packet = "0|C|" + u.getValue().player().getPlayerID() + "|" + u.getValue().player().getShipID() + "|3|CLANTAG|" + u.getValue().player().getUserName() + "|" + u.getValue().player().getPosition().getX() + "|" + u.getValue().player().getPosition().getY() + "|" + u.getValue().player().getFactionID() + "|0|1|0|0|0|0|0";
					sendPacket(userSocket, packet);
					
					if(u.getValue().player().isMoving()) {
						//Si el player estaba moviendose falseo ese movimiento
						sendPacket(userSocket, "0|1|" + u.getValue().player().getPlayerID() + "|" + u.getValue().player().movement().destination().getX() + "|" + u.getValue().player().movement().destination().getY() + "|" + u.getValue().player().movement().timeRemaining());
					}
				}
			}
		}
		
		//Envia mis datos a los usuarios del mapa
		private void sendMyShip() {
			//0|C|USERID|SHIPID|EXPANSION|CLANTAG|USERNAME|X|Y|FactionId|CLANID|RANK|WARNICON|CLANDIPLOMACY|GALAXYGATES|NPC|CLOACK
			String packet = "0|C|" + player.getPlayerID() + "|" + player.getShipID() + "|3|CLANTAG|" + player.getUserName() + "|" + player.getPosition().getX() + "|" + player.getPosition().getY() + "|" + player.getFactionID() + "|0|1|0|0|0|0|0";
			sendToMap(player.getMapID(), packet);
		}
		
}
