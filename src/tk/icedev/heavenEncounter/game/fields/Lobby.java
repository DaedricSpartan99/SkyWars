package tk.icedev.heavenEncounter.game.fields;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class Lobby {
	
	public static final int NO_PLACE = -1;

	private List<Player> players;
	private final int size;
	
	public Lobby(int size) {
		
		this.size = size;
		this.players = new ArrayList<Player>();
	}
	
	public int addPlayer(Player player) {
		
		if (players.size() == this.size)
			return NO_PLACE;
		
		players.add(player);
		return players.indexOf(player);
	}
	
	public void removePlayer(Player player) {
		
		players.remove(player);
	}
	
	public List<Player> getPlayers() {
		
		return players;
	}
	
	public Player[] playersArray() {
		
		return players.toArray(new Player[this.players.size()]);
	}
	
	public boolean isFull() {
		
		return players.size() >= size;
	}
	
	public void clear() {
		
		players.clear();
	}
	
	public int getSize() {
		
		return players.size();
	}
	
	public int getMaximalSize() {
		
		return size;
	}
	
	public boolean contains(Player player) {
		
		return players.contains(player);
	}
}
