package tk.icedev.heavenEncounter.ranking;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.defaults.Message;

public class Rank {

	private Map<String, Profile> profileMap = new HashMap<String, Profile>();
	
	public void load(ConfigurationSection section) {
		
		for (String key : section.getKeys(false)) {
			
			Message.console("Loading profile: " + key);
			
			Object obj = section.get(key);
			
			if (obj instanceof Profile)
				profileMap.put(key, (Profile)obj);
		}
	}
	
	public void save(ConfigurationSection section) {
		
		for (Entry<String, Profile> entry : profileMap.entrySet()) {
			
			if (!section.contains(entry.getKey()))
				section.createSection(entry.getKey());
				
			section.set(entry.getKey(), entry.getValue());
		}
	}
	
	public Profile getProfile(Player player) {
		
		return this.getProfile(player.getName());
	}
	
	public Profile getProfile(String player) {
		
		if (!profileMap.containsKey(player))
			profileMap.put(player, new Profile());
		
		return profileMap.get(player);
	}
}
