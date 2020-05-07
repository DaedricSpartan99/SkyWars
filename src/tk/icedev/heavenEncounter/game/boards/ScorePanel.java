package tk.icedev.heavenEncounter.game.boards;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Score;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.game.Game;
import tk.icedev.heavenEncounter.game.Game.QuitCause;
import tk.icedev.heavenEncounter.game.fields.Lobby;

public class ScorePanel extends GameBoard {
	
	GameBoardFormat format;
	
	public ScorePanel(Lobby lobby, String[] title, GameBoardFormat format, Plugin plugin) {
		
		super(lobby, title, plugin);
		
		this.format = format;
		
		for (Player player : lobby.getPlayers()) {
			
			PlayerBoardFormat form = format.getByPermission(player);
			
			Score score = obj.getScore(Message.formatPlayerName(form.alive, player));
			score.setScore(0);
		}
		
		update();
	}
	
	public static class GameBoardFormat {
		
		public static PlayerBoardFormat defaultFormat = new PlayerBoardFormat();
		
		public Map<String, PlayerBoardFormat> map = new HashMap<String, PlayerBoardFormat>();
		
		public PlayerBoardFormat getByPermission(Player player) {
			
			for (Entry<String, PlayerBoardFormat> entry : map.entrySet()) {
				if (player.hasPermission(entry.getKey()))
					return entry.getValue();
			}
			
			return defaultFormat;
		}
		
		public void write(ConfigurationSection section) {
			
			ConfigurationSection desSec = section.getConfigurationSection("design");
			
			if (desSec == null)
				desSec = section.createSection("design");
			
			for (Entry<String, PlayerBoardFormat> entry : map.entrySet()) {
				
				String key = entry.getKey().replaceAll("\\.", "_");
				
				if (!desSec.contains(key))
					desSec.createSection(key);
				
				desSec.set(key, entry.getValue());
			}
		}
		
		public static GameBoardFormat load(ConfigurationSection section) {
			
			GameBoardFormat format = new GameBoardFormat();
			
			for (String perm : section.getKeys(false)) {
				
				Message.console("Loading board permission: " + perm);
				
				Object value = section.get(perm);
					
				if (value instanceof PlayerBoardFormat)
					format.map.put(perm.replaceAll("_", "\\."), (PlayerBoardFormat)value);
			}
			
			return format;
		}
	}

	@Override
	public void onTimerStarting(Game game) {}

	@Override
	public void onPlayerJoin(Player player, Game game) {}

	@Override
	public void onPlayerQuit(Player player, Game game, QuitCause cause) {
		
		PlayerBoardFormat form = format.getByPermission(player);
		
		board.resetScores(Message.formatPlayerName(form.alive, player));
		Score score = obj.getScore(Message.formatPlayerName(form.dead, player));
		score.setScore(- lobby.getSize() + 1);
		super.removeScore(player);
		update();
	}

	@Override
	public void onLobbyTurnOpen(Game game) {}

	@Override
	public void onGameStart(Game game) {}

	@Override
	public void afterGameStart(Game game) {}

	@Override
	public void onGameEnd(Game game, Player winner) {}
}
