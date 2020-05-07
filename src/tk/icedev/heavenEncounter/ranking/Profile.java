package tk.icedev.heavenEncounter.ranking;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class Profile implements ConfigurationSerializable {

	public int kills, deaths, played, wins;
	
	public Profile() {
		
		kills = 0;
		deaths = 0;
		played = 0;
		wins = 0;
	}
	
	public Profile(int kills, int deaths, int played, int wins) {
		
		this.kills = kills;
		this.deaths = deaths;
		this.played = played;
		this.wins = wins;
	}

	@Override
	public Map<String, Object> serialize() {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("kills", kills);
		map.put("deaths", deaths);
		map.put("played", played);
		map.put("wins", wins);
		
		return map;
	}
	
	public static void registerConfiguration() {
		
		ConfigurationSerialization.registerClass(Profile.class);
	}
	
	public static Profile deserialize(Map<String, Object> map) {
		
		int kills = 0, deaths = 0, played = 0, wins = 0;
		
		if (map.get("kills") instanceof Integer)
			kills = (Integer)map.get("kills");
		
		if (kills < 0)
			kills = 0;
	
		if (map.get("deaths") instanceof Integer)
			deaths = (Integer)map.get("deaths");
		
		if (deaths < 0)
			deaths = 0;
		
		if (map.get("played") instanceof Integer)
			played = (Integer)map.get("played");
		
		if (played < 0)
			played = 0;
		
		if (map.get("wins") instanceof Integer)
			wins = (Integer)map.get("wins");
		
		if (wins < 0)
			wins = 0;
		
		return new Profile(kills, deaths, played, wins);
	}
}
