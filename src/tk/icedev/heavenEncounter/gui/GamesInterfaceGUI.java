package tk.icedev.heavenEncounter.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tk.icedev.heavenEncounter.game.Game;
import tk.icedev.heavenEncounter.game.Game.GameStatus;
import tk.icedev.heavenEncounter.game.Game.QuitCause;
import tk.icedev.heavenEncounter.utils.Config;
import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.GameListener;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;

public class GamesInterfaceGUI implements GameListener {

	private final Inventory inventory;
	private GameMeta meta;
	private HeavenEncounter plugin;
	private StatusItems statusItems;
	
	public GamesInterfaceGUI(String title, int size, HeavenEncounter plugin) {
		
		this.plugin = plugin;
		
		this.inventory = Bukkit.createInventory(null, size, title);
		this.meta = new GameMeta();
		this.statusItems = new StatusItems();
	}
	
	public int getSize() {
		
		return inventory.getSize();
	}
	
	public static int gameSlot(int id) {
		
		return id - 1;
	}
	
	public static int gameID(int slot) {
		
		return slot + 1;
	}
	
	public void checkUpdateItem(int id, Game game) {
		
		int slot = gameSlot(id);
		
		if (game == null || id == HeavenEncounter.NULL_ID) {
			inventory.setItem(slot, null);
			return;
		}
		
		ItemStack newitem = statusItems.getItem(game.getStatus());
		
		newitem.setAmount(id);
		inventory.setItem(slot, meta.attachItemMeta(newitem, id, game));
	}
	
	public void openGUI(Player player) {
		
		if (!player.hasPermission("skywars.game.join")) {
			Message.NO_PERMISSIONS.send(player);
			return;
		}
		
		player.openInventory(inventory);
		player.updateInventory();
	}
	
	public void update(Game game) {
		
		this.checkUpdateItem(plugin.getGameManager().getID(game), game);
	}
	
	public void remove(Game game) {
		
		this.checkUpdateItem(plugin.getGameManager().getID(game), null);
	}
	
	public boolean onInventoryClick(InventoryClickEvent event) {
		
		if (!event.getInventory().equals(inventory))
			return false;
		
		event.setCancelled(true);
		
		if (event.getSlot() != event.getRawSlot())
			return true;
		
		ItemStack item = event.getCurrentItem();
		
		if (item == null || !(event.getWhoClicked() instanceof Player))
			return true;
		else if (item.getType() == Material.AIR)
			return true;
		
		Player player = (Player)event.getWhoClicked();
		int id = item.getAmount();
		
		if (plugin.getGameManager().joinGame(player, id))
			player.closeInventory();
		
		return true;
	}
	
	public class StatusItems {
		
		private Map<GameStatus, ItemStack> map;
		
		public StatusItems() {
			
			map = new HashMap<GameStatus, ItemStack>();
			map.put(GameStatus.WAITING, new ItemStack(Material.STAINED_CLAY, 1, (short)5));
			map.put(GameStatus.STARTING, new ItemStack(Material.STAINED_CLAY, 1, (short)4));
			map.put(GameStatus.CLOSED, new ItemStack(Material.STAINED_CLAY, 1, (short)10));
		}
		
		public ItemStack getItem(GameStatus status) {
			
			return map.get(status).clone();
		}
		
		public boolean load(ConfigurationSection section) {
			
			boolean modified = false;
			
			if (section.isSet("open"))
				map.put(GameStatus.WAITING, section.getItemStack("open"));
			else {
				section.createSection("open");
				section.set("open", map.get(GameStatus.WAITING));
				modified = true;
			}
			
			if (section.isSet("starting"))
				map.put(GameStatus.STARTING, section.getItemStack("starting"));
			else {
				section.createSection("starting");
				section.set("starting", map.get(GameStatus.STARTING));
				modified = true;
			}
			
			if (section.isSet("running"))
				map.put(GameStatus.CLOSED, section.getItemStack("running"));
			else {
				section.createSection("running");
				section.set("running", map.get(GameStatus.CLOSED));
				modified = true;
			}
			
			return modified;
		}
	}
	
	public class GameMeta {
		
		public String titleFormat;
		public GameLore lore;
		public Map<GameStatus, String> statusSignal;
		
		public GameMeta() {
			
			this.titleFormat = "SkyWars | %d";
			this.lore = new GameLore();
			this.statusSignal = new HashMap<GameStatus, String>();
			this.statusSignal.put(GameStatus.WAITING, "Waiting for players...");
			this.statusSignal.put(GameStatus.STARTING, "Starting!");
			this.statusSignal.put(GameStatus.CLOSED, "Running...");
		}
		
		public ItemStack attachItemMeta(ItemStack stack, int id, Game game) {
			
			ItemMeta meta = stack.getItemMeta();
			
			meta.setDisplayName(Message.formatArgs(titleFormat, id));
			
			List<String> formatted = new ArrayList<String>();
			
			formatted.add(Message.formatArgs(lore.map, game.getCapitalFieldName()));
			formatted.add(Message.formatArgs(lore.status, statusSignal.get(game.getStatus())));
			formatted.add(Message.formatArgs(lore.lobby, game.sizeofLobby(), game.maxSizeofLobby()));
			
			meta.setLore(formatted);
			
			stack.setItemMeta(meta);
			
			return stack;
		}
		
