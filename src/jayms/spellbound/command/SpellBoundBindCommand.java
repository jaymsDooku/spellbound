package jayms.spellbound.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jayms.plugin.system.description.Version;
import jayms.plugin.util.CommonUtil;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.util.Permissions;

public class SpellBoundBindCommand extends AbstractSpellBoundCommand {
	
	public SpellBoundBindCommand(SpellBoundPlugin running) {
		super("bind", running);
		setAlias(new String[] {"b"});
		setDescription(new String[] {"SpellBound Bind Command."});
		setFormat("[spell_unique_name] [slot] [subSlot]");
	}

	@Override
	public Version getVersion() {
		return new Version(1, 0, 0);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission(Permissions.SPELLBOUND_COMMAND_BIND)) {
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
		
		String sun = args[0];
		if (!CommonUtil.isInteger(args[1])) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "Slot needs to be an integer!");
			return false;
		}
		int slot = Integer.parseInt(args[1]);
		if (!CommonUtil.isInteger(args[2])) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "SubSlot needs to be an integer!");
			return false;
		}
		int subSlot = Integer.parseInt(args[2]);
		
		SpellHandler sh = running.getSpellHandler();
		Spell sp = sh.getSpell(sun);
		if (sp == null) {
			sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "That unique spell name doesn't match to a spell!");
		}
		sbp.getBinds().bind(slot, subSlot, sp, true);
		sbp.getBukkitPlayer().sendMessage(ChatColor.DARK_RED + "You have bound: " + ChatColor.RED + sp.getDisplayName() + ChatColor.DARK_RED + " to Slot: " + ChatColor.RED + slot + ChatColor.DARK_RED + " SubSlot: " + ChatColor.RED + subSlot);
		return true;
	}
}
