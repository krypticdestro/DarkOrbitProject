package com.darkorbit.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.darkorbit.net.GameServer;
import com.darkorbit.utils.Console;

public class Launcher {

	private static final int PORT = 8080;
	private static BufferedReader configReader;
	public static boolean developmentMode = false;
	private static final String version = "Development version v0.1";

	public static void main(String[] args) throws IOException {
		System.out.println("DarkOrbit Game Server (C) 2015 - " + version);
		System.out.println("Starting up everything...\n");
		readConfigFile();
		new GameServer(PORT);
	}
	
	/**
	 * Lee el archivo de configuración
	 */
	static void readConfigFile() {
		Console.out("Reading config file...");
		try {
			configReader = new BufferedReader(new FileReader("config/config.ini"));
			String line;
			while((line=configReader.readLine()) != null) {
				//Si la linea comienza con #, [, lo que sea pasa a la siguiente
				
				if (line.startsWith("[") || line.startsWith("#") || !line.contains("=")) continue;

				String[] values = line.split("=");
				switch(values[0]) {
					case "developmentMode":
						developmentMode = Boolean.parseBoolean(values[1]);
						if(developmentMode) Console.alert("DevelopmentMode Activated!!");
						break;
				}
			}
		} catch(FileNotFoundException e) {
			//TODO: Archivo no encontrado :/
			
		} catch (IOException e) {
			Console.error("Couldn't read config file... Should be located in 'config/config.ini");
			
			if(developmentMode) {
				e.printStackTrace();
			}
		}
	}
}
