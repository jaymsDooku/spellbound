package jayms.spellbound;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import jayms.java.mcpe.command.CommandCenter;
import jayms.java.mcpe.common.util.ServerUtil;
import jayms.java.mcpe.event.Updater;
import jayms.java.mcpe.log.LogWrapper;
import jayms.java.mcpe.plugin.AbstractJavaPlugin;
import jayms.java.mcpe.yaml.YAMLFileMCExt;
import jayms.spellbound.command.SpellBoundCommand;
import jayms.spellbound.items.wands.WandHandler;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.spells.defense.Eludere;
import jayms.spellbound.spells.offense.CalidumDolor;
import jayms.spellbound.spells.offense.Laedo;

public class Main extends AbstractJavaPlugin {
	
	public static LogWrapper log = new LogWrapper(Logger.getLogger("Minecraft")).prefix("Spellbound");
	public static Main self;
	
	private SpellBoundPlayerHandler sbph;
	private SpellHandler sh;
	private WandHandler wh;
	private Updater updater;
	
	@Override
	public void onEnable() {
		log.info("Enabling Spellbound...");
		self = this;
		initDBInConfig();
		initDatabase();
		sbph = new SpellBoundPlayerHandler();
		sh = new SpellHandler();
		wh = new WandHandler();
		updater = new Updater(getEventDispatcher(), this);
		setUpConfiguration();
		registerSpells();
		CommandCenter.instance.registerCommand(new SpellBoundCommand(), true);
		ServerUtil.joinPlayers(getEventDispatcher());
		log.info("Finished Enabling Spellbound!");
	}
	
	@Override
	public void onDisable() {
		log.info("Disabling Spellbound...");
		getYAMLFileMCExt().save();
		ServerUtil.quitPlayers(getEventDispatcher());
		closeDB();
		log.info("Finished Disabling Spellbound!");
	}
	
	private void setUpConfiguration() {
		log.info("Setting up configuration...");
		YAMLFileMCExt yfme = getYAMLFileMCExt();
		FileConfiguration config = yfme.getFC();
		
		config.addDefault("Settings.AutoSaveBinds", true);
		config.addDefault("Settings.AutoSaveAttributes", true);
		config.addDefault("Settings.StopManaRegenIfHit", true);
		config.addDefault("Settings.ReturnRegenAfterTime", 1000L);
		config.addDefault("Settings.StopManaRegenIfCast", true);
		config.addDefault("Settings.ReturnRegenAfterCast", 3000L);
		
		config.addDefault("SpellBoundPlayerDefaults.maxMana", 100D);
		config.addDefault("SpellBoundPlayerDefaults.manaRegen.rate", 10D);
		config.addDefault("SpellBoundPlayerDefaults.manaRegen.rateTime", 1000L);
		config.addDefault("SpellBoundPlayerDefaults.mana", 100D);
		
		config.addDefault("Spells.Offense.CalidumDolor.Damage", 4D);
		config.addDefault("Spells.Offense.CalidumDolor.Fire", true);
		config.addDefault("Spells.Offense.CalidumDolor.FireChance", 25);
		config.addDefault("Spells.Offense.CalidumDolor.FireTicks", 100);
		config.addDefault("Spells.Offense.CalidumDolor.Speed", 3.3D);
		config.addDefault("Spells.Offense.CalidumDolor.Cooldown", 2000L);
		config.addDefault("Spells.Offense.CalidumDolor.ManaCost", 10D);
		config.addDefault("Spells.Offense.CalidumDolor.HealthCost", 0D);
		config.addDefault("Spells.Offense.CalidumDolor.Gravity", -0.21D);
		config.addDefault("Spells.Offense.CalidumDolor.Range", 150D);
		
		config.addDefault("Spells.Offense.Laedo.Damage", 1D);
		config.addDefault("Spells.Offense.Laedo.Speed", 3.3D);
		config.addDefault("Spells.Offense.Laedo.Cooldown", 3000L);
		config.addDefault("Spells.Offense.Laedo.ManaCost", 10D);
		config.addDefault("Spells.Offense.Laedo.HealthCost", 0D);
		config.addDefault("Spells.Offense.Laedo.Gravity", -0.21D);
		config.addDefault("Spells.Offense.Laedo.Range", 150D);
		config.addDefault("Spells.Offense.Laedo.Duration", 5000L);
		
		config.addDefault("Spells.Defense.Eludere.Damage", 40D);
		config.addDefault("Spells.Defense.Eludere.Cooldown", 2500L);
		config.addDefault("Spells.Defense.Eludere.ManaCost", 10D);
		config.addDefault("Spells.Defense.Eludere.HealthCost", 0D);
		config.addDefault("Spells.Defense.Eludere.Range", 2D);
		config.addDefault("Spells.Defense.Eludere.Duration", 2000L);
		
		config.options().copyDefaults(true);
		yfme.save();
		log.info("Finished setting up configuration!");
	}
	
	private void registerSpells() {
		sh.registerSpell(new CalidumDolor());
		sh.registerSpell(new Laedo());
		sh.registerSpell(new Eludere());
	}
	
	public WandHandler getWandHandler() {
		return wh;
	}
	
	public SpellBoundPlayerHandler getSpellBoundPlayerHandler() {
		return sbph;
	}
	
	public SpellHandler getSpellHandler() {
		return sh;
	}
	
	public Updater getUpdater() {
		return updater;
	}
}
