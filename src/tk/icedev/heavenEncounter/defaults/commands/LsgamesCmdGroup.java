package tk.icedev.heavenEncounter.defaults.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;

import atlas.commands.CommandGroup;
import atlas.commands.MainCommand;

public class LsgamesCmdGroup implements CommandGroup {
	
	HeavenEncounter plugin;
	
	public LsgamesCmdGroup(HeavenEncounter plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		
		return "lsgames";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand,
			String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@MainCommand(permissions = {"skywars.match.list", "skywars.bypass"})
	public boolean main(CommandSender sender, String[] args) {
		
		return MatchCmdGroup._list(sender, args, this.plugin);
	}
}
