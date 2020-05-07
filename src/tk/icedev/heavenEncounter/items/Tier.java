package tk.icedev.heavenEncounter.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import tk.icedev.heavenEncounter.utils.BukkitMath;

public class Tier implements ConfigurationSerializable {

	public List<ItemStack> items;
	public double probability;
	
	public Tier(List<ItemStack> items, double probability) {
		
		this.items = items;
		this.probability = probability;
	}
	
	public ItemStack randomItem() {
		
		if (items.size() == 0)
			return null;
		
		int rand = BukkitMath.randomInt(items.size());
		return items.get(rand);
	}
	
	public static void registerConfiguration() {
		
		ConfigurationSerialization.registerClass(Tier.class);
	}

	@Override
	public Map<String, Object> serialize() {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("items", items);
		map.put("probability", probability);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static Tier deserialize(Map<String, Object> map) {
		
		Object _items = map.get("items"), _prob = map.get("probability");
		List<ItemStack> items;
		double prob;
		
		if (_items instanceof List<?>)
			items = (List<ItemStack>)_items;
		else
			items = new ArrayList<ItemStack>();
		
		if (_prob instanceof Double)
			prob = (Double)_prob;
		else
			prob = 0;
		
		return new Tier(items, prob);
	}
}
