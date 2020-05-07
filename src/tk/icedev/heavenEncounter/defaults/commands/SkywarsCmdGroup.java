package tk.icedev.heavenEncounter.defaults.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tk.icedev.heavenEncounter.builder.FieldBuilder;
import tk.icedev.heavenEncounter.builder.JoinPointBuilder;
import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.items.Tier;

import atlas.commands.CommandCallback;
import atlas.commands.CommandGroup;

public class SkywarsCmdGroup implements CommandGroup {
	
	private HeavenEncounter plugin;
	private static String[] mapcmds = {"build", "cancel", "remove",
										"create", "adminspawn", "addspawn", 
										"bind"};
	
	public SkywarsCmdGroup(HeavenEncounter plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		
		return "skywars";
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String subcommand, String[] args) {
		
		if (subcommand.equalsIgnoreCase("map")) {
			
			if (args.length == 0)
				return null;
			
			List<String> list = new ArrayList<String>();
			
			for (String cmd : mapcmds) {
				if (cmd.startsWith(args[0]))
					list.add(cmd);
			}
			
			return list;
		}
		
		return null;
	}

	@CommandCallback(command = "map", permissions = {"skywars.map", "skywars.bypass"})
	public boolean map(CommandSender sender, String[] args) {
		
		if (args.length == 0) {
			Message.dynamicMessage(sender, "Specify a map subcommand");
			return true;
		}
		
		String subcmd = args[0];
		String[] subargs = Arrays.copyOfRange(args, 1, args.length);
		
		if (subcmd.equalsIgnoreCase("build"))
			return build(sender);
		else if (subcmd.equalsIgnoreCase("cancel"))
			return cancel(sender);
		else if (subcmd.equalsIgnoreCase("remove"))
			return remove(sender, args);
		
		if (!(sender instanceof Player)) {
			Message.dynamicMessage(sender, "You must be a player to perform this command");
			return true;
		}
		
		Player player = (Player)sender;
		
		if (subcmd.equalsIgnoreCase("create"))
			return create(player, subargs);
		else if (subcmd.equalsIgnoreCase("adminwarp"))
			return adminwarp(player);
		else if (subcmd.equalsIgnoreCase("addspawn"))
			return addspawn(player);
		else if (subcmd.equalsIgnoreCase("bind"))
			return bind(player);
		
		return true;
	}
	
	/*
	 *  All map subcommands
	 */
	
	private boolean build(CommandSender sender) {
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder == null) {
			Message.dynamicMessage(sender, "Cannot compile a null FieldBuilder object");
			Message.dynamicMessage(sender, "Please, launch '/sw map create <name>' to initialize one");
			return true;
		}
		
		StaticField field = builder.compile();
		
		if (field == null) {
			sender.sendMessage("Spawn or chests number cannot be zero");
			return true;
		}
		
		plugin.addField(builder.getWorld(), field);
		FieldBuilder.destroyDefaultBuilder();
		
		Message.dynamicMessage(sender, "Field " + field.getName() + " added successfully");
		
