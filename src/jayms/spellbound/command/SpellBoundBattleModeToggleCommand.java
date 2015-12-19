package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jayms.java.mcpe.common.Version;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.util.Permissions;

public class SpellBoundBattleModeToggleCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundBattleModeToggleCommand() {
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public void onCommand(Player sender, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_BATTLEMODE)) {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions!");
			return;
		}
		
		SpellBoundPlayer sbp = extractSBP(sender);
		
		if (sbp == null) {
			sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command!");
			return;
		}
		
		if (args.length != 0) {
			sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments!");
			return;
		}
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		
		if (!sbph.toggleBattleMode(sbp)) {
			sender.sendMessage(ChatColor.DARK_RED + "You've failed the requirements to toggle battlemode!");
		}
		return;
	}

	@Override
	public String[] getAlias() {
		return new String[] {
				"bmt"
		};
	}

	@Override
	public String[] getDescription() {
		return new String[] {
				"SpellBound Battle Mode Toggle Command."
		};
	}

	@Override
	public String getFormat() {
		return "[no args]";
	}

	@Override
	public String[] getHelp() {
		return new String[] {
		};
	}

	@Override
	public String getName() {
		return "battlemodetoggle";
	}

	@Override
	public int requiredArgs() {
		return 0;
	}
}
