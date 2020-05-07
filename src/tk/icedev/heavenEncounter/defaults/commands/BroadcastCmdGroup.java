package tk.icedev.heavenEncounter.defaults.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;

import atlas.commands.CommandGroup;
import atlas.commands.MainCommand;

public class BroadcastCmdGroup implements CommandGroup {
	
	HeavenEncounter plugin;
	
	public BroadcastCmdGroup(HeavenEncounter plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		
		return "broadcast";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand, String[] args) {
		return null;
	}

	@MainCommand(permissions = {"skywars.broadcast", "skywars.bypass"})
	public boolean main(Player player, String[] args) {
		
		if (args.length == 0)
			return true;
		
		String msg = "";
		
		for (int i = 0; i < args.length; i++)
			msg += args[i] + " ";
		
		Bukkit.broadcastMessage(plugin.getChatFormats().getGlobalMessage(player, msg));
		return true;
	}
}
