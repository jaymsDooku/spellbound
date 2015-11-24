package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jayms.plugin.system.description.Version;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.util.Permissions;

public class SpellBoundBattleModeToggleCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundBattleModeToggleCommand(SpellBoundPlugin running) {
		super("battlemodetoggle", running);
		setAlias(new String[] {"bmt"});
		setDescription(new String[] {"SpellBound Battle Mode Toggle Command."});
		setFormat("[no args]");
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_BATTLEMODE)) {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions!");
			return true;
		}
		
		SpellBoundPlayer sbp = extractSBP(sender);
		
		if (sbp == null) {
			sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command!");
			return true;
		}
		
		if (args.length != 0) {
			sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments!");
			return false;
		}
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		
		if (!sbph.toggleBattleMode(sbp)) {
			sender.sendMessage(ChatColor.DARK_RED + "You've failed the requirements to toggle battlemode!");
		}
		return true;
	}
}
