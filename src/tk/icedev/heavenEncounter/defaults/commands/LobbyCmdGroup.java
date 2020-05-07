package tk.icedev.heavenEncounter.defaults.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.hub.Hub;

import atlas.commands.CommandGroup;
import atlas.commands.MainCommand;

public class LobbyCmdGroup implements CommandGroup {
	
	HeavenEncounter plugin;
	
	public LobbyCmdGroup(HeavenEncounter plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		
		return "lobby";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand, String[] args) {
		
		return null;
	}

	@MainCommand(permissions = {"skywars.lobby", "skywars.bypass"})
	public boolean main(Player player, String[] args) {
		
		Hub hub = plugin.getHub();
		
		if (hub == null) {
			Message.dynamicMessage(player, "Sorry, no hub available");
			return true;
		}
		
		hub.spawn(player);
		
		return true;
	}
}
