package tk.icedev.heavenEncounter.game.boards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import tk.icedev.heavenEncounter.utils.Config;

public class PlayerBoardFormat implements ConfigurationSerializable {
	
	public String alive = "[✔] %player%", dead = "[✗] %player%";

	@Override
	public Map<String, Object> serialize() {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("alive", Config.staticWriteFormat(alive));
		map.put("dead", Config.staticWriteFormat(dead));
		
		return map;
	}
	
	public static void registerConfigurations() {
		
		ConfigurationSerialization.registerClass(PlayerBoardFormat.class);
	}
	
	public static PlayerBoardFormat deserialize(Map<String, Object> map) {
		
		PlayerBoardFormat format = new PlayerBoardFormat();
		
		Object _alive = map.get("alive"), _dead = map.get("dead");
		
		if (_alive instanceof String)
			format.alive = Config.staticReadFormat((String)_alive);
		
		if (_dead instanceof String)
			format.dead = Config.staticReadFormat((String)_dead);
		
		return format;
	}
}