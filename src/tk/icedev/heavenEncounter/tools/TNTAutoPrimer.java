package tk.icedev.heavenEncounter.tools;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TNTAutoPrimer {
	
	private World world;
	private Map<TNTPrimed, Player> map;
	private boolean active;
	
	public TNTAutoPrimer(World world) {
		
		this.world = world;
		map = new HashMap<TNTPrimed, Player>();
		active = false;
	}
	
	public Player getSource(TNTPrimed tnt) {
		
		return map.get(tnt);
	}
	
	public void setActive(boolean active) {
		
		this.active = active;
	}
	
	public boolean isActive() {
		
		return active;
	}
	
	public void reset(boolean active) {
		
		map.clear();
		this.active = active;
	}
	
	public void destroy() {
		
		map.clear();
		map = null;
		world = null;
	}
	
	public void onPlayerUse(PlayerInteractEvent event) {
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR || !active)
			return;
		
		ItemStack inHand = event.getPlayer().getItemInHand();
		
		if (inHand == null)
			return;
		
		if (inHand.getType() != Material.TNT)
			return;
			
		int amount = inHand.getAmount();
		Player player = event.getPlayer();
			
		if (amount < 2)
			player.getInventory().setItemInHand(null);
		else {
			inHand.setAmount(amount - 1);
			player.getInventory().setItemInHand(inHand);
		}
		
		Location spawnLoc = player.getLocation();
		spawnLoc.add(spawnLoc.getDirection().multiply(2));
		
		TNTPrimed tnt = world.spawn(spawnLoc, TNTPrimed.class);
		tnt.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.1));
		tnt.setFuseTicks(40);	// 2 seconds
		
		map.put(tnt, event.getPlayer());
	}

	public boolean onPlayerPlace(BlockPlaceEvent event) {
		
		if (event.getBlockPlaced().getType() != Material.TNT || !active)
			return false;
		
		event.getBlock().setType(Material.AIR);
		Location spawnLoc = event.getBlock().getLocation();
		
		TNTPrimed tnt = world.spawn(spawnLoc, TNTPrimed.class);
		tnt.setFuseTicks(40);	// 2 seconds
		
		map.put(tnt, event.getPlayer());
		return true;
	}
	
	public boolean onEntityExplode(EntityExplodeEvent event) {
		
		if (map.containsKey(event.getEntity())) {
			map.remove(event.getEntity());
			return true;
		}
		
		return false;
	}
}
