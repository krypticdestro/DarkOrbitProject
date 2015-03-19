package com.darkorbit.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkorbit.main.Launcher;
import com.darkorbit.net.GameManager;
import com.darkorbit.objects.Ammunition;
import com.darkorbit.objects.Clan;
import com.darkorbit.objects.Drone;
import com.darkorbit.objects.Equipment;
import com.darkorbit.objects.Player;
import com.darkorbit.objects.Portal;
import com.darkorbit.objects.Rockets;
import com.darkorbit.objects.Settings;
import com.darkorbit.objects.Ship;
import com.darkorbit.utils.Console;
import com.darkorbit.utils.Vector;

public class QueryManager extends MySQLManager {
	
	public static String query = null;
	
	
	public static void updateSql(String query) {
		SQLUpdate(query);
	}
	
	/**
	 * Carga la cuenta del usuario desde la base de datos
	 * @param playerID
	 */
	public static Player loadAccount(int playerID) {
		Player player = null;
		Settings playerSettings = null;
		
		query = "SELECT * FROM server_1_players WHERE playerID=" + playerID;
		ResultSet playerResult;
		
		try {
			playerResult = query(query);
			
			if(!playerResult.isBeforeFirst()) {
				//Si no encuentra la cuenta en la base de datos devuelve null y termina
				Console.error("Account " + playerID + " not found...");
				return null;
				
			} else {
				//Se encuentra la cuenta en la db
				String settingsQuery = "SELECT * FROM server_1_players_settings WHERE playerID=" + playerID;
				ResultSet settingsResult = query(settingsQuery);
				
				//Si encuentra la fila de las opciones...
				if((settingsResult.isBeforeFirst()) && (settingsResult.next())) {
					playerSettings = new Settings(
							settingsResult.getString("SETTINGS"),
							settingsResult.getString("MINIMAP_SCALE"),
							settingsResult.getString("DISPLAY_PLAYER_NAMES"),
							settingsResult.getString("DISPLAY_CHAT"),
							settingsResult.getString("PLAY_MUSIC"),
							settingsResult.getString("PLAY_SFX"),
							settingsResult.getString("BAR_STATUS"),					
							settingsResult.getString("WINDOW_SETTINGS"),
							settingsResult.getString("AUTO_REFINEMENT"),
							settingsResult.getString("QUICKSLOT_STOP_ATTACK"),
							settingsResult.getString("DOUBLECLICK_ATTACK"),
							settingsResult.getString("AUTO_START"),
							settingsResult.getString("DISPLAY_NOTIFICATIONS"),
							settingsResult.getString("SHOW_DRONES"),
							settingsResult.getString("DISPLAY_WINDOW_BACKGROUND"),
							settingsResult.getString("ALWAYS_DRAGGABLE_WINDOWS"),
							settingsResult.getString("PRELOAD_USER_SHIPS"),
							settingsResult.getString("QUALITY_PRESETTING"),
							settingsResult.getString("QUALITY_CUSTOMIZED"),
							settingsResult.getString("QUALITY_BACKGROUND"),						
							settingsResult.getString("QUALITY_POIZONE"),						
							settingsResult.getString("QUALITY_SHIP"),
							settingsResult.getString("QUALITY_ENGINE"),
							settingsResult.getString("QUALITY_COLLECTABLE"),
							settingsResult.getString("QUALITY_ATTACK"),
							settingsResult.getString("QUALITY_EFFECT"),
							settingsResult.getString("QUALITY_EXPLOSION"),
							settingsResult.getString("QUICKBAR_SLOT"),
							settingsResult.getString("SLOTMENU_POSITION"),
							settingsResult.getString("SLOTMENU_ORDER"),
							settingsResult.getString("MAINMENU_POSITION")
							);
				}
				
				if(playerResult.next()) {
					//Una vez tiene las opciones del jugador y sus datos los retorna
					player = new Player(playerID, playerSettings, 
							playerResult.getString("username"),
							playerResult.getShort("shipId"),
							playerResult.getShort("factionId"),
							playerResult.getShort("mapId"),
							new Vector(playerResult.getInt("x"), playerResult.getInt("y")),
							playerResult.getInt("Health"),
							playerResult.getBoolean("premium"),
							playerResult.getLong("exp"),
							playerResult.getLong("credits"),
							playerResult.getLong("uri"),
							playerResult.getLong("honor"),
							playerResult.getInt("level"),
							playerResult.getDouble("jackpot"),
							playerResult.getInt("rank"),
							playerResult.getInt("GG"),
							playerResult.getInt("clanId")
							);
					
					return player;
				} else {
					//Sino pues n�!
					return null;
				}
			}
		} catch(SQLException e) {
			Console.error("Couldn't load the player " + playerID + " account...");
			
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
			return null;
		}	
	}

