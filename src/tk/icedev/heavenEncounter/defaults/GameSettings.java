package tk.icedev.heavenEncounter.defaults;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import tk.icedev.heavenEncounter.game.boards.MapBoard.MapBoardFormat;
import tk.icedev.heavenEncounter.game.boards.ScorePanel;
import tk.icedev.heavenEncounter.game.boards.ScorePanel.GameBoardFormat;
import tk.icedev.heavenEncounter.utils.Config;

public class GameSettings {

	public int min_players = 2;
	public String emptyWorld = "skyworld";
	public int initMapNumber = 5;
	public int mapsDistance = 5000;
	public String[] boardTitle = ScorePanel.DEFAULT_TITLE;
	public List<String> commandsInGame = Arrays.asList("shop", "global");
	public GameBoardFormat gameBoardFormat = new GameBoardFormat();
	public MapBoardFormat mapBoardFormat = new MapBoardFormat();
	
	@SuppressWarnings("unchecked")
	public static boolean load(GameSettings settings, ConfigurationSection section) {
		
		boolean modified = false;
		
		// Empty world name
		
		if (!section.isSet("empty_world")) {
			
			section.createSection("empty_world");
			section.set("empty_world", settings.emptyWorld);
			modified = true;
			
		} else
			settings.emptyWorld = section.getString("empty_world");
				
		// Maps distance
		
		if (!section.isSet("maps_distance")) {
			
			section.createSection("maps_distance");
			section.set("maps_distance", settings.mapsDistance);
			modified = true;
			
		} else
			settings.mapsDistance = section.getInt("maps_distance");
		
		// Minimal players
		
		if (!section.isSet("min_players")) {
			
			section.createSection("min_players");
			section.set("min_players", settings.min_players);
			modified = true;
			
		} else
			settings.min_players = section.getInt("min_players");
		
		if (!section.isSet("initMapNumber")) {
			
			section.createSection("initMapNumber");
			section.set("initMapNumber", settings.initMapNumber);
			modified = true;
			
		} else
			settings.initMapNumber = section.getInt("initMapNumber");
		
		// Board title
		
		List<String> bTitleList;
		
		if (!section.isSet("boardTitle")) {
			
			bTitleList = Arrays.asList(settings.boardTitle);
			
			section.createSection("boardTitle");
			section.set("boardTitle", bTitleList);
			modified = true;
			
		} else {
		
			bTitleList = (List<String>) section.getList("boardTitle");
			settings.boardTitle = new String[bTitleList.size()];
			
			for (int i = 0; i < settings.boardTitle.length; i++)
				settings.boardTitle[i] = Config.staticReadFormat(bTitleList.get(i));
		}
		
		if (!section.isList("commandsInGame")) {
			
			section.createSection("commandsInGame");
			section.set("commandsInGame", settings.commandsInGame);
			modified = true;
			
		} else {
			
			settings.commandsInGame = (List<String>)section.getList("commandsInGame");
		}
		
		ConfigurationSection mapBoard = section.getConfigurationSection("mapBoard");
		
		if (mapBoard == null) {
			
			mapBoard = section.createSection("mapBoard");
			settings.mapBoardFormat = new MapBoardFormat();
			settings.mapBoardFormat.write(mapBoard);
			modified = true;
			
		} else
			settings.mapBoardFormat = MapBoardFormat.load(mapBoard);
		
		ConfigurationSection gameBoard = section.getConfigurationSection("gameBoard");
		
		if (gameBoard == null) {
			
			gameBoard = section.createSection("gameBoard");
			settings.gameBoardFormat = new GameBoardFormat();
			settings.gameBoardFormat.write(gameBoard);
			modified = true;
			
		} else
			settings.gameBoardFormat = GameBoardFormat.load(gameBoard);
		
		return modified;
	}
}
