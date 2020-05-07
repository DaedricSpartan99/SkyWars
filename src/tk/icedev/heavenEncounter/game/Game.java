package tk.icedev.heavenEncounter.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.GameListener;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.game.boards.GameBoard;
import tk.icedev.heavenEncounter.game.boards.MapBoard;
import tk.icedev.heavenEncounter.game.boards.ScorePanel;
import tk.icedev.heavenEncounter.game.fields.Field;
import tk.icedev.heavenEncounter.game.fields.Lobby;
import tk.icedev.heavenEncounter.game.fields.Field.FieldLoader;
import tk.icedev.heavenEncounter.game.fields.Field.LoaderEndListener;
import tk.icedev.heavenEncounter.ranking.Rank;
import tk.icedev.heavenEncounter.utils.Timer;

public class Game {
	
	private Field field;
	private boolean running;
	private HeavenEncounter plugin;
	private GameBoard score;
	private StartTimer timer;
	private EndTimer endTimer;
	//private TNTAutoPrimer autoPrimer;
	
	private List<GameListener> listeners;
	
	/*
	 * Constructors
	 */
	
	public Game(final Field field, final HeavenEncounter plugin) {
		
		this.field = field;
		this.field.setTaken(true);
		this.plugin = plugin;
		
		if (this.field.isBuilding()) {	// set start callback
			
			this.field.getFieldLoader().addListener(new LoaderEndListener() {

				@Override
				public void endAction(FieldLoader loader) {
					
					if (checkForStart())
						startTimer();
				}
				
			});
		}
		
		this.running = false;
		this.listeners = new ArrayList<GameListener>();
		
		this.score = new MapBoard(this.field.lobby, this.getCapitalFieldName(), 
								plugin.getGameManager().getGameSettings().boardTitle, 
								plugin.getGameManager().getGameSettings().mapBoardFormat, plugin.getPlugin());
		
		this.addGameListener(this.score);
		
		this.timer = null;
		this.endTimer = null;
	}
	
	public void destroy() {
		
		if (score != null) {
			score.destroy();
			this.removeGameListener(score);
			this.score = null;
		}
		
		listeners.clear();
		listeners = null;
		
		if (timer != null) {
			timer.interrupt();
			timer = null;
		}
		
		if (endTimer != null) {
			endTimer.interrupt();
			endTimer = null;
		}
		
		field = null;
	}
	
	/*
	 * public start/end fields
	 */

	public void start() {
		
		if (running)
			return;
		
		for (GameListener lis : listeners)
			lis.onGameStart(this);
		
		Rank rank = plugin.getRank();
		
		for (Player player : field.lobby.getPlayers()) {
			
			Message.ONSTART.send(player);
			rank.getProfile(player).played++;
		}
		
		field.randomAssignChests(plugin.getChestsManager(), InventoryType.CHEST.getDefaultSize());
		field.setLockMap(false);
		field.getAutoPrimer().setActive(true);
		field.breakGlass();
		
		score.destroy();	// destroy map board
		this.removeGameListener(this.score);
		
		this.score = new ScorePanel(field.lobby, plugin.getGameManager().getGameSettings().boardTitle, 
							plugin.getGameManager().getGameSettings().gameBoardFormat, plugin.getPlugin());
		
		this.addGameListener(this.score);
		
		timer = null;
		running = true;
		
		for (GameListener lis : listeners)
			lis.afterGameStart(this);
	}
	
	public void finish(Player winner) {
		
		if (!running)
			return;
		
		for (GameListener lis : listeners)
			lis.onGameEnd(this, winner);
		
		this.quit(winner, QuitCause.WINNER);
		
		plugin.getHub().broadcastMessage(Message.BROADCAST_WIN, winner, this.getCapitalFieldName());
		
		field.reset(plugin.getPlugin());
		field.getAutoPrimer().setActive(false);
		field.setTaken(false);
		
		plugin.getGameManager().removeGame(this);
		
		running = false;
		
		this.destroy();
	}
	
	public void interrupt() {
		
		if (endTimer != null) {
			
			endTimer.interrupt();
			this.finish(endTimer.winner);	// end normally
			return;
		}
		
		for (GameListener lis : listeners)
			lis.onGameEnd(this, null);
		
		for (Player player : field.lobby.playersArray()) {
			
			Message.ON_INTERRUPT.send(player);
			this.quit(player, QuitCause.KICK);
		}
		
		field.reset(plugin.getPlugin());
		field.getAutoPrimer().setActive(false);
		field.setTaken(false);
		
		plugin.getGameManager().removeGame(this);
		
		running = false;
		
		this.destroy();
	}
	
