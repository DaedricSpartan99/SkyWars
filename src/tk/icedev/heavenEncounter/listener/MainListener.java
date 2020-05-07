package tk.icedev.heavenEncounter.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;

public class MainListener implements Listener {
	
	HeavenEncounter plugin;

	public MainListener(HeavenEncounter plugin) {
		
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin.getPlugin());
	}
	
	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent event) {
		
		Player player = event.getPlayer();
		
		if (plugin.getGameManager().onChatMessage(event, player))
			;
		else
			plugin.getHub().onChatMessage(event, player);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		plugin.getHub().playerJoin(event);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		plugin.getHub().playerQuit(event);
		plugin.getGameManager().onPlayerQuit(event);
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		
		if (plugin.getHub().playerDamage(event))
			;
		else if (plugin.getGameManager().onPlayerDamage(event))
			;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		
		String msg = event.getMessage();
		
		if (msg == "/")
			return;
		
		String[] label = cmdToLabel(msg);
		Player player = event.getPlayer();
		
		if (plugin.getGameManager().onPlayerCommand(event, player, label))
			;
		else if (plugin.getHub().onCommand(event, player, label))
			;
	}
	
	public static String[] cmdToLabel(String msg) {
		
		msg = msg.replaceFirst("/", "");
		
		String[] label = msg.split(" ");
		
		if (label == null) {
			label = new String[1];
			label[0] = msg;
		} else if (label.length == 0) {
			label = new String[1];
			label[0] = msg;
		}
		
		return label;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (plugin.getHub().onInteract(event))
			;
		else if (plugin.getGameManager().onPlayerInteract(event))
			;
	}
	
	@EventHandler
	public void onPlayerIgnite(BlockIgniteEvent event) {
		
		Player player = event.getPlayer();
		
		if (plugin.getGameManager().onPlayerIgnite(event, player))
			;
		else
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onFireSpread(BlockSpreadEvent event) {
		
		Block source = event.getSource();
		
		if (plugin.getGameManager().onFireSpread(event, source))
			;
		else
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		
		if (plugin.getHub().onBlockPlace(event))
			;
		else if (plugin.getGameManager().onBlockPlace(event))
			;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		if (plugin.getHub().onBlockBreak(event))
			;
		else if (plugin.getGameManager().onBlockBreak(event))
			;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		if (plugin.getGamesGUI().onInventoryClick(event))
			;
		else if (plugin.getHub().onInventoryClick(event))
			;
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		
		plugin.getHub().onDropItem(event);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		
		event.getEntity().remove();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		
		plugin.getGameManager().onEntityExplode(event);
	}
	
	public void destroy() {
		
		HandlerList.unregisterAll(this);
	}
}
