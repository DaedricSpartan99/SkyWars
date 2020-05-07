package tk.icedev.heavenEncounter.hub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;

public class JoinPoint {
	
	// indica come si vuole entrare in una partita
	
	private final Block[] volume;
	private final Material triggerType;
	private final String name;

	private HeavenEncounter plugin;
	private boolean enabled;
	
	public JoinPoint(String name, Block[] volume, Material triggerType, HeavenEncounter plugin) {
		
		this.plugin = plugin;
		this.name = name;
		this.volume = volume;
		this.triggerType = triggerType;
		this.enable();
	}
	
	public String getName() {
		
		return name;
	}
	
	public Block[] getBlocks() {
		
		return volume;
	}
	
	public Material getTriggerType() {
		
		return this.triggerType;
	}
	
	public void enable() {
		
		enabled = true;
	}
	
	public void disable() {
		
		enabled = false;
	}
	
	public boolean isEnabled() {
		
		return enabled;
	}

	public boolean onInteract(PlayerInteractEvent event) {
		
		Block block = event.getClickedBlock();
		
		if (block == null)
			return false;
		
		if (block.getType().equals(triggerType)) {
			
			for (Block b : volume) {
				if (b.equals(block)) {
					
					plugin.getGameManager().joinRandomGame(event.getPlayer());
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void writeJoinPoint(JoinPoint point, ConfigurationSection section) {
		
		/*
		 * Structure of JoinPoint
		 * 
		 * <gamename>:
		 * 	triggerType: <material>
		 *  world: <world>
		 *  vectors: <locationList>
		 */
		
		Block[] blocks = point.getBlocks();
		
		if (blocks.length == 0) {
			System.err.println("Cannot write a non existant volume");
			return;
		}
		
		if (!section.contains("triggerType"))
			section.createSection("triggerType");
		
		section.set("triggerType", point.getTriggerType().name());
		
		if (!section.contains("world"))
			section.createSection("world");
		
		section.set("world",  blocks[0].getWorld().getName());
		
		if (!section.contains("vectors"))
			section.createSection("vectors");
		
		List<Vector> vlist = new ArrayList<Vector>();
		
		for (Block b : blocks)
			vlist.add(b.getLocation().toVector());
		
		section.set("vectors", vlist);
	}
	
	public static JoinPoint loadJoinPoint(ConfigurationSection section, HeavenEncounter plugin) {
		
		if (!(section.isSet("triggerType") && section.isSet("world") && section.isSet("vectors")))
			return null;
		
		Material tType = Material.valueOf(section.getString("triggerType"));
		World world = Bukkit.getWorld(section.getString("world"));
		
		@SuppressWarnings("unchecked")
		List<Vector> vecs = (List<Vector>)section.getList("vectors");
		
		Block[] volume = new Block[vecs.size()];
		
		for (int i = 0; i < volume.length; i++)
			volume[i] = vecs.get(i).toLocation(world).getBlock();
		
		return new JoinPoint(section.getName(), volume, tType, plugin);
	}
}
