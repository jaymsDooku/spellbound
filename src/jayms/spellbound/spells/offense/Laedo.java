package jayms.spellbound.spells.offense;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import jayms.plugin.event.update.UpdateEvent;
import jayms.plugin.packet.ParticleEffect;
import jayms.plugin.system.description.Version;
import jayms.plugin.util.ColourableString;
import jayms.plugin.util.MCUtil;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.AbstractSpell;
import jayms.spellbound.spells.CommonSpellVariables;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.spells.data.CommonData;

public class Laedo extends AbstractSpell {
	
	private LaedoSpellVariables variables;
	
	private class LaedoSpellVariables extends CommonSpellVariables {
		
		public int slowIntensity;
		public long slowTime;
		
		public LaedoSpellVariables() {
			
		}
	}
	
	private class LaedoData extends CommonData {
		
		public LaedoData(Spell parent) {
			super(parent);
		}
	}
		
	public Laedo(SpellBoundPlugin running) {
		super(running);
		variables = new LaedoSpellVariables();
		FileConfiguration config = running.getConfiguration();
		cooldown = config.getLong("Spells.Offense.Laedo.Cooldown");
		manaCost = config.getDouble("Spells.Offense.Laedo.ManaCost");
		healthCost = config.getDouble("Spells.Offense.Laedo.HealthCost");
		variables.gravity = config.getDouble("Spells.Offense.Laedo.Gravity");
		variables.range = config.getDouble("Spells.Offense.Laedo.Range");
		variables.damage = config.getDouble("Spells.Offense.Laedo.Damage");
		variables.speed = config.getDouble("Spells.Offense.Laedo.Speed");
		variables.slowIntensity = config.getInt("Spells.Offense.Laedo.SlowIntensity");
		variables.slowTime = config.getLong("Spells.Offense.Laedo.SlowTime");
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
	public ColourableString getDisplayName() {
		return new ColourableString("Laedo") {

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
		return new ColourableString[]{};
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
	
	//private double u = -Math.PI/2;
	//private double v = 0;
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onUpdate(UpdateEvent e) {
		for (SpellBoundPlayer sbp : sbPlayers) {
			Player player = sbp.getBukkitPlayer();
			LaedoData data = (LaedoData) sbp.getSpellData(this);
			if (data == null) {
				sbp.putSpellData(this, new LaedoData(this));
				data = (LaedoData) sbp.getSpellData(this);
				data.loc = player.getEyeLocation();
				data.origin = player.getLocation();
				playSound(data.origin, Sound.FIREWORK_LAUNCH, 0.5f, 2f);
				data.dir = player.getEyeLocation().getDirection();
				data.velocity = new Vector();
			}

			double delta = extractDelta();

			variables.speedfactor = variables.speed * delta;

			data.velocity = data.velocity.add(data.dir.multiply(variables.speedfactor));

			Location loc = data.loc;
			data.loc = loc.add(data.velocity);
			data.velocity = applyGravity(data.velocity, variables.gravity, delta);
			
			Location tempLoc = loc.clone();
			
			World w = tempLoc.getWorld();
			Vector origin = tempLoc.toVector();
			Vector originDir = tempLoc.getDirection();
			
			double forward = 2;
			double lengths = 1.5;
			
			Vector pointA = new Vector();
			Vector pointB = new Vector();
			Vector pointC = new Vector();
			Vector forwardPoint = origin.add(originDir.multiply(forward));
			
			pointA = origin.add(new Vector(0, (Math.sqrt(3)/2) * lengths, 0));
			pointB = origin.add(new Vector(lengths/2, -(Math.sqrt(3)/2) * lengths, 0));
			pointC = origin.add(new Vector(0, 0, -lengths/2));
			
			line(w, pointA, pointB);
			line(w, pointB, pointC);
			line(w, pointA, pointC);
			line(w, forwardPoint, pointA);
			line(w, forwardPoint, pointB);
			line(w, forwardPoint, pointC);
			
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
		}
	}
	
	private void line(World w, Vector h, Vector t) {
		
		Vector here = h.clone();
		Vector there = t.clone();
		
		double dist = here.distance(there);
		
		double here_x = here.getX();
		double here_y = here.getY();
		double here_z = here.getZ();
		
		double there_x = there.getX();
		double there_y = there.getY();
		double there_z = there.getZ();
		
		double delta_x = there_x - here_x;
		double delta_y = there_y - here_y;
		double delta_z = there_z - here_z;
		
		double new_x = here_x;
		double new_y = here_y;
		double new_z = here_z;
		while (dist > 0) {
			ParticleEffect.FLAME.display(new Location(w, new_x, new_y, new_z), 0, 0, 0, 0, 1);
			new_x = new_x + delta_x;
			new_y = new_y + delta_y;
			new_z = new_z + delta_z;
		}
	}
}
