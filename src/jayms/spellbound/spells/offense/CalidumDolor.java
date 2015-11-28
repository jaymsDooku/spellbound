package jayms.spellbound.spells.offense;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import jayms.plugin.event.update.UpdateEvent;
import jayms.plugin.packet.ParticleEffect;
import jayms.plugin.packet.ParticleEffect.BlockData;
import jayms.plugin.system.description.Version;
import jayms.plugin.util.ColourableString;
import jayms.plugin.util.CommonUtil;
import jayms.plugin.util.MCUtil;
import jayms.plugin.util.tuple.Tuple;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.CommonSpellVariables;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.spells.data.CommonData;

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

		public CalidumDolorData(Spell parent) {
			super(parent);
		}

	}

	public CalidumDolor(SpellBoundPlugin running) {
		super(running);
		variables = new CalidumDolorSpellVariables();
		FileConfiguration config = running.getConfiguration();
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
	public ColourableString getDisplayName() {
		return new ColourableString("CalidumDolor") {

			@Override
			public String applyColour(ChatColor... extras) {
				String result = "&4";
				String format = getFormatFromExtras(extras);
				if (!format.isEmpty()) {
					result += format;
				}
				result += toString();

				result = ChatColor.translateAlternateColorCodes('&', result);

				return result;
			}

		};
	}

	@Override
	public ColourableString[] getDescription() {
		return new ColourableString[] { new ColourableString("Have a coal ore in your hot bar to use this spell.") {

			@Override
			public String applyColour(ChatColor... extras) {
				return ChatColor.translateAlternateColorCodes('&', "&6" + toString());
			}

		}, new ColourableString("Click to heat up the coal, and then shoot it forward.") {

			@Override
			public String applyColour(ChatColor... extras) {
				return ChatColor.translateAlternateColorCodes('&', "&6" + toString());
			}
		} };
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
				sbp.putSpellData(this, new CalidumDolorData(this));
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

			Location loc = data.loc;
			data.loc = loc.add(data.velocity);
			data.velocity = applyGravity(data.velocity, variables.gravity, delta);

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

			List<Entity> affectedEntities = getAffectedEntities(data.loc, 2, sbp);

			if (affectedEntities.size() > 0) {
				Tuple<Boolean, Integer> fire = new Tuple<>(false, 100);
				if (CommonUtil.hasChance(fireChance)) {
					fire.setA(true);
				}
				damageEntities(affectedEntities, variables.damage, sbp, fire);
				disable(sbp, true);
				return;
			}

			collideSpells(sbp, loc, variables.range, data.uuid);
		}
	}

}
