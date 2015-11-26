package jayms.spellbound.spells;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;

import jayms.plugin.event.EventDispatcher;
import jayms.plugin.util.CooldownHandler;
import jayms.plugin.util.MCUtil;
import jayms.plugin.util.tuple.Tuple;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.event.CastSpellEvent;
import jayms.spellbound.player.SpellBoundPlayer;

public abstract class AbstractSpell implements Spell {

	protected final SpellBoundPlugin running;

	protected EventDispatcher eventDispatcher;
	protected Set<SpellBoundPlayer> sbPlayers = new HashSet<>();
	protected CooldownHandler<SpellBoundPlayer> cooldownHandler = new CooldownHandler<>();
	protected long cooldown;
	protected double manaCost;
	protected double healthCost;
	protected int power = 0;

	protected AbstractSpell(SpellBoundPlugin running) {
		this.running = running;
		this.eventDispatcher = this.running.getEventDispatcher();
		initEventDispatcher();
	}

	private void initEventDispatcher() {
		eventDispatcher.registerListener(this);
		eventDispatcher.registerListener(cooldownHandler);
	}

	protected final boolean applyManaCost(SpellBoundPlayer sbPlayer) {
		double manaCost = getManaCost();
		double setMana = sbPlayer.getMana() - manaCost;
		if (setMana < 0) {
			return false;
		}
		sbPlayer.setMana(setMana);
		return true;
	}

	protected final boolean applyHealthCost(SpellBoundPlayer sbPlayer) {
		double healthCost = getHealthCost();
		double setHealth = sbPlayer.getBukkitPlayer().getHealth() - healthCost;
		if (setHealth < 0) {
			return false;
		}
		sbPlayer.getBukkitPlayer().setHealth(setHealth);
		return true;
	}
	
	protected final void playSound(Location loc, Sound sound, float vol, float pit) {
		MCUtil.playSound(loc, sound, vol, pit);
	}
	
	protected final void collideSpells(SpellBoundPlayer sbp, Location loc, double range,UUID... except) {
		
		List<Tuple<Spell, SpellBoundPlayer>> spells = running.getSpellHandler().getSpellAroundPoint(loc, range, except);
		
		if (!spells.isEmpty()) {
			for (Tuple<Spell, SpellBoundPlayer> spellTuple : spells) {
				SpellBoundPlayer spellSbp = spellTuple.getB();
				Spell spell  = spellTuple.getA();
				int compare = compareTo(spell);
				switch (compare) {
				case 1:
					disable(sbp, true);
					break;
				case 0:
					disable(sbp, true);
					spell.disable(spellSbp, true);
					break;
				case -1:
					spell.disable(spellSbp, true);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (cooldownHandler.isOnCooldown(sbPlayer)) {
			sbPlayer.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + getDisplayName() + " is on cooldown! Time Left: " + ((float) cooldownHandler.timeLeft(sbPlayer) / 1000) + " Seconds");
			return false;
		}
		if (!applyManaCost(sbPlayer)) {
			sbPlayer.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "You do not have enough mana to cast this spell!");
			return false;
		}
		if (!applyHealthCost(sbPlayer)) {
			sbPlayer.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "You do not have enough health to cast this spell!");
			return false;
		}
		
		CastSpellEvent event = new CastSpellEvent(sbPlayer, this);
		
		eventDispatcher.callEvent(event);
		
		return !event.isCancelled();
	}

	@Override
	public Set<SpellBoundPlayer> getUsing() {
		return sbPlayers;
	}

	@Override
	public boolean hasEnabled(SpellBoundPlayer sbPlayer) {
		return sbPlayers.contains(sbPlayer);
	}

	@Override
	public double getManaCost() {
		return manaCost;
	}

	@Override
	public double getHealthCost() {
		return healthCost;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public int getPower() {
		return power;
	}
	
	@Override
	public int compareTo(Spell spell) {
		
		int ouPower = getPower();
		int spPower = spell.getPower();
		
		if (spPower > ouPower) {
			return 1;
		}else if (spPower == ouPower) {
			return 0;
		}else if (spPower < ouPower) {
			return -1;
		}
		
		return 0;
	}
	
}
