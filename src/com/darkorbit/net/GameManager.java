package com.darkorbit.net;

import java.util.Map;
import java.util.TreeMap;

import com.darkorbit.objects.Drone;
import com.darkorbit.objects.GameMap;
import com.darkorbit.objects.Player;
import com.darkorbit.objects.Portal;
import com.darkorbit.objects.Ship;

public class GameManager {
	/* Online players map */
		public static Map<Integer, ConnectionManager> onlinePlayers = new TreeMap<Integer, ConnectionManager>();
		
		public static void connectPlayer(ConnectionManager c) {
			onlinePlayers.put(c.player().getPlayerID(), c);
		}
		
		public static void disconnectPlayer(int playerID) {
			onlinePlayers.remove(playerID);
		}
		
		public static ConnectionManager getConnectionManager(int playerID) {
			return onlinePlayers.get(playerID);
		}
		
		public static boolean isOnline(int playerID) {
			if(onlinePlayers.containsKey(playerID)) {
				return true;
			} else {
				return false;
			}
		}

	/* Ships map */
		public static Map<Short, Ship> ships = new TreeMap<Short, Ship>();
		
		public static void addShip(Ship ship) {
			ships.put(ship.getShipID(), ship);
		}
		
		public static Ship getShip(short shipID) {
			return ships.get(shipID);
		}
		
	/* Portals map */
		public static Map<Integer, Portal> portals = new TreeMap<Integer, Portal>();
		
		public static void addPortal(Portal p) {
			portals.put(p.getPortalID(), p);
		}
		
		public static Portal getPortal(int id) {
			return portals.get(id);
		}
		
	/* Range phoenix (Extra function) */
		public static Map<Integer, Integer> rangeShips = new TreeMap<Integer, Integer>();
		
		public static void addRangeShip(int id) {
			rangeShips.put(rangeShips.size(), id);
		}
		
	/* GameMaps map*/
		public static Map<Short, GameMap> gameMaps = new TreeMap<Short, GameMap>();
		
		public static void addMap(GameMap m) {
			gameMaps.put(m.getMapID(), m);
		}
		
	/* Players map */
		public static Map<Integer, Player> playersMap = new TreeMap<Integer, Player>();
		
		public static Player getPlayer(int playerID) {
			return playersMap.get(playerID);
		}
		
		public static void addPlayer(Player player) {
			playersMap.put(player.getPlayerID(), player);
		}
		
		public static void updatePlayer(Player player) {
			if(playersMap.containsKey(player.getPlayerID())) {
				playersMap.remove(player.getPlayerID());
				addPlayer(player);
			} else {
				addPlayer(player);
			}
		}
		
	/* Drones bought */
		public static Map<Integer, Drone[]> dronesBought = new TreeMap<Integer, Drone[]>();
		
		public static Drone[] getDrones(int playerID) {
			return dronesBought.get(playerID);
		}
}
