package jayms.spellbound.spells.offense;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import jayms.java.mcpe.common.Handler;
import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.collect.Tuple;
import jayms.java.mcpe.common.util.MCUtil;
import jayms.java.mcpe.common.util.NumberUtil;
import jayms.java.mcpe.common.util.ParticleEffect;
import jayms.java.mcpe.common.util.ParticleEffect.BlockData;
import jayms.java.mcpe.event.UpdateEvent;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.spells.collision.CollisionPriority;
import jayms.spellbound.spells.collision.CollisionResult;
import jayms.spellbound.spells.data.CommonData;
import jayms.spellbound.spells.variables.CommonSpellVariables;

public class CalidumDolor extends AbstractSpell {

	private CalidumDolorSpellVariables variables;
	private boolean fire;
	private int fireChance;
	
	private class CalidumDolorSpellVariables extends CommonSpellVariables {
	
		public int fireTicks;
		
		public CalidumDolorSpellVariables() {
		}
	}

	private class CalidumDolorData extends CommonData {

		public CalidumDolorData(UUID user, Spell parent) {
			super(user, parent);
		}

	}

	public CalidumDolor() {
		variables = new CalidumDolorSpellVariables();
		FileConfiguration config = Main.self.getYAMLFileMCExt().getFC();
		cooldown = config.getLong("Spells.Offense.CalidumDolor.Cooldown");
		manaCost = config.getDouble("Spells.Offense.CalidumDolor.ManaCost");
		healthCost = config.getDouble("Spells.Offense.CalidumDolor.HealthCost");
		variables.gravity = config.getDouble("Spells.Offense.CalidumDolor.Gravity");
		variables.range = config.getDouble("Spells.Offense.CalidumDolor.Range");
		fire = config.getBoolean("Spells.Offense.CalidumDolor.Fire");
		fireChance = config.getInt("Spells.Offense.CalidumDolor.FireChance");
		variables.damage = config.getDouble("Spells.Offense.CalidumDolor.Damage");
		variables.speed = config.getDouble("Spells.Offense.CalidumDolor.Speed");
		fire = config.getBoolean("Spells.Offense.CalidumDolor.Fire");
		variables.fireTicks = config.getInt("Spells.Offense.CalidumDolor.FireTicks");
		power = toPower(variables.damage);
	}

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (!super.enable(sbPlayer)) {
			return false;
		}
		applyCooldown(sbPlayer);
		returnToMainSlots(sbPlayer);
		return sbPlayers.add(sbPlayer);
	}

	@Override
	public boolean disable(SpellBoundPlayer sbPlayer, boolean effects) {
		if (!hasEnabled(sbPlayer)) {
			return false;
		}

		CalidumDolorData data = (CalidumDolorData) sbPlayer.getSpellData(this);
		data.loc = data.loc.add(data.loc.getDirection().multiply(-1).multiply(1.1).normalize());
		playSound(data.loc, Sound.EXPLODE, 1f, 50f);
		ParticleEffect.LAVA.display(data.loc, 0.2f, 0.2f, 0.2f, 0.3f, 12);
		
		sbPlayer.putSpellData(this, null);
		sbPlayers.remove(sbPlayer);
		return true;
	}

	@Override
	public String getUniqueName() {
		return "CalidumDolor";
	}

	@Override
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&', "&4CalidumDolor");
	}

	@Override
	public String[] getDescription() {
		return new String[] {
				ChatColor.translateAlternateColorCodes('&', "&6Have a coal ore in your hot bar to use this spell.")
				, ChatColor.translateAlternateColorCodes('&', "&6Click to heat up the coal, and then shoot it forward.")};
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}

	@Override
	public boolean isShiftClick() {
		return false;
	}

	@Override
	public long getChargeTime() {
		return 0;
	}

	@Override
	public ChatColor getPowerColor() {
		return ChatColor.GOLD;
	}

	@Override
	public SpellType getType() {
		return SpellType.OFFENSE;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onUpdate(UpdateEvent e) {
		for (SpellBoundPlayer sbp : sbPlayers) {
			Player player = sbp.getBukkitPlayer();
			CalidumDolorData data = (CalidumDolorData) sbp.getSpellData(this);
			if (data == null) {
				sbp.putSpellData(this, new CalidumDolorData(player.getUniqueId(), this));
				data = (CalidumDolorData) sbp.getSpellData(this);
				data.loc = player.getEyeLocation();
				data.origin = player.getLocation();
				playSound(data.origin, Sound.BLAZE_HIT, 0.5f, 2f);
				data.dir = player.getEyeLocation().getDirection();
				data.velocity = new Vector();
			}

			double delta = extractDelta();

			variables.speedfactor = variables.speed * delta;

			data.velocity = data.velocity.add(data.dir.multiply(variables.speedfactor));
			data.velocity = applyGravity(data.velocity, variables.gravity, delta);

			Location loc = data.loc;
			data.loc = loc.add(data.velocity);

			ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.OBSIDIAN, (byte) 0), 0.2f, 0.2f, 0.2f, 0.04f, 11,
					data.loc, 257);
			ParticleEffect.FLAME.display(data.loc, 0.1f, 0.1f, 0.1f, 0.03f, 9);
			playSound(data.loc, Sound.BLAZE_BREATH, 0.4f, 100f);

			if (!sbp.getBukkitPlayer().isOnline()) {
				disable(sbp, false);
				return;
			}

			if (restrictRange(data.loc, data.origin, variables.range)) {
				disable(sbp, true);
				return;
			}
			
			Block block = data.loc.getBlock();

			if (MCUtil.isSolid(block) || block.isLiquid()) {
				disable(sbp, true);
				return;
			}

			List<LivingEntity> affectedEntities = getAffectedEntities(data.loc, 2, sbp);

			if (affectedEntities.size() > 0) {
				Tuple<Boolean, Integer> fire = new Tuple<>(false, 100);
				if (NumberUtil.hasChance(fireChance)) {
					fire.setA(true);
				}
				damageEntities(affectedEntities, variables.damage, sbp, fire);
				disable(sbp, true);
				return;
			}
		}
	}

	@Override
	public CollisionPriority getPriority() {
		return CollisionPriority.NORMAL;
	}

	@Override
	public double getCollisionRange() {
		return 2;
	}

	@Override
	public Handler<Tuple<SpellBoundPlayer, CollisionResult>> getCollisionHandler() {
		return new Handler<Tuple<SpellBoundPlayer, CollisionResult>>() {

			@Override
			public void handle(Tuple<SpellBoundPlayer, CollisionResult> ob) {
				SpellBoundPlayer sbp = ob.getA();
				CollisionResult result = ob.getB();
				switch (result) {
				case DESTROYED:
					disable(sbp, true);
					break;
				case SUCCESS:
					break;
				default:
					break;
				}
			}
			
		};
	}

}
