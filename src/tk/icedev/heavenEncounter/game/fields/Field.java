package tk.icedev.heavenEncounter.game.fields;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.items.ChestsManager;
import tk.icedev.heavenEncounter.items.Tier;
import tk.icedev.heavenEncounter.tools.SyncLooper;
import tk.icedev.heavenEncounter.tools.TNTAutoPrimer;
import tk.icedev.heavenEncounter.utils.blocks.BlockData;
import tk.icedev.heavenEncounter.defaults.Message;

public class Field {
	
	public static final int LOADING_SPEED = 1000, UNLOADING_SPEED = 1000, LOADING_FREQ = 1;
	
	/*
	 * public variables
	 */
	
	private StaticField staticField;
	private final Block[] blocks;
	private final Location[] spawns;
	private final Location adminWarp;
	private final FieldBounds bounds;
	private boolean accessible, taken;
	
	public Lobby lobby;
	
	private List<Block> placedBlocks;
	private FieldLoader runningLooper;
	private boolean mapLock;
	private TNTAutoPrimer autoPrimer;
	
	private List<Block> glassBlocks, fireSpread;
	
	/*
	 * Constructors
	 */
	
	public Field(FieldBounds bounds, StaticField field) {
		
		this.staticField = field;
		this.bounds = bounds;
		this.spawns = field.getSpawns(bounds.getPhase());
		this.blocks = field.getUnsetBlocks(bounds.getPhase());
		this.lobby = new Lobby(spawns.length);
		this.adminWarp = field.getAdminWarp(bounds.getPhase());
		
		this.autoPrimer = new TNTAutoPrimer(bounds.getPhase().getWorld());
		
		this.accessible = false;
		this.taken = false;
		this.mapLock = false;
		this.placedBlocks = new ArrayList<Block>();
		this.fireSpread = new ArrayList<Block>();
		
		this.sorroundSpawns();
	}
	
