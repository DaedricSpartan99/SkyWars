package tk.icedev.heavenEncounter.defaults.commands;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.game.Game;

import atlas.commands.CommandCallback;
import atlas.commands.CommandGroup;

public class MatchCmdGroup implements CommandGroup {
	
	HeavenEncounter plugin;
	
	public MatchCmdGroup(HeavenEncounter plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		
		return "match";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand, String[] args) {
		
		return null;
	}
	
	@CommandCallback(command = "list", aliases = {"ls"}, permissions = {"skywars.list", "skywars.bypass"})
	public boolean list(CommandSender sender, String[] args) {
		
		return _list(sender, args, plugin);
	}
	
	/*
	 * External usage access for aliases, see LsgamesCmdGroup::main
	 */
	
	public static boolean _list(CommandSender sender, String[] args, HeavenEncounter plugin) {
		
		for (Entry<Integer, Game> game : plugin.getGameManager().getGamesMap()) {
			
			switch (game.getValue().getStatus()) {
			
			case WAITING:
				Message.LIST_OPEN_GAME.send(sender, game.getKey(), game.getValue().getFieldName());
				break;
			case STARTING:
				Message.LIST_STARTING_GAME.send(sender, game.getKey(), game.getValue().getFieldName());
				break;
			case CLOSED:
				Message.LIST_CLOSED_GAME.send(sender, game.getKey(), game.getValue().getFieldName());
				break;
			}
		}

		return true;
	}
	
	@CommandCallback(command = "start", permissions = {"skywars.start", "skywars.bypass"})
	public boolean start(CommandSender sender, String[] args) {
		
		int id;
		
		if (args.length == 0) {
			
			id = plugin.getGameManager().newRandomGame();
			
			if (id != HeavenEncounter.NULL_ID)
				Message.dynamicMessage(sender, "Game " + id + " started in " + plugin.getGameManager().getGame(id).getFieldName());
			
			return true;
		}
		
		id = plugin.getGameManager().startNewGame(args[0]);
		
		if (id != HeavenEncounter.NULL_ID)
			Message.dynamicMessage(sender, "Game " + id + " started in " + plugin.getGameManager().getGame(id).getFieldName());
		
		return true;
	}
	
	@CommandCallback(command = "stop", permissions = {"skywars.stop", "skywars.bypass"})
	public boolean stop(CommandSender sender, String[] args) {
		
		if (args.length < 1) {
			Message.dynamicMessage(sender, "Specify a game id");
			return true;
		}
		
		int id;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			Message.dynamicMessage(sender, "The game id must be a number");
			return true;
		}
		
		Message.dynamicMessage(sender, "Stopping game " + args[0]);
		plugin.getGameManager().interruptGame(id);
		
		return true;
	}
	
	@CommandCallback(command = "warp", permissions = {"skywars.warp", "skywars.bypass"})
	public boolean warp(Player player, String[] args) {
		
		if (args.length < 1) {
			Message.dynamicMessage(player, "Specify an id");
			return true;
		}
		
		int id;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			Message.dynamicMessage(player, "The game id must be a number");
			return true;
		}
		
		Game game = plugin.getGameManager().getGame(id);
		
		if (game == null) {
			Message.dynamicMessage(player, "The specified game is not running");
			return true;
		}
		
		player.teleport(game.getField().getAdminWarp());
		
		return true;
	}

	@CommandCallback(command = "join", permissions = {"skywars.join", "skywars.bypass"})
	public boolean join(Player player, String[] args) {
		
		if (args.length == 0) {
			plugin.openGamesInterface(player);
			return true;
		}
		
		int id;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			Message.dynamicMessage(player, "The game id must be a number");
			return true;
		}
		
		Game game = plugin.getGameManager().getGame(id);
		
		if (game == null) {
			Message.JOIN_UNEXISTING.send(player);
			return true;
		}
		
		game.join(player);
		
		return true;
	}
}
