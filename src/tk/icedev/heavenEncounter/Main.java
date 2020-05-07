package tk.icedev.heavenEncounter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import atlas.commands.CommandManager;

import tk.icedev.heavenEncounter.game.GameManager;
import tk.icedev.heavenEncounter.game.boards.PlayerBoardFormat;
import tk.icedev.heavenEncounter.gui.GamesInterfaceGUI;
import tk.icedev.heavenEncounter.defaults.ChatFormats;
import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.defaults.commands.BroadcastCmdGroup;
import tk.icedev.heavenEncounter.defaults.commands.LeaveCmdGroup;
import tk.icedev.heavenEncounter.defaults.commands.LobbyCmdGroup;
import tk.icedev.heavenEncounter.defaults.commands.LsgamesCmdGroup;
import tk.icedev.heavenEncounter.defaults.commands.MatchCmdGroup;
import tk.icedev.heavenEncounter.defaults.commands.SkywarsCmdGroup;
import tk.icedev.heavenEncounter.extern.AdvancedLicense;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.hub.Hub;
import tk.icedev.heavenEncounter.hub.JoinPoint;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.items.ChestsManager;
import tk.icedev.heavenEncounter.items.Tier;
import tk.icedev.heavenEncounter.listener.MainListener;
import tk.icedev.heavenEncounter.ranking.Profile;
import tk.icedev.heavenEncounter.ranking.Rank;
import tk.icedev.heavenEncounter.ranking.RankAutoSave;
import tk.icedev.heavenEncounter.utils.Config;
import tk.icedev.heavenEncounter.utils.blocks.BlockData;
import tk.icedev.heavenEncounter.utils.blocks.RawLocation;

public class Main extends JavaPlugin implements HeavenEncounter {
	
	static {
		
		PlayerBoardFormat.registerConfigurations();
		BlockData.registerConfiguration();
		Tier.registerConfiguration();
		RawLocation.registerConfiguration();
		Profile.registerConfiguration();
	}
	
	private Map<String, StaticField> fields;
	
	private GamesInterfaceGUI gamesGUI;
	private GameManager gameMan;
	
	private MainListener listener;
	
	private Rank rank;
	private RankAutoSave rankAutoSave;
	
	private ChestsManager chestsMan;
	
	private boolean enabled = false;
	
	private CommandManager cmdMan = new CommandManager();
	private ChatFormats chatFormats;
	
	private Hub hub;
	
	private Config messageFile, fieldFile, itemFile, hubFile, gamesGuiFile, rankFile;
	
	private static final String LICENSE_KEY = "FRGA-CT66-5627-I76M";
	private static final String LICENSE_URL = "http://icedev.altervista.org/license/verify.php";

