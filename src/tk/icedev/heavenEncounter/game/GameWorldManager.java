package tk.icedev.heavenEncounter.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

import tk.icedev.heavenEncounter.defaults.Message;
import tk.icedev.heavenEncounter.extern.HeavenEncounter;
import tk.icedev.heavenEncounter.game.fields.Field;
import tk.icedev.heavenEncounter.game.fields.FieldBounds;
import tk.icedev.heavenEncounter.hub.StaticField;
import tk.icedev.heavenEncounter.tools.EmptyChunkGenerator;
import tk.icedev.heavenEncounter.tools.EntityEraser;
import tk.icedev.heavenEncounter.utils.BukkitFiles;

public class GameWorldManager {
	
	private World world;
	private Map<String, FieldsLine> fieldsMap;
	private final int mapsInitNumber, mapsDistance;
	private EntityEraser eraser;

	public GameWorldManager(World world, int mapsInitNumber, int mapsDistance, Plugin plugin) {
		
		this.world = world;
		this.mapsInitNumber = mapsInitNumber;
		this.mapsDistance = mapsDistance;
		this.fieldsMap = new LinkedHashMap<String, FieldsLine>();
		this.eraser = new EntityEraser(world, 400L, plugin);
	}
	
	public World getWorld() {
		
		return world;
	}
	
	public void destroy(Plugin plugin) {
		
		this.eraser.destroy();
		eraser = null;
		
		for (Entry<String, FieldsLine> entry : fieldsMap.entrySet())
			entry.getValue().unload(plugin, false);
		
		Message.console("Removing game world");
		
		Bukkit.getServer().unloadWorld(world.getName(), true);
		BukkitFiles.recursiveRemove(world.getWorldFolder());
		
		fieldsMap.clear();
		fieldsMap = null;
		world = null;
	}
	
	public FieldsLine getFieldsLine(String name) {
		
		return this.fieldsMap.get(name);
	}
	
	public void reload(Set<String> fieldsNames, HeavenEncounter plugin) {
		
		for (Entry<String, FieldsLine> entry : this.fieldsMap.entrySet()) {
			
			if (!fieldsNames.contains(entry.getKey()))
				entry.getValue().unload(plugin.getPlugin(), true);
			else if (!plugin.getField(entry.getKey()).equals(entry.getValue().getBaseField()))
				entry.getValue().reload(plugin.getPlugin());
		}
		
		for (String fieldName : fieldsNames) {
			
			if (!this.fieldsMap.containsKey(fieldName))
				this.registerNewField(plugin.getField(fieldName), plugin.getPlugin());
		}
	}
	
	public void registerFields(Collection<StaticField> fields, Plugin plugin) {
		
		if (!fieldsMap.isEmpty())
			this.fieldsMap.clear();
		
		int z = 0;
		
		for (StaticField sfield : fields) {
			
			Message.console("Configuring field " + sfield.getName());
			
			FieldsLine fline;
			this.fieldsMap.put(sfield.getName(), fline = new FieldsLine(sfield, z++));
			fline.load(plugin);
		}
		
		Message.console("Building all fields, expect some lag");
	}
	
	public void registerNewField(StaticField sfield, Plugin plugin) {
		
		Message.console("Configuring field " + sfield.getName());
		
		if (fieldsMap.containsKey(sfield.getName())) {
			
			Message.console("Destroying and rebuilding fields, expect some lag");
			
			FieldsLine fline = fieldsMap.get(sfield.getName());
			fline.setBaseField(sfield);
			fline.reload(plugin);
			
		} else {
		
			int z = this.fieldsMap.size();
			this.fieldsMap.put(sfield.getName(), new FieldsLine(sfield, z));
		}
	}
	
	public Field getFreeField(StaticField sfield, Plugin plugin) {
		
		Field field = this.fieldsMap.get(sfield.getName()).getFreeField();
		
		if (field == null)
			field = this.fieldsMap.get(sfield.getName()).appendNewField(plugin);
		
		return field;
	}
	
	/*
	 * Field info struct
	 */
	
	public class FieldsLine {
		
		private List<Field> fields = new ArrayList<Field>();
		private StaticField sfield;
		private final int z;
		
		public FieldsLine(StaticField sfield, int z) {
			
			this.sfield = sfield;
			this.z = z;
		}
		
		public void load(Plugin plugin) {
			
			for (int x = 0; x < mapsInitNumber; x++) {
				
				Location phase = getPhase(world, x, z, mapsDistance);
				Field field;
				fields.add(field = new Field(new FieldBounds(phase, mapsDistance, mapsDistance), sfield));
				field.build(plugin);
			}
		}
		
		public void unload(Plugin plugin, boolean destroy) {
			
			for (Field field : fields)
				field.destroy(plugin, destroy);
			fields.clear();
		}
		
		public void reload(Plugin plugin) {
			
			this.unload(plugin, true);
			this.load(plugin);
		}
		
		public void setBaseField(StaticField newField) {
			
			this.sfield = newField;
		}
		
		public StaticField getBaseField() {
			
			return this.sfield;
		}
		
		public int getZ() {
			
			return z;
		}
		
		public Field appendNewField(Plugin plugin) {
			
			Field field;
			Location phase = getPhase(world, this.fields.size(), z, mapsDistance);
			this.fields.add(field = new Field(new FieldBounds(phase, mapsDistance, mapsDistance), sfield));
			field.build(plugin);
			
			return field;
		}
		
		public Field getFreeField() {
			
			Field naccessible = null;
			
			for (Field field : this.fields) {
				if (!field.isTaken()) {
					if (field.isAccessible())
						return field;
					else if (naccessible == null)
						naccessible = field;
				}
			}
			
			return naccessible;
		}
	}
	
	/*
	 * Static members
	 */
	
	public static World generateVoidWorld(String name, Plugin plugin) {
		
		Message.console("Generating void world " + name);
		
		WorldCreator creator = new WorldCreator(name);
		
		/*creator.generator(new ChunkGenerator() {
		    @Override
		    public byte[] generate(World world, Random random, int x, int z) {
		        return new byte[32768]; //Empty byte array
		    }
		});*/
		
		creator.generator(new EmptyChunkGenerator());
		creator.type(WorldType.FLAT);
		
		return plugin.getServer().createWorld(creator);
	}
	
	public static Location getPhase(World world, int x, int z, int mapsDistance) {
		
		return new Location(world, x * mapsDistance, 0, z * mapsDistance);
	}
}
