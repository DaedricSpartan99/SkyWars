package tk.icedev.heavenEncounter.builder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.utils.blocks.BlockData;
import tk.icedev.heavenEncounter.utils.blocks.ChestData;

public class FieldBuilder extends AbstractBuilder {
	
	public static FieldBuilder defaultBuilder = null;
	
	World world;
	List<Vector> spawns;
	List<BlockData> blocks;
	BlockCatcher blockCatcher;
	Location adminWarp;
	String name;
	Vector phase;

	public FieldBuilder(String name, Plugin plugin, Player builder) {
		
		super(plugin, builder);
		spawns = new ArrayList<Vector>();
		blocks = new ArrayList<BlockData>();
		blockCatcher = new BlockCatcher();
		phase = null;
		
		world = builder.getWorld();
		adminWarp = builder.getLocation();
		this.name = name;
		
		Message.dynamicMessage(builder, "World " + world.getName() + " registered");
		Message.dynamicMessage(builder, "Spawn closed");
		Message.dynamicMessage(builder, "Launch '/sw map adminwarp' to overwrite the admin spawn with your location");
		Message.dynamicMessage(builder, "Object fbuilder initialized");
		Message.dynamicMessage(builder, "The world's name will determine the name of the Field");
		Message.dynamicMessage(builder, "Launch '/sw map addspawn' to register a player spawn in your location");
		Message.dynamicMessage(builder, "Launch '/sw map bound' to register one extreme of chest catcher");
		Message.dynamicMessage(builder, "If you register two extremes, all the blocks inside that space will be taken");
		Message.dynamicMessage(builder, "Launch '/sw map build' to confirm this configuration");
	}
	
	@Override
	public void destroy() {
		
		super.destroy();
		
		if (spawns != null)
			spawns.clear();
		
		if (blocks != null)
			blocks.clear();
		
		spawns = null;
		blocks = null;
		blockCatcher = null;
		phase = null;
		world = null;
		adminWarp = null;
		name = null;
	}
	
	public static FieldBuilder initDefaultBuilder(String name, Plugin plugin, Player builder) {
		
		return defaultBuilder = new FieldBuilder(name, plugin, builder);
	}
	
	public static FieldBuilder getDefaultBuilder() {
		
		return defaultBuilder;
	}
	
	public static void destroyDefaultBuilder() {
		
		if (defaultBuilder != null) {
			
			defaultBuilder.destroy();
			defaultBuilder = null;
		}
	}
	
	public void recordNewSpawn() {
		
		Vector loc = builder.getLocation().toVector();
		loc.setX(loc.getBlockX() + 0.5f);
		loc.setY(loc.getBlockY() + 0.5f);
		loc.setZ(loc.getBlockZ() + 0.5f);
		spawns.add(loc);
		Message.dynamicMessage(builder, "Spawn number " + spawns.size() + " registered");
	}
	
	public void setAdminWarp() {
		
		adminWarp = builder.getLocation();
		Message.dynamicMessage(builder, "Admin spawn set");
	}
	
	public void takeBound() {
		
		int bound = blockCatcher.setBound(builder.getLocation());
		
		Message.dynamicMessage(builder, "Bound " + bound + " set");
		
		if (blockCatcher.areSet()) {
			
			this.phase = blockCatcher.genList(blocks);
			Message.dynamicMessage(builder, "Chest list catched");
			blockCatcher.clear();
		}
	}
	
	public StaticField compile() {
		
		if (spawns.size() < 2 || blocks.size() == 0 || phase == null) {
			return null;
		}
		
		Message.dynamicMessage(builder, "Field " + world.getName() + " has been registered");
		Message.console("Field " + world.getName() + " has been registered");
		
		BlockData[] blocks = this.blocks.toArray(new BlockData[this.blocks.size()]);
		
		for (int i = 0; i < blocks.length; i++)
			blocks[i].location.subtract(phase);
		
		Vector[] spawns = this.spawns.toArray(new Vector[this.spawns.size()]);
		
		for (int i = 0; i < spawns.length; i++)
			spawns[i].subtract(phase);
		
		Vector adminWarp = this.adminWarp.toVector().subtract(phase);
		
		return new StaticField(this.name, blocks, spawns, adminWarp);
	}
	
	public World getWorld() {
		
		return world;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		
		if (!event.getPlayer().equals(builder))
			return;
		
		String cmd = event.getMessage();
		
		if (cmd.startsWith("/sw map") || cmd.startsWith("/skywars map"))
			return;
		
		event.setCancelled(true);
		Message.dynamicMessage(builder, "You cannot perform any other command while you're creating a field");
	}
	
	public class BlockCatcher {
		
		public Location one, two;
		
		public BlockCatcher() {
			
			one = null;
			two = null;
		}
		
		public int setBound(Location loc) {
			
			if (one == null) {
				one = loc;
				return 0;
			} else {
				two = loc;
				return 1;
			}
		}
		
		public boolean areSet() {
			
			return one != null && two != null;
		}
		
		public void clear() {
			
			one = null;
			two = null;
		}
		
		@SuppressWarnings("deprecation")
		public Vector genList(List<BlockData> buffer) {
			
			if (one == null || two == null)
				return new Vector(0, 0, 0);
			
			int dx, dy, dz;
			Location dir = two.clone().subtract(one);
			
			dx = dir.getBlockX();
			dy = dir.getBlockY();
			dz = dir.getBlockZ();
			
			if (dx == 0 || dy == 0 || dz == 0)
				return new Vector(0, 0, 0);
			
			dx = (dx > 0) ? 1 : -1;
			dy = (dy > 0) ? 1 : -1;
			dz = (dz > 0) ? 1 : -1;
			
			World world = one.getWorld();
			
			buffer.clear();
			
			int min_x = 0, min_z = 0;
			
			for (int x = one.getBlockX() + dx; (dx > 0) ? x < two.getBlockX() : x > two.getBlockX(); x += dx) {
				for (int y = one.getBlockY() + dy; (dy > 0) ? y < two.getBlockY() : y > two.getBlockY(); y += dy) {
					for (int z = one.getBlockZ() + dz; (dz > 0) ? z < two.getBlockZ() : z > two.getBlockZ(); z += dz) {
						
						Block block = world.getBlockAt(x, y, z);
						Material type = block.getType();
						
						if (type == Material.AIR || type == Material.WATER || type == Material.LAVA)
							continue;
						else {
							
							if (block.getState() instanceof Chest)
								buffer.add(new ChestData(new Vector(x, y, z), block.getData()));
							else
								buffer.add(new BlockData(new Vector(x, y, z), block.getType(), block.getData()));
							
							if (z < min_z)
								min_z = z;
						}
					}
				}
				
				if (x < min_x)
					min_x = x;
			}
			
			return new Vector(min_x, 0, min_z);
		}
	}
}
