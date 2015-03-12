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
import com.darkorbit.objects.Player;
import com.darkorbit.objects.Portal;
import com.darkorbit.packets.ClientCommands;
import com.darkorbit.packets.ServerCommands;
import com.darkorbit.utils.Console;

/**
 * Administra las conexiones entrantes
 * @author Borja Sanchidrián
 */

public class ConnectionManager extends Global implements Runnable {

	private BufferedReader in;
	private Socket userSocket;
	private Thread thread;
	private Player player = null;
	private Timer timeOutTimer, jumpTimer;
	
	private LoginAssembly loginAssembly;
	
	private int playerID = 0;
	private int idle = 0;
	private final int maxIdle = 24; // 1 = 25 segundos ; 24 = 600 segundos = 10 minutos
	private long lastPacket = 0;
	public boolean timedOut = false;

	

	public ConnectionManager(Socket userSocket) {
		this.userSocket = userSocket;
		thread = new Thread(this);
		thread.start();
	}
	
	public void closeConnection() throws IOException {
		userSocket.close();
		in.close();
		
		//Borramos el connection de este usuario
		GameManager.onlinePlayers.remove(playerID);
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
							cancelTimeOut();
						} catch (InterruptedException e) {
							if(Launcher.developmentMode) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							// Por el disconnectPlayer
							e.printStackTrace();
						}
					}
				}
			}
		}, 0, 30000);
		
	}

	
	/**
	 * Cancela el timer del timeout
	 */
	public void cancelTimeOut() {
		timeOutTimer.cancel();
		timeOutTimer.purge();
	}

	/**
	 * Desconecta al usuario y closeConnection()
	 * @throws IOException
	 */
	public void disconnectPlayer() throws IOException {
		
		//if(player.canDisconnect()) - por si le estan atacando, etc...
		//saveData();
		
		//Borra al usuario del mapa
		sendToMap(player.getMapID(), "0|R|" + playerID);
		Console.out("Player " + player.getPlayerID() + " disconnected or exceeded max idle time");
		GameManager.onlinePlayers.remove(playerID);
		cancelTimeOut();
		closeConnection();
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
			
			while(in.read(packetChar, 0, 1) != -1 ) {
				
				//Comprueba que el caracter no sea ni nulo, espacio en blanco, linea nueva
				if(packetChar[0] != '\u0000' && packetChar[0] != '\n' && packetChar[0] != '\r') {
					//Si no añade el caracter a packet
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
						sendPacket(userSocket, p[1]);
					} catch(Exception e) {
						if(Launcher.developmentMode) {
							e.printStackTrace();
						}
					}
					break;
			}
			
		} else {
			String[] p = packet.split("\\|");
			
			switch(p[0]) {
				case ServerCommands.REQUEST_POLICY:
					//Envia la informacion necesaria a flash para la conexión
					sendPolicy(userSocket);
					break;
				
				case ServerCommands.REQUEST_LOGIN:
					//LOGIN|playerID|sessionID|clientVersion
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

								//Añade el connectionManager al de jugadores online
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
					//Mueve la nave
					player.movement().moveShip(p);
					break;
				
				case ClientCommands.PORTAL_JUMP:
					//Cuando se pulsa la 'j'
					for(Entry<Integer, Portal> portal : GameManager.portals.entrySet()) {
						//Si el jugador esta en el mismo mapa que el portal..
						if(player.getMapID() == portal.getValue().getMapID()) {
							
							//Comprueba que el jugador no este ya saltando..
							if(!player.isJumping()) {
								if(player.isInRange(portal.getValue())) {
									
									//Si el jugador tiene al menos el nivel requerido del portal
									if(player.getLevel() >= portal.getValue().requiredLevel()) {
										
										//El jugador esta en rango, ejecuto el salto!
										sendPacket(userSocket, "0|A|STD|Jumping");
										player.isJumping(true);
										
										jumpTimer = new Timer("Player" + player.getPlayerID() + " Jump Timer");
										jumpTimer.schedule(new TimerTask(){
											public void run() {
												try {
													//Jump time :)
													Thread.sleep(3250);
													
													//TODO: JUMP
													
													player.isJumping(false);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
										}, 0);
									} else {
										//El jugador no tiene el nivel necesario
										sendPacket(userSocket, "0|A|STM|");
									}
								} else {
									//No esta en rango, mando paquete que avisa de ello..
									sendPacket(userSocket, "0|A|STM|jumpgate_failed_no_gate");
								}
							}
						}
					}
					
					break;
				
			}
		}
	}
}
