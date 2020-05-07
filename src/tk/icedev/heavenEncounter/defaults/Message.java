package tk.icedev.heavenEncounter.defaults;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.entity.Player;

import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.utils.Config;

public enum Message {

	// enum
	PREFIX ("Prefix", MessageType.NONE),
	SHOP_DENIED ("ShopDenied", MessageType.MESSAGE),
	NO_PERMISSIONS ("NoPermissions", MessageType.MESSAGE),
	MAP_PERCENTAGE ("MapPercentage", MessageType.ACTIONBAR),
	START_TIMER ("StartTimer", MessageType.MESSAGE),
	START_TIMER_UNDER_3 ("StartTimerU3", MessageType.TITLE),
	START_TIMER_ZERO ("StartTimerZero", MessageType.MESSAGE),
	ONSTART ("OnStart", MessageType.TITLE),
	ON_INTERRUPT ("OnInterrupt", MessageType.MESSAGE),
	ON_COMMAND_IN_GAME ("OnCommandInGame", MessageType.MESSAGE),
	JOIN_UNEXISTING ("JoinUnexisting", MessageType.MESSAGE),
	JOIN_PROGRESS ("JoinProgress", MessageType.MESSAGE),
	JOIN_ALREADY ("JoinAlready", MessageType.MESSAGE),
	JOIN_FULL ("JoinFull", MessageType.MESSAGE), 
	JOIN_SPAWN_FULL ("JoinSpawnFull", MessageType.MESSAGE), 
	JOIN_MAP_BUILDING ("JoinMapBuilding", MessageType.MESSAGE),
	JOIN_SUCCESS ("JoinSuccess", MessageType.MESSAGE), 
	LEAVE_WARNING ("LeaveWarning", MessageType.MESSAGE),
	LIST_OPEN_GAME ("ListOpenGame", MessageType.NONE),
	LIST_STARTING_GAME ("ListStartingGame", MessageType.NONE),
	LIST_CLOSED_GAME ("ListClosedGame", MessageType.NONE),
	LOBBY_STATE ("LobbyState", MessageType.MESSAGE),
	WINNER ("Winner", MessageType.TITLE), 
	ONDEATH ("OnDeath", MessageType.TITLE),
	ON_KILL ("OnKill", MessageType.MESSAGE),
	ON_KILLER_ATTACK ("OnKillerAttack", MessageType.MESSAGE),
	ON_ARROW_ATTACK ("OnArrowAttack", MessageType.MESSAGE),
	ON_SNOWBALL_ATTACK ("OnSnowballAttack", MessageType.MESSAGE),
	ON_POTION_ATTACK ("OnPotionAttack", MessageType.MESSAGE),
	ON_PROJECTILE_ATTACK ("OnProjectileAttack", MessageType.MESSAGE),
	ON_TNT_DEATH ("OnTntDeath", MessageType.MESSAGE),
	ON_FIRE_DEATH ("OnFireDeath", MessageType.MESSAGE),
	ON_POISON_DEATH ("OnPoisonDeath", MessageType.MESSAGE),
	ON_IND_DEATH ("OnIndeterminatedDeath", MessageType.MESSAGE),
	ONFALL ("OnFall", MessageType.MESSAGE),
	ONQUIT ("OnQuit", MessageType.MESSAGE),
	ONQUIT_NO_MONEY ("OnQuitNoMoney", MessageType.MESSAGE),
	BROADCAST_WIN ("BroadcastWin", MessageType.MESSAGE);
	
	private static Map<Message, String> messages = new HashMap<Message, String>();
	
	private String name;
	private MessageType type;
	
