package com.darkorbit.net;

import java.util.Map;
import java.util.TreeMap;

import com.darkorbit.objects.Player;

public class GameManager {
	/* Online players map */
		public static Map<Integer, ConnectionManager> onlinePlayers = new TreeMap<Integer, ConnectionManager>();
		
		public static void connectPlayer(ConnectionManager c) {
			onlinePlayers.put(c.get().getPlayerID(), c);
		}
		
		public static void disconnectPlayer(int playerID) {
			onlinePlayers.remove(playerID);
		}
		
		public static boolean isOnline(int playerID) {
			if(onlinePlayers.containsKey(playerID)) {
				return true;
			} else {
				return false;
			}
		}
		
	/* Players map */
		public static Map<Integer, Player> players = new TreeMap<Integer, Player>();
		
		public static void addPlayer(Player player) {
			players.put(player.getPlayerID(), player);
		}
		
		public static boolean playerExist(int playerID) {
			if(players.containsKey(playerID)) {
				return true;
			} else {
				return false;
			}
		}
	
}
