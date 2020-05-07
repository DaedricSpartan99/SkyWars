package tk.icedev.heavenEncounter.hub;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.ranking.Profile;
import tk.icedev.heavenEncounter.utils.Config;
import tk.icedev.heavenEncounter.defaults.Message;

public class HubBoard {

	private Scoreboard board;
	private Objective obj;
	private Player player;
	
	public HubBoard(ProfileScoreFormat format, Player player, HeavenEncounter plugin) {
		
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.obj = board.registerNewObjective("hubBoard", "dummy");
		this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.player = player;
		
		String dName = Message.formatPlayerName(format.title, player);
		
		if (dName.length() > 32)
			dName = dName.substring(0, 32);
		
		obj.setDisplayName(dName);
		
		Profile profile = plugin.getRank().getProfile(player);
		
		obj.getScore(ChatColor.RESET.toString()).setScore(8);
		
		obj.getScore(Message.formatInt(format.kills, profile.kills)).setScore(7);
		obj.getScore(Message.formatInt(format.deaths, profile.deaths)).setScore(6);
		
		obj.getScore(ChatColor.RESET.toString() + ChatColor.RESET).setScore(5);
		
		obj.getScore(Message.formatInt(format.played, profile.played)).setScore(4);
		obj.getScore(Message.formatInt(format.wins, profile.wins)).setScore(3);
		
		obj.getScore(ChatColor.RESET.toString() + ChatColor.RESET + ChatColor.RESET).setScore(2);
		
		obj.getScore(ChatColor.RESET.toString() + ChatColor.RESET + ChatColor.RESET + ChatColor.RESET).setScore(0);
		
		this.update();
	}
	
	public void update() {
		
		player.setScoreboard(board);
	}
	
	public void destroy() {
		
		if (player.getScoreboard().equals(board))
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	public static String bulletFormat(String format) {
		
		return format.replaceFirst("@", "\u2022");
	}
	
	public static class ProfileScoreFormat {
		
		public String title = "Profile",
					kills = "\u2022 Kills: %d", 
					deaths = "\u2022 Deaths: %d", 
					played = "\u2022 Games played: &d", 
					wins = "\u2022 Wins: %d", 
					coins_p = "\u2022 Coins: %f", 
					coins_n = "\u2022 Coins: %f";
		
		public static ProfileScoreFormat load(ConfigurationSection section) {
			
			ProfileScoreFormat format = new ProfileScoreFormat();
			
			if (section.isSet("title"))
				format.title = bulletFormat(Config.staticReadFormat(section.getString("title")));
			
			if (section.isSet("kills"))
				format.kills = bulletFormat(Config.staticReadFormat(section.getString("kills")));
			
			if (section.isSet("deaths"))
				format.deaths = bulletFormat(Config.staticReadFormat(section.getString("deaths")));
			
			if (section.isSet("played"))
				format.played = bulletFormat(Config.staticReadFormat(section.getString("played")));
			
			if (section.isSet("wins"))
				format.wins = bulletFormat(Config.staticReadFormat(section.getString("wins")));
			
			if (section.isSet("coins_p"))
				format.coins_p = bulletFormat(Config.staticReadFormat(section.getString("coins_p")));
			
			if (section.isSet("coins_n"))
				format.coins_n = bulletFormat(Config.staticReadFormat(section.getString("coins_n")));
			
			return format;
		}
		
		public void write(ConfigurationSection section) {
			
			section.set("title", title);
			section.set("kills", kills);
			section.set("deaths", deaths);
			section.set("played", played);
			section.set("wins", wins);
			section.set("coins_p", coins_p);
			section.set("coins_n", coins_n);
		}
	}
}