	Message (String name, MessageType type) {
		
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	public MessageType getType() {
		
		return this.type;
	}
	
	public String getMessage(Object... args) {
		
		String msg = messages.get(this);
		
		if (msg == null)
			return "";
		
		return formatArgs(msg, args);
	}
	
	@SuppressWarnings("deprecation")
	public void send(CommandSender sender, Object... args) {
		
		String msg = messages.get(this);
		
		if (msg == null)
			return;
		
		msg = formatArgs(msg, args);
		
		switch (this.type) {
			
		case MESSAGE:
			sender.sendMessage(Message.PREFIX.getMessage() + msg);
			break;
		case ACTIONBAR:
			if (sender instanceof Player)
				sendActionBar((Player)sender, msg);
			break;
		case TITLE:
			if (sender instanceof Player)
				((Player)sender).sendTitle(msg, null);
			break;
		default:
			sender.sendMessage(msg);
			break;
		}
	}
	
	public void broadcast(Object... args) {
		
		String msg = messages.get(this);
		
		if (msg == null)
			return;
		
		msg = formatArgs(msg, args);
		
		switch (this.type) {
			
		case MESSAGE:
			Bukkit.broadcastMessage(Message.PREFIX.getMessage() + msg);
			break;
		default:
			Bukkit.broadcastMessage(msg);
			break;
		}
	}
	
	public static void dynamicMessage(CommandSender sender, String msg) {
		
		sender.sendMessage(Message.PREFIX.getMessage() + msg);
	}
	
	public static void console(String msg) {
		
		String prefix = Message.PREFIX.getMessage();
		Bukkit.getConsoleSender().sendMessage((prefix == null) ? HeavenEncounter.TITLE : prefix + msg);
	}
	
	public static Message getByName(String name) {
		
		for (Message msg : Message.values()) {
			if (msg.getName().equals(name))
				return msg;
		}
		
		return null;
	}
	
	public static String formatArgs(String msg, Object... args) {
		
		String out = msg;
		
		for (Object obj : args) {
			
			if (obj instanceof Integer)
				out = Message.formatInt(out, (Integer)obj);
			else if (obj instanceof Long)
				out = Message.formatLong(out, (Long)obj);
			else if (obj instanceof Short)
				out = Message.formatInt(out, (Short)obj);
			else if (obj instanceof Byte)
				out = Message.formatInt(out, (Byte)obj);
			else if (obj instanceof Double)
				out = Message.formatDouble(out, (Double)obj);
			else if (obj instanceof Float)
				out = Message.formatFloat(out, (Float)obj);
			else if (obj instanceof Player)
				out = Message.formatPlayer(out, (Player)obj);
			else if (obj instanceof String)
				out = Message.formatString(out, (String)obj);
			else if (obj instanceof ChatColor)
				out = Message.formatColor(out, (ChatColor)obj);
		}
		
		return out;
	}
	
	public static void sendActionBar(Player player, String msg) {
		
        IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte)2);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(bar);
	}
	
	public static String attachPrefix(String prefix, String input) {
		
		if (prefix == null)
			return input;
		else if (input == null)
			return prefix;
		
		return prefix + input;
	}
	
	public static String initChatFormat(String prefix, String input) {
		
		return attachPrefix(prefix, Config.staticReadFormat(input));
	}
	
	public static void loadMessages(Config config) {
		
		if (!messages.isEmpty())
			messages.clear();
		
		Message.console("Loading messages");
		
		for (Message msg : Message.values()) {
			
			String name = msg.getName();
			
			String value = config.getConfig().getString(name);
			
			if (value == null) {
				
				if (msg == Message.PREFIX) {
					value = "[" + HeavenEncounter.TITLE + "] ";
				} else {
					Message.console("Warning! Could not load message " + name);
					continue;
				}
			}
			
			Message.console("Loading message " + name);
			messages.put(msg, Config.staticReadFormat(value));
		}
	}
	
	public static String formatPlayer(String input, Player p) {
		
		return input.replaceFirst("%player%", p.getDisplayName());
	}
	
	public static String formatPlayerName(String input, Player p) {
		
		return input.replaceFirst("%player%", p.getName());
	}
	
	public static String formatInt(String input, int... ns) {
		
		// %d e' una convenzione del linguaggio C, dai un occhiata a sprintf()
		
		String out = input;
		
		for (int n : ns)
			out = out.replaceFirst("%d", String.valueOf(n));
		
		return out;
	}
	
	public static String formatLong(String input, long... ns) {
		
		// %d e' una convenzione del linguaggio C, dai un occhiata a sprintf()
		
		String out = input;
		
		for (long n : ns)
			out = out.replaceFirst("%d", String.valueOf(n));
		
		return out;
	}
	
	public static String formatDouble(String input, double... ns) {
		
		// %f e' una convenzione del linguaggio C, dai un occhiata a sprintf()
		
		String out = input;
		
		for (double n : ns)
			out = out.replaceFirst("%f", String.valueOf(n));
		
		return out;
	}
	
	public static String formatFloat(String input, float... ns) {
		
		// %f e' una convenzione del linguaggio C, dai un occhiata a sprintf()
		
		String out = input;
		
		for (float n : ns)
			out = out.replaceFirst("%f", String.valueOf(n));
		
		return out;
	}
	
	public static String formatString(String input, String... ns) {
		
		// %s e' una convenzione del linguaggio C, dai un occhiata a sprintf()
		
		String out = input;
		
		for (String n : ns)
			out = out.replaceFirst("%s", n);
		
		return out;
	}
	
	public static String formatColor(String input, ChatColor...colors) {
		
		String out = input;
		
		for (ChatColor n : colors)
			out = out.replaceFirst("%c", String.valueOf(n));
		
		return out;
	}
	
	public enum MessageType {
		
		NONE, MESSAGE, ACTIONBAR, TITLE
	}
}
