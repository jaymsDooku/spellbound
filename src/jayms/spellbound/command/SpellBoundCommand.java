package jayms.spellbound.command;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jayms.java.mcpe.command.CommandCenter;
import jayms.java.mcpe.command.ICommand;
import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.collect.ArrayUtil;
import jayms.java.mcpe.common.util.PlayerUtil;
import jayms.java.mcpe.message.MessageConstants;
import jayms.spellbound.util.Permissions;

public class SpellBoundCommand extends AbstractSpellBoundCommand {

	private ArrayList<ICommand> commands = new ArrayList<>();
	
	public SpellBoundCommand() {
		commands.add(new SpellBoundBattleModeToggleCommand());
		commands.add(new SpellBoundBindCommand());
		commands.add(new SpellBoundGiveWandCommand());
		commands.add(new SpellBoundSetWandPercentCommand());
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public void onCommand(Player sender, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_HELP)) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "You don't have enough permissions!");
			return;
		}
		
		if (args.length == 0) {
			CommandCenter.showHelp(commands, sender);
			return;
		}
		
		String command = args[0];
		
		String[] commandArgs = ArrayUtil.remove(args, String.class, 0);
		ICommand iCommand = CommandCenter.findCommand(command, commands);
		
		if (iCommand == null) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + MessageConstants.COMMAND_NOT_EXIST);
			return;	
		}
		
		iCommand.onCommand(sender, commandArgs);
	}

	@Override
	public String[] getAlias() {
		return new String[] {
				"sb"
		};
	}

	@Override
	public String[] getDescription() {
		return new String[] {
			"SpellBound Main Command."
		};
	}

	@Override
	public String getFormat() {
		return "[args]";
	}

	@Override
	public String[] getHelp() {
		return new String[] {
				
		};
	}

	@Override
	public String getName() {
		return "spellbound";
	}

	@Override
	public int requiredArgs() {
		return -1;
	}
}
