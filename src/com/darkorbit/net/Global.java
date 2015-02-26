package com.darkorbit.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.darkorbit.main.Launcher;
import com.darkorbit.utils.Console;

public class Global {
	private static PrintWriter out;
	
	/**
	 * Metodo para enviar paquetes al socket
	 * @param socket - Socket al que enviar los paquetes
	 * @param packet - Paquete a enviar
	 */
	public static void sendPacket(Socket socket, String packet) {
		try {
			out = new PrintWriter(socket.getOutputStream(), false);
			out.print(packet + (char)0x00);
			out.flush();
			
			if(Launcher.developmentMode) {
				Console.out("Packet sent: " + packet);
			}
			
		} catch (IOException e) {
			// error abriendo el stream...
			Console.error("Error opening the output socket stream...");
			
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Envia el policy necesario de flash - Buscar en google para mas info xD
	 * @param socket
	 */
	public static void sendPolicy(Socket socket) {
		String policy = "<?xml version=\"1.0\"?>\r\n" +
                "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n" +
                "<cross-domain-policy>\r\n" +
                "<allow-access-from domain=\"*\" to-ports=\"*\" />\r\n" +
                "</cross-domain-policy>";
		sendPacket(socket, policy);
	}
}
