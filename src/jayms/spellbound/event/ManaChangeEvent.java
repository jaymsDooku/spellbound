package jayms.spellbound.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import jayms.spellbound.player.SpellBoundPlayer;

public class ManaChangeEvent extends SpellBoundPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private boolean cancelled = false;
	
	private double difference;
	private double manaToChange;
	private double currentMana;
	
	public ManaChangeEvent(SpellBoundPlayer sbp, double manaToChange) {
		super(sbp);
		this.manaToChange = manaToChange;
		this.currentMana = sbp.getMana();
		this.difference = this.currentMana - this.manaToChange;
	}

	public double getManaToChange() {
		return manaToChange;
	}

	public void setManaToChange(double manaToChange) {
		this.manaToChange = manaToChange;
	}
	
	public double getCurrentMana() {
		return currentMana;
	}
	
	public double getDifference() {
		return difference;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
