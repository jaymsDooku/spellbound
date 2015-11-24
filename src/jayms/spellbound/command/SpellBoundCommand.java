package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jayms.plugin.system.description.Version;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.util.Permissions;

public class SpellBoundCommand extends AbstractSpellBoundCommand {

	public SpellBoundCommand(SpellBoundPlugin running) {
		super("spellbound", running);
		setAlias(new String[] {"sb"});
		setDescription(new String[] {"SpellBound Main Command."});
		setFormat("[args]");
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_HELP)) {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions!");
			return false;
		}
		
		return super.onCommand(sender, cmd, label, args);
	}
}
