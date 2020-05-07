package tk.icedev.heavenEncounter.game.boards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tk.icedev.heavenEncounter.extern.GameListener;
import tk.icedev.heavenEncounter.game.fields.Lobby;

public abstract class GameBoard implements Runnable, GameListener {
	
	public static final String[] DEFAULT_TITLE = {"HeavenEncounter"};

	protected Scoreboard board;
	protected Objective obj;
	private int schedulerID, titleStep;
	protected Lobby lobby;
	private final String[] title;

	public GameBoard(Lobby lobby, String[] title, Plugin plugin) {
		
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.obj = board.registerNewObjective("heavenEncounter", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.title = (title == null) ? DEFAULT_TITLE : title;
		this.lobby = lobby;
		obj.setDisplayName(title[0]);
		
		schedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 4L);
		titleStep = 0;
	}
	
	public void update() {
		
		for (Player player : lobby.getPlayers())
			player.setScoreboard(board);
	}
	
	public void removeScore(Player owner) {
		
		owner.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	public void destroy() {
		
		for (Player player : lobby.getPlayers())
			this.removeScore(player);
		
		Bukkit.getScheduler().cancelTask(schedulerID);
	}

	@Override
	public void run() {
		
		String title = this.title[titleStep++];
		obj.setDisplayName(title);
		titleStep %= this.title.length;
		
		update();
	}
}