	/**
	 * Carga todas las naves guardadas en la base de datos al iniciar el emulador
	 * @return Numero de naves cargadas
	 */
	public static int loadShips() {
		query = "SELECT * FROM ships";
		ResultSet result;
		int num = 0;
		
		try {
			result = query(query);
			
			while(result.next()) {
				Ship ship = new Ship(
						result.getShort("Id"),
						result.getInt("HP"),
						result.getInt("Speed"),
						result.getInt("Batteries"),
						result.getInt("Rockets"),
						result.getInt("Cargo")
						);
				
				GameManager.addShip(ship);
				num++;
			}
			return num;
		} catch (SQLException e) {
			Console.error("Couldn't load ships");
			
			if(Launcher.developmentMode) {
				e.printStackTrace();	
			}
			
			//No se puede jugar sin naves
			System.exit(0);
			return num;
		}
	}

	/**
	 * Carga la municion del usuario //Falla si se compra y reconecta idk why
	 * @param playerID
	 * @return Objecto ammunition con los valores de la municion
	 */
	public static Ammunition loadAmmunition(int playerID) {
		int lcb10 = 0, mcb25 = 0, mcb50 = 0, sab50 = 0, ucb100 = 0;
		
		try {
			query = "SELECT * FROM server_1_player_all_items WHERE playerID=" + playerID + " AND lootid LIKE '%ammunition_laser%'";
			ResultSet result;
			
			result = query(query);
			
			while(result.next()) {
				String[] ammoType = result.getString("lootid").split("_");
				
				//lcb-10 | mcb-25 | mcb-50 | sab-50 | ucb-100 | rsb-75
				switch(ammoType[2]) {
					case "lcb-10":
						lcb10 = result.getInt("Q");
						break;
						
					case "mcb-25":
						mcb25 = result.getInt("Q");
						break;
						
					case "mcb-50":
						mcb50 = result.getInt("Q");
						break;
						
					case "sab-50":
						sab50 = result.getInt("Q");
						break;
						
					case "ucb-100":
						ucb100 = result.getInt("Q");
						break;
				}
			}
			
		} catch (SQLException e) {
			
			Console.error("Couldn't load the player ammunition...");
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		return new Ammunition(lcb10, mcb25, mcb50, sab50, ucb100);
	}

	
	/**
	 * Carga los misiles del usuario //Falla si se compra y reconecta idk why
	 * @param playerID
	 * @return Objecto Rockets con los valores de la municion
	 */
	public static Rockets loadRockets(int playerID) {
		query = "SELECT * FROM server_1_player_all_items WHERE playerID=" + playerID + " AND lootid LIKE '%ammunition_rocket%'";
		ResultSet result;
		int r310 = 0, plt2026 = 0, plt3030 = 0, plt2021 = 0;
		
		try {
			result = query(query);
			
			while(result.next()) {
				
				//r-310 | plt-2026 | plt-3030 | plt-2021 => RocketType
				switch(result.getString("lootid").split("_")[2]) {
					case "r-310":
						r310 = result.getInt("Q");
						break;
						
					case "plt-2026":
						plt2026 = result.getInt("Q");
						break;
						
					case "plt-3030":
						plt3030 = result.getInt("Q");
						break;
						
					case "plt-2021":
						plt2021 = result.getInt("Q");
						break;
				}
			}
			
		} catch (SQLException e) {
			
			Console.error("Couldn't load the player rockets...");
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		return new Rockets(r310, plt2026, plt3030, plt2021);
	}
	
	
	/**
	 * Carga los drones del player
	 * @param playerID
	 * @return Array of Drone
	 */
	public static Drone[] loadDrones(int playerID) {
		query = "SELECT * FROM server_1_player_drones WHERE playerID=" + playerID;
		ResultSet result;
		//Vamos a poner 8 drones por ahora
		Drone[] drones = new Drone[8];
		for(int i=0; i<8; i++) {
			//Inicializo el array en nulo por si las moscas...
			drones[i] = null;
		}
		
		try {
			result = query(query);
			
			int contador = 0;
			//Porque solo hay 8 drones, por ahora
			while(result.next()) {
				Drone drone = new Drone(result.getInt("drone_level"), result.getString("drone_kind"));
				
				drones[contador] = drone;
				contador++;
			}
		} catch (SQLException e) {
			Console.error("Couldn't load drones of player " + playerID);
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		return drones;
	}
	

	/**
	 * Carga la informaci�n del clan del player
	 * @param clanID ID del clan
	 * @return Clan object
	 */
	public static Clan loadClan(int clanID) {
		query = "SELECT * FROM server_1_clan WHERE clanID=" + clanID;
		ResultSet result;
		Clan clan = null;
		
		try {
			result = query(query);
			
			if(result.next()) {
				clan = new Clan(clanID, result.getString("tagName"));
			}
			
		} catch (SQLException e) {
			Console.error("Couldn't load player clan...");
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		return clan;
	}

	
	/**
	 * Carga los portales del juego..
	 */
	public static void loadPortals() {
		//TODO: borrar el where!
		query = "SELECT * FROM portals";
		ResultSet result;
		
		try {
			result = query(query);
			
			while(result.next()) {
				Portal portal = new Portal(
						result.getInt("id"),
						result.getShort("mapId"),
						new Vector(result.getInt("x"), result.getInt("y")),
						result.getShort("toMapId"),
						new Vector(result.getInt("newX"), result.getInt("newY")),
						result.getInt("reqLvl"),
						result.getInt("portalGFX")
						);
				
				GameManager.addPortal(portal);
			}
			
		} catch (SQLException e) {
			Console.error("Couldn't load the game portals..");
			if(Launcher.developmentMode) {
				e.printStackTrace();	
			}
			/*
			 * No se puede jugar sin portales..
			 * Relamente si, pero no quiero xD
			 */
			System.exit(0);
		}
	}

	//TODO: Poner los datos de las query bien :P | Care with config :/
	public static Equipment loadEquipment(int playerID, int config) {
		/*
		 * Aqui deberia seleccionar el equipamiento que tiene el usuario, si no me equivoco vienen dados con la siguiente estructura...
		 * 
		 * (string) = "12,41,5,1,2,5,...";
		 * 
		 * Y luego cada uno de ellos compararlo en la tabla de '_all_items" para ver que diablos son..
		 */
		String[] itemArray = null;
		int B02 = 0, B01 = 0, A03 = 0, A02 = 0, A01 = 0;
		
		query = "SELECT * FROM server_1_general_ship WHERE playerID=" + playerID;
		ResultSet result;
		
		try {
			result = query(query);
			while(result.next()) {
				itemArray = result.getString("generators").split(",");
				
				//TODO: Add more equipment objects.. | Tengo que ver como hacer un array que englobe todos los objetos
			}
			
			//Ahora busco cada item.. si el array no esta vacio
			if(!itemArray.equals(null)) {
				for(int i=0; i<itemArray.length; i++) {
					query = "SELECT * FROM server_1_player_all_items WHERE playerID=" + playerID + " AND id=" + itemArray[i];
					ResultSet itemResult = query(query);
					
					while(itemResult.next()) {
						//En funcion de que objeto sea.. TODO: Cambiar la posicion del array if needed
						switch(itemResult.getString("lootid").split("_")[0]) {
							case "B02":
								B02++;
								break;
							case "B01":
								B01++;
								break;
							case "A03":
								A03++;
								break;
							case "A02":
								A02++;
								break;
							case "A01":
								A01++;
								break;
						}
					}
				}
			}
			
			
			
		} catch (SQLException e) {
			Console.error("Couldn't load player " + playerID + " equipment");
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
		}
		
		return new Equipment(B02, B01, A03, A02, A01);
	}
}
