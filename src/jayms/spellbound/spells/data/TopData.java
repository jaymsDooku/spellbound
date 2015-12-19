package jayms.spellbound.spells.data;

import java.util.UUID;

import jayms.spellbound.spells.Spell;

public class TopData {

	public Spell parent;
	public UUID uuid = UUID.randomUUID();
	public UUID user;
	
	public TopData(UUID user, Spell parent) {
		this.user = user;
		this.parent = parent;
	}
}