	public void build(Plugin plugin) {
		
		this.mapLock = true;
		
		if (runningLooper != null)
			runningLooper.destroy();
		
		BlockData[] rawBlocks = this.staticField.getBlocks();
		
		try {
		
			runningLooper = new FieldLoader(this.blocks, rawBlocks, plugin);
			
		} catch (IllegalPluginAccessException ex) {
		
			Message.console("Error trying to build the field during more ticks, using 1 tick");
			
			for (int i = 0; i < blocks.length; i++)
				rawBlocks[i].setBlock(blocks[i]);
			
		} catch (Exception ex) {
			
			new FieldBuildingException(ex).printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void reset(Plugin plugin) {
		
		autoPrimer.reset(false);
		
		for (Block block : placedBlocks)
			block.setTypeIdAndData(0, (byte)0, false);
		
		for (Block block : fireSpread)
			block.setTypeIdAndData(0, (byte)0, false);
		
		placedBlocks.clear();
		fireSpread.clear();
		
		this.clearItems();
		
		this.sorroundSpawns(); // in case interrupt
		this.build(plugin);
	}
	
	@SuppressWarnings("deprecation")
	public void destroy(Plugin plugin, boolean unload) {
		
		autoPrimer.destroy();
		
		if (runningLooper != null)
			runningLooper.destroy();
		
		if (unload) {
			
			this.breakGlass();
			
			for (Block block : placedBlocks)
				block.setTypeIdAndData(0, (byte)0, false);
		
			BlockData[] rawBlocks = this.staticField.getBlocks();
		
			try {
		
				runningLooper = new FieldDestroyer(this.blocks, rawBlocks, plugin);
			
			} catch (IllegalPluginAccessException ex) {
		
				Message.console("Error trying to destroy the field during more ticks, using 1 tick");
			
				for (int i = 0; i < blocks.length; i++)
					BlockData.erase(blocks[i]);
			}
		}
		
		autoPrimer = null;
		staticField = null;
		placedBlocks.clear();
		placedBlocks = null;
	}
	
	/*
	 * public useful fields
	 */
	
	public boolean isAccessible() {
		
		return accessible;
	}
	
	public boolean isTaken() {
		
		return taken;
	}
	
	public boolean isBuilding() {
		
		return runningLooper != null;
	}
	
	public boolean isMapLocked() {
		
		return mapLock;
	}
	
	public void setTaken(boolean taken) {
		
		this.taken = taken;
	}
	
	public void setLockMap(boolean lockMap) {
		
		this.mapLock = lockMap;
	}
	
	public TNTAutoPrimer getAutoPrimer() {
		
		return autoPrimer;
	}
	
	public Location[] getSpawns() {
		
		return spawns;
	}
	
	public String getName() {
		
		return staticField.getName();
	}
	
	public Location getAdminWarp() {
		
		return adminWarp;
	}
	
	public FieldLoader getFieldLoader() {
		
		return runningLooper;
	}
	
	public void randomAssignChests(ChestsManager chestsMan, int max) {
		
		if (max > InventoryType.CHEST.getDefaultSize())
			max = InventoryType.CHEST.getDefaultSize();
		
		for (Chest chest : staticField.getChests(bounds.getPhase())) {
			
			Inventory inv = chest.getBlockInventory();
			inv.clear();
		
			for (int i = 0; i < max; i++) {
				
				Tier tier = chestsMan.randomTier();
				
				if (tier == null)
					continue;
				
				ItemStack stack = tier.randomItem();
			
				if (stack != null)
					inv.addItem(stack);
			}
		}
	}
	
	public void clearItems() {
		
		for (Entity ent : bounds.getPhase().getWorld().getEntities()) {
			if (ent instanceof Item && bounds.isInside(ent.getLocation()))
				ent.remove();
		}
	}
	
	public void spawnPlayer(int index, Player player) {
		
		player.teleport(this.spawns[index]);
	}
	
	public void warpAdmin(Player player) {
		
		player.teleport(adminWarp);
	}
	
	public void sorroundSpawns() {
		
		this.glassBlocks = new ArrayList<Block>();
		
		for (Location spawn : spawns) {
			
			Block block = spawn.getBlock();
			
			this.toGlass(block.getRelative(0, -1, 0));
			
			for (int i = 0; i < 3; i++) {
				
				this.toGlass(block.getRelative(-1, i, 0));
				this.toGlass(block.getRelative(1, i, 0));
				this.toGlass(block.getRelative(0, i, 1));
				this.toGlass(block.getRelative(0, i, -1));
			}
			
			this.toGlass(block.getRelative(0, 3, 0));
		}
	}
	
	public void breakGlass() {
		
		for (Block block : glassBlocks)
			block.setType(Material.AIR);
		
		glassBlocks.clear();
	}
	
	private void toGlass(Block block) {
		
		this.glassBlocks.add(block);
		block.setType(Material.GLASS);
	}
	
	/*
	 * Handlers
	 */
	
	public void onBlockBreak(BlockBreakEvent event) {
		
		if (mapLock)
			event.setCancelled(true);
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		
		if (mapLock)
			event.setCancelled(true);
		else if (autoPrimer.onPlayerPlace(event))
			;
		else
			this.addBlockPlace(event.getBlock());
	}
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (!mapLock)
			autoPrimer.onPlayerUse(event);
	}
	
	public boolean onEntityExplode(EntityExplodeEvent event) {
		
		return autoPrimer.onEntityExplode(event);
	}
	
	public void onFireIgnite(BlockIgniteEvent event) {
		
		fireSpread.add(event.getBlock());
	}
	
	public boolean onSpread(BlockSpreadEvent event, Block source) {
		
		if (fireSpread.contains(source)) {
			fireSpread.add(event.getBlock());
			return true;
		}
		
		return false;
	}
	
	public void addBlockPlace(Block block) {
		
		if (!placedBlocks.contains(block))
			placedBlocks.add(block);
	}
	
	public class FieldLoader extends SyncLooper<BlockData> {
		
		protected Block[] buffer;
		List<LoaderEndListener> listeners;

		public FieldLoader(Block[] buffer, BlockData[] data, Plugin plugin) {
			super(data, LOADING_SPEED, LOADING_FREQ, plugin);
			this.buffer = buffer;
			this.listeners = new ArrayList<LoaderEndListener>();
		}

		public void addListener(LoaderEndListener listener) {
			
			this.listeners.add(listener);
		}
		
		@Override
		public void beginAction() {
			
			accessible = false;
			
			if (runningLooper != null)
				runningLooper.destroy();
			
			runningLooper = this;
			
			Message.console("Building map " + getName());
		}

		@Override
		public void tickAction(float percent) {
			
		}

		@Override
		public void loopAction(int index, BlockData obj) {
			
			obj.setBlock(this.buffer[index]);
		}
		
		@Override
		public void endAction() {
			
			runningLooper = null;
			accessible = true;
			
			Message.console("Map " + getName() + " built");
			
			for (LoaderEndListener listener : listeners)
				listener.endAction(this);
			
			listeners.clear();
		}
	}
	
	public class FieldDestroyer extends FieldLoader {

		public FieldDestroyer(Block[] buffer, BlockData[] data, Plugin plugin) {
			super(buffer, data, plugin);
		}

		@Override
		public void loopAction(int index, BlockData obj) {
			
			BlockData.erase(this.buffer[index]);
		}
	}
	
	public static interface LoaderEndListener {
		
		public abstract void endAction(FieldLoader loader);
	}
	
	public class FieldBuildingException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public FieldBuildingException(Throwable cause) {
			
			super("Error while building field " + staticField.getName() + " Phase: " + 
					bounds.getPhase().getBlockX() + " " 
					+ bounds.getPhase().getBlockZ(), cause);
		}
	}
}
