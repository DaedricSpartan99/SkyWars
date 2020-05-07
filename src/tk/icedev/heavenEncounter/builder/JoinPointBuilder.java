package tk.icedev.heavenEncounter.builder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.hub.JoinPoint;

import static tk.icedev.heavenEncounter.extern.HeavenEncounter.TITLE;

public class JoinPointBuilder extends AbstractBuilder {
	
	String name;
	HeavenEncounter plugin;
	Material trigger;

	public JoinPointBuilder(String name, Material trigger, Player builder, HeavenEncounter plugin) {
		
		super((Plugin)plugin, builder);
		this.name = name;
		this.plugin = plugin;
		this.trigger = trigger;
	}
	
	private static Block[] getTriggerSurface(Block entry, Material trigger) {
		
		List<Block> list = new ArrayList<Block>();
		genListRecursion(list, trigger, entry);
		return list.toArray(new Block[list.size()]);
	}
	
	private static void genListRecursion(List<Block> list, Material trigger, Block entry) {
		
		if (list.contains(entry))
			return;
		
		list.add(entry);
		
		BlockFace[] dirs = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		Block tmp;
		
		for (BlockFace face : dirs) {
		
			tmp = entry.getRelative(face);
		
			if (tmp.getType().equals(trigger))
				genListRecursion(list, trigger, tmp);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (!event.getPlayer().equals(builder))
			return;
		
		Block block;
		
		if ((block = event.getClickedBlock()).getType().equals(trigger)) {
			
			plugin.addJoinPoint(new JoinPoint(name, getTriggerSurface(block, trigger), trigger, plugin));
			builder.sendMessage("[" + TITLE + "] Join point " + name + " has been registered");
			System.out.println("[" + TITLE + "] Join point " + name + " has been registered");
			this.destroy();
		}
	}
}
