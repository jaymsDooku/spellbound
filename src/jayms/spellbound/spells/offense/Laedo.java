package jayms.spellbound.spells.offense;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import jayms.java.mcpe.common.Handler;
import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.collect.Tuple;
import jayms.java.mcpe.common.util.MCUtil;
import jayms.java.mcpe.common.util.ParticleEffect;
import jayms.java.mcpe.common.util.VectorUtils;
import jayms.java.mcpe.event.UpdateEvent;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.spells.collision.CollisionPriority;
import jayms.spellbound.spells.collision.CollisionResult;
import jayms.spellbound.spells.data.SpellData;
import jayms.spellbound.spells.data.TimeAffectedData;
import jayms.spellbound.spells.variables.CommonSpellVariables;

public class Laedo extends AbstractSpell {

	private LaedoSpellVariables variables;

	private class LaedoSpellVariables extends CommonSpellVariables {

		public long duration;
		
		public LaedoSpellVariables() {
		}
	}

	private class LaedoData extends TimeAffectedData {

		public int point;

		public LaedoData(UUID user, Spell parent) {
			super(user, parent);
		}
	}

	public Laedo() {
		variables = new LaedoSpellVariables();
		FileConfiguration config = Main.self.getYAMLFileMCExt().getFC();
		cooldown = config.getLong("Spells.Offense.Laedo.Cooldown");
		manaCost = config.getDouble("Spells.Offense.Laedo.ManaCost");
		healthCost = config.getDouble("Spells.Offense.Laedo.HealthCost");
		variables.gravity = config.getDouble("Spells.Offense.Laedo.Gravity");
		variables.range = config.getDouble("Spells.Offense.Laedo.Range");
		variables.damage = config.getDouble("Spells.Offense.Laedo.Damage");
		variables.speed = config.getDouble("Spells.Offense.Laedo.Speed");
		variables.duration = config.getLong("Spells.Offense.Laedo.Duration");
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

		sbPlayer.putSpellData(this, null);
		sbPlayers.remove(sbPlayer);
		return true;
	}

	@Override
	public String getUniqueName() {
		return "Laedo";
	}

	@Override
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&', "&4Laedo");
	}

	@Override
	public String[] getDescription() {
		return new String[] {};
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
			LaedoData data = (LaedoData) sbp.getSpellData(this);
			if (data == null) {
				sbp.putSpellData(this, new LaedoData(player.getUniqueId(), this));
				data = (LaedoData) sbp.getSpellData(this);
				data.loc = player.getEyeLocation();
				data.origin = player.getLocation();
				playSound(data.origin, Sound.FIREWORK_LAUNCH, 0.5f, 2f);
				data.dir = player.getEyeLocation().getDirection();
				data.velocity = new Vector();
			}
			
			if (data.timeData.has("paralyse")) {
				return;
			}

			double delta = extractDelta();

			variables.speedfactor = variables.speed * delta;

			data.velocity = data.velocity.add(data.dir.multiply(variables.speedfactor));
			data.velocity = applyGravity(data.velocity, variables.gravity, delta);
			
			Location loc = data.loc;
			data.loc = loc.add(data.velocity);

			final Location tempLoc = data.loc.clone();
			final Vector tempDir = data.dir.clone();
			
			for (int i = 0; i < 2; i++) {
				for (double d = -4.0D; d <= 0.0D; d += 0.1D) {
					if (data.origin.distance(data.loc) >= d) {
						Location l = tempLoc.clone().add(tempDir.clone().normalize().multiply(d));
						double r = d * -1.0D / 5.0D;
						if (r > 0.75D) {
							r = 0.75D;
						}
						Vector ov = VectorUtils.getOrthogonalVector(data.loc.getDirection(), data.point + 90 * i + d, r);
						Location pl = l.clone().add(ov);
						if (i == 0) {
							ParticleEffect.FIREWORKS_SPARK.display(pl, 0, 0, 0, 0, 1);
						}else if (i == 1) {
							ParticleEffect.SPELL_WITCH.display(pl, 0, 0, 0, 0, 1);
						}
					}
				}
			}
			data.point = data.point + 20;
			if (data.point >= 360) {
				data.point = 0;
			}

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
				//damageEntities(affectedEntities, variables.damage, sbp, null);
				data.affected = affectedEntities;
				for (Entity en : affectedEntities) {
					Location len = en.getLocation();
					int iter = 1;
					int point = 0;
					double radius = 0.9;
					for (double y = 0.2; y < 2; y+= 0.2) {
						iter++;
						if (point >= 360) {
							point = 0;
						}
						point+=20;
						ParticleEffect teffect = null;
						if (iter % 2 == 0) {
							teffect = ParticleEffect.FIREWORKS_SPARK; 
						}else {
							teffect = ParticleEffect.CRIT_MAGIC;
						}
						ParticleEffect effect = teffect;
						int finPoint = point;
						double finY = y;
						new BukkitRunnable() {
							
							int point = finPoint;
							double y = finY;
							
							@Override
							public void run() {
								for (int i = -180; i < 180; i+= 2) {
									double angle = i * 3.141592653589793D / 180.0D;
									double x = radius * Math.cos(angle + point);
									double z = radius * Math.sin(angle + point);
									Vector vec = new Vector(x, y, z);
									len.add(vec);
									effect.display(len, 0, 0, 0, 0, 1);
									len.subtract(vec);
								}
							}
						}.runTaskLater(Main.self, (long) iter);
					}
					point = 360;
					for (double y = 2; y > 0.2; y-= 0.2) {
						iter++;
						if (point <= 360) {
							point = 360;
						}
						point-=20;
						ParticleEffect teffect = null;
						if (iter % 2 == 0) {
							teffect = ParticleEffect.FIREWORKS_SPARK; 
						}else {
							teffect = ParticleEffect.CRIT_MAGIC;
						}
						ParticleEffect effect = teffect;
						int finPoint = point;
						double finY = y;
						new BukkitRunnable() {
							
							int point = finPoint;
							double y = finY;
							
							@Override
							public void run() {
								for (int i = -180; i < 180; i+= 2) {
									double angle = i * 3.141592653589793D / 180.0D;
									double x = radius * Math.cos(angle + point);
									double z = radius * Math.sin(angle + point);
									Vector vec = new Vector(x, y, z);
									len.add(vec);
									effect.display(len, 0, 0, 0, 0, 1);
									len.subtract(vec);
								}
							}
						}.runTaskLater(Main.self, (long) iter);
					}
				}
				if (!data.timeData.has("paralyse")) {
					data.timeData.put("paralyse", System.currentTimeMillis() + variables.duration);
				}
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		for (SpellBoundPlayer sbp : sbPlayers) {
			SpellData data = sbp.getSpellData(this);
			if (data != null) {
				LaedoData ldata = (LaedoData) data;
				List<LivingEntity> living = ldata.affected;
				if (living != null) {
					for (LivingEntity le : living) {
						if (p.getUniqueId().equals(le.getUniqueId())) {
							e.setCancelled(true);
							if (ldata.timeData.get("paralyse") < System.currentTimeMillis()) {
								disable(sbp, true);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public CollisionPriority getPriority() {
		return CollisionPriority.NORMAL;
	}

	@Override
	public double getCollisionRange() {
		return 1.8;
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

