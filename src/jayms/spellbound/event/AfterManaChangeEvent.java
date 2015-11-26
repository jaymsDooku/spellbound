package jayms.spellbound.event;

import org.bukkit.event.HandlerList;

import jayms.spellbound.player.SpellBoundPlayer;

public class AfterManaChangeEvent extends SpellBoundPlayerEvent {

	private static final HandlerList handlers = new HandlerList();
	
	public AfterManaChangeEvent(SpellBoundPlayer sbp) {
		super(sbp);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
