package com.darkorbit.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.darkorbit.main.Launcher;
import com.darkorbit.net.GameManager;
import com.darkorbit.objects.Ammunition;
import com.darkorbit.objects.Clan;
import com.darkorbit.objects.Drone;
import com.darkorbit.objects.Equipment;
import com.darkorbit.objects.GameMap;
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
					//Sino pues nï¿½!
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
						result.getInt("Shield"),
						result.getInt("shieldAbsorb"),
						result.getInt("Speed"),
						result.getInt("Batteries"),
						result.getInt("Rockets"),
						result.getInt("Cargo"),
						result.getInt("Damage"),
						result.getInt("maxDamage"),
						result.getInt("experience"),
						result.getInt("honor"),
						result.getInt("credits"),
						result.getInt("uridium")
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
	 * Carga la informaciï¿½n del clan del player
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
	
	/**
	 * Carga el equipamiento del jugador. Cargar configuracion 1 y 2 en player
	 * @param playerID
	 * @param config
	 * @return Equipment object
	 */
	public static Equipment loadEquipment(int playerID, int config) {
		List<String> itemArray = new ArrayList<String>();
		int currentShield = 0;
		//Generators
		int B02 = 0, B01 = 0, A03 = 0, A02 = 0, A01 = 0, G3N79 = 0, G3N69 = 0, G3N33 = 0, G3N32 = 0, G3N20 = 0, G3N10 = 0;
		//Lasers
		int LF3 = 0, LF2 = 0, MP1 = 0, LF1 = 0;
		
		query = "SELECT * FROM server_1_hangar_config_ship WHERE playerID=" + playerID + " AND configNum=" + config;
		ResultSet result;
		
		try {
			/* player current shield */
			ResultSet playerResult = query("SELECT shield" + config + " FROM server_1_players WHERE playerID=" + playerID);
			while(playerResult.next()) {
				currentShield = playerResult.getInt("shield" + config);
			}
			
			/* get ship equipment */
			result = query(query);
			//Podría ser if porque solo deberia haber 1 resultado..
			while(result.next()) {
				
				if(!result.getString("generators").isEmpty()) {
					itemArray.add(result.getString("generators"));
				}
				
				if(!result.getString("lasers").isEmpty()) {
					itemArray.add(result.getString("lasers"));
				}
				//TODO: Add more equipment objects..
			}
			
			/* get drones equipment */
			query = "SELECT * FROM server_1_hangar_config_drones WHERE playerID=" + playerID;
			
			ResultSet dronesResult = query(query);
			//Podría ser if porque solo deberia haber 1 resultado..
			while(dronesResult.next()) {
				if(!dronesResult.getString("EQ").isEmpty() && config == 1) {
					itemArray.add(dronesResult.getString("EQ"));
				}
				
				if(!dronesResult.getString("EQ2").isEmpty() && config == 2) {
					itemArray.add(dronesResult.getString("EQ2"));
				}
			}
			
			//Ahora busco cada item.. si el array no esta vacio
			if(itemArray.size() > 0) {
				for(int i=0; i<itemArray.size(); i++) {
					for(int j=0; j<itemArray.get(i).split("\\|").length; j++) {
						query = "SELECT * FROM server_1_player_all_items WHERE playerID=" + playerID + " AND id=" + itemArray.get(i).split("\\|")[j];
						ResultSet itemResult = query(query);
						
						while(itemResult.next()) {
							/*
							 * equipment_generator_shield_sg3n-b02
							 * [1] {
							 * 	generator
							 * 	weapon
							 * 	-.-.-
							 * }
							 */
							String[] item = itemResult.getString("lootid").split("_");
							switch(item[1]) {
								case "generator":
									/*
									 * Shields and engines.
									 */
									switch(item[3]) {
										case "sg3n-b02":
											B02++;
											break;
										case "sg3n-b01":
											B01++;
											break;
										case "sg3n-a03":
											A03++;
											break;
										case "sg3n-a02":
											A02++;
											break;
										case "sg3n-a01":
											A01++;
											break;
										case "g3n-7900":
											G3N79++;
											break;
										case "g3n-6900":
											G3N69++;
											break;
										case "g3n-3310":
											G3N33++;
											break;
										case "g3n-3210":
											G3N32++;
											break;
										case "g3n-2010":
											G3N20++;
											break;
										case "g3n-1010":
											G3N10++;
											break;
									}
									break;
									
								case "weapon":
									/*
									 * Lasers and RocketLauncher?
									 */
									switch(item[3]) {
										case "lf-3":
											LF3++;
											break;
										case "lf-2":
											LF2++;
											break;
										case "mp-1":
											MP1++;
											break;
										case "lf-1":
											LF1++;
											break;
									}
									break;
							}
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
		return new Equipment(currentShield, B02, B01, A03, A02, A01, G3N79, G3N69, G3N33, G3N32, G3N20, G3N10, LF3, LF2, MP1, LF1);
	}
	
	/**
	 * Comprueba los objetos equipados en la web
	 * @param packet
	 * @return
	 */
	public static void checkObject(String packet) {
		//Generators
		int B02 = 0, B01 = 0, A03 = 0, A02 = 0, A01 = 0, G3N79 = 0, G3N69 = 0, G3N33 = 0, G3N32 = 0, G3N20 = 0, G3N10 = 0;
		//Lasers
		int LF3 = 0, LF2 = 0, MP1 = 0, LF1 = 0;
		
		/*
		 * webPacket|equipment|lasers|1|1|27|16|18
		 *	item 0: webPacket
		 *	item 1: equipment
		 *	item 2: lasers | generators | EQ | EQ2
		 *	item 3: playerID
		 *	item 4: config | droneID
		 *	item 5: 27
		 *	item 6: 16
		 *	item 7: 18
		 */
		
		String[] p = packet.split("\\|");
		int playerID = Integer.parseInt(p[3]), config = Integer.parseInt(p[4]);
		
		//Empieza en 5 por el resumen de arriba, para empezar directamente a leer objectos
		for(int i=5; i<p.length; i++) {
			query = "SELECT * FROM server_1_player_all_items WHERE playerID=" + playerID + " AND id=" + p[i];
			ResultSet itemResult;
			try {
				itemResult = query(query);
				
				while(itemResult.next()) {
					String[] item = itemResult.getString("lootid").split("_");
					/*
					 * TODO: Un poco cutre porque hay que copiar todo el switch de la funcion de arriba
					 */
					switch(item[1]) {
						case "generator":
							/*
							 * Shields and engines.
							 */
							switch(item[3]) {
								case "sg3n-b02":
									B02++;
									break;
								case "sg3n-b01":
									B01++;
									break;
								case "sg3n-a03":
									A03++;
									break;
								case "sg3n-a02":
									A02++;
									break;
								case "sg3n-a01":
									A01++;
									break;
								case "g3n-7900":
									G3N79++;
									break;
								case "g3n-6900":
									G3N69++;
									break;
								case "g3n-3310":
									G3N33++;
									break;
								case "g3n-3210":
									G3N32++;
									break;
								case "g3n-2010":
									G3N20++;
									break;
								case "g3n-1010":
									G3N10++;
									break;
							}
							break;
							
						case "weapon":
							/*
							 * Lasers and RocketLauncher?
							 */
							switch(item[3]) {
								case "lf-3":
									LF3++;
									break;
								case "lf-2":
									LF2++;
									break;
								case "mp-1":
									MP1++;
									break;
								case "lf-1":
									LF1++;
									break;
							}
							break;
					}
				}
			} catch (SQLException e) {
				if(Launcher.developmentMode) {
					e.printStackTrace();
				}
			}
		}
		
		if(GameManager.playersMap.containsKey(playerID)) {
			/*
			 * Si el jugador se ha llegado a conectar se coge su cuenta del map y se actualiza su equipamiento para que si sigue online le cambie
			 * y/o si se conecta de nuevo tenga el actualizado.
			 * 
			 * CurrentShield lo que hace es comprobar cuando escudo tenia en la configuracion que se va a sustituir para mantenerlo y 'dejar vacio'
			 * el resto hasta completar el total.
			 */
			Player player = GameManager.getPlayer(playerID);
			int currentShield = 0;
			if(config == 1) { currentShield = player.config1().getCurrentShield(); } else if(config == 2) {currentShield = player.config1().getCurrentShield();}
			
			player.setConfig(config, new Equipment(currentShield, B02, B01, A03, A02, A01, G3N79, G3N69, G3N33, G3N32, G3N20, G3N10, LF3, LF2, MP1, LF1));
			GameManager.updatePlayer(player);
		} else {
			/*
			 * si es su primer login o ni siquiera se ha logueado aun y esta cambiando el equipamiento desde la web.
			 * 
			 * Lo mismo que arriba con la diferencia de que en vez de actualizar una cuenta, se añade una nueva al map
			 */
			Player player = loadAccount(playerID);
			int currentShield = 0;
			if(config == 1) { currentShield = player.config1().getCurrentShield(); } else if(config == 2) {currentShield = player.config1().getCurrentShield();}
			
			player.setConfig(config, new Equipment(currentShield, B02, B01, A03, A02, A01, G3N79, G3N69, G3N33, G3N32, G3N20, G3N10, LF3, LF2, MP1, LF1));
			GameManager.addPlayer(player);
		}
	}
	
	/**
	 * Carga los mapas del juego. A medias!
	 */
	public static void loadMaps() {
		query = "SELECT * FROM maps WHERE id=1";
		try {
			//TODO: add more maps
			ResultSet result = query(query);
			
			while(result.next()) {
				//Crea un Mapa en el Mapa xD
				GameManager.addMap(new GameMap(result.getShort("id"), result.getString("NPCS")));
			}
			
		} catch(Exception e) {
			Console.error("Couldn't load gameMaps...");
			if(Launcher.developmentMode) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
}
