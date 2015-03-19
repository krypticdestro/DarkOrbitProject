package com.darkorbit.objects;

import java.util.HashMap;
import java.util.Map;

public class GameMap {
    private short mapID;
    private Map<Npc, Integer> npcs = new HashMap<Npc, Integer>();
  
    public GameMap(short mapID, String npcsString) {
        this.mapID = mapID;
        
        //68|50|124|24|12|54
        String[] npcsArray = npcsString.split("\\|");
        
        for(int i=0; i<npcsArray.length; i+=2)  {
            npcs.put(new Npc(Short.parseShort(npcsArray[i])), Integer.parseInt(npcsArray[i+1]));
        }
        
        int test = npcs.get(new Npc((short)84));
        System.out.println(test);
    }
    
    /* get methods */
    	public short getMapID() { return mapID; }
    	
    	public Map<Npc, Integer> getNpcMap() { return npcs; }
}