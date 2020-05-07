package tk.icedev.heavenEncounter.defaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.utils.Config;

public class ChatFormats {
	
	private static final String HUB_DEFAULT = "[\u00a7a%f\u00a7f$] \u00a76%player%\u00a74>\u00a7f %s",
								GAME_DEFAULT = "[\u00a7a%f\u00a7f$] \u00a76%player%\u00a74>\u00a7f %s",
								GLOBAL_DEFAULT = "\u00a7a[GLOBALE] [\u00a7a%f\u00a7f$] \u00a76%player%\u00a74>\u00a77 %s";
	
	Map<String, String> hubFormats = new HashMap<String, String>(),
						gameFormats = new HashMap<String, String>(),
						globalFormats = new HashMap<String, String>();
	
	public boolean load(ConfigurationSection section) {
		
		boolean mod = false;
		
		// loading hub chat
		
		ConfigurationSection hub = section.getConfigurationSection("hub");
		
		if (hub == null) {
			hub = section.createSection("hub");
			mod = true;
		}
		
		if (!hubFormats.isEmpty())
			hubFormats.clear();
		
		for (String key : hub.getKeys(false)) {
			
			if (!hub.isString(key))
				continue;
			
			hubFormats.put(key.replaceAll("_", "\\."), Config.staticReadFormat(hub.getString(key)));
		}
		
		// loading game chat
		
		ConfigurationSection game = section.getConfigurationSection("game");
		
		if (game == null) {
			game = section.createSection("game");
			mod = true;
		}
		
		if (!gameFormats.isEmpty())
			gameFormats.clear();
		
		for (String key : game.getKeys(false)) {
			
			if (!game.isString(key))
				continue;
			
			gameFormats.put(key.replaceAll("_", "\\."), Config.staticReadFormat(game.getString(key)));
		}
		
		// loading global chat
		
		ConfigurationSection global = section.getConfigurationSection("global");
				
		if (global == null) {
			global = section.createSection("global");
			mod = true;
		}
				
		if (!globalFormats.isEmpty())
			globalFormats.clear();
				
		for (String key : global.getKeys(false)) {
					
			if (!global.isString(key))
				continue;
					
			globalFormats.put(key.replaceAll("_", "\\."), Config.staticReadFormat(global.getString(key)));
		}
		
		return mod;
	}

	public String getHubMessage(Player player, String msg) {
		
		String format = null;
		
		for (Entry<String, String> entry : hubFormats.entrySet()) {
			if (player.hasPermission(entry.getKey())) {
				format = entry.getValue();
				break;
			}
		}
		
		if (format == null)
			format = HUB_DEFAULT;
		
		return Message.formatArgs(format, player, msg);
	}

	public String getGameMessage(Player player, String msg) {
		
		String format = null;
		
		for (Entry<String, String> entry : gameFormats.entrySet()) {
			if (player.hasPermission(entry.getKey())) {
				format = entry.getValue();
				break;
			}
		}
		
		if (format == null)
			format = GAME_DEFAULT;
		
		return Message.formatArgs(format, player, msg);
	}
	
	public String getGlobalMessage(Player player, String msg) {
		
		String format = null;
		
		for (Entry<String, String> entry : globalFormats.entrySet()) {
			if (player.hasPermission(entry.getKey())) {
				format = entry.getValue();
				break;
			}
		}
		
		if (format == null)
			format = GLOBAL_DEFAULT;
		
		return Message.formatArgs(format, player, msg);
	}
}
