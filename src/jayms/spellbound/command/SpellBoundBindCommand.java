package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jayms.java.mcpe.common.Version;
import jayms.java.mcpe.common.util.NumberUtil;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.util.Permissions;

public class SpellBoundBindCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundBindCommand() {
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public void onCommand(Player sender, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_BIND)) {
			sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permissions!");
			return;
		}
		
		SpellBoundPlayer sbp = extractSBP(sender);
		
		if (sbp == null) {
			sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command!");
			return;
		}
		
		if (args.length != 3) {
			sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments!");
			return;
		}
		
		String sun = args[0];
		if (!NumberUtil.isInteger(args[1])) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "Slot needs to be an integer!");
			return;
		}
		int slot = Integer.parseInt(args[1]);
		if (!NumberUtil.isInteger(args[2])) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "SubSlot needs to be an integer!");
			return;
		}
		int subSlot = Integer.parseInt(args[2]);
		
		SpellHandler sh = Main.self.getSpellHandler();
		Spell sp = sh.getSpell(sun);
		if (sp == null) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "That unique spell name doesn't match to a spell!");
		}
		sbp.getBinds().bind(slot, subSlot, sp, true);
		sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "You have bound: " + ChatColor.RED + sp.getDisplayName() + ChatColor.DARK_RED + " to Slot: " + ChatColor.RED + slot + ChatColor.DARK_RED + " SubSlot: " + ChatColor.RED + subSlot);
		return;
	}

	@Override
	public String[] getAlias() {
		return new String[] {"b"};
	}

	@Override
	public String[] getDescription() {
		return new String[] {"SpellBound Bind Command."};
	}

	@Override
	public String getFormat() {
		return "[spell_unique_name] [slot] [subSlot]";
	}

	@Override
	public String[] getHelp() {
		return new String[]{
				
		};
	}

	@Override
	public String getName() {
		return "bind";
	}

	@Override
	public int requiredArgs() {
		return 3;
	}
}
