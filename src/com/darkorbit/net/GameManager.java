package com.darkorbit.net;

import java.util.Map;
import java.util.TreeMap;

import com.darkorbit.objects.Ship;

public class GameManager {
	/* Online players map */
		public static Map<Integer, ConnectionManager> onlinePlayers = new TreeMap<Integer, ConnectionManager>();
		
		public static void connectPlayer(ConnectionManager c) {
			onlinePlayers.put(c.get().getPlayerID(), c);
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
}
