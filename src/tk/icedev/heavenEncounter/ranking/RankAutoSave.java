package tk.icedev.heavenEncounter.ranking;

import org.bukkit.Bukkit;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.defaults.Message;

public class RankAutoSave implements Runnable {
	
	private int taskID;
	private HeavenEncounter plugin;
	
	public RankAutoSave(HeavenEncounter plugin, long delay) {
		
		this.plugin = plugin;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin.getPlugin(), this, 0, delay);
	}
	
	public void destroy() {
		
		Bukkit.getScheduler().cancelTask(taskID);
	}

	@Override
	public void run() {
		
		Message.console("Saving profiles...");
		plugin.saveRank();
	}

}
