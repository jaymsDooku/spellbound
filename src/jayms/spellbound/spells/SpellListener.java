package jayms.spellbound.spells;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import jayms.plugin.packet.chat.ChatHandler;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;

public class SpellListener implements Listener {

	private SpellBoundPlugin running;
	
	public SpellListener(SpellBoundPlugin running) {
		this.running = running;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onClick(PlayerAnimationEvent e) {
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		SpellBoundPlayer sbp = sbph.getSpellBoundPlayer(e.getPlayer());
		
		ItemStack it = sbp.getBukkitPlayer().getItemInHand();
		
		/*Wand wand = new JihadStick(running);
		sbp.getBukkitPlayer().setItemInHand(wand.getItemStack());*/
		
		if (it != null) {
			/*net.minecraft.server.v1_8_R3.ItemStack nmsIt = CraftItemStack.asNMSCopy(it);
			NBTTagCompound root = nmsIt.hasTag() ? nmsIt.getTag() : new NBTTagCompound();
			root.setString("Nigga", "I am a joyful nigga");
			nmsIt.setTag(root);
			it = CraftItemStack.asCraftMirror(nmsIt);
			sbp.getBukkitPlayer().setItemInHand(it);*/
			/*System.out.println(CraftItemStack.asNMSCopy(sbp.getBukkitPlayer().getItemInHand()).getTag().getString("Nigga"));*/
		}
		
		if (!sbp.isBattleMode()) {
			if (it != null) {
				if (!sbp.assureSelectedWand()) {
					return;
				}
				Wand w = sbp.getSelectedWand();
				if (w.equalsItem(it)) {
					sbph.toggleBattleMode(sbp);
				}
			}
			return;
		}
		
		int slotInside = sbp.getSlotInside();
		int currentSlot = sbp.getBukkitPlayer().getInventory().getHeldItemSlot()+1;
		
		if (sbp.isBattleMode()) {
			if (slotInside == -1) {
				sbp.setSlotInside(currentSlot);
				return;
			}
			Spell sp = sbp.getBinds().getSpell(slotInside, currentSlot);
			if (sp == null) {
				return;
			}
			if (sp.isShiftClick() && !sbp.getBukkitPlayer().isSneaking()) {
				return;
			}
			sp.enable(sbp);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSlotChange(PlayerItemHeldEvent e) {
		
		SpellBoundPlayer sbp = running.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		
		if (!sbp.isBattleMode()) {
			return;
		}
		
		int slotInside = sbp.getSlotInside();
		
		if (slotInside == -1) {
			return;
		}
		
		Spell sp = sbp.getBinds().getSpell(slotInside, e.getNewSlot()+1);
		
		if (sp == null) {
			return;
		}
		
		String spTs = sp.getDisplayName();
		
		ChatHandler.sendActionBar(spTs, sbp.getBukkitPlayer());
	}
}
