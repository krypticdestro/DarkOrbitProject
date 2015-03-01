package com.darkorbit.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.darkorbit.assemblies.LoginAssembly;
import com.darkorbit.main.Launcher;
import com.darkorbit.objects.Player;
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
	public Timer timeOutTimer;
	
	private LoginAssembly loginAssembly;
	
	private int playerID = 0;
	private long lastPacket = 0;
	

	public ConnectionManager(Socket userSocket) {
		this.userSocket = userSocket;
		thread = new Thread(this);
		thread.setDaemon(true);
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
		final int timeOut = 30000;
		final int minTimeOut = 10000;
		
		
		timeOutTimer = new Timer("TimeOut - Player " + playerID);
		timeOutTimer.schedule(new TimerTask() {
			
			public void run() {
				boolean timedOut = false;
				
				while(!timedOut) {
					long timeElapsed = Calendar.getInstance().getTimeInMillis() - lastPacket;
					
					//En caso de que haya pasado mas de 'timeOut' desde el ultimo paquete se inicia la desconexion 
					if(timeElapsed >= timeOut) {
						try {
							//espera otros 10 segundos mas para asegurar que el usuario se ha desconectado
							Thread.sleep(minTimeOut);
							disconnectPlayer();
							timedOut = true;
							Console.alert("Player " + playerID + " time-out");
							
						} catch (InterruptedException e) {
							if(Launcher.developmentMode) {
								e.printStackTrace();
							}
						}
					}
				}
				cancelTimeOut();
			}
		}, 0);
		
	}

	
	/**
	 * Cancela el timer del timeout
	 */
	public void cancelTimeOut() {
		timeOutTimer.cancel();
		timeOutTimer.purge();
	}

	
	public void disconnectPlayer() {
		//saveData();
		sendToMap(player.getMapID(), "0|R|" + playerID);
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
								player.setMovementHelper();
								
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
				
			}
		}
	}
}