	/*
	 * Player join/quit manager
	 */
	
	public boolean isBuilding() {
		
		return field.isBuilding();
	}
	
	public boolean isOpen() {
		
		return !this.hasMinimalPlayers();
	}
	
	public boolean isJoinable() {
		
		return !(field.lobby.isFull() || running);
	}
	
	public boolean isRunning() {
		
		return this.running;
	}
	
	public boolean isOver() {
		
		return this.running && this.endTimer != null;
	}
	
	public JoinStatus join(Player player) {
		
		if (running)
			return JoinStatus.RUNNING_GAME;
		else if (this.inGame(player))
			return JoinStatus.IN_GAME;
		
		int index = field.lobby.addPlayer(player);
		
		if (index == Lobby.NO_PLACE)
			return JoinStatus.FULL_LOBBY;
		
		player.setHealth(20.0d);
		player.setFoodLevel(20);
		player.getInventory().clear();
		
		field.spawnPlayer(index, player);
		plugin.getHub().closeBoard(player);
		
		this.broadcastMessage(Message.JOIN_SUCCESS, player);
		this.broadcastMessage(Message.LOBBY_STATE, field.lobby.getSize(), field.lobby.getMaximalSize());
		
		Message.LEAVE_WARNING.send(player);
		
		for (GameListener listener : this.listeners)
			listener.onPlayerJoin(player, this);
		
		// check for start
		if (this.checkForStart())
			this.startTimer();
		
		return JoinStatus.SUCCESS;
	}
	
	public void quit(Player player, QuitCause cause) {
		
		ItemStack[] nullArmor = {null, null, null, null};
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(nullArmor);
		player.setHealth(20.0d);
		player.setFoodLevel(20);
		
		if (!running && timer != null)
			timer.restart();
		
		field.lobby.removePlayer(player);
		
		for (GameListener listener : this.listeners)
			listener.onPlayerQuit(player, this, cause);
		
		plugin.getHub().spawn(player);
	}
	
	public enum QuitCause {
		
		DEATH, LEAVE, WINNER, KICK
	}
	
	/*
	 * Manage the winner
	 */
	
	private Player manageWinner() {
		
		Player winner = this.lastPlayer();
		
		Message.WINNER.send(winner);
		
		plugin.getRank().getProfile(winner).wins++;
		
		return winner;
	}
	
	/*
	 * public utility fields
	 */
	
	public Field getField() {
		
		return field;
	}
	
	public int sizeofLobby() {
		
		return field.lobby.getSize();
	}
	
	public int maxSizeofLobby() {
		
		return field.lobby.getMaximalSize();
	}
	
	public boolean hasMinimalPlayers() {
		
		return field.lobby.getSize() >= plugin.getGameManager().getGameSettings().min_players;
	}
	
	public boolean inGame(Player p) {
		
		return field.lobby.contains(p);
	}
	
	public boolean fullLobby() {
		
		return field.lobby.isFull();
	}
	
	public List<Player> getPlayers() {
		
		return field.lobby.getPlayers();
	}
	
	public String getFieldName() {
		
		return field.getName();
	}
	
	public String getHostedWorldName() {
		
		return plugin.getGameManager().getGameWorld().getName();
	}
	
	public String getCapitalFieldName() {
		
		String name = this.getFieldName();
		return name.substring(0,1).toUpperCase() + name.substring(1);
	}
	
	public GameStatus getStatus() {
		
		if (running || !this.isJoinable())
			return GameStatus.CLOSED;
		else {
			
			if (this.hasMinimalPlayers() && field.isAccessible())
				return GameStatus.STARTING;
			else
				return GameStatus.WAITING;
		}
	}
	
	public void startTimer() {
		
		if (timer == null)
			timer = new StartTimer(this, plugin.getPlugin(), 10);
		else
			timer.restart();
		
		for (GameListener lis : listeners)
			lis.onTimerStarting(this);
	}
	
	/*
	 * private utility fields
	 * used only in this class
	 */
	
	private Player lastPlayer() {	// return first Player of list
		
		for (Player p : field.lobby.getPlayers())
			return p;
		
		return null;
	}
	
	public boolean checkForStart() {
		
		return (this.hasMinimalPlayers() || field.lobby.isFull()) && field.isAccessible();
	}
	
	private boolean checkForEnd() {
		
		return sizeofLobby() < 2;
	}
	
