package jayms.spellbound;

import org.bukkit.plugin.java.JavaPlugin;

import jayms.plugin.system.ParentPlugin;

public class Main extends JavaPlugin {
	
	private ParentPlugin spellbound;
	
	@Override
	public void onEnable() {
		initialize();
		spellbound.enable();
	}
	
	@Override
	public void onDisable() {
		spellbound.disable();
	}
	
	private void initialize() {
		spellbound = new SpellBoundPlugin() {

			@Override
			public JavaPlugin getSelf() {
				return Main.this;
			}
			
		};
	}
	
}
