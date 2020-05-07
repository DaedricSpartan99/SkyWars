package tk.icedev.heavenEncounter.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.hub.HubBoard.ProfileScoreFormat;
import tk.icedev.heavenEncounter.utils.Config;
import tk.icedev.heavenEncounter.utils.blocks.RawLocation;

public class Hub {
	
	public Location spawn;
	public List<JoinPoint> joins;
	private ItemStack compass;
	private Map<Player, HubBoard> openBoards;
	public ProfileScoreFormat boardFormat;
	private HeavenEncounter plugin;

	public Hub(HeavenEncounter plugin) {
		
		this.spawn = new Location(Bukkit.getWorld("world"), 0, 64, 0);
		this.joins = new ArrayList<JoinPoint>();
		this.compass = new ItemStack(Material.COMPASS);
		this.boardFormat = new ProfileScoreFormat();
		this.plugin = plugin;
		this.openBoards = new HashMap<Player, HubBoard>();
	}
	
	public void destroy() {
		
		joins.clear();
		openBoards.clear();
		
		joins = null;
		openBoards = null;
		boardFormat = null;
		spawn = null;
		compass = null;
	}
	
	public void reload() {
		
		Player[] playersIn = openBoards.keySet().toArray(new Player[openBoards.size()]);
		
		for (Player player : playersIn)
			this.closeBoard(player);
		
		this.load();
		
		for (Player player : playersIn)
			this.spawn(player);
	}
	
	public ItemStack getJoinCompass() {
		
		return compass;
	}
	
	public void giveCompass(Player player) {
		
		PlayerInventory inv = player.getInventory();
		
		boolean hasAlready = false;
		
		for (ItemStack stack : inv) {
			if (compass.isSimilar(stack)) {
				hasAlready = true;
				break;
			}
		}
		
		if (!hasAlready) {
			inv.setItem(inv.firstEmpty(), compass);
			player.updateInventory();
		}
	}
	
	public void spawn(Player player) {
		
		this.openBoard(player);
		this.giveCompass(player);
		player.teleport(spawn);
	}
	
	public void openBoard(Player player) {
		
		if (!openBoards.containsKey(player))
			openBoards.put(player, new HubBoard(boardFormat, player, plugin));
	}
	
	public void closeBoard(Player player) {
		
		HubBoard board = openBoards.get(player);
		
		if (board != null) {
			board.destroy();
			openBoards.remove(player);
		}
	}
	
	public HubBoard getBoard(Player player) {
		
		return openBoards.get(player);
	}
	
	public void broadcastMessage(String msg) {
		
		for (Player player : openBoards.keySet())
			player.sendMessage(msg);
	}
	
	public void broadcastMessage(Message message, Object... args) {
		
		for (Player player : openBoards.keySet())
			message.send(player, args);
	}
	
	public boolean onCommand(PlayerCommandPreprocessEvent event, Player player, String[] label) {
		
		
		if (label[0].equalsIgnoreCase("shop")) {
			Message.SHOP_DENIED.send(player);
			event.setCancelled(true);
			return true;
		}
		
		return false;
	}
	
	public boolean onInteract(PlayerInteractEvent event) {
		
		for (JoinPoint join : this.joins) {
			if (join.onInteract(event))
				return true;
		}
		
		if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
			return false;
		
		if (event.getPlayer().getItemInHand().isSimilar(compass)) {
			plugin.openGamesInterface(event.getPlayer());
			return true;
		}
		
		return false;
	}
	
	public boolean onInventoryClick(InventoryClickEvent event) {
		
		ItemStack stack = event.getCurrentItem();
		
		if (stack == null)
			return false;
		else if (stack.isSimilar(compass)) {
			event.setCancelled(true);
			return true;
		}
		
		return false;
	}
	
	public void onChatMessage(AsyncPlayerChatEvent event, Player player) {
		
		event.setCancelled(true);
		this.broadcastMessage(plugin.getChatFormats().getHubMessage(player, event.getMessage()));
	}
	
	public boolean onDropItem(PlayerDropItemEvent event) {
		
		if (event.getItemDrop().getItemStack().isSimilar(compass)) {
			
			PlayerInventory inv = event.getPlayer().getInventory();
			Item item = event.getItemDrop();
			item.remove();
			inv.setItemInHand(compass);
			return true;
		}
		
		return false;
	}
	