		return true;
	}
	
	private boolean cancel(CommandSender sender) {
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder == null) {
			Message.dynamicMessage(sender, "There's no FieldBuilder object to cancel...");
			return true;
		}
		
		FieldBuilder.destroyDefaultBuilder();
		Message.dynamicMessage(sender, "FieldBuilder object cancelled");
		
		return true;
	}
	
	private boolean remove(CommandSender sender, String[] args) {
		
		if (args.length == 0) {
			Message.dynamicMessage(sender, "Specify a field to remove");
			return true;
		}
		
		
		
		return true;
	}
	
	private boolean create(Player player, String args[]) {
		
		if (args.length == 0) {
			Message.dynamicMessage(player, "Specify a probability for the item");
			return true;
		}
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder != null) {
			Message.dynamicMessage(player, "Cannot create a new FieldBuilder object, still one existing");
			return true;
		}
		
		if (player.getWorld().equals(plugin.getHub().spawn.getWorld())) {
			player.sendMessage("You cannot register a field in the hub world");
			return true;
		}
		
		Message.dynamicMessage(player, "Field builder created");
		builder = FieldBuilder.initDefaultBuilder(args[0], plugin.getPlugin(), player);
		
		return true;
	}
	
	private boolean adminwarp(Player player) {
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder == null) {
			Message.dynamicMessage(player, "Cannot add a spawn in a null FieldBuilder object");
			Message.dynamicMessage(player, "Please, launch '/sw map create <name>' to initialize one");
			return true;
		}
		
		builder.setAdminWarp();
		
		return true;
	}
	
	private boolean addspawn(Player player) {
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder == null) {
			Message.dynamicMessage(player, "Cannot add a spawn in a null FieldBuilder object");
			Message.dynamicMessage(player, "Please, launch '/sw map create <name>' to initialize one");
			return true;
		}
		
		builder.recordNewSpawn();
		
		return true;
	}
	
	private boolean bind(Player player) {
		
		FieldBuilder builder = FieldBuilder.getDefaultBuilder();
		
		if (builder == null) {
			Message.dynamicMessage(player, "Cannot add a spawn in a null FieldBuilder object");
			Message.dynamicMessage(player, "Please, launch '/sw map create <name>' to initialize one");
			return true;
		}
		
		builder.takeBound();
		return true;
	}
	
	/*
	 * Add a tier
	 */
	
	@CommandCallback(command = "addtier", permissions = {"skywars.addtier", "skywars.bypass"})
	public boolean addtier(CommandSender sender, String[] args) {
		
		if (args.length < 2) {
			Message.dynamicMessage(sender, "Specify a name and a probability");
			return true;
		}
		
		String name = args[0];
		
		double prob;
		
		try {
			prob = Double.parseDouble(args[1]);
		} catch (NumberFormatException ex) {
			Message.dynamicMessage(sender, "The second argument must be a number");
			return true;
		}
		
		if (prob < 0.0d || prob > 100.0d) {
			Message.dynamicMessage(sender, "The probability must be a percentage");
			return true;
		}
		
		plugin.getChestsManager().addTier(name, prob);
		Message.dynamicMessage(sender, "Tier " + name + " registered successfully");
		
		return true;
	}
	
	/*
	 * Add a item to a tier
	 */
	
	@CommandCallback(command = "additem", permissions = {"skywars.additem", "skywars.bypass"})
	public boolean additem(Player player, String[] args) {
		
		if (args.length == 0) {
			Message.dynamicMessage(player, "Specify a tier name for the item");
			return true;
		}
		
		String name = args[0];
		
		ItemStack item  = player.getItemInHand();
		
		if (item == null) {
			Message.dynamicMessage(player, "Cannot register null items");
			return true;
		} else if (item.getType() == Material.AIR) {
			Message.dynamicMessage(player, "Cannot register null items");
			return true;
		}
			
		Tier tier = plugin.getChestsManager().getTier(name);
		
		if (tier == null) {
			Message.dynamicMessage(player, "The specified tier does not exist");
			return true;
		}
		
		tier.items.add(item);
		
		Message.dynamicMessage(player, "Item " + item.getType().name() + " registered successfully");
		
		return true;
	}
	
	/*
	 * Save tiers configurations
	 */
	
	@CommandCallback(command = "savetiers", permissions = {"skywars.savetiers", "skywars.bypass"})
	public boolean savetiers(CommandSender sender, String[] args) {
		
		plugin.getChestsManager().write(plugin.getItemConfig());
		Message.dynamicMessage(sender, "Tiers configurations saved correctly");
		
		return true;
	}
	
	/*
	 * Create a new join point
	 */
	
	@CommandCallback(command = "newjoin", permissions = {"skywars.newjoin", "skywars.bypass"})
	public boolean newjoin(Player player, String[] args) {
		
		if (args.length < 2) {
			Message.dynamicMessage(player, "Specify a name and a trigger material");
			return true;
		}
		
		Material trigger = Material.getMaterial(args[1]);
		
		if (trigger == null) {
			Message.dynamicMessage(player, "The second argument must be an existing material name (ex: " + Material.GOLD_PLATE.name() +")");
			return true;
		}
		
		new JoinPointBuilder(args[0], trigger, player, plugin);
		
		return true;
	}
	
	/*
	 * Set spawn location
	 */
	
	@CommandCallback(command = "setspawn", permissions = {"skywars.setspawn", "skywars.bypass"})
	public boolean setspawn(Player player, String[] args) {
		
		if (plugin.getHub() == null) {
			
			Message.dynamicMessage(player, "Null pointer hub, cannot set spawn");
			return true;
		}
		
		plugin.getHub().spawn = player.getLocation();
		Message.dynamicMessage(player, "Spawn set");
		plugin.getHub().write();
		
		return true;
	}
	
	/*
	 * Reload HeavenEncounter plugin safely
	 */
	
	@CommandCallback(command = "reload", permissions = {"skywars.reload", "skywars.bypass"})
	public boolean reload(CommandSender sender, String[] args) {
		
		Message.dynamicMessage(sender, "Reloading...");
		Message.dynamicMessage(sender, "Could be laggy...");
		
		plugin.reload();
		
		return true;
	}
}
