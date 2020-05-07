package tk.icedev.heavenEncounter.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {
	
	File fstream = null;
	FileConfiguration config = null;
	String filename;
	Plugin plugin;

	public Config(String filename, Plugin plugin) {
		
		this.filename = filename;
		this.plugin = plugin;
	}
	
	public void save() {
		
		if (config == null || fstream == null)
	        return;
	    
	    try {
	        this.getConfig().save(fstream);
	    } catch (IOException ex) {
	        plugin.getLogger().log(Level.SEVERE, "Could not save config to " + fstream, ex);
	    }
	}
	
	public void reload() {
		
		if (fstream == null)
		    fstream = new File(plugin.getDataFolder(), filename);
		
		config = YamlConfiguration.loadConfiguration(fstream);

		// Look for defaults in the jar
		Reader defConfigStream = new InputStreamReader(plugin.getResource(filename));
		    
		if (defConfigStream != null) {
		    	
		    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
		    config.setDefaults(defConfig);
		}
		
		if (!fstream.exists())            
	         plugin.saveResource(filename, false);
	}
	
	public FileConfiguration getConfig() {
		
		if (config == null)
	        reload();
	    
	    return config;
	}
	
	public void saveDefault() {
		
		if (fstream == null)
	        fstream = new File(plugin.getDataFolder(), filename);
	    
	    if (!fstream.exists())            
	         plugin.saveResource(filename, false);
	}
	
	public static String formatColor(String input) {
		
		if (input == null)
			return null;
		
		return input.replace("&", "\u00a7");
	}
	
	public static String staticReadFormat(String input) {
		
		if (input == null)
			return null;
		
		return input.replace("&", "\u00a7").replace('"', '\'');
	}
	
	public static String staticWriteFormat(String input) {
		
		if (input == null)
			return null;
		
		return input.replace("\u00a7", "&").replace('\'', '"');
	}
	
	public static String formatApostrophe(String input) {
		
		if (input == null)
			return null;
		
		return input.replace('"', '\'');
		
	}
}

