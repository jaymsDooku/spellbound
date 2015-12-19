package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.util.PlayerUtil;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.util.Permissions;

public class SpellBoundGiveWandCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundGiveWandCommand() {
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public void onCommand(Player sender, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_GIVEWAND)) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "You don't have enough permissions!");
			return;
		}
		
		SpellBoundPlayer sbp = extractSBP(sender);
		
		if (sbp == null) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "You cannot run this command!");
			return;
		}
		
		if (args.length != 1) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "Not enough arguments!");
			return;
		}
		
		Inventory inv = sbp.getBukkitPlayer().getInventory();
		if (inv.firstEmpty() == -1) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "Your inventory is full!");
			return;
		}
		
		inv.addItem(new Wand(args[0]).getItemStack());
	}

	@Override
	public String[] getAlias() {
		return new String[] {
				"gw"
		};
	}

	@Override
	public String[] getDescription() {
		return new String[] {
				"SpellBound Give Wand Command."
		};
	}

	@Override
	public String getFormat() {
		return "[name]";
	}

	@Override
	public String[] getHelp() {
		return new String[] {
				
		};
	}

	@Override
	public String getName() {
		return "givewand";
	}

	@Override
	public int requiredArgs() {
		return 1;
	}
}
