package tk.icedev.heavenEncounter.defaults.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.defaults.Message;
import atlas.commands.CommandGroup;
import atlas.commands.MainCommand;

public class LeaveCmdGroup implements CommandGroup {

	@Override
	public String getCommand() {
		
		return "leave";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand, String[] args) {
		return null;
	}
	
	@MainCommand(permissions = {"skywars.leave", "skywars.bypass"})
	public boolean main(Player player, String[] args) {
		
		Message.dynamicMessage(player, "You can perform this command only if you are in game");
		return true;
	}
}