	private void dropAll(Player player) {
		
		Location loc = player.getLocation();
		
		for (ItemStack item : player.getInventory().getContents()) {
			
			if (item == null)
				continue;
			else if (item.getType() == Material.AIR)
				continue;
			
			loc.getWorld().dropItem(loc, item).setPickupDelay(20);
		}
		
		for (ItemStack item : player.getInventory().getArmorContents()) {
			
			if (item == null)
				continue;
			else if (item.getType() == Material.AIR)
				continue;
			
			loc.getWorld().dropItem(loc, item).setPickupDelay(20);
		}
	}
	
	public void broadcastMessage(Message message, Object... args) {
		
		for (Player player : this.field.lobby.getPlayers())
			message.send(player, args);
	}
	
	public void broadcastMessage(String msg) {
		
		for (Player player : this.field.lobby.getPlayers())
			player.sendMessage(msg);
	}
	
	/*
	 * Listener settings
	 */
	
	public void addGameListener(GameListener listener) {
		
		if (this.listeners.contains(listener))
			return;
		
		this.listeners.add(listener);
	}
	
	public void removeGameListener(GameListener listener) {
		
		if (this.listeners.contains(listener))
			this.listeners.remove(listener);
	}
	
	public void clearGameListeners() {
		
		this.listeners.clear();
	}
	
	public List<GameListener> getGameListeners() {
		
		return this.listeners;
	}
	
	/*
	 * 	Death handler
	 * 	fallen: true if the player has fallen down
	 * 	event: null if the player has left or quit
	 */
	
	public void onDeath(Player player, EntityDamageEvent event, boolean fallen) {
		
		/*
		 *  Dead or left during the end timer, ends normally
		 */
		
		if (endTimer != null) {
			if (endTimer.winner.equals(player)) {
				
				endTimer.interrupt();
				this.finish(endTimer.winner);
				return;
			}
		}
		
		/*
		 *  Player left
		 */
		
		QuitCause quitCause;
		
		if (event != null) {
			
			quitCause = QuitCause.DEATH;
			
			Message.ONDEATH.send(player);
			
			if (!fallen)
				this.dropAll(player);
			else
				player.setFallDistance(0);
			
		} else
			quitCause = QuitCause.LEAVE;
		
		/*
		 * 	Broadcast message of the cause
		 */
		
		Player killer = this.broadcastCause(player, event, fallen);
		
		/*
		 *  Subtract money
		 */
		
		if (running) {
			
			plugin.getRank().getProfile(player).deaths++;
			
			if (killer != null && !player.equals(killer)) {
			
				Message.ON_KILL.send(killer, player);
				plugin.getRank().getProfile(killer).kills++;
			}
		}
		
		/*
		 * 	Quit the player
		 */
		
		this.quit(player, quitCause);
		
		/*
		 *  Perform callback related to the game
		 *  
		 *  1) One player left, start the end timer
		 *  2) Game still not started, there are players in lobby, stop or restart the timer
		 * 	3) No player left, the lobby is empty, interrupt the game
		 */
		
		if (running && this.checkForEnd())
			endTimer = new EndTimer(this, this.manageWinner(), plugin.getPlugin(), 5);
		else if (!running && field.lobby.getSize() > 0) {
			
			if (this.checkForStart())
				this.startTimer();
			else {
				
				if (timer != null) {
					
					timer.interrupt();
					timer = null;
				
					for (GameListener lis : listeners)
						lis.onLobbyTurnOpen(this);
				}
			}
			
		} else if (!running && field.lobby.getSize() == 0)
			this.interrupt();
	}
	
