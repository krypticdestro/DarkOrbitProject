package com.darkorbit.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
	
	private LoginAssembly loginAssembly;
	
	private int playerID = 0;
	private Player player = null;
	
	
	public ConnectionManager(Socket userSocket) {
		this.userSocket = userSocket;
		thread = new Thread(this);
		thread.start();
	}
	
	private void closeConnection() throws IOException {
		userSocket.close();
		in.close();
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
	public Player get() {
		return player;
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

					//Set the packet again to ""
					packet = "";
				}
			}
		} catch(IOException e) {
			Console.alert("User disconnected. Socket closed.");
			
			//Un poco chapuza, pero no se me ocurre ahora mismo algo mejor :(
			try {
				closeConnection();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
			//XDLOLTEST
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
								thread.setName("ConnectionManager-User_" + playerID);
								
								//Añade al jugador al mapa y el connectionManager al de jugadores online
								GameManager.addPlayer(player);
								GameManager.connectPlayer(this);
								
							} else {
								//sino se cierra su socket
								closeConnection();
								
							}
						} else {
							Console.error("Error with the login packet...");
						}
						
					} catch(Exception e) {
						//En caso de que el paquete falle :/
						if(Launcher.developmentMode) {
							e.printStackTrace();
						}
					}
					break;
					
				//TODO: Una vez se sepa la estructura de los paqutes se debe decidir si finalmente
				//	establece conexion o se cierra el socket de esa conexion	
			}
		}
	}
}
