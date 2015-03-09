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
import com.darkorbit.packets.ServerCommands;
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
		setDrones(player);
		setAmmunition();
		setRocketsAndMines();
		setExtras();
		checkPlayerPosition();
		loadUsers();
		sendMyShip();
		loadHUD();
		sendStations();
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
			
			
			//selecciona config 1 -> en el paquete mando por defecto datos de la config 1
	        sendPacket(userSocket, "0|A|CC|1");
		}
		
		/*
		 * Envia la informacion para cargar los drones
		 * TODO: Rehacer de una mejor forma..
		 */
		private void setDrones(Player p) {
			Drone[] playerDrones = p.getDrones();
			String packet = "";
			
			//Suponiendo que la nave mira hacia arriba, ire poniendolos true cuando el grupo de drones se haya llenado
			
			//El array tiene 8 posiciones, pero pueden ser null... asi que busco cuantos drones de verdad hay
			int numDrones = 0;
			
			for(int i=0; i<playerDrones.length; i++) {
				if(!(playerDrones[i] == null)) {
					numDrones++;
				}
			}
			
			//Si el usuario tiene activados los drones
			if(p.getSettings().SHOW_DRONES.equals("1")) {
				while(numDrones > 0) {
					
					/* Si solo se tienen entre 0 y 4 drones (bottom group) */
					if((numDrones > 0) && (numDrones <= 4)) {
						//0|n|d|5|1/4-26-26-26-26 -> 4 drones
						packet += "1/" + numDrones;
						
						for(int i=0; i < numDrones; i++) {
							packet += "-" + playerDrones[i].getDronePacket();
						}
						
						//En esta parte del condicional se ponen del dron 1-4 asi que igualo a 0 para salir del bucle
						numDrones = 0;
					} else {
						/*
						 * 0|n|d|5|3/2-96-96,2/4-26-26-26-26,3/2-86-86 -> 8 drones
						 * 0|n|d|5|3/1-96,2/4-26-26-26-26,3/1-86 -> 6 drones
						 * 0|n|d|5|1/4-26-26-26-26 -> 4 drones
						 */
						
						switch(numDrones) {
							//Si tiene 5 drones (4 bottom, 1 right)
							case 5:
								packet += "3/1"; //right
								
								//playerDrones[4] -> porque es un array, dron 5 = [4]
								packet += "-" + playerDrones[4].getDronePacket() + ",";
								numDrones--;
								break;
							
							//6 drones (4 bot, 1 right, 1 left)
							case 6:
								packet += "3/1"; //right
								
								//playerDrones[4] -> porque es un array, dron 5 = [4]
								packet += "-" + playerDrones[4].getDronePacket() + ",";
								
								//4 bot
								packet += "2/4";
								
								for(int i=0; i < 4; i++) {
									packet += "-" + playerDrones[i].getDronePacket();
								}
								
								packet += ",";
								
								//por ahora iria asi -> 0|n|d|5|3/1-96,2/4-26-26-26-26,
								
								packet += "3/1";
								packet += "-" + playerDrones[5].getDronePacket() ;
								
								//Para salir del bucle
								numDrones = 0;
								break;
								
							//7 drones (4 bot, 2 right, 1 left)
							case 7:
								packet += "3/2"; //right
								
								//playerDrones[4] -> porque es un array, dron 5 = [4]
								packet += "-" + playerDrones[4].getDronePacket() + "-" + playerDrones[6].getDronePacket() + ",";
								
								//4 bot
								packet += "2/4";
								
								for(int i=0; i < 4; i++) {
									packet += "-" + playerDrones[i].getDronePacket();
								}
								
								packet += ",";
								
								//por ahora iria asi -> 0|n|d|5|3/1-96,2/4-26-26-26-26,
								
								packet += "3/1";
								packet += "-" + playerDrones[5].getDronePacket() ;
								
								//Para salir del bucle
								numDrones = 0;
								break;
								
							//8 drones (4 bot, 2 right, 2 left)
							case 8:
								packet += "3/2"; //right
								
								//playerDrones[4] -> porque es un array, dron 5 = [4]
								packet += "-" + playerDrones[4].getDronePacket() + "-" + playerDrones[6].getDronePacket() + ",";
								
								//4 bot
								packet += "2/4";
								
								for(int i=0; i < 4; i++) {
									packet += "-" + playerDrones[i].getDronePacket();
								}
								
								packet += ",";
								
								//por ahora iria asi -> 0|n|d|5|3/1-96,2/4-26-26-26-26,
								
								packet += "3/2";
								packet += "-" + playerDrones[5].getDronePacket() + "-" + playerDrones[7].getDronePacket();
								
								//Para salir del bucle
								numDrones = 0;
								break;
						}
					}
				}
				

				sendPacket(userSocket, "0|n|d|" + p.getPlayerID() + "|" + packet);
				
			} else {
				//Si el usuario no quiere ver los drones
				int numIris = 0;
				int numFlax = 0;

				for(Drone d : playerDrones) {
					if(!(d == null)) {
						if(d.getDoneKind().equals("drone_iris")) {
							numIris++;
						} else {
							numFlax++;
						}
					}
				}
				//Send F:******* I:******* packet
				sendPacket(userSocket, "0|n|e|" + p.getPlayerID() + "|" + numFlax + "/" + numIris);
			}
		}
		
		//Carga la munición
		private void setAmmunition() {
			// 0|B|x1|x2|x3|x4|sab|rsb
	        sendPacket(userSocket, "0|B|" + player.getAmmo().getLcb10() + "|" + player.getAmmo().getMcb25() + "|" + player.getAmmo().getMcb50() + "|" + player.getAmmo().getUcb100() + "|0");
		}
		
		//Carga los misiles y minas
		private void setRocketsAndMines() {
			//0|3|m1|m2|m3|m4|PLD-8|DRC|Wiz|minas|SMB|ISH|PEM|MinaPem|mina2|mina3
	        sendPacket(userSocket, "0|3|" + player.getRocket().getR310() + "|" + player.getRocket().getPlt2026() + "|" + player.getRocket().getPlt2021() + "|" + player.getRocket().getPlt3030() + "|0|0|0|0|0|0|0|0|0");
		}
	
		//Carga los extras -> TODO:
		private void setExtras() {
			//Inicializa los extras vacios. TODO:
			sendPacket(userSocket, "0|A|ITM|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0");
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
					
					setDrones(u.getValue().player());
					
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
		
		//Varios paquetes sobre el cliente
		private void loadHUD() {
			//quita icono de ayuda
			sendPacket(userSocket, "0|UI|W|HW|11");
			
			//quita icono de grupo
			sendPacket(userSocket, "0|UI|W|HW|23");
			
			//quita chat
			sendPacket(userSocket, "0|UI|W|HW|20");
			
			//crea menu de compra rapida
			sendPacket(userSocket, "0|g|a|b,1000,1,10000.0,C,2,500.0,U,3,1000.0,U,5,1000.0,U|r,100,1,10000,C,2,50000,C,3,500.0,U,4,700.0,U");
		}

		//Crea las estaciones espaciales
		private void sendStations() {
				switch(player.getMapID())
				{
					//hero is in 1-1, send MMO station
					case 1:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|redStation|1|1500|1000|1000");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|1");
						break;
					//hero is in 2-1, send EIC station
					case 5:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|blueStation|2|1500|19500|1250");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|2");
						break;
					//hero is in 3-1, send VRU station
					case 9:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|greenStation|3|1500|19500|12500");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|3");
						break;
					//hero is in 1-8, send MMO station
					case 20:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|redStation|1|1500|1000|6200");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|3");
						break;
					//hero is in 2-8, send EIC station
					case 24:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|blueStation|2|1500|10000|1000");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|3");
						break;
					//hero is in 3-8, send VRU station
					case 28:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|greenStation|3|1500|20000|6200");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|3");
						break;
					//hero is in 5-2, send Pirate station
					case 92:
						sendPacket(userSocket, "0|" + ServerCommands.CREATE_STATION + "|0|1|pirateStation|6|1500|11000|6400");
						sendPacket(userSocket, "0|" + ServerCommands.CHANGE_HEALTH_STATION_STATUS + "|1");
						sendPacket(userSocket, "0|" + ServerCommands.SET_MAP_PVP_STATUS + "|1|3");
						break;
				}
			}

}
