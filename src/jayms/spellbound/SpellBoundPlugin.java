package jayms.spellbound;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jayms.plugin.event.EventDispatcher;
import jayms.plugin.event.update.Updater;
import jayms.plugin.system.ParentPlugin;
import jayms.spellbound.command.SpellBoundBattleModeToggleCommand;
import jayms.spellbound.command.SpellBoundBindCommand;
import jayms.spellbound.command.SpellBoundCommand;
import jayms.spellbound.command.SpellBoundGiveWandCommand;
import jayms.spellbound.items.wands.WandHandler;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.spells.offense.CalidumDolor;

public abstract class SpellBoundPlugin extends ParentPlugin {
	
	private EventDispatcher eventDispatcher;
	private Updater updater;
	private SpellBoundPlayerHandler sbph;
	private SpellHandler sh;
	private WandHandler wh;
	
	public SpellBoundPlugin() {
		super();
	}
	
	@Override
	public boolean enable() {
		if (!super.enable()) {
			return false;
		}
		initialize();
		registerSpells();
		wh.initialize();
		setUpConfiguration();
		Server server = getSelf().getServer();
		for (Player p : server.getOnlinePlayers()) {
			sbph.onPlayerJoin(new PlayerJoinEvent(p, null));
			wh.onPlayerJoin(new PlayerJoinEvent(p, null));
		}
		registerCommand("spellbound", new SpellBoundCommand(this)
				, new SpellBoundBindCommand(this)
				, new SpellBoundBattleModeToggleCommand(this)
				, new SpellBoundGiveWandCommand(this));
		return true;
	}
	
	@Override
	public boolean disable() {
		if (!super.disable()) {
			return false;
		}
		Server server = getSelf().getServer();
		for (Player p : server.getOnlinePlayers()) {
			sbph.onPlayerQuit(new PlayerQuitEvent(p, null));
			wh.onPlayerQuit(new PlayerQuitEvent(p, null));
		}
		shutdown();
		return true;
	}

	private void initialize() {
		System.out.println("Initializing...");
		eventDispatcher = new EventDispatcher(getSelf());
		updater = new Updater(eventDispatcher, getSelf());
		sbph = new SpellBoundPlayerHandler(this);
		sh = new SpellHandler(this);
		wh = new WandHandler(this);
		eventDispatcher.registerListener(sbph);
	}
	
	private void registerSpells() {
		sh.registerSpell(new CalidumDolor(this));
	}
	
	private void setUpConfiguration() {
		System.out.println("Setting up configuration...");
		FileConfiguration config = getConfiguration();
		
		config.addDefault("Settings.AutoSaveBinds", true);
		
		config.addDefault("SpellBoundPlayerDefaults.maxMana", 1000D);
		config.addDefault("SpellBoundPlayerDefaults.manaRegen.rate", 10D);
		config.addDefault("SpellBoundPlayerDefaults.manaRegen.rateTime", 5000L);
		config.addDefault("SpellBoundPlayerDefaults.mana", 1000D);
		
		config.addDefault("Spells.Offense.CalidumDolor.Damage", 2D);
		config.addDefault("Spells.Offense.CalidumDolor.Fire", true);
		config.addDefault("Spells.Offense.CalidumDolor.SpeedFactor", 1.2D);
		config.addDefault("Spells.Offense.CalidumDolor.Cooldown", 5000L);
		config.addDefault("Spells.Offense.CalidumDolor.ManaCost", 10D);
		config.addDefault("Spells.Offense.CalidumDolor.HealthCost", 0D);
		config.addDefault("Spells.Offense.CalidumDolor.Gravity", -0.25D);
		config.addDefault("Spells.Offense.CalidumDolor.Range", 150D);
		
		saveConfiguration();
	}
	
	private void shutdown() {
		System.out.println("Shutting down!");
		getDatabase().close();
	}

	public SpellBoundPlayerHandler getSpellBoundPlayerHandler() {
		return sbph;
	}
	
	public SpellHandler getSpellHandler() {
		return sh;
	}
	
	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}
	
	public WandHandler getWandHandler() {
		return wh;
	}
}
