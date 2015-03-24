package com.darkorbit.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.darkorbit.assemblies.LoginAssembly;
import com.darkorbit.main.Launcher;
import com.darkorbit.mysql.QueryManager;
import com.darkorbit.objects.Drone;
import com.darkorbit.objects.Player;
import com.darkorbit.objects.Portal;
import com.darkorbit.packets.ClientCommands;
import com.darkorbit.packets.ServerCommands;
import com.darkorbit.packets.WebCommands;
import com.darkorbit.utils.Console;

/**
 * Administra las conexiones entrantes
 * @author Borja Sanchidriï¿½n
 */

public class ConnectionManager extends Global implements Runnable {

	private BufferedReader in;
	private Socket userSocket;
	private Thread thread;
	private Player player = null;
	private Timer timeOutTimer, jumpTimer, configTimer;
	
	private LoginAssembly loginAssembly;
	
	private int playerID = 0;
	private int idle = 0;
	private final int maxIdle = 24; // 1 = 25 segundos ; 24 = 600 segundos = 10 minutos
	private long lastPacket = 0;
	private boolean portalFound = false;
	private boolean configChanged = false;
	public boolean timedOut = false;

	

	public ConnectionManager(Socket userSocket) {
		this.userSocket = userSocket;
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Inicia los streams del socket para poder leer/escribir datos
	 */
	private void setStreams() {
		try {
			//entrada
			in = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (IOException e) {
			Console.error("There was an error setting up the socket streams...");

			if (Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Devuelve la cuenta del jugador
	 * @return
	 */
	public Player player() {
		return player;
	}
	
	public Socket getSocket() {
		return userSocket;
	}
	
	/**
	 * Inicia el timer del timeout
	 */
	private void startTimeOut() {
		final int timeOut = 25000;
		final int minTimeOut = 10000;
		
		
		timeOutTimer = new Timer("TimeOut - Player " + playerID);
		timeOutTimer.schedule(new TimerTask() {
						
			public void run() {
				
				if(!timedOut) {
					long timeElapsed = Calendar.getInstance().getTimeInMillis() - lastPacket;
					
					//En caso de que haya pasado mas de 'timeOut' desde el ultimo paquete se inicia la desconexion 
					if(timeElapsed >= timeOut) {
						try {
							//espera otros 10 segundos mas para asegurar que el usuario se ha desconectado
							Thread.sleep(minTimeOut);
							disconnectPlayer();
							timedOut = true;
							
							Console.alert("Player " + playerID + " time-out");
						} catch (InterruptedException | IOException e) {
							if(Launcher.developmentMode) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}, 0, 30000);
		
	}
	
	/**
	 * Cancela los timeOut y cierra sockets y streams
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		timeOutTimer.cancel();
		timeOutTimer.purge();
		player.movement().close();
		userSocket.close();
		in.close();
	}		
	
	/**
	 * Desconecta al usuario y closeConnection() | usado en loginAssembly too
	 * @throws IOException
	 */
	public void disconnectPlayer() throws IOException {
		
		//TODO: if(player.canDisconnect()) - por si le estan atacando, etc...
		//Actualiza la posicion del usuario para que al guardarla sea la "ultima conocida"
		player.movement().position();
		saveData();
		GameManager.updatePlayer(player);
		
		//Borra al usuario del mapa
		sendToMap(player.getMapID(), "0|R|" + playerID);
		Console.out("Player " + player.getPlayerID() + " disconnected or exceeded max idle time");
		GameManager.onlinePlayers.remove(playerID);
		closeConnection();
	}
	
	/**
	 * Funcion aparte para la desconexion por salto en portal
	 * no tiene que cerrar sockets ni nada, solo cancelar los timers..
	 * @throws IOException
	 */
	public void jumpDisconnetion() throws IOException {
		//TODO: if(player.canDisconnect()) - por si le estan atacando, etc... (PVP) dat stuff
		saveData();
		
		GameManager.onlinePlayers.remove(playerID);
		timeOutTimer.cancel();
		timeOutTimer.purge();
		//TODO BIATCH
		player.movement().close();
	}
	
	/**
	 * Guarda toda la informacion necesaria..
	 */
	private void saveData() {
		savePlayerData();
		savePlayerSettings();
	}
	
	private void savePlayerData() {
		String query = "";
		/* player data */
		
			//Default player update query
			query = "UPDATE server_1_players SET ";
			
			//Actualizo y guardo la posicion del usuario
			query += "x=" + player.getPosition().getX() + ", y=" + player.getPosition().getY() + ", mapId=" + player.getMapID() + ", ";
			query += "shield1=" + player.config1().getCurrentShield() + ", shield2=" + player.config2().getCurrentShield();
			query += " WHERE playerID=" + player.getPlayerID();
		
			QueryManager.updateSql(query);
			query = "";
		/* @end */
	}
	
	private void savePlayerSettings() {
		String query = "";
		/* player settings */
			
			//Default player update query
			query = "UPDATE server_1_players_settings SET ";
			
			query += "AUTO_REFINEMENT=" + player.getSettings().AUTO_REFINEMENT + ", QUICKSLOT_STOP_ATTACK=" + player.getSettings().QUICKSLOT_STOP_ATTACK + ", ";
			query += "DOUBLECLICK_ATTACK=" + player.getSettings().DOUBLECLICK_ATTACK + ", AUTO_START=" + player.getSettings().AUTO_START + ", ";
			query += "DISPLAY_NOTIFICATIONS=" + player.getSettings().DISPLAY_NOTIFICATIONS + ", SHOW_DRONES=" + player.getSettings().SHOW_DRONES + ", ";
			query += "DISPLAY_WINDOW_BACKGROUND=" + player.getSettings().DISPLAY_WINDOW_BACKGROUND + ", ALWAYS_DRAGGABLE_WINDOWS=" + player.getSettings().ALWAYS_DRAGGABLE_WINDOWS + ", ";
			query += "PRELOAD_USER_SHIPS=" + player.getSettings().PRELOAD_USER_SHIPS + ", QUICKBAR_SLOT='" + player.getSettings().QUICKBAR_SLOT + "', ";
			query += "MAINMENU_POSITION='" + player.getSettings().MAINMENU_POSITION + "', SLOTMENU_POSITION='" + player.getSettings().SLOTMENU_POSITION + "', ";
			query += "SLOTMENU_ORDER=" + player.getSettings().SLOTMENU_ORDER + ", SETTINGS='" + player.getSettings().SET + "', WINDOW_SETTINGS='" + player.getSettings().WINDOW_SETTINGS + "'";
			
			query += " WHERE playerID=" + player.getPlayerID();
			
			QueryManager.updateSql(query);
			query = "";
		/* @end */
	}
	
	/**
	 * Lee la entrada del socket
	 */
	public void run() {
		//Llama a la funcion de arriba
		setStreams();
		
		try {
			String packet = "";
			char[] packetChar = new char[1];
			
			while(in.read(packetChar, 0, 1) != -1) {
				//Comprueba que el caracter no sea ni nulo, espacio en blanco, linea nueva
				if(packetChar[0] != '\u0000' && packetChar[0] != '\n' && packetChar[0] != '\r') {
					//Si no aï¿½ade el caracter a packet
					packet += packetChar[0];
					
				} else if(!packet.isEmpty()) {
					packet = new String(packet.getBytes(), "UTF-8");
					
					checkPacket(packet);
					lastPacket = Calendar.getInstance().getTimeInMillis();
					
					/*
					 * Si el paquete es un ping aumento la variable idle en 1, sino resto 1...
					 * 
					 * De esta forma si se reciven X pings seguidos se considera timeOut
					 */
					if(packet.equals("PNG")) {
						if(idle >= (maxIdle - 1)) {
							disconnectPlayer();
						} else {
							idle++;
						}
					} else {
						idle = 0;
					}
					
					//Set the packet again to ""
					packet = "";
				}
			}
		} catch(IOException e) {
			Console.alert("User disconnected. Socket closed.");
			
			//Un poco chapuza, pero no se me ocurre ahora mismo algo mejor :(
			try {
				closeConnection();
			} catch (IOException e1) {}
		}
	}
	
	
	/**
	 * Lee e interpreta los paquetes recibidos.
	 * @param packet Paquete enviado desde run
	 */
	private void checkPacket(String packet) {
		//Si el modo desarrollador esta activado se leen los paquetes entrantes
		if(Launcher.developmentMode) {
			Console.alert("Packet from user-" + playerID + ": " + packet);
		}
		
		if(packet.startsWith("/")) {
			String[] p = packet.split(" ");
			
			switch(p[0]) {
				case "/p":
					//Envia un paquete directamente al usuario
					try {
						sendToMap(player.getMapID(), p[1]);
					} catch(Exception e) {
						if(Launcher.developmentMode) {
							e.printStackTrace();
						}
					}
					break;
				
				// /portalsRange <true/false> - Shows the portal range (development tool)
				case "/portalsRange":
					try {
						if(Boolean.parseBoolean(p[1]) == true) {
							for(Entry<Integer, Portal> p2 : GameManager.portals.entrySet()) {
								if(player.getMapID() == p2.getValue().getMapID()) {
									p2.getValue().drawRange();
								}
							}
						} else {
							for(Entry<Integer, Integer> ship : GameManager.rangeShips.entrySet()) {
								sendToMap(player.getMapID(), "0|n|EMP|" + (ship.getValue() - 1));
								sendToMap(player.getMapID(), "0|K|" + (ship.getValue() - 1)); //No me preguntes muy bien porque xD
							}
						}
						
					} catch(Exception e) {
						sendPacket(userSocket, "0|A|STM|wrong_packet_parameter");
						if(Launcher.developmentMode) {
							e.printStackTrace();
						}
					}
					break;
				
				// /close <seconds> - Shutdown the server
				case "/close":
					try {
						int seconds = Integer.parseInt(p[1]);
						Timer closeTimer = new Timer("closeTimer");
						closeTimer.schedule(new TimerTask() {
							public void run() {
								if(seconds >= 0) {
									for(int i=seconds; i>0; i--) {
										sendToAll("0|A|STM|server_close_n_seconds|" + i);
										Console.alert("Closing server in " + i);
										try {
											Thread.sleep(1000);
										} catch (InterruptedException ignore) { }
									}
									
									for(Entry<Integer, ConnectionManager> c : GameManager.onlinePlayers.entrySet()) {
										try {
											c.getValue().disconnectPlayer();
										} catch (IOException ignore) { }
									}
									Console.out("Server shutdown");
									System.exit(0);
								}
								
								closeTimer.cancel();
								closeTimer.purge();
							}
						}, 0);
					} catch(Exception e) {
						sendPacket(userSocket, "0|A|STD|You should introduce the ammount of time (in seconds)");
					}
					break;
					
				// /msg <message> - sends a global message
				case "/msg":
					try {
						String msg = "";
						for(int i=1; i<p.length; i++) {
							msg += p[i] + " ";
						}
						sendToAll("0|A|STD|" + msg + "\nGlobal message from: " + player.getUserName());
					} catch(Exception e) {
						sendPacket(userSocket, "0|A|STD|Introducce the message!");
					}
					break;
			}
			
		} else {
			String[] p = packet.split("\\|");
			
			switch(p[0]) {
			/* #############################WEB PACKETS############################# */
				case WebCommands.WEB_PACKET:
					switch(p[1]) {
						case WebCommands.EQUIPMENT_UPDATE:
							//webPacket|equipment|TYPE|PLAYERID|CONFIGNUM|27|16|18 objects
							QueryManager.checkObject(packet);
							break;
							
						case WebCommands.DRONE_EQUIPMENT_UPDATE:
							//webPacket|droneEquipment|DRONEID|PLAYERID|CONFIGNUM|ITEMS[]
							QueryManager.checkObject(packet);
							break;
							
						case WebCommands.BUY_DRONE:
							//Obtengo la cuenta del jugador que compra el vant
							//webPacket|buydrone|".$playerID."|".$kind."|".$item_id
							player = QueryManager.loadAccount(Integer.parseInt(p[2]));
							int dronPosition = 0;
							/*
							 * El array de drones tiene 8 posiciones, pero pueden ser nulas, por lo que veo el numero real ocupadas..
							 */
							for(int i=0; i<player.getDrones().length; i++) {
								if(!(player.getDrones()[i] == null)) {
									dronPosition++;
								}
							}
							//numDrones puede ir de 0 a 7 => 8 drones
							
							//Y añado el dron al array
							if(dronPosition < 8) {
								player.addDrone(dronPosition, new Drone(Integer.parseInt(p[4]), 1, p[3]));
							}
							
							if(GameManager.dronesBought.containsKey(player.getPlayerID())) {
								//Borro el antiguo y cargo el nuevo en memoria
								GameManager.dronesBought.remove(player.getPlayerID());
								GameManager.dronesBought.put(player.getPlayerID(), player.getDrones());
							} else {
								GameManager.dronesBought.put(player.getPlayerID(), player.getDrones());
							}
							
							if(GameManager.isOnline(player.getPlayerID())) {
								ConnectionManager playerCM = GameManager.getConnectionManager(player.getPlayerID());
								//Acutliza los drones del usuario
								playerCM.player().updateDrones();
								sendPacket(playerCM.getSocket(), LoginAssembly.setDrones(playerCM.player()));
								
								sendToOthers(playerCM.player(), LoginAssembly.setDrones(playerCM.player()));
							}
							break;
					}
					break;
					
			/* ############################GAME PACKETS############################# */
				case ServerCommands.REQUEST_POLICY:
					//Envia la informacion necesaria a flash para la conexion
					sendPolicy(userSocket);
					break;

				case ServerCommands.REQUEST_LOGIN:
					//LOGIN|playerID|sessionID|clientVersion
					/*
					 * TODO: Solucionar el problema de que se pueda enviar un paquete de login desde un socket falso
					 * La solucion que se me ocurre es usar el sessionID y si se rechaza la conexion bloquear cualquier otro tipo de paquete.
					 * es decir, que no se puedan mandar paquetes de movimiento, etc... sin "autorizacion"
					 */
					
					try {
						
						//Comprueba que el paquete este completo y la version del cliente sea correcta
						if((p.length == 4) && (p[3].equals(Launcher.clientVersion))) {
							
							/*
							 * IF THE LOGIN ACCEPTS THE CONNECTION!!
							 */
							loginAssembly = new LoginAssembly(userSocket);
							
							//Comprueba si puede loguearse
							if(loginAssembly.requestLogin(p)) {
								
								//Set the playerID and threadName
								player = loginAssembly.getPlayer();
								playerID = player.getPlayerID();

								//Aï¿½ade el connectionManager al de jugadores online
								GameManager.connectPlayer(this);
								
								//Inicia el movementHelper del player
								player.setMovementSystem();
								
								thread.setName("ConnectionManager-User_" + player.getPlayerID());
								
								//Inicia el timeout
								startTimeOut();

							} else {
								//sino se cierra su socket
								closeConnection();
								
							}
						} else {
							Console.error("Error with the login packet...");
							closeConnection();
						}
						
					} catch(Exception e) {
						//En caso de que el paquete falle :/
						if(Launcher.developmentMode) {
							e.printStackTrace();
						}
						
						try {
							closeConnection();
						} catch (IOException e1) {}
					}
					break;
					
				case ServerCommands.SHIP_MOVEMENT:
					//1|newX|newY|oldX|oldY => Mueve la nave
					player.movement().moveShip(p);
					break;
				
				case ClientCommands.PORTAL_JUMP:
					//Cuando se pulsa la 'j'
					portalFound = false;
					for(Entry<Integer, Portal> portal : GameManager.portals.entrySet()) {
						
						//Si el jugador esta en el mismo mapa que el portal..
						if(player.getMapID() == portal.getValue().getMapID()) {
							
							//Comprueba que el jugador no este ya saltando..
							if(!player.isJumping()) {
								if(player.isInRange(portal.getValue())) {
									
									//Si el jugador tiene al menos el nivel requerido del portal
									if(player.getLevel() >= portal.getValue().requiredLevel()) {
										//El jugador esta en rango, ejecuto el salto!
										
										portalFound = true; /* para bloquear el mensaje de portal no encontrado generado por el bucle for */
										
										player.isJumping(true);
										
										jumpTimer = new Timer("Player" + player.getPlayerID() + " Jump Timer");
										jumpTimer.schedule(new TimerTask(){
											public void run() {
												try {
													//Jump time :)
													sendPacket(userSocket, "0|U|99|" + portal.getValue().getPortalID());
													Thread.sleep(3250); //tiempo de salto..
													
													//Borra el grafico del usuario..
													sendToMap(player.getMapID(), "0|R|" + player.getPlayerID());
													
													player.setPosition(portal.getValue().getDestination());
													player.setMapID(portal.getValue().getToMapID());
													saveData();
																									
													/*
													 * TODO: Pos eso Tete, cancela los timer y deberia el thread del movimiento, pero no lo consigo.
													 * No supone una gran carga porque esta suspendido, pero no es "bonito"
													 */
													GameManager.getConnectionManager(player.getPlayerID()).jumpDisconnetion();
													
													//Creo un nuevo paquete de conexion y magic :D
													String loginPacket = "LOGIN|" + player.getPlayerID() + "|142312|" + Launcher.clientVersion;
													checkPacket(loginPacket);
													
													Console.out("Player " + player.getPlayerID() + " jumped to mapID=" + portal.getValue().getToMapID());
													
													player.isJumping(false);
													portalFound = false;
												} catch (InterruptedException | IOException e) {
													e.printStackTrace();
												}
												
												jumpTimer.cancel();
												jumpTimer.purge();
											}
										}, 0);
										
										break;
										
									} else {
										//El jugador no tiene el nivel necesario
										portalFound = true;
										sendPacket(userSocket, "0|A|STM|jumplevelfalse|" + portal.getValue().requiredLevel());
										break;
									}
								}
							}
						}
					} /*@end for*/
					
					//Si no se ha encontrado un portal y el usuario no esta saltando--
					 if(!portalFound && !player.isJumping()) {
							//No esta en rango, mando paquete que avisa de ello..
							sendPacket(userSocket, "0|A|STM|jumpgate_failed_no_gate");
					 }
					
					break;
					
				case ServerCommands.SELECT:
					int targetID = Integer.parseInt(p[1]);
					//0|N|TargetID|TargetShipID|shd|getMaxShield()|hp|maxHp|shieldSkill
					
					if(GameManager.isOnline(targetID)) {
						//Si el objetivo en un usuario
						Player target = GameManager.getConnectionManager(targetID).player();
						sendPacket(userSocket, "0|N|" + targetID + "|" + target.getShipID() + "|" + target.activeConfig().getCurrentShield() + "|" + target.activeConfig().getShield() + "|" + target.getHealth() + "|" + target.getShip().getShipHealth() + "|0");
						player.isPlayer(true);
						player.setTarget(targetID);
					} else {
						//Es un NPC
					}
					break;

				/*
				 * Cuando el jugador cambia alguna opcion del cliente :D
				 */
				case ServerCommands.CLIENT_SETTING:
					try {
						switch(p[1]) {
							case "AUTO_REFINEMENT":
								player.getSettings().AUTO_REFINEMENT = p[2];
								break;
								
							case "QUICKSLOT_STOP_ATTACK":
								player.getSettings().QUICKSLOT_STOP_ATTACK = p[2];
								break;
								
							case "DOUBLECLICK_ATTACK":
								player.getSettings().DOUBLECLICK_ATTACK = p[2];
								break;
								
							case "AUTO_START":
								player.getSettings().AUTO_START = p[2];
								break;
								
							case "DISPLAY_NOTIFICATIONS":
								player.getSettings().DISPLAY_NOTIFICATIONS = p[2];
								break;
								
							case "SHOW_DRONES":
								player.getSettings().SHOW_DRONES = p[2];
								break;
								
							case "DISPLAY_WINDOW_BACKGROUND":
								player.getSettings().DISPLAY_WINDOW_BACKGROUND = p[2];
								break;
								
							case "ALWAYS_DRAGGABLE_WINDOWS":
								player.getSettings().ALWAYS_DRAGGABLE_WINDOWS = p[2];
								break;
								
							case "PRELOAD_USER_SHIPS":
								player.getSettings().PRELOAD_USER_SHIPS = p[2];
								break;
								
							case "QUICKBAR_SLOT":
								player.getSettings().QUICKBAR_SLOT = p[2];
								break;
								
							case "MAINMENU_POSITION,3":
								player.getSettings().MAINMENU_POSITION = p[2];
								break;
								
							case "SLOTMENU_POSITION,3":
								player.getSettings().SLOTMENU_POSITION = p[2];
								break;
								
							case "SLOTMENU_ORDER,3":
								player.getSettings().SLOTMENU_ORDER = p[2];
								break;
								
							case "WINDOW_SETTINGS,3":
								player.getSettings().WINDOW_SETTINGS = p[2];
								break;
							
						}
						
						savePlayerSettings();
					} catch(Exception e) {
						if(Launcher.developmentMode) e.printStackTrace();
					}
					break;
					
				/*
				 * 
				 */
				case ServerCommands.SET_ATTRIBUTE:
					/*
					 * Mas opciones del cliente como el switch de arriba..
					 */
					if(p[1].equals(ServerCommands.SET_FLASH_SETTINGS)) {
						/*
						 * El paquete es 0|A|SET|1|0|0|1|.... => 27 elementos
						 */
						String paket = "";
						for(int i=2; i<27; i++) {
							paket += "|" + p[i];
						}
						
						player.getSettings().SET = paket;
					}
					break;
					
				case ClientCommands.SELECT:
					switch(p[1]) {
						case ClientCommands.CONFIGURATION:
							if(!configChanged) {
								configChanged = true; /* para bloquear que se cambie de config antes de 5 seg */
								
								configTimer = new Timer("Player" + player.getPlayerID() + " Jump Timer");
								configTimer.schedule(new TimerTask(){
									public void run() {
										try {
											//Cambio de config
											if(Integer.parseInt(p[2]) == 1) {
												sendPacket(userSocket, "0|S|CFG|1");
												player.activeConfig(1);
											} else {
												sendPacket(userSocket, "0|S|CFG|2");
												player.activeConfig(2);
											}
											
											//Si el escudo actual es mayo que el total, se igualan
											if(player.activeConfig().getCurrentShield() > player.activeConfig().getShield()) {
												player.activeConfig().setCurrentShield(player.activeConfig().getShield());
											}
											
											sendPacket(userSocket, "0|A|SHD|" + player.activeConfig().getCurrentShield() + "|" + player.activeConfig().getShield());
											/*
											 * La velocidad se actualiza sola porque se coge directamente en el movementSystem.
											 * Al igual que el daño
											 */
											
											for(Entry<Integer, ConnectionManager> on : GameManager.onlinePlayers.entrySet()) {
												/*Si algun jugador del mismo mapa que yo, me tiene selecionado mando de nuevo el paquete de 
												 * seleccion con el escudo actualizado
												 * 
												 * 0|N|TargetID|TargetShipID|shd|getMaxShield()|hp|maxHp|shieldSkill
												 */
												if(on.getValue().player().getMapID() == player.getMapID() && on.getValue().player().getTargetID() == player.getPlayerID()) {
													sendPacket(on.getValue().getSocket(), "0|N|" + player().getPlayerID() + "|" + player().getShipID() 
															+ "|" + player().activeConfig().getCurrentShield() + "|" + player.activeConfig().getShield()
															+ "|" + player().getHealth() + "|" + player.getShip().getShipHealth() + "|0");
												}
											}
											
											Thread.sleep(100);
											
											configChanged = false;
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										
										configTimer.cancel();
										configTimer.purge();
									}
								}, 0);
							} else {
								sendPacket(userSocket, "0|A|STM|config_change_failed_time");
							}
							
							
							
							break;
					}
					break;
			}
		}
	}
}
