package jayms.spellbound.spells.offense;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import jayms.plugin.event.update.UpdateEvent;
import jayms.plugin.packet.ParticleEffect;
import jayms.plugin.packet.ParticleEffect.BlockData;
import jayms.plugin.system.description.Version;
import jayms.plugin.util.CommonUtil;
import jayms.plugin.util.MCUtil;
import jayms.plugin.util.tuple.Tuple;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.data.CommonData;

public class CalidumDolor extends AbstractSpell {
	
	private double damage;
	private double speed;
	private double gravity;
	private double range;
	private boolean fire;
	private int fireChance;
	
	private enum CalidumDolorState {
		START, SHOOT;
	}

	private static class CalidumDolorData extends CommonData {

		CalidumDolorState state = CalidumDolorState.START;
		
		public CalidumDolorData(Spell parent) {
			super(parent);
		}

	}

	public CalidumDolor(SpellBoundPlugin running) {
		super(running);
		FileConfiguration config = running.getConfiguration();
		cooldown = config.getLong("Spells.Offense.CalidumDolor.Cooldown");
		manaCost = config.getDouble("Spells.Offense.CalidumDolor.ManaCost");
		healthCost = config.getDouble("Spells.Offense.CalidumDolor.HealthCost");
		gravity = config.getDouble("Spells.Offense.CalidumDolor.Gravity");
		range = config.getDouble("Spells.Offense.CalidumDolor.Range");
		fire = config.getBoolean("Spells.Offense.CalidumDolor.Fire");
		fireChance = config.getInt("Spells.Offense.CalidumDolor.FireChance");
		damage = config.getDouble("Spells.Offense.CalidumDolor.Damage");
		speed = config.getDouble("Spells.Offense.CalidumDolor.SpeedFactor");
		fire = config.getBoolean("Spells.Offense.CalidumDolor.Fire");
		power = (int) (damage * 5);
	}

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (!super.enable(sbPlayer)) {
			return false;
		}
		sbPlayer.putSpellData(this, new CalidumDolorData(this));
		cooldownHandler.cooldown(sbPlayer, getCooldown());
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
		return "CalidumDolor";
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Have a coal ore in your hot bar to use this spell.",
				"Click to heat up the coal, and then shoot it forward." };
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onUpdate(UpdateEvent e) {
		for (SpellBoundPlayer sbp : sbPlayers) {
			CalidumDolorData data = (CalidumDolorData) sbp.getSpellData(this);
			if (data == null) {
				throw new RuntimeException("WTF EVERYTHING IS BROKEN");
			}
			Player player = sbp.getBukkitPlayer();
			switch (data.state) {
			case START:
				data.loc = player.getEyeLocation();
				data.origin = player.getLocation();
				playSound(data.origin, Sound.BLAZE_HIT, 0.5f, 2f);
				data.dir = player.getEyeLocation().getDirection();
				data.velocity = data.dir.multiply(speed);
				data.state = CalidumDolorState.SHOOT;
				break;
			case SHOOT:

				Location loc = data.loc;
				data.loc = loc.add(data.velocity);
				data.velocity = data.velocity.add(new Vector(0, gravity, 0).multiply(0.15f));

				ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.OBSIDIAN, (byte) 0), 0.2f, 0.2f, 0.2f, 0.04f, 11, data.loc, 257);
				ParticleEffect.FLAME.display(data.loc, 0.1f, 0.1f, 0.1f, 0.03f, 9);
				playSound(data.loc, Sound.BLAZE_BREATH, 0.4f, 100f);

				if (!sbp.getBukkitPlayer().isOnline()) {
					disable(sbp, false);
					return;
				}

				Block block = data.loc.getBlock();
				
				if (data.loc.distance(data.origin) > range) {
					disable(sbp, true);
					return;
				}

				if (MCUtil.isSolid(block) || block.isLiquid()) {
					disable(sbp, true);
					return;
				}
				
				List<Entity> affectedEntities = MCUtil.getEntities(data.loc, 2, sbp.getBukkitPlayer());
				
				if (affectedEntities.size() > 0) {
					List<LivingEntity> livingAE = MCUtil.getLivingEntitiesFromEntities(affectedEntities);
					LivingEntity[] livingAEArray = livingAE.toArray(new LivingEntity[livingAE.size()]);
					MCUtil.damageEntities(damage, sbp.getBukkitPlayer(), livingAEArray);
					if (CommonUtil.hasChance(fireChance)) {
						MCUtil.setFire(100, livingAEArray);
					}
					disable(sbp, true);
					return;
				}
				
				collideSpells(sbp, loc, range, data.uuid);
				break;
			default:
				break;
			}
		}
	}

}
