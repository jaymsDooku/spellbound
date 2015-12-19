package jayms.spellbound.spells.defense;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import jayms.java.mcpe.common.Handler;
import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.collect.Tuple;
import jayms.java.mcpe.common.util.ParticleEffect;
import jayms.java.mcpe.common.util.ParticleEffect.BlockData;
import jayms.java.mcpe.common.util.ParticleEffect.OrdinaryColor;
import jayms.java.mcpe.common.util.VectorUtils;
import jayms.java.mcpe.event.UpdateEvent;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.spells.collision.CollisionPriority;
import jayms.spellbound.spells.collision.CollisionResult;
import jayms.spellbound.spells.data.TimeData;
import jayms.spellbound.spells.variables.CommonSpellVariables;

public class Eludere extends AbstractSpell {

	private EludereSpellVariables variables;

	private class EludereSpellVariables extends CommonSpellVariables {

		public long duration;
		public int xR = 7/10;
		public int zR = 1;
		
		public EludereSpellVariables() {
		}
	}

	private class EludereData extends TimeData {

		public EludereData(UUID user, Spell parent) {
			super(user, parent);
		}
	}

	public Eludere() {
		variables = new EludereSpellVariables();
		FileConfiguration config = Main.self.getYAMLFileMCExt().getFC();
		cooldown = config.getLong("Spells.Defense.Eludere.Cooldown");
		manaCost = config.getDouble("Spells.Defense.Eludere.ManaCost");
		healthCost = config.getDouble("Spells.Defense.Eludere.HealthCost");
		variables.range = config.getDouble("Spells.Defense.Eludere.Range");
		variables.damage = config.getDouble("Spells.Defense.Eludere.Damage");
		variables.duration = config.getLong("Spells.Defense.Eludere.Duration");
		variables.speed = 0;
		power = toPower(variables.damage);
	}

	@Override
	public boolean enable(SpellBoundPlayer sbPlayer) {
		if (!super.enable(sbPlayer)) {
			return false;
		}
		returnToMainSlots(sbPlayer);
		return sbPlayers.add(sbPlayer);
	}

	@Override
	public boolean disable(SpellBoundPlayer sbPlayer, boolean effects) {
		if (!hasEnabled(sbPlayer)) {
			return false;
		}
		
		if (effects) {
			EludereData data = (EludereData) sbPlayer.getSpellData(this);
			Location loc = data.loc;
			Location floc = loc.add(0.0D, 0.4D, 0.0D);
			ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.STAINED_GLASS_PANE, (byte) 11), 0.6F, 0.6F, 0.6F, 5F, 50, floc, 257);
			ParticleEffect.SMOKE_LARGE.display(floc, 0.5F, 0.5F, 0.5F, 0.001F, 10);
		}
		
		sbPlayer.putSpellData(this, null);
		sbPlayers.remove(sbPlayer);
		return true;
	}

	@Override
	public String getUniqueName() {
		return "Eludere";
	}

	@Override
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&', "&1Eludere");
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
		return ChatColor.AQUA;
	}

	@Override
	public SpellType getType() {
		return SpellType.DEFENSE;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onUpdate(UpdateEvent e) {
		for (SpellBoundPlayer sbp : sbPlayers) {
			Player player = sbp.getBukkitPlayer();
			EludereData data = (EludereData) sbp.getSpellData(this);
			if (data == null) {
				sbp.putSpellData(this, new EludereData(player.getUniqueId(), this));
				data = (EludereData) sbp.getSpellData(this);
				data.origin = player.getLocation();
				data.loc = reCalcLoc(player);
				data.velocity = new Vector();
				data.put("expire", System.currentTimeMillis() + variables.duration);
			}
			
			data.loc = reCalcLoc(player);
			Location loc = data.loc;
			for (double r = 0; r < 1.5; r+= 0.3) {
				for (double i = 0; i < 360; i += 2.0D) {
					double x = Math.cos(i) * (r*0.7);
					double z = Math.sin(i) * (r*1);
					Vector v = new Vector(x, 0, z);
					VectorUtils.rotateAroundAxisX(v, (loc.getPitch() + 90.0F) * 0.017453292F);
					VectorUtils.rotateAroundAxisY(v, (-loc.getYaw() * 0.017453292F));
					loc.add(v);
					//ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.STAINED_GLASS_PANE, (byte) 11), 0F, 0F, 0F, 0F, 1, loc, 257);
					ParticleEffect.REDSTONE.display(new OrdinaryColor(162, 214, 250), loc, 257);
					loc.subtract(v);
				}
			}
			
			long toExpire = data.get("expire");
			
			if (System.currentTimeMillis() > toExpire) {
				applyCooldown(sbp);
				disable(sbp, true);
				return;
			}
		}
	}
	
	private Location reCalcLoc(Player player) {
		return player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(variables.range));
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
				case SUCCESS:
					refundManaCost(sbp);
					disable(sbp, true);
					break;
				default:
					break;
				}
			}
			
		};
	}

	@Override
	public CollisionPriority getPriority() {
		return CollisionPriority.HIGHEST;
	}

	@Override
	public double getCollisionRange() {
		return 2;
	}

}
