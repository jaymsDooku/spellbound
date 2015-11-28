package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import jayms.plugin.system.description.Version;
import jayms.plugin.util.CommonUtil;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.util.Permissions;

public class SpellBoundSetWandPercentCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundSetWandPercentCommand(SpellBoundPlugin running) {
		super("setwandpercent", running);
		setAlias(new String[] {"swp"});
		setDescription(new String[] {"SpellBound Set Wand Percentage Command."});
		setFormat("[spell-type] [percent-type] [percent]");
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
		
		if (args.length != 3) {
			sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments!");
			return false;
		}
		
		if (!CommonUtil.isInteger(args[2])) {
			sender.sendMessage(ChatColor.DARK_RED + "Percentage must be a number!");
			return false;
		}
		
		int percent = Integer.parseInt(args[2]);
		
		ItemStack it = sbp.getBukkitPlayer().getInventory().getItemInHand();
		if (it == null || !Wand.isWand(it)) {
			sender.sendMessage(ChatColor.DARK_RED + "You must be holding your wand!");
			return true;
		}
		
		if (!SpellType.isShortenedName(args[0])) {
			sender.sendMessage(ChatColor.DARK_RED + "You must specify the spell type!");
			return false;
		}
		
		SpellType type = SpellType.getFromShortened(args[0]);
		
		Wand w = running.getWandHandler().getWand(it);
		
		switch (args[1]) {
		case "mana":
			w.setManaPercentage(type, percent);
			break;
		case "health":
			w.setHealthPercentage(type, percent);
			break;
		case "damage":
			w.setDamagePercentage(type, percent);
			break;
		case "cooldown":
			w.setCooldownPercentage(type, percent);
			break;
		case "backfire":
			w.setBackfirePercentage(type, percent);
			break;
		case "knockback":
			w.setKnockbackPercentage(type, percent);
			break;
		default:
			sender.sendMessage(ChatColor.DARK_RED + "In-valid type!");
			return false;
		}
		
		sbp.getBukkitPlayer().setItemInHand(w.getItemStack());
		return true;
	}
}
