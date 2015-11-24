package jayms.spellbound.spells;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;

import jayms.plugin.event.EventDispatcher;
import jayms.plugin.util.CooldownHandler;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;

public abstract class AbstractSpell implements Spell {

	protected final SpellBoundPlugin running;

	protected EventDispatcher eventDispatcher;
	protected Set<SpellBoundPlayer> sbPlayers = new HashSet<>();
	protected CooldownHandler<SpellBoundPlayer> cooldownHandler = new CooldownHandler<>();
	protected long cooldown;
	protected double manaCost;
	protected double healthCost;

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

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (hasEnabled(sbPlayer)) {
			sbPlayer.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "You can only run one instance of " + getDisplayName() + "!");
			return false;
		}
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
		}
		return true;
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

}
