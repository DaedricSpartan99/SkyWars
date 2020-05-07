package tk.icedev.heavenEncounter.tools;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EntityEraser implements Runnable {
	
	World world;
	int taskID;
	
	public EntityEraser(World world, long delay, Plugin plugin) {
		
		this.world = world;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, delay);
	}
	
	public void destroy() {
		
		Bukkit.getScheduler().cancelTask(taskID);
	}

	@Override
	public void run() {
		
		for (Entity ent : world.getEntities()) {
			
			if (!(ent instanceof Player) && ent.getLocation().getBlockY() < 0)
				ent.remove();
		}
	}
}
