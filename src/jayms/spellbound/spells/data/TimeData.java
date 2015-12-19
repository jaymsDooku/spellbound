package jayms.spellbound.spells.data;

import java.util.HashMap;
import java.util.UUID;

import jayms.spellbound.spells.Spell;

public class TimeData extends CommonData {

	public HashMap<String, Long> times = new HashMap<>();
	
	public TimeData(UUID user, Spell parent) {
		super(user, parent);
	}
	
	public void put(String key, long value) {
		times.put(key, value);
	}
	
	public long get(String key) {
		return times.get(key);
	}
	
	public boolean has(String key) {
		return times.containsKey(key);
	}
	
	public long remove(String key) {
		return times.remove(key);
	}

}
