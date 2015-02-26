package com.darkorbit.net;

import java.util.Map;
import java.util.TreeMap;

import com.darkorbit.objects.Player;

public class GameManager {
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
