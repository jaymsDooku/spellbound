package jayms.spellbound.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jayms.java.mcpe.command.ICommand;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;

public abstract class AbstractSpellBoundCommand implements ICommand {
	
	protected final SpellBoundPlayer extractSBP(CommandSender sender) {
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			return sbph.getSpellBoundPlayer(player);
		}
		return null;
	}
}
