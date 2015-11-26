package jayms.spellbound.spells.data;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import jayms.spellbound.spells.Spell;

public class CommonData extends TopData implements SpellData {
	
	public Spell parent;
	public Location loc;
	public Location origin;
	public Vector dir;
	public Vector velocity = new Vector(0, 2, 0);
	
	public CommonData(Spell parent) {
		this.parent = parent;
	}
}
