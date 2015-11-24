package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

import jayms.plugin.system.description.Version;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.util.Permissions;

public class SpellBoundGiveWandCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundGiveWandCommand(SpellBoundPlugin running) {
		super("givewand", running);
		setAlias(new String[] {"gw"});
		setDescription(new String[] {"SpellBound Give Wand Command."});
		setFormat("[name]");
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_GIVEWAND)) {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions!");
			return true;
		}
		
		SpellBoundPlayer sbp = extractSBP(sender);
		
		if (sbp == null) {
			sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command!");
			return true;
		}
		
		if (args.length != 1) {
			sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments!");
			return false;
		}
		
		Inventory inv = sbp.getBukkitPlayer().getInventory();
		if (inv.firstEmpty() == -1) {
			sender.sendMessage(ChatColor.DARK_RED + "Your inventory is full!");
			return true;
		}
		
		inv.addItem(new Wand(running, args[0]).getItemStack());
		return true;
	}
}
