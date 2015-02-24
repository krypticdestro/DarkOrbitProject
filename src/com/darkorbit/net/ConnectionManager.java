package com.darkorbit.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.darkorbit.main.Launcher;
import com.darkorbit.packets.Packet;
import com.darkorbit.utils.Console;

/**
 * Administra las conexiones entrantes
 * 
 * @author Borja Sanchidrián
 */

public class ConnectionManager extends Global implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	private Socket userSocket;
	private Thread thread;
	
	public ConnectionManager(Socket userSocket) {
		this.userSocket = userSocket;
		thread = new Thread(this);
		thread.start();
	}
	
	void closeSockets() throws IOException {
		userSocket.close();
		in.close();
		out.close();
	}

	/**
	 * Inicia los streams del socket para poder leer/escribir datos
	 */
	private void setStreams() {
		try {
			//entrada
			in = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			
			//salida - despreciado porque se inicia luego en Global.java, y esta clase extiende la anterior
			//out = new PrintWriter(userSocket.getOutputStream(), false);
		} catch (IOException e) {
			Console.error("There was an error setting up the socket streams...");

			if (Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
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
				closeSockets();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Lee e interpreta los paquetes recibidos.
	 * @param packet Paquete enviado desde run
	 */
	void checkPacket(String packet) {
		Console.out(packet);
		if(packet.startsWith("/")) {
			//XDLOLTEST
		} else {
			String[] p = packet.split("\\|");
			
			switch(p[0]) {
				case Packet.POLICY:
					//Envia la informacion necesaria a flash para la conexión
					sendPolicy(userSocket);
					break;
				
				case Packet.LOGIN:
					String test = "0|I|1|username|4|10|5|10|20|30|0|10|1000|1000|1|1|0|1|10|3|1|1|2|3|124|412312|3|1|CLAN|0|0|0";
					sendPacket(userSocket, test);
					break;
					
				//TODO: Una vez se sepa la estructura de los paqutes se debe decidir si finalmente
				//	establece conexion o se cierra el socket de esa conexion
				//			thread.setName("ConnectionManager-USER_NUM");
			}
		}
	}
}
