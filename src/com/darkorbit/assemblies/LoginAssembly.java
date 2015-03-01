package com.darkorbit.assemblies;

import java.io.IOException;
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
	//private long sessionID; no usado aun
	private Player player;
	
	public LoginAssembly(Socket userSocket) {
		this.userSocket = userSocket;
	}
	
	/**
	 * Comprueba si el usuario se puede conectar
	 * @param p Packet login
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
					//Cierran los sockets antiguos que tenia abiertos
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
			//0|I|playerID|username|shipID|maxSpeed|shield|maxShield|health|maxHealth|cargo|maxCargo|user.x|user.y|mapId|factionId|clanId|shipAmmo|shipRockets|expansion|premium|exp|honor|level|credits|uridium|jackpot|rank|clanTag|ggates|0|cloaked
			String loginPacket = "0|I|" + player.getPlayerID() + "|" + player.getUserName() + "|" + player.getShipID() + "|" + player.getShip().getShipSpeed() + "|5|10|20|" + player.getShip().getShipHealth() + "|0|" + player.getShip().getMaxCargo() + "|1000|1000|1|" + player.getFactionID() + "|0|" + player.getShip().getBatteries() + "|" + player.getShip().getRockets() + "|3|1|1|2|3|124|412312|3|1|Borja mola mucho|0|0|0";
			sendPacket(userSocket, loginPacket);
		}
		
		
}
