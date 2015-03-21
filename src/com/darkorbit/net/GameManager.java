package com.darkorbit.net;

import java.util.Map;
import java.util.TreeMap;

import com.darkorbit.objects.Equipment;
import com.darkorbit.objects.GameMap;
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
		
	/* Equipment map */
		public static Map<String, Equipment> equipment = new TreeMap<String, Equipment>();
		
		public static void addEquipment(Equipment e) {
			equipment.put(e.getIdentifier(), e);
		}
		
		public static Equipment getEquipment(String identifier) {
			return equipment.get(identifier);
		}
}