	@Override
	public void onEnable() {
		
		if (!new AdvancedLicense(LICENSE_KEY, LICENSE_URL, this).register()) {
			
			Message.console("You are not allowed to use this plugin, license is missing");
			getServer().getPluginManager().disablePlugin(this);
		    return;
		}
		
		StaticField.checkForFieldBlockFolder(this);
		
		messageFile = new Config("messages.yml", this);
		fieldFile = new Config("fields.yml", this);
		itemFile = new Config("items.yml", this);
		hubFile = new Config("hub.yml", this);
		gamesGuiFile = new Config("gamesGUI.yml", this);
		rankFile = new Config("rank.yml", this);
		
		try {
			
			safeLoadConfig(messageFile);
			safeLoadConfig(fieldFile);
			safeLoadConfig(itemFile);
			safeLoadConfig(hubFile);
			safeLoadConfig(gamesGuiFile);
			safeLoadConfig(rankFile);
			
		} catch (IOException ex) {
			
			Message.console("Failed to initialize plugin");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		try {
		
			Message.loadMessages(messageFile);
			
			Message.console("Loading chat formats");
			this.loadChatFormats();
			
			Message.console("Loading fields");
			this.loadFields();
			Message.console("Loading rank");
			this.loadRank();
			this.registerCommands();
		
			Message.console("Loading hub settings");
			hub = new Hub(this);
			hub.load();
		
			Message.console("Setting up game manager");
			gameMan = GameManager.load(this);
			gameMan.getWorldManager().registerFields(fields.values(), this);
		
			Message.console("Loading games GUI");
			gamesGUI = GamesInterfaceGUI.load(this);
		
			Message.console("Loading items");
			chestsMan = new ChestsManager();
			chestsMan.load(itemFile);
		
			listener = new MainListener(this);
			
		} catch (Exception ex) {
			
			Message.console("Failed to initialize plugin: loading error...");
			Message.console("Disabling it...");
			ex.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			
		} finally {
			
			Message.console("Plugin initialized");
			enabled = true;
		}
	}
	
	@Override
	public void onDisable() {
		
		if (!enabled)
			return;
		
		Message.console("Disabling " + TITLE);
		/*Message.console("Saving configurations");
		
		this.messageFile.save();
		this.fieldFile.save();
		this.itemFile.save();
		this.hubFile.save();
		this.gamesGuiFile.save();*/
		
		Message.console("Saving rank");
		this.saveRank();
		
		this.messageFile = null;
		this.fieldFile = null;
		this.itemFile = null;
		this.hubFile = null;
		this.gamesGuiFile = null;
		this.rankFile = null;
		
		this.listener.destroy();
		
		Message.console("Disabling game manager");
		
		try {
			this.gameMan.destroy();
		} catch (Exception ex) {
			Message.console("An error occured while disabling the game manager");
			ex.printStackTrace();
			Message.console("Check in the file system and remove the game world");
		}
		
		this.rankAutoSave.destroy();
		this.fields.clear();
		
		this.fields = null;
		this.listener = null;
		this.gameMan = null;
		this.rankAutoSave = null;
		this.gamesGUI = null;
		
		Message.console("Cancelling running tasks");
		
		this.getServer().getScheduler().cancelTasks(this);
		
		Message.console("Disabling hub manager");
		
		try {
			this.hub.destroy();
		} catch (Exception ex) {
			Message.console("An error occured while disabling the hub manager");
			ex.printStackTrace();
		}
		
		this.rank = null;
		this.hub = null;
		this.cmdMan = null;
	}
	
	private static void safeLoadConfig(Config config) throws IOException {
		
		try {
			config.reload();
		} catch (Exception ex) {
			
			Message.console("Error loading " + config.getConfig().getName());
			Message.console("Saving default " + config.getConfig().getName());
			config.saveDefault();
			
			try {
				config.reload();
			} catch (Exception exc) {
				Message.console("Default + " + config.getConfig().getName() + " corrupted");
				throw new IOException("Default " + config.getConfig().getName() + " corrupted");
			}
		}
	}
	
	private void registerCommands() {
		
		this.cmdMan.registerGroup(new SkywarsCmdGroup(this), this);
		this.cmdMan.registerGroup(new MatchCmdGroup(this), this);
		this.cmdMan.registerGroup(new LeaveCmdGroup(), this);
		this.cmdMan.registerGroup(new BroadcastCmdGroup(this), this);
		this.cmdMan.registerGroup(new LobbyCmdGroup(this), this);
		this.cmdMan.registerGroup(new LsgamesCmdGroup(this), this);
	}
	
	private void loadChatFormats() {
		
		chatFormats = new ChatFormats();
		
		boolean mod = false;
		
		ConfigurationSection chat = getConfig().getConfigurationSection("chat");
		
		if (chat == null)
			chat = getConfig().createSection("chat");
		
		mod |= chatFormats.load(chat);
		
		if (mod)
			this.saveConfig();
	}
	
	private void loadFields() {
		
		if (!getConfig().contains("fields")) {
			getConfig().createSection("fields");
			getConfig().set("fields", new ArrayList<String>());
			saveConfig();
		}
		
		fields = new HashMap<String, StaticField>();
		
		@SuppressWarnings("unchecked")
		List<String> fieldList = (List<String>)getConfig().getList("fields");
		
		if (fieldList == null)
			return;
		
		for (String name : fieldList) {
			
			StaticField fd = StaticField.load(name, fieldFile, this);
			
			if (fd == null) {
				Message.console("Failed loading field " + name);
				continue;
			}
			
			Message.console("Loading field " + name);
			fields.put(name, fd);
		}
	}
	
	private void loadRank() {
		
		rank = new Rank();
		ConfigurationSection section = rankFile.getConfig().getConfigurationSection("rank");
		
		if (section == null)
			section = rankFile.getConfig().createSection("rank");
		
		rank.load(section);
		
		long freq = 2400L;
		
		if (rankFile.getConfig().isLong("autosave"))
			freq = rankFile.getConfig().getLong("autosave");
		else {
			rankFile.getConfig().createSection("autosave");
			rankFile.getConfig().set("autosave", freq);
			rankFile.save();
		}
		
		if (freq > 0)
			this.rankAutoSave = new RankAutoSave(this, freq);
	}
	
	@Override
	public void saveRank() {
		
		ConfigurationSection section = rankFile.getConfig().getConfigurationSection("rank");
		
		if (section == null)
			section = rankFile.getConfig().createSection("rank");
		
		rank.save(section);
		
		rankFile.save();
	}
	
	@Override
	public Hub getHub() {
		
		return hub;
	}

	@Override
	public Config getFieldConfig() {
		
		return this.fieldFile;
	}

	@Override
	public Config getItemConfig() {
		
		return this.itemFile;
	}

	@Override
	public Config getHubConfig() {
		
		return this.hubFile;
	}

	@Override
	public void addJoinPoint(JoinPoint point) {
		
		hub.joins.add(point);
		hub.write();	// save hub status
	}

	@Override
	public StaticField getField(String name) {
		
		return fields.get(name);
	}

	@Override
	public void addField(World world, StaticField field) {
		
		StaticField.backup(world, field, fieldFile, this);
		fields.put(field.getName(), field);
		
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)getConfig().getList("fields");
		
		if (list == null)
			list = new ArrayList<String>();
		
		if (list.contains(field.getName()))
			list.remove(field.getName());
		
		list.add(field.getName());
		
		getConfig().set("fields", list);
		saveConfig();
		
		gameMan.getWorldManager().registerNewField(field, this);
	}
	
