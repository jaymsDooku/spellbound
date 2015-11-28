package jayms.spellbound.spells;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import jayms.plugin.event.EventDispatcher;
import jayms.plugin.util.CommonUtil;
import jayms.plugin.util.CooldownHandler;
import jayms.plugin.util.MCUtil;
import jayms.plugin.util.tuple.Tuple;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.event.CastSpellEvent;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.data.CommonData;

public abstract class AbstractSpell implements Spell {

	protected final SpellBoundPlugin running;

	protected EventDispatcher eventDispatcher;
	protected Set<SpellBoundPlayer> sbPlayers = new HashSet<>();
	protected CooldownHandler<SpellBoundPlayer> cooldownHandler;
	protected long cooldown;
	protected double manaCost;
	protected double healthCost;
	protected int power = 0;

	protected AbstractSpell(SpellBoundPlugin running) {
		this.running = running;
		this.eventDispatcher = this.running.getEventDispatcher();
		this.cooldownHandler = new CooldownHandler<>(eventDispatcher);
		initEventDispatcher();
	}

	private void initEventDispatcher() {
		eventDispatcher.registerListener(this);
		eventDispatcher.registerListener(cooldownHandler);
	}

	protected final boolean applyManaCost(SpellBoundPlayer sbPlayer) {
		Map<String, Integer> percentages = extractPercentagesFromWand(sbPlayer);
		double manaCost = percentageIncrease(getManaCost(), percentages.get(Wand.MANA_PERCENT));
		double setMana = sbPlayer.getMana() - manaCost;
		if (setMana < 0) {
			return false;
		}
		sbPlayer.setMana(setMana);
		return true;
	}

	protected final boolean applyHealthCost(SpellBoundPlayer sbPlayer) {
		Map<String, Integer> percentages = extractPercentagesFromWand(sbPlayer);
		double healthCost = percentageIncrease(getHealthCost(), percentages.get(Wand.HEALTH_PERCENT));
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
	
	protected final void collideSpells(SpellBoundPlayer sbp, Location loc, double range, UUID... except) {
		
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
	
	protected final void applyCooldown(SpellBoundPlayer sbPlayer) {
		
		if (cooldownHandler.isOnCooldown(sbPlayer)) {
			return;
		}
		
		cooldownHandler.cooldown(sbPlayer, cooldown);
	}
	
	protected final void returnToMainSlots(SpellBoundPlayer sbp) {
		sbp.setSlotInside(-1);
	}
	
	protected final double percentageIncrease(double first, int percent) {
		return CommonUtil.percentageIncrease(first, percent);
	}
	
	protected final Map<String, Integer> extractPercentagesFromWand(SpellBoundPlayer sbp) {
		
		if (!sbp.assureSelectedWand()) {
			return new HashMap<>();
		}
		
		Wand w = sbp.getSelectedWand();
		SpellType type = getType();
		
		Map<String, Integer> result = new HashMap<>();
		
		result.put(Wand.MANA_PERCENT, w.getManaPercentage(type));
		result.put(Wand.HEALTH_PERCENT, w.getHealthPercentage(type));
		result.put(Wand.CD_PERCENT, w.getCooldownPercentage(type));
		result.put(Wand.DMG_PERCENT, w.getDamagePercentage(type));
		result.put(Wand.BACKFIRE_PERCENT, w.getBackfirePercentage(type));
		result.put(Wand.KNOCKBACK_PERCENT, w.getKnockbackPercentage(type));
		
		return result;
	}
	
	protected final String getFormatFromExtras(ChatColor... extras) {
		String result = "";
		if (extras.length == 1) {
			ChatColor c = extras[0];
			if (c.isFormat()) {
				result += "&" + c.getChar();
			}
		}
		return result;
	}
	
	protected final int toPower(double damage) {
		return (int) damage * 5;
	}
	
	protected final Vector applyGravity(Vector velocity, double gravity, double delta) {
		return velocity.add(new Vector(0, gravity, 0).multiply(delta));
	}
	
	protected final boolean restrictRange(Location loc, Location origin, double range) {
		return loc.distance(origin) > range;
	}
	
	protected final double extractDelta() {
		return (double) running.getDelta() / 230;
	}
	
	protected final List<Entity> getAffectedEntities(Location loc, double range, SpellBoundPlayer sbp) {
		SpellListener listener = running.getSpellHandler().getListener();
		Set<Entity> holograms = listener.getHologramEntitys();

		return MCUtil.getEntities(loc, range, CommonUtil.addArrays(new Entity[] { sbp.getBukkitPlayer() }, CommonUtil.toArray(holograms, Entity.class), Entity.class));
	}
	
	protected final void damageEntities(List<Entity> affected, double damage, SpellBoundPlayer sbp, Tuple<Boolean, Integer> fire) {
		List<LivingEntity> livingAE = MCUtil.getLivingEntitiesFromEntities(affected);
		LivingEntity[] livingAEArray = livingAE.toArray(new LivingEntity[livingAE.size()]);
		MCUtil.damageEntities(damage, sbp.getBukkitPlayer(), livingAEArray);
		if (fire.getA()) {
			MCUtil.setFire(fire.getB(), livingAEArray);
		}
	}
	
	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (cooldownHandler.isOnCooldown(sbPlayer)) {
			sbPlayer.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + getDisplayName().applyColour() + " is on cooldown! Time Left: " + ((float) cooldownHandler.timeLeft(sbPlayer) / 1000) + " Seconds");
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
	
	@Override
	public CooldownHandler<SpellBoundPlayer> getCooldowns() {
		return cooldownHandler;
	}
	
}
