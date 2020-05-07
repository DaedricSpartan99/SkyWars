package tk.icedev.heavenEncounter.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tk.icedev.heavenEncounter.defaults.GameSettings;
import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.game.fields.Field;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.utils.BukkitMath;

import static tk.icedev.heavenEncounter.extern.HeavenEncounter.NULL_ID;

public class GameManager {

	private Map<Integer, Game> gamesMap;
	private GameSettings settings;
	private GameWorldManager gameWorldMan;
	private HeavenEncounter plugin;
	
	public GameManager(GameSettings settings, World world, HeavenEncounter plugin) {
		
		this.plugin = plugin;
		this.settings = settings;
		this.gameWorldMan = new GameWorldManager(world, settings.initMapNumber, settings.mapsDistance, plugin.getPlugin());
		
		this.gamesMap = new HashMap<Integer, Game>();
	}
	
	public void destroy() {
		
		// interrupt running games
		
		this.clearGames();
		gamesMap.clear();
		
		gamesMap = null;
				
		// erase game world
				
		gameWorldMan.destroy(plugin.getPlugin());
		gameWorldMan = null;
		settings = null;
	}
	
	public void reload() {
		
		this.clearGames();
		
		if (loadSettings(this.settings, plugin.getConfig()))
			plugin.saveConfig();
		
		gameWorldMan.reload(plugin.getFieldsNames(), plugin);
	}
	
	/*
	 * Loading functions
	 */
	
	public static GameManager load(HeavenEncounter plugin) {
		
		FileConfiguration config = plugin.getConfig();
		
		GameSettings settings = new GameSettings();
		
		if (loadSettings(settings, config))
				plugin.saveConfig();
		
		World voidWorld = GameWorldManager.generateVoidWorld(settings.emptyWorld, plugin.getPlugin());
		
		return new GameManager(settings, voidWorld, plugin);
	}
	
	private static boolean loadSettings(GameSettings settings, FileConfiguration config) {
		
		boolean mod = false;
		
		ConfigurationSection section = config.getConfigurationSection("game");
		
		if (section == null)
			section = config.createSection("game");
		
		if (GameSettings.load(settings, section))
			mod = true;
		
		return mod;
	}
	
	/*
	 *  Listeners
	 */
	
	public boolean onBlockPlace(BlockPlaceEvent event) {
		
		Player player = event.getPlayer();
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().getField().onBlockPlace(event);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean onBlockBreak(BlockBreakEvent event) {
		
		Player player = event.getPlayer();
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().getField().onBlockBreak(event);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean onPlayerDamage(EntityDamageEvent event) {
		
		Entity ent = event.getEntity();
		
		if (ent instanceof Player) {
			
			Player player = (Player)ent;
			
			for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
				if (entry.getValue().inGame(player)) {
					entry.getValue().onPlayerDamage(player, event);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean onPlayerCommand(PlayerCommandPreprocessEvent event, Player player, String[] label) {
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			
			if (entry.getValue().inGame(player)) {
				event.setCancelled(entry.getValue().onPlayerCommand(player, label));
				return true;
			}
		}
		
		return false;
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().onDeath(player, null, false);
				break;
			}
		}
	}
	
	public boolean onPlayerIgnite(BlockIgniteEvent event, Player player) {
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().getField().onFireIgnite(event);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean onFireSpread(BlockSpreadEvent event, Block source) {
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().getField().onSpread(event, source))
				return true;
		}
		
		return false;
	}
	
	public boolean onChatMessage(AsyncPlayerChatEvent event, Player player) {
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().onChatMessage(player, event.getMessage());
				event.setCancelled(true);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		

		Player player = event.getPlayer();
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().inGame(player)) {
				entry.getValue().getField().onPlayerInteract(event);
				return true;
			}
		}
		