		public boolean load(ConfigurationSection section) {
			
			boolean mod = false;
			
			if (section.isSet("title"))
				titleFormat = Config.staticReadFormat(section.getString("title"));
			else {
				section.createSection("title");
				section.set("title", Config.staticWriteFormat(titleFormat));
				mod |= true;
			}
			
			ConfigurationSection loreSec = section.getConfigurationSection("lore");
			
			if (loreSec == null)
				loreSec = section.createSection("lore");
			
			mod |= lore.load(loreSec);
			
			ConfigurationSection statusSigSec = section.getConfigurationSection("statusSignal");
			
			if (statusSigSec == null)
				statusSigSec = section.createSection("statusSignal");
			
			mod |= this.loadStatusSignal(statusSigSec);
			
			return mod;
		}
		
		private boolean loadStatusSignal(ConfigurationSection section) {
			
			boolean mod = false;
			
			if (section.isSet("open"))
				statusSignal.put(GameStatus.WAITING, Config.staticReadFormat(section.getString("open")));
			else {
				section.createSection("open");
				section.set("open", Config.staticWriteFormat(statusSignal.get(GameStatus.WAITING)));
				mod = true;
			}
			
			if (section.isSet("starting"))
				statusSignal.put(GameStatus.STARTING, Config.staticReadFormat(section.getString("starting")));
			else {
				section.createSection("starting");
				section.set("starting", Config.staticWriteFormat(statusSignal.get(GameStatus.STARTING)));
				mod = true;
			}
			
			if (section.isSet("running"))
				statusSignal.put(GameStatus.CLOSED, Config.staticReadFormat(section.getString("running")));
			else {
				section.createSection("running");
				section.set("running", Config.staticWriteFormat(statusSignal.get(GameStatus.CLOSED)));
				mod = true;
			}
			
			return mod;
		}
		
		public class GameLore {
			
			public String map, status, lobby;
			
			public GameLore() {
				
				map = "Map: %s";
				status = "Status: %s";
				lobby = "Players: %d / %d";
			}
			
			public GameLore (String map, String status, String lobby) {
				
				this.map = map;
				this.status = status;
				this.lobby = lobby;
			}
			
			public boolean load(ConfigurationSection section) {
				
				boolean mod = false;
				
				if (section.isSet("map"))
					map = Config.staticReadFormat(section.getString("map"));
				else {
					section.createSection("map");
					section.set("map", Config.staticWriteFormat(map));
					mod = true;
				}
				
				if (section.isSet("status"))
					status = Config.staticReadFormat(section.getString("status"));
				else {
					section.createSection("status");
					section.set("status", Config.staticWriteFormat(status));
					mod = true;
				}
				
				if (section.isSet("lobby"))
					lobby = Config.staticReadFormat(section.getString("lobby"));
				else {
					section.createSection("lobby");
					section.set("lobby", Config.staticWriteFormat(lobby));
					mod = true;
				}
				
				return mod;
			}
		}
	}
	
	public boolean loadConfigs(Config config) {
		
		boolean mod = false;
		
		ConfigurationSection items = config.getConfig().getConfigurationSection("statusItems");
		
		if (items == null)
			items = config.getConfig().createSection("statusItems");
		
		mod |= this.statusItems.load(items);
		
		ConfigurationSection meta = config.getConfig().getConfigurationSection("meta");
		
		if (meta == null)
			meta = config.getConfig().createSection("meta");
		
		mod |= this.meta.load(meta);
		
		return mod;
	}
	
	public static GamesInterfaceGUI load(HeavenEncounter plugin) {
		
		/*	Structure
		 * 
		 * 	title: <title>
		 * 	size: <size>
		 *  meta:
		 *    title: <display name format>
		 *    lore:
		 *      map: <map format>
		 *      status: <status format>
		 *      lobby: <lobby format>
		 *    statusItems:
		 * 	    open:	<open item>
		 * 	    starting: <starting item>
		 * 	    close: <close item>
		 */
		
		Config config = plugin.getGamesGUIConfig();
		
		boolean modified = false;
		
		String title;
		
		if (config.getConfig().isSet("title"))
			title = Config.staticReadFormat(config.getConfig().getString("title"));
		else {
			
			title = "Running games";
			
			config.getConfig().createSection("title");
			config.getConfig().set("title", title);
			modified = true;
		}
		
		int size;
		
		if (config.getConfig().isSet("size"))
			size = config.getConfig().getInt("size");
		else {

			size = 27;
			config.getConfig().createSection("size");
			config.getConfig().set("size", size);
			modified = true;
		}
		
		size -= size % 9;	// only 9 multiples
		
		GamesInterfaceGUI gui = new GamesInterfaceGUI(title, size, plugin);
		
		modified |= gui.loadConfigs(config);
		
		if (modified) {
			Message.console("Saving GUI");
			config.save();
		}
		
		return gui;
	}

	@Override
	public void onTimerStarting(Game game) {
		
		update(game);
	}

	@Override
	public void onPlayerJoin(Player player, Game game) {
		
		update(game);
	}

	@Override
	public void onPlayerQuit(Player player, Game game, QuitCause cause) {
		
		if (cause != QuitCause.WINNER)
			update(game);
	}

	@Override
	public void onLobbyTurnOpen(Game game) {
		
		update(game);
	}

	@Override
	public void onGameStart(Game game) {
		
	}

	@Override
	public void afterGameStart(Game game) {
		
		update(game);
	}

	@Override
	public void onGameEnd(Game game, Player winner) {
		
		remove(game);
	}
}
