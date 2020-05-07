package tk.icedev.heavenEncounter.extern;

import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.game.Game;
import tk.icedev.heavenEncounter.game.Game.QuitCause;

public interface GameListener {

	public abstract void onTimerStarting(Game game);
	public abstract void onPlayerJoin(Player player, Game game);
	public abstract void onPlayerQuit(Player player, Game game, QuitCause cause);
	public abstract void onLobbyTurnOpen(Game game);
	public abstract void onGameStart(Game game);
	public abstract void afterGameStart(Game game);
	public abstract void onGameEnd(Game game, Player winner);	// null if no winner
}
