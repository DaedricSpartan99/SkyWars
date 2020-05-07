package tk.icedev.heavenEncounter.game.fields;

import org.bukkit.Location;

public class FieldBounds {

	private Location phase;
	private int extx, extz;
	
	public FieldBounds(Location phase, int extx, int extz) {
		
		this.phase = phase;
		this.extx = extx;
		this.extz = extz;
	}
	
	public Location getPhase() {
		
		return phase;
	}
	
	public int getExtX() {
		
		return extx;
	}
	
	public int getExtZ() {
		
		return extz;
	}
	
	public boolean isInside(Location loc) {
		
		return loc.getX() - phase.getX() < extx && loc.getZ() - phase.getZ() < extz;
	}
}
