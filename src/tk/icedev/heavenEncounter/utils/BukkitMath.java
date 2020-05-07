package tk.icedev.heavenEncounter.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.util.Vector;

public class BukkitMath {
	
	private static Random random = new Random();

	public static int randomInt() { // [0, 2**32 [
		
		return random.nextInt();
	}
	
	public static int randomInt(int range) { // [0, range]
		
		return random.nextInt(range);
	}
	
	public static int randomInt(int min, int max) {
		
		return random.nextInt(max - min + 1) + min;
	}
	
	public static double randomDouble() {	// [0, 1]
		
		return random.nextDouble();
	}
	
	public static float round(float d, int decimalPlace) {
		
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
	
	public static int sgn(int x) {
		
		if (x < 0)
			return -1;
		else if (x > 0)
			return 1;
		else
			return 0;
	}
	
	public static Vector circle(float angle, float ray, int blockY) {
		
		return new Vector(Math.cos(angle) * ray, blockY, Math.sin(angle) * ray);
	}
	
	public static int circle(int x, int ray) {
		
		return (int)Math.sqrt(ray * ray - x * x);
	}
	
	public static void setCircleCoordAt(Vector buffer, int ray,  boolean positive) {
		
		buffer.setX(positive ? circle(buffer.getBlockX(), ray) : -circle(buffer.getBlockX(), ray));
	}
	
	public static <T> List<T> sortMapValues(Map<Integer, T> map) {
		
		List<T> list = new ArrayList<T>();
		
		int last = -2147483648;
		while(list.size() < map.size()) {
			int min = 2147483647;
			for (Entry<Integer, T> entry : map.entrySet()) {
				int val = entry.getKey();
				if (val < min && val > last)
					min = val;
			}
			list.add(map.get(last = min));
		}
		
		return list;
	}
	
	public static <T> List<T> sortMapByValues(Map<T, Double> map) {
		
		List<T> list = new ArrayList<T>();
		
		List<Double> values = new ArrayList<Double>(map.values());
		Collections.sort(values);
		
		for (double value : values) {
			for (Entry<T, Double> entry : map.entrySet()) {
				if (entry.getValue().doubleValue() == value && !list.contains(entry.getKey())) {
					list.add(entry.getKey());
					break;
				}
			}
		}
		
		return list;
	}
}
