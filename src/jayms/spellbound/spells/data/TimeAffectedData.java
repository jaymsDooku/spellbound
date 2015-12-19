package jayms.spellbound.spells.data;

import java.util.UUID;

import jayms.spellbound.spells.Spell;

public class TimeAffectedData extends AffectedData {

	public TimeData timeData;
	
	public TimeAffectedData(UUID user, Spell spell) {
		super(user, spell);
		timeData = new TimeData(user, spell);
	}
}
