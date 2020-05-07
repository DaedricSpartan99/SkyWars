package tk.icedev.heavenEncounter.builder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractBuilder implements Listener {
	
	protected Player builder;

	public AbstractBuilder(Plugin plugin, Player builder) {
		
		this.enable(plugin);
		this.builder = builder;
	}
	
	public void enable(Plugin plugin) {
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public void destroy() {
		
		HandlerList.unregisterAll(this);
	}
}
