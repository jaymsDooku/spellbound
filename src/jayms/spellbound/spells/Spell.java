package jayms.spellbound.spells;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import jayms.java.mcpe.common.CooldownHandler;
import jayms.java.mcpe.common.Handler;
import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.collect.Tuple;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.collision.CollisionPriority;
import jayms.spellbound.spells.collision.CollisionResult;

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
	
	ChatColor getPowerColor();
	
	Set<SpellBoundPlayer> getUsing();
	
	CooldownHandler<SpellBoundPlayer> getCooldowns();
	
	SpellType getType();
	
	CollisionPriority getPriority();
	
	double getCollisionRange();
	
	Handler<Tuple<SpellBoundPlayer, CollisionResult>> getCollisionHandler();
}