	public Player broadcastCause(Player player, EntityDamageEvent event, boolean fallen) {
		
		if (event == null) {
			
			this.broadcastMessage(Message.ONQUIT, player);
			
			return null;
		}
		
		if (fallen) {
			this.broadcastMessage(Message.ONFALL, player);
			return null;
		}
		
		Player killer = null;
		
		DamageCause cause = event.getCause();
		
		if (event instanceof EntityDamageByEntityEvent) {
		
			Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
			
			switch (cause) {
			
			case ENTITY_ATTACK:
				
				if (damager instanceof Player)
					this.broadcastMessage(Message.ON_KILLER_ATTACK, player, killer = (Player)damager);
				
				break;
				
			case PROJECTILE:
				
				if (damager instanceof Projectile) {
					
					ProjectileSource source = ((Projectile)damager).getShooter();
					
					if (!(source instanceof Player))
						return null;
					
					killer = (Player)source;
					
					if (damager instanceof Arrow)
						this.broadcastMessage(Message.ON_ARROW_ATTACK, player, killer);
					else if (damager instanceof Snowball)
						this.broadcastMessage(Message.ON_SNOWBALL_ATTACK, player, killer);
					else if (damager instanceof ThrownPotion)
						this.broadcastMessage(Message.ON_POTION_ATTACK, player, killer);
					else
						this.broadcastMessage(Message.ON_PROJECTILE_ATTACK, player, killer);
				}
				
				break;
				
			case ENTITY_EXPLOSION:
				
				if (damager instanceof TNTPrimed) {
					
					TNTPrimed tnt = (TNTPrimed)damager;
					Player source = field.getAutoPrimer().getSource(tnt);
					
					if (source == null)
						this.broadcastMessage(Message.ON_IND_DEATH, player);
					else
						this.broadcastMessage(Message.ON_TNT_DEATH, player, killer = source);
				}
				
				break;
			
			default:
				break;
			}
			
		} else {
			
			switch (cause) {
			
			case FIRE:
				this.broadcastMessage(Message.ON_FIRE_DEATH, player);
				break;
				
			case FIRE_TICK:
				this.broadcastMessage(Message.ON_FIRE_DEATH, player);
				break;
				
			case POISON:
				this.broadcastMessage(Message.ON_POISON_DEATH, player);
				break;
			
			default:
				this.broadcastMessage(Message.ON_IND_DEATH, player);
				break;
			}
		}
		
		return killer;
	}
	
	/*
	 * 	Damage handler
	 */
	
	public void onPlayerDamage(Player player, EntityDamageEvent event) {
		
		switch (event.getCause()) {
		
		case FALL:
			event.setCancelled(true);
			break;
			
		case VOID:
			event.setCancelled(true);
			this.onDeath(player, event, true);
			break;
			
		default:
			
			if (((Damageable)player).getHealth() <= event.getDamage()) {
				
				event.setCancelled(true);
				this.onDeath(player, event, false);
			}
			
			break;
		}
	}
	
	/*
	 * 	Commands handler
	 */
	
	public boolean canUseCommand(String cmd) {
		
		for (String av : plugin.getGameManager().getGameSettings().commandsInGame) {
			if (av.equalsIgnoreCase(cmd))
				return true;
		}
		
		return false;
	}
	
	public boolean onPlayerCommand(Player player, String[] label) {
		
		if (label[0].equalsIgnoreCase("leave")) {
			
			if (!player.hasPermission("skywars.leave")) {
				Message.NO_PERMISSIONS.send(player);
				return true;
			}
			
			this.onDeath(player, null, false);
			
		} else if (this.canUseCommand(label[0]))
			return false;	// use the command
		else 
			Message.ON_COMMAND_IN_GAME.send(player);
		
		return true;
	}
	
	/*
	 * 	Chat limiter
	 */
	
	public void onChatMessage(Player player, String msg) {
	
		this.broadcastMessage(plugin.getChatFormats().getGameMessage(player, msg));
	}
	
	/*
	 * Nested Timer class
	 * It manages the game beginning
	 */
	
	public class StartTimer extends Timer {
		
		Game game;

		public StartTimer(Game game, Plugin plugin, int seconds) {
			
			super(plugin, seconds);
			this.game = game;
		}

		@Override
		public void handleSecond(int second) {
			
			if (second == 0)
				game.broadcastMessage(Message.START_TIMER_ZERO);
			else if (second % 5 == 0)
				game.broadcastMessage(Message.START_TIMER, second);
			else if (second < 5) {
				
				if (second <= 3)
					game.broadcastMessage(Message.START_TIMER_UNDER_3, second);
				
				game.broadcastMessage(Message.START_TIMER, second);
			}
		}

		@Override
		public void endAction() {
			
			game.start();
		}
	}
	
	/*
	 * 	End timer definition
	 * 	Called at the end of the game
	 */
	
	public class EndTimer extends Timer {
		
		Game game;
		public Player winner;
		
		public EndTimer(Game game, Player winner, Plugin plugin, int seconds) {
			
			super(plugin, seconds);
			this.game = game;
			this.winner = winner;
		}

		@Override
		public void handleSecond(int second) {
			
			// nothing
		}

		@Override
		public void endAction() {
			
			game.finish(winner);
		}
	}
	
	/*
	 *  Game status informations, gotten by Game.getStatus()
	 */
	
	public enum GameStatus {
		
		WAITING, STARTING, CLOSED
	}
	
	/*
	 * Join status informations, returned by Game.join(Player player)
	 */
	
	public enum JoinStatus {
		
		SUCCESS, FULL_LOBBY, IN_GAME, RUNNING_GAME
	}
}
