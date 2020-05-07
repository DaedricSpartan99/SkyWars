package tk.icedev.heavenEncounter.hub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import tk.icedev.heavenEncounter.utils.Config;
import tk.icedev.heavenEncounter.utils.blocks.BlockData;
import tk.icedev.heavenEncounter.utils.blocks.ChestData;
import tk.icedev.heavenEncounter.defaults.Message;

public class StaticField {

	private final String name;
	private final BlockData[] blocks;
	private final Vector[] spawns;
	private final Vector adminWarp;
	
	public static final List<String> BACKUP_IGNORE = Arrays.asList("uid.dat", "session.dat");
	
	public StaticField(String name, BlockData[] blocks, Vector[] spawns, Vector adminWarp) {
		
		this.name = name;
		this.spawns = spawns;
		this.blocks = blocks;
		this.adminWarp = adminWarp;
	}
	
	public String getName() {
		
		return name;
	}
	
	public int getCapacity() {
		
		return spawns.length;
	}
	
	public Vector[] getSpawns() {
		
		return spawns;
	}
	
	public BlockData[] getBlocks() {
		
		return blocks;
	}
	
	public Vector getAdminWarp() {
		
		return adminWarp;
	}
	
	public Block[] getUnsetBlocks(Location phase) {
		
		Block[] blocks = new Block[this.blocks.length];
		
		for (int i = 0; i < blocks.length; i++)
			blocks[i] = phase.clone().add(this.blocks[i].location).getBlock();
		
		return blocks;
	}
	
	public Location[] getSpawns(Location phase) {
		
		Location[] out = new Location[spawns.length];
		
		for (int i = 0; i < spawns.length; i++)
			out[i] = phase.clone().add(spawns[i]);
		
		return out;
	}
	
	@SuppressWarnings("deprecation")
	public List<Chest> getChests(Location phase) {
		
		List<Chest> out = new ArrayList<Chest>();
		
		for (BlockData data : blocks) {
			
			if (!(data instanceof ChestData))
				continue;
			
			Block block = phase.clone().add(data.location).getBlock();
			BlockState state;
			
			if (!((state = block.getState()) instanceof Chest)) {
				block.setType(Material.CHEST);
				block.setData(data.data);
				state = block.getState();
			}
			
			out.add((Chest)state);
		}
		
		return out;
	}
	
	public Location getAdminWarp(Location phase) {
		
		return phase.clone().add(adminWarp);
	}
	
	public static void backup(World world, StaticField field, Config config, Plugin plugin) {
		
		if (world == null) {
			System.err.println("Could not write backup for " + field.getName());
			return;
		}
		
		/* write info into config */
		
		ConfigurationSection section = config.getConfig().createSection(field.getName());
		
		Message.console("Writing spawns");
		
		section.createSection("spawns");
		section.set("spawns", Arrays.asList(field.getSpawns()));
		
		Message.console("Writing blocks");
		
		File blockFile = getBlocksFile(field.getName(), plugin);
		
		if (blockFile.exists())
			blockFile.delete();
		
		try {
			
			blockFile.createNewFile();
			BlockData.writeBlocks(new FileOutputStream(blockFile), field.getBlocks());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		Message.console("Writing admin warp");
		
		section.createSection("adminWarp");
		section.set("adminWarp", field.getAdminWarp());
		
		Message.console("Saving configurations");
		
		config.save();
	}
	
	public static void remove(String name, Config config, Plugin plugin) {
		
		File file = getBlocksFile(name, plugin);
		
		if (file.exists())
			file.delete();
		
		config.getConfig().set(name, null);
		config.save();
	}
	
	@SuppressWarnings("unchecked")
	public static StaticField load(String name, Config config, Plugin plugin) {
		
		if (!config.getConfig().contains(name))
			return null;
		
		ConfigurationSection section = config.getConfig().getConfigurationSection(name);
		
		List<Vector> list = (List<Vector>) section.getList("spawns");
		
		if (list == null)
			return null;
		
		Vector[] spawns = list.toArray(new Vector[list.size()]);
		
		BlockData[] blocks = null;
		File blockFile = getBlocksFile(name, plugin);
		
		if (!blockFile.exists())
			return null;
		
		try {
			blocks = BlockData.loadBlocks(new FileInputStream(blockFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			
			if (blocks == null)
				return null;
		}
		
		Vector adminWarp = section.getVector("adminWarp");
		
		if (adminWarp == null)
			return null;
		
		return new StaticField(name, blocks, spawns, adminWarp);
	}
	
	public static File getBlocksFile(String name, Plugin plugin) {
		
		return new File(plugin.getDataFolder().getAbsolutePath() + "/fieldsblock/" + name + ".blks");
	}
	
	public static void checkForFieldBlockFolder(Plugin plugin) {
		
		File folder = new File(plugin.getDataFolder().getAbsolutePath() + "/fieldsblock");
		
		if (!folder.exists())
			folder.mkdirs();
	}
}
