package com.darkorbit.assemblies;

import java.net.Socket;

import com.darkorbit.main.Launcher;
import com.darkorbit.mysql.QueryManager;
import com.darkorbit.net.GameManager;
import com.darkorbit.net.Global;
import com.darkorbit.objects.Player;
import com.darkorbit.utils.Console;


public class LoginAssembly extends Global {
	private Socket userSocket;
	private int playerID;
	private long sessionID;
	private Player player;
	
	public LoginAssembly(Socket userSocket) {
		this.userSocket = userSocket;
	}
	
	public boolean requestLogin(String[] p) {
		try {
			
			this.playerID = Integer.parseInt(p[1]);
			this.sessionID = Long.parseLong(p[2]);
			
		} catch(Exception e) {
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		player = QueryManager.loadAccount(playerID);
		
		if(!player.equals(null)) {
			//Cuenta cargada!
			
			//Si la cuenta no esta ya en el mapa, es decir logueada
			if(!GameManager.playerExist(player.getPlayerID())) {
				//pos te logueas
				startLogin();
				return true;
				
			} else {
				//TODO / TO THINK XD
				//check sessionID Return false por ahora
				Console.error("PLAYER " + playerID + " SE INTENTA RECONECTAR!");
				return false;
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
		
	}
	
	/* Login functions */
	
		private void setSettings() {
			//Envia al cliente las opciones del juego
			
			sendPacket(userSocket, "0|A|SET|"+ player.getSettings().SET);
			sendPacket(userSocket, "0|7|MINIMAP_SCALE,1|"+player.getSettings().MINIMAP_SCALE);
			sendPacket(userSocket, "0|7|DISPLAY_PLAYER_NAMES|"+ player.getSettings().DISPLAY_PLAYER_NAMES);
			sendPacket(userSocket, "0|7|DISPLAY_CHAT|"+ player.getSettings().DISPLAY_CHAT);
			sendPacket(userSocket, "0|7|PLAY_MUSIC|"+ player.getSettings().PLAY_MUSIC);
			sendPacket(userSocket, "0|7|PLAY_SFX|"+ player.getSettings().PLAY_SFX);
			sendPacket(userSocket, "0|7|BAR_STATUS|"+ player.getSettings().BAR_STATUS);
			sendPacket(userSocket, "0|7|WINDOW_SETTINGS,1|"+ player.getSettings().WINDOW_SETTINGS);
			sendPacket(userSocket, "0|7|AUTO_REFINEMENT|"+ player.getSettings().AUTO_REFINEMENT);
			sendPacket(userSocket, "0|7|QUICKSLOT_STOP_ATTACK|"+ player.getSettings().QUICKSLOT_STOP_ATTACK);
			sendPacket(userSocket, "0|7|DOUBLECLICK_ATTACK|"+ player.getSettings().DOUBLECLICK_ATTACK);
			sendPacket(userSocket, "0|7|AUTO_START|"+ player.getSettings().AUTO_START);
			sendPacket(userSocket, "0|7|DISPLAY_NOTIFICATIONS|"+ player.getSettings().DISPLAY_NOTIFICATIONS);
			sendPacket(userSocket, "0|7|SHOW_DRONES|"+ player.getSettings().SHOW_DRONES);
		}
		
		private void setPlayer() {
			String test = "0|I|1|username|4|1000|5|10|20|30|0|1|1000|1000|1|1|0|1|10|3|1|1|2|3|124|412312|3|1|CLAN|0|0|0";
			sendPacket(userSocket, test);
		}
		
		
}
