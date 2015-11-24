package jayms.spellbound.spells.offense;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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
import jayms.plugin.util.MCUtil;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.SpellData;

public class CalidumDolor extends AbstractSpell {

	private enum CalidumDolorState {
		START, SHOOT;
	}

	private static class CalidumDolorData implements SpellData {

		CalidumDolorState state = CalidumDolorState.START;
		Location loc;
		Location origin;
		Vector dir;
		Vector velocity = new Vector(0, 2, 0);

		double damage;
		double speed;
		boolean fire;
		double gravity;
		double range;

		public CalidumDolorData(SpellBoundPlugin running) {
			FileConfiguration config = running.getConfiguration();
			damage = config.getDouble("Spells.Offense.CalidumDolor.Damage");
			speed = config.getDouble("Spells.Offense.CalidumDolor.SpeedFactor");
			fire = config.getBoolean("Spells.Offense.CalidumDolor.Fire");
			gravity = config.getDouble("Spells.Offense.CalidumDolor.Gravity");
			range = config.getDouble("Spells.Offense.CalidumDolor.Range");
		}

	}

	public CalidumDolor(SpellBoundPlugin running) {
		super(running);
		FileConfiguration config = running.getConfiguration();
		cooldown = config.getLong("Spells.Offense.CalidumDolor.Cooldown");
		manaCost = config.getDouble("Spells.Offense.CalidumDolor.ManaCost");
		healthCost = config.getDouble("Spells.Offense.CalidumDolor.HealthCost");
	}

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (!super.enable(sbPlayer)) {
			return false;
		}
		sbPlayer.putSpellData(this, new CalidumDolorData(running));
		return sbPlayers.add(sbPlayer);
	}

	@Override
	public boolean disable(SpellBoundPlayer sbPlayer, boolean cooldown) {
		if (!hasEnabled(sbPlayer)) {
			return false;
		}
		sbPlayer.putSpellData(this, null);
		sbPlayers.remove(sbPlayer);

		if (cooldown) {
			cooldownHandler.cooldown(sbPlayer, getCooldown());
		}
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
				data.dir = player.getEyeLocation().getDirection();
				data.velocity = data.dir.multiply(data.speed);
				data.state = CalidumDolorState.SHOOT;
				break;
			case SHOOT:

				Location loc = data.loc;
				data.loc = loc.add(data.velocity);
				data.velocity = data.velocity.add(new Vector(0, data.gravity, 0).multiply(0.15f));

				//ParticleEffect.SMOKE_NORMAL.display(data.loc, 0.05f, 0.05f, 0.05f, 0.01f, 11);
				ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.OBSIDIAN, (byte) 0), 0.2f, 0.2f, 0.2f, 0.04f, 11, data.loc, 257);
				ParticleEffect.FLAME.display(data.loc, 0.1f, 0.1f, 0.1f, 0.06f, 9);

				if (!sbp.getBukkitPlayer().isOnline()) {
					disable(sbp, false);
					return;
				}

				Block block = data.loc.getBlock();
				
				if (data.loc.distance(data.origin) > data.range) {
					disable(sbp, true);
					return;
				}

				if (block.getType() != Material.AIR) {
					disable(sbp, true);
					return;
				}
				
				List<Entity> affectedEntities = MCUtil.getEntities(data.loc, 2, sbp.getBukkitPlayer());
				
				if (affectedEntities.size() > 0) {
					List<LivingEntity> livingAE = MCUtil.getLivingEntitiesFromEntities(affectedEntities);
					MCUtil.damageEntities(data.damage, sbp.getBukkitPlayer(), livingAE.toArray(new LivingEntity[livingAE.size()]));
					disable(sbp, true);
					return;
				}
				break;
			default:
				break;
			}
		}
	}

}
