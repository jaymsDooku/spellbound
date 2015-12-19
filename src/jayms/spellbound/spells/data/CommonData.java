package jayms.spellbound.spells.data;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import jayms.spellbound.spells.Spell;

public class CommonData extends TopData implements SpellData {
	
	public Location loc;
	public Location origin;
	public Vector dir;
	public Vector velocity;
	
	public CommonData(UUID user, Spell parent) {
		super(user, parent);
	}
}
