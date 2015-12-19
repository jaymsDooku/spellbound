package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.util.NumberUtil;
import jayms.java.mcpe.common.util.PlayerUtil;
import jayms.spellbound.Main;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.SpellType;
import jayms.spellbound.util.Permissions;

public class SpellBoundSetWandPercentCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundSetWandPercentCommand() {
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
		
		if (args.length != 3) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "Not enough arguments!");
			return;
		}
		
		if (!NumberUtil.isInteger(args[2])) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "Percentage must be a number!");
			return;
		}
		
		int percent = Integer.parseInt(args[2]);
		
		ItemStack it = sbp.getBukkitPlayer().getInventory().getItemInHand();
		if (it == null || !Wand.isWand(it)) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "You must be holding your wand!");
			return;
		}
		
		if (!SpellType.isShortenedName(args[0])) {
			PlayerUtil.message(sender, ChatColor.DARK_RED + "You must specify the spell type!");
			return;
		}
		
		SpellType type = SpellType.getFromShortened(args[0]);
		
		Wand w = Main.self.getWandHandler().getWand(it);
		
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
			PlayerUtil.message(sender, ChatColor.DARK_RED + "In-valid type!");
			return;
		}
		
		sbp.getBukkitPlayer().setItemInHand(w.getItemStack());
	}

	@Override
	public String[] getDescription() {
		return new String[] {
				"SpellBound Set Wand Percentage Command."
		};
	}

	@Override
	public String getFormat() {
		return "[spell-type] [percent-type] [percent]";
	}

	@Override
	public String[] getHelp() {
		return new String[]{};
	}

	@Override
	public String getName() {
		return "setwand";
	}

	@Override
	public int requiredArgs() {
		return 3;
	}

	@Override
	public String[] getAlias() {
		return new String[] {
				"swp"
		};
	}
}
