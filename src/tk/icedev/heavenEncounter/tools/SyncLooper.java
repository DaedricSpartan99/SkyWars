package tk.icedev.heavenEncounter.tools;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class SyncLooper<T> implements Runnable {
	
	private int id;
	private final int actionsPerTick;
	private int index;
	private T[] data;
	
	public SyncLooper(T[] data, int actionsPerTick, long frequency, Plugin plugin) {
		
		this.data = data;
		this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, frequency);
		this.index = 0;
		this.actionsPerTick = actionsPerTick;
		this.beginAction();
	}
	
	public void destroy() {
		
		Bukkit.getScheduler().cancelTask(id);
		data = null;
	}
	
	public abstract void beginAction();
	public abstract void tickAction(float percent);
	public abstract void loopAction(int index, T obj);
	public abstract void endAction();

	@Override
	public void run() {
		
		this.tickAction(100f * index / data.length);
		
		for (int i = 0; i < actionsPerTick && index < data.length; i++)
			this.loopAction(index, data[index++]);
		
		if (index == data.length) {
			this.endAction();
			this.destroy();
		}
	}

}
