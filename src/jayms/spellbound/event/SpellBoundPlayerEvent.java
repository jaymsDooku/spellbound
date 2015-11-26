package jayms.spellbound.event;

import org.bukkit.event.Event;

import jayms.spellbound.player.SpellBoundPlayer;

public abstract class SpellBoundPlayerEvent extends Event {
	
	private SpellBoundPlayer sbp;
	
	protected SpellBoundPlayerEvent(SpellBoundPlayer sbp) {
		this.sbp = sbp;
	}
	
	public SpellBoundPlayer getSpellBoundPlayer() {
		return sbp;
	}
}