	public void playerJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		player.teleport(spawn);
		this.giveCompass(player);
		this.openBoard(player);
	}
	
	public void playerQuit(PlayerQuitEvent event) {
		
		this.closeBoard(event.getPlayer());
	}
	
	public boolean playerDamage(EntityDamageEvent event) {
		
		if (event.getCause() == DamageCause.VOID)
			event.setDamage(0);
		
		if (event.getEntity().getLocation().getWorld().equals(spawn.getWorld())) {
			
			if (event instanceof EntityDamageByEntityEvent) {
				Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
				
				if (damager instanceof Player) {
					if (((Player)damager).hasPermission("skywars.hub.hit"))
						return true;
				}
			}
				
			event.setCancelled(true);
			return true;
		}
		
		return false;
	}
	
	public boolean onBlockPlace(BlockPlaceEvent event) {
		
		if (event.getBlock().getWorld().equals(spawn.getWorld())) {
			if (!event.getPlayer().hasPermission("skywars.hub.build"))
				event.setCancelled(true);
			return true;
		}
			
		return false;
	}
	
	public boolean onBlockBreak(BlockBreakEvent event) {
		
		if (event.getBlock().getWorld().equals(spawn.getWorld())) {
			if (!event.getPlayer().hasPermission("skywars.hub.build"))
				event.setCancelled(true);
			return true;
		}
			
		return false;
	}
	
	public void write() {
		
		/*
		 * Hub structure
		 * 
		 * spawn: <RawLocation>
		 * compass: <ItemStack>
		 * joins:
		 *   <pointname>:
		 *   	<JoinPoint Structure...>
		 * board:
		 */
		
		Config config = plugin.getHubConfig();
		
		if (!config.getConfig().contains("spawn"))
			config.getConfig().createSection("spawn");
		
		config.getConfig().set("spawn", new RawLocation(this.spawn));
		
		if (!config.getConfig().contains("compass"))
			config.getConfig().createSection("compass");
		
		ItemStack compassItem = compass.clone();
		List<String> formLore = new ArrayList<String>();
		
		for (String s : compass.getItemMeta().getLore())
			formLore.add(Config.staticWriteFormat(s));
		
		ItemMeta meta = compassItem.getItemMeta();
		
		meta.setDisplayName(Config.staticWriteFormat(compass.getItemMeta().getDisplayName()));
		meta.setLore(formLore);
		
		compassItem.setItemMeta(meta);
		
		config.getConfig().set("compass", compassItem);
		
		ConfigurationSection joins = config.getConfig().createSection("joins");
		
		for (JoinPoint point : this.joins)
			JoinPoint.writeJoinPoint(point, joins.createSection(point.getName()));
		
		ConfigurationSection board = config.getConfig().createSection("board");
		this.boardFormat.write(board);
		
		config.save();
	}
	
	public void load() {
		
		Config config = plugin.getHubConfig();
		
		Object spawn = config.getConfig().get("spawn");
		
		if (spawn instanceof RawLocation)
			this.spawn = ((RawLocation)spawn).getLocation();
		
		ItemStack compassItem = config.getConfig().getItemStack("compass");
		
		if (compassItem != null) {
			
			List<String> lore = compassItem.getItemMeta().getLore();
			List<String> formLore = new ArrayList<String>();
			
			for (String s : lore)
				formLore.add(Config.staticReadFormat(s));
			
			String name = Config.staticReadFormat(compassItem.getItemMeta().getDisplayName());
			
			ItemMeta meta = compassItem.getItemMeta();
			
			meta.setDisplayName(name);
			meta.setLore(formLore);
			
			compassItem.setItemMeta(meta);
			this.compass = compassItem;
		}
		
		ConfigurationSection joins = config.getConfig().getConfigurationSection("joins");
		
		if (joins != null) {

			this.joins.clear();
			
			for (String name : joins.getKeys(false))
				this.joins.add(JoinPoint.loadJoinPoint(joins.getConfigurationSection(name), plugin));
		}
		
		ConfigurationSection board = config.getConfig().getConfigurationSection("board");
		
		if (board != null)
			this.boardFormat = ProfileScoreFormat.load(board);
	}
	
	public static ItemStack genCompass(String name, List<String> lore) {
		
		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta meta = compass.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		compass.setItemMeta(meta);
		
		return compass;
	}
}
