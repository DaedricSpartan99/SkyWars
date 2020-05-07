package tk.icedev.heavenEncounter.extern;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import tk.icedev.heavenEncounter.defaults.ChatFormats;
import tk.icedev.heavenEncounter.game.GameManager;
import tk.icedev.heavenEncounter.gui.GamesInterfaceGUI;
import tk.icedev.heavenEncounter.hub.Hub;
import tk.icedev.heavenEncounter.hub.JoinPoint;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.items.ChestsManager;
import tk.icedev.heavenEncounter.ranking.Rank;
import tk.icedev.heavenEncounter.utils.Config;

public interface HeavenEncounter {
	
	String TITLE = "SkyWars";
	String PLUGIN_NAME = "Skywars";
	int NULL_ID = -1;
	
	StaticField getField(String name);
	void addField(World world, StaticField field);
	Set<Entry<String, StaticField>> getFieldMap();
	Collection<StaticField> getFields();
	Set<String> getFieldsNames();
	void reloadField(String name);
	void removeField(String name);
	
	void addJoinPoint(JoinPoint point);
	
	Hub getHub();
	
	GameManager getGameManager();
	
	Config getFieldConfig();
	Config getItemConfig();
	Config getHubConfig();
	Config getGamesGUIConfig();
	FileConfiguration getConfig();
	void saveConfig();
	
	void saveRank();
	
	GamesInterfaceGUI getGamesGUI();
	
	ChatFormats getChatFormats();
	
	void openGamesInterface(Player player);
	
	Rank getRank();
	
	ChestsManager getChestsManager();
	
	Plugin getPlugin();
	
	void reload();
}