		return false;
	}
	
	public void onEntityExplode(EntityExplodeEvent event) {
		
		for (Entry<Integer, Game> entry : plugin.getGameManager().getGamesMap()) {
			if (entry.getValue().getField().onEntityExplode(event))
				break;
		}
	}
	
	/*
	 *  Accessible functions
	 */
	
	public GameWorldManager getWorldManager() {
		
		return gameWorldMan;
	}
	
	public Set<Entry<Integer, Game>> getGamesMap() {
		
		return this.gamesMap.entrySet();
	}

	public Game getGame(int id) {
		
		return this.gamesMap.get(id);
	}
	
	public void registerGame(int id, Game game) {
		
		this.interruptGame(id);
		
		gamesMap.put(id, game);
		plugin.getGamesGUI().checkUpdateItem(id, game);
		game.addGameListener(plugin.getGamesGUI());
	}
	
	public int getFreeID() {
		
		for (int id = 1; id < plugin.getGamesGUI().getSize(); id++) {
			
			if (!this.gamesMap.containsKey(id))
				return id;
		}
		
		return NULL_ID;
	}
	
	public int startNewGame(String fieldName) {
		
		return this.startNewGame(plugin.getField(fieldName));
	}
	
	public int startNewGame(StaticField sfield) {
		
		if (sfield == null) {
			Message.console("This field is not available, could not start the game");
			return NULL_ID;
		}
			
		int id = this.getFreeID();
		
		if (id == NULL_ID)
			return NULL_ID;
		else if (gameWorldMan.getWorld() == null) {
			Message.console("There is no empty world available");
			return NULL_ID;
		}
		
		Field field = gameWorldMan.getFreeField(sfield, plugin.getPlugin());
		
		this.registerGame(id, new Game(field, plugin));
		
		return id;
	}

	public StaticField randomField() {
		
		Set<Entry<String, StaticField>> fieldMap = plugin.getFieldMap();
		
		if (fieldMap.size() == 0)
			return null;
		
		int index = BukkitMath.randomInt(fieldMap.size());
		
		int i = 0;
		for (Entry<String, StaticField> entry : fieldMap) {
			if (i == index)
				return entry.getValue();
			i++;
		}
		
		return null;
	}

	public int newRandomGame() {
		
		return this.startNewGame(this.randomField());
	}
	
	public boolean joinGame(Player player, int id) {
		
		boolean call = false;
		
		Game game = this.gamesMap.get(id);
		
		if (id == NULL_ID || game == null) {
			Message.dynamicMessage(player, "Cannot join a null game");
			return false;
		}
		
		switch(game.join(player)) {
		
		case SUCCESS:
			call = true;
			break;
		case RUNNING_GAME:
			Message.JOIN_PROGRESS.send(player);
			break;
		case IN_GAME:
			Message.JOIN_ALREADY.send(player);
			break;
		case FULL_LOBBY:
			Message.JOIN_FULL.send(player);
			break;
		default:
			break;
		}
		
		return call;
	}

	public void joinRandomGame(Player player) {
		
		for (Entry<Integer, Game> entry : this.gamesMap.entrySet()) {
			
			if (entry.getValue().isJoinable()) {
				entry.getValue().join(player);
				return;
			}
		}
		
		int id = this.newRandomGame();
		
		if (id == NULL_ID) {
			Message.dynamicMessage(player, "Cannot join a null game");
			return;
		}
		
		this.joinGame(player, id);
	}
	
	public int getID(Game game) {
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			if (entry.getValue().equals(game)) {
				return entry.getKey();
			}
		}
		
		return NULL_ID;
	}

	public Game gameOf(Player player) {
		
		for (Entry<Integer, Game> entry : this.gamesMap.entrySet()) {
			if (entry.getValue().inGame(player))
				return entry.getValue();
		}
		
		return null;
	}

	public void removeGame(Game game) {
		
		int id = getID(game);
		
		if (id != NULL_ID)
			gamesMap.remove(id);
	}

	public void interruptGame(int id) {
		
		if (!gamesMap.containsKey(id))
			return;
		
		gamesMap.get(id).interrupt();
	}

	public void interruptGame(Game game) {
		
		game.interrupt();
	}
	
	public void interruptGames(StaticField field) {
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			if (entry.getValue().getFieldName().equals(field.getName()))
				entry.getValue().interrupt();
		}
	}

	public Collection<Game> getGames() {
		
		return gamesMap.values();
	}

	public void clearGames() {
		
		for (Entry<Integer, Game> entry : gamesMap.entrySet()) {
			Message.console("interrupting game " + entry.getKey());
			entry.getValue().interrupt();
		}
	}
	
	public World getGameWorld() {
		
		return gameWorldMan.getWorld();
	}
	
	public GameSettings getGameSettings() {
		
		return this.settings;
	}
}

