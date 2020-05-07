package tk.icedev.heavenEncounter.game.boards;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import tk.icedev.heavenEncounter.game.Game;
import tk.icedev.heavenEncounter.game.Game.GameStatus;
import tk.icedev.heavenEncounter.game.Game.QuitCause;
import tk.icedev.heavenEncounter.game.fields.Lobby;
import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.utils.Config;

public class MapBoard extends GameBoard {
	
	MapBoardFormat format;

	public MapBoard(Lobby lobby, String map, String[] title, MapBoardFormat format, Plugin plugin) {
		
		super(lobby, title, plugin);
		
		obj.getScore("" + ChatColor.RESET).setScore(15);
		obj.getScore(Message.formatInt(format.players, lobby.getSize(), lobby.getMaximalSize())).setScore(14);
		obj.getScore("" + ChatColor.RESET + ChatColor.RESET).setScore(13);
		obj.getScore(Message.formatString(format.map, map)).setScore(12);
		obj.getScore("" + ChatColor.RESET + ChatColor.RESET + ChatColor.RESET).setScore(11);
		obj.getScore(Message.formatString(format.status, format.statusMap.get(GameStatus.WAITING))).setScore(10);
		obj.getScore("" + ChatColor.RESET + ChatColor.RESET + ChatColor.RESET + ChatColor.RESET).setScore(2);
		obj.getScore(format.server).setScore(1);
		
		this.format = format;
	}
	
	@Override
	public void onTimerStarting(Game game) {
		
		board.resetScores(Message.formatString(format.status, format.statusMap.get(GameStatus.WAITING)));
		obj.getScore(Message.formatString(format.status, format.statusMap.get(GameStatus.STARTING))).setScore(10);
		
		this.update();
	}

	@Override
	public void onPlayerJoin(Player player, Game game) {
		
		board.resetScores(Message.formatInt(format.players, lobby.getSize() - 1, lobby.getMaximalSize()));
		obj.getScore(Message.formatInt(format.players, lobby.getSize(), lobby.getMaximalSize())).setScore(14);
		
		this.update();
	}

	@Override
	public void onPlayerQuit(Player player, Game game, QuitCause cause) {
		
		board.resetScores(Message.formatInt(format.players, lobby.getSize() + 1, lobby.getMaximalSize()));
		obj.getScore(Message.formatInt(format.players, lobby.getSize(), lobby.getMaximalSize())).setScore(14);
		
		this.removeScore(player);
		this.update();
	}

	@Override
	public void onLobbyTurnOpen(Game game) {
		
		board.resetScores(Message.formatString(format.status, format.statusMap.get(GameStatus.STARTING)));
		obj.getScore(Message.formatString(format.status, format.statusMap.get(GameStatus.WAITING))).setScore(10);
	}

	@Override
	public void onGameStart(Game game) {}

	@Override
	public void afterGameStart(Game game) {}

	@Override
	public void onGameEnd(Game game, Player winner) {}
	
	public static class MapBoardFormat {
		
		public String players = "Players: %d", 
				map = "Map: %s", 
				status = "Status: %s", 
				server = "mc.unknown.it";
		
		public Map<GameStatus, String> statusMap;
		
		public MapBoardFormat() {
			
			statusMap = new HashMap<GameStatus, String>();
			statusMap.put(GameStatus.WAITING, "Open");
			statusMap.put(GameStatus.STARTING, "Starting");
			statusMap.put(GameStatus.CLOSED, "Running");
		}
		
		public void write(ConfigurationSection section) {
			
			if (section.contains("players"))
				section.createSection("players");
			
			section.set("players", Config.staticWriteFormat(players));
			
			if (section.contains("map"))
				section.createSection("map");
			
			section.set("map", Config.staticWriteFormat(map));
			
			if (section.contains("status"))
				section.createSection("status");
			
			section.set("status", Config.staticWriteFormat(status));
			
			if (section.contains("server"))
				section.createSection("server");
			
			section.set("server", Config.staticWriteFormat(server));
			
			ConfigurationSection statusMap = section.createSection("statusMap");
			statusMap.createSection("open");
			statusMap.createSection("starting");
			statusMap.createSection("running");
			
			statusMap.set("open", Config.staticWriteFormat(this.statusMap.get(GameStatus.WAITING)));
			statusMap.set("starting", Config.staticWriteFormat(this.statusMap.get(GameStatus.STARTING)));
			statusMap.set("running", Config.staticWriteFormat(this.statusMap.get(GameStatus.CLOSED)));
		}
		
		public static MapBoardFormat load(ConfigurationSection section) {
			
			MapBoardFormat format = new MapBoardFormat();
			
			if (section.isSet("players"))
				format.players = Config.staticReadFormat(section.getString("players"));
			
			if (section.isSet("map"))
				format.map = Config.staticReadFormat(section.getString("map"));
			
			if (section.isSet("status"))
				format.status = Config.staticReadFormat(section.getString("status"));
			
			if (section.isSet("server"))
				format.server = Config.staticReadFormat(section.getString("server"));
			
			ConfigurationSection statusMap = section.getConfigurationSection("statusMap");
			
			if (statusMap != null) {
				
				if (statusMap.isSet("open"))
					format.statusMap.put(GameStatus.WAITING, Config.staticReadFormat(statusMap.getString("open")));
				
				if (statusMap.isSet("starting"))
					format.statusMap.put(GameStatus.STARTING, Config.staticReadFormat(statusMap.getString("starting")));
				
				if (statusMap.isSet("running"))
					format.statusMap.put(GameStatus.CLOSED, Config.staticReadFormat(statusMap.getString("running")));
			}
			
			return format;
		}
	}
}