	@Override
	public Set<Entry<String, StaticField>> getFieldMap() {
		
		return fields.entrySet();
	}
	
	@Override
	public void reloadField(String name) {
		
		StaticField field = StaticField.load(name, fieldFile, this);
		
		if (field == null) {
			Message.console("Failed loading field " + name);
			return;
		}
		
		Message.console("Loading field " + name);
		
		fields.put(name, field);
	}

	@Override
	public void removeField(String name) {
		
		gameMan.interruptGames(this.getField(name));
		
		StaticField.remove(name, fieldFile, this);
		fields.remove(name);
		
		gameMan.getWorldManager().getFieldsLine(name).unload(this, true);
		
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>)getConfig().getList("fields");
		
		if (list.contains(name))
			list.remove(name);
		
		getConfig().set("fields", list);
		saveConfig();
	}

	@Override
	public void openGamesInterface(Player player) {
		
		this.gamesGUI.openGUI(player);
	}

	@Override
	public Plugin getPlugin() {
		
		return this;
	}

	@Override
	public Rank getRank() {
		
		return rank;
	}

	@Override
	public GameManager getGameManager() {
		
		return gameMan;
	}

	@Override
	public GamesInterfaceGUI getGamesGUI() {
		
		return gamesGUI;
	}

	@Override
	public void reload() {
		
		StaticField.checkForFieldBlockFolder(this);
		
		try {
			
			safeLoadConfig(messageFile);
			safeLoadConfig(fieldFile);
			safeLoadConfig(itemFile);
			safeLoadConfig(hubFile);
			safeLoadConfig(gamesGuiFile);
			safeLoadConfig(rankFile);
			
		} catch (IOException ex) {
			
			Message.console("Failed to reload plugin");
			Message.console("Some .yml file could be corrupted");
			return;
		}
		
		Message.loadMessages(messageFile);
		
		this.loadFields();
		this.loadChatFormats();
		this.loadRank();
		
		gameMan.reload();
		
		for (Player player : Bukkit.getOnlinePlayers())
			player.closeInventory();
		
		gamesGUI = GamesInterfaceGUI.load(this);
		
		hub.reload();
		chestsMan.reload(itemFile);
	}

	@Override
	public Collection<StaticField> getFields() {
		
		return fields.values();
	}

	@Override
	public Set<String> getFieldsNames() {
		
		return fields.keySet();
	}

	@Override
	public Config getGamesGUIConfig() {
		
		return gamesGuiFile;
	}

	@Override
	public ChestsManager getChestsManager() {
		
		return chestsMan;
	}

	@Override
	public ChatFormats getChatFormats() {
		
		return chatFormats;
	}
}
