package jayms.spellbound.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jayms.plugin.command.AbstractCommand;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;

public abstract class AbstractSpellBoundCommand extends AbstractCommand {

	protected SpellBoundPlugin running;
	
	protected AbstractSpellBoundCommand(String name, SpellBoundPlugin running) {
		super(name);
		this.running = running;
	}
	
	protected final SpellBoundPlayer extractSBP(CommandSender sender) {
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			return sbph.getSpellBoundPlayer(player);
		}
		return null;
	}
}
