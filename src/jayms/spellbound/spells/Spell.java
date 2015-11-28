package jayms.spellbound.spells;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import jayms.plugin.system.description.Version;
import jayms.plugin.util.ColourableString;
import jayms.plugin.util.CooldownHandler;
import jayms.spellbound.player.SpellBoundPlayer;

public interface Spell extends Listener, Comparable<Spell> {
	
	boolean enable(SpellBoundPlayer sbPlayer);
	
	boolean disable(SpellBoundPlayer sbPlayer, boolean effects);
	
	boolean hasEnabled(SpellBoundPlayer sbPlayer);
	
	String getUniqueName();
	
	ColourableString getDisplayName();
	
	ColourableString[] getDescription();
	
	Version getVersion();
	
	boolean isShiftClick();
	
	long getChargeTime();
	
	double getManaCost();
	
	double getHealthCost();
	
	long getCooldown();
	
	int getPower();
	
	ChatColor getPowerColor();
	
	Set<SpellBoundPlayer> getUsing();
	
	CooldownHandler<SpellBoundPlayer> getCooldowns();
	
	SpellType getType();
}
