package jayms.spellbound.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.Spell;

public class CastSpellEvent extends SpellBoundPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private boolean cancelled = false;
	
	private Spell casted;
	
	public CastSpellEvent(SpellBoundPlayer sbp, Spell casted) {
		super(sbp);
		this.casted = casted;
	}
	
	public Spell getCasted() {
		return casted;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean set) {
		this.cancelled = set;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
