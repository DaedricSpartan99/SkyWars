package tk.icedev.heavenEncounter.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import tk.icedev.heavenEncounter.utils.BukkitMath;
import tk.icedev.heavenEncounter.utils.Config;

public class ChestsManager {

	private double none = 0;
	private Map<String, Tier> tiers = new LinkedHashMap<String, Tier>();
	
	public void destroy() {
		
		tiers.clear();
	}
	
	public void reload(Config config) {
		
		this.none = 0;
		this.tiers.clear();
		this.load(config);
	}
	
	public double getNoneProbability() {
		
		return none;
	}
	
	public void addTier(String tier, double prob) {
		
		if (tiers.containsKey(tier)) {
			tiers.get(tier).items.clear();
			tiers.get(tier).probability = prob;
		} else
			tiers.put(tier, new Tier(new ArrayList<ItemStack>(), prob));
	}
	
	public Tier getTier(String name) {
		
		return tiers.get(name);
	}
	
	public Tier randomTier() {
		
		double rand = BukkitMath.randomDouble() * 100;
		
		if (rand < none)
			return null;
		
		do {
		
			for (Entry<String, Tier> entry : tiers.entrySet()) {
			
				rand = BukkitMath.randomDouble() * 100;
			
				if (rand < entry.getValue().probability)
					return entry.getValue();
			}
			
		} while (tiers.size() > 0);
		
		return null;
	}
	
	public void write(Config config) {
		
		FileConfiguration conf = config.getConfig();
		
		conf.createSection("none");
		conf.set("none", none);
		
		ConfigurationSection tiers_sec = conf.createSection("tiers");
		
		for (Entry<String, Tier> entry : tiers.entrySet()) {
			tiers_sec.createSection(entry.getKey());
			tiers_sec.set(entry.getKey(), entry.getValue());
		}
		
		config.save();
	}
	
	public void load(Config config) {
		
		FileConfiguration conf = config.getConfig();
		
		if (conf.isDouble("none"))
			this.none = conf.getDouble("none");
		
		ConfigurationSection tiers = conf.getConfigurationSection("tiers");
		
		Map<String, Double> unord = new HashMap<String, Double>();
		
		for (String key : tiers.getKeys(false)) {
			
			Object obj = tiers.get(key);
			
			if (obj instanceof Tier)
				unord.put(key, ((Tier)obj).probability);
		}
		
		List<String> ordkeys = BukkitMath.sortMapByValues(unord);
		Collections.reverse(ordkeys);
		
		for (String key : ordkeys) {
			
			Object obj = tiers.get(key);
			
			if (obj instanceof Tier)
				this.tiers.put(key, (Tier)obj);
		}
	}
 }
