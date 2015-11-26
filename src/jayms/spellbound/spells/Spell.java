package jayms.spellbound.spells;

import java.util.Set;

import org.bukkit.event.Listener;

import jayms.plugin.system.description.Version;
import jayms.spellbound.player.SpellBoundPlayer;

public interface Spell extends Listener, Comparable<Spell> {
	
	boolean enable(SpellBoundPlayer sbPlayer);
	
	boolean disable(SpellBoundPlayer sbPlayer, boolean effects);
	
	boolean hasEnabled(SpellBoundPlayer sbPlayer);
	
	String getUniqueName();
	
	String getDisplayName();
	
	String[] getDescription();
	
	Version getVersion();
	
	boolean isShiftClick();
	
	long getChargeTime();
	
	double getManaCost();
	
	double getHealthCost();
	
	long getCooldown();
	
	int getPower();
	
	Set<SpellBoundPlayer> getUsing();
}
