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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import jayms.java.mcpe.common.CooldownHandler;
import jayms.java.mcpe.common.collect.Tuple;
import jayms.java.mcpe.common.util.MCUtil;
import jayms.java.mcpe.common.util.NumberUtil;
import jayms.java.mcpe.event.EventDispatcher;
import jayms.spellbound.Main;
import jayms.spellbound.event.CastSpellEvent;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;

public abstract class AbstractSpell implements Spell {

	protected EventDispatcher eventDispatcher;
	protected Set<SpellBoundPlayer> sbPlayers = new HashSet<>();
	protected CooldownHandler<SpellBoundPlayer> cooldownHandler;
	protected long cooldown;
	protected double manaCost;
	protected double healthCost;
	protected int power = 0;
	
	private HashMap<UUID, Double> manaCache = new HashMap<>(); 
	private HashMap<UUID, Double> healthCache = new HashMap<>();

	protected AbstractSpell() {
		this.eventDispatcher = Main.self.getEventDispatcher();
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
		manaCache.put(sbPlayer.getBukkitPlayer().getUniqueId(), manaCost);
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
		healthCache.put(sbPlayer.getBukkitPlayer().getUniqueId(), healthCost);
		return true;
	}
	
	protected final boolean refundManaCost(SpellBoundPlayer sbPlayer) {
		UUID uuid = sbPlayer.getBukkitPlayer().getUniqueId();
		if (manaCache.containsKey(uuid)) {
			double mana = manaCache.remove(uuid);
			double setMana = sbPlayer.getMana() + mana;
			if (setMana > sbPlayer.getMaxMana()) {
				setMana = sbPlayer.getMaxMana();
			}
			sbPlayer.setMana(setMana);
			return true;
		}
		return false;
	}
	
	protected final boolean refundHealthCost(SpellBoundPlayer sbPlayer) {
		UUID uuid = sbPlayer.getBukkitPlayer().getUniqueId();
		if (healthCache.containsKey(uuid)) {
			double health = healthCache.remove(uuid);
			double setHealth = sbPlayer.getBukkitPlayer().getHealth() + health;
			if (setHealth > sbPlayer.getBukkitPlayer().getMaxHealth()) {
				setHealth = sbPlayer.getBukkitPlayer().getMaxHealth();
			}
			sbPlayer.setMana(setHealth);
			return true;
		}
		return false;
	}
	
	protected final void clearCostCaches(SpellBoundPlayer sbPlayer) {
		if (manaCache.containsKey(sbPlayer.getBukkitPlayer().getUniqueId())) {
			manaCache.remove(sbPlayer.getBukkitPlayer().getUniqueId());
		}
		if (healthCache.containsKey(sbPlayer.getBukkitPlayer().getUniqueId())) {
			healthCache.remove(sbPlayer.getBukkitPlayer().getUniqueId());
		}
	}
	
	protected final void playSound(Location loc, Sound sound, float vol, float pit) {
		MCUtil.playSound(loc, sound, vol, pit);
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
		return NumberUtil.percentageIncrease(first, percent);
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
		return (double) Main.self.getUpdater().getDelta() / 230;
	}
	
	protected final List<LivingEntity> getAffectedEntities(Location loc, double range, SpellBoundPlayer sbp) {
		return MCUtil.getLivingEntities(loc, range, sbp.getBukkitPlayer());
	}
	
	protected final void damageEntities(List<LivingEntity> affected, double damage, SpellBoundPlayer sbp, Tuple<Boolean, Integer> fire) {
		MCUtil.damageEntities(damage, sbp.getBukkitPlayer(), affected.toArray(new LivingEntity[affected.size()]));
		if (fire != null) {
			if (fire.getA()) {
				MCUtil.setFire(fire.getB(), affected.toArray(new LivingEntity[affected.size()]));
			}
		}
	}
	
	protected final void givePotionEffect(PotionEffectType effect, int duration, int amplifier, boolean ambient, boolean particles, List<Entity> affected) {
		List<LivingEntity> le = MCUtil.getLivingEntitiesFromEntities(affected);
		MCUtil.givePotionEffect(effect, duration, amplifier, ambient, particles, le.toArray(new LivingEntity[le.size()]));
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
	public boolean disable(SpellBoundPlayer sbPlayer, boolean effects) {
		clearCostCaches(sbPlayer);
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
