package jayms.spellbound.spells;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import jayms.plugin.event.update.UpdateEvent;
import jayms.plugin.packet.chat.ChatHandler;
import jayms.plugin.util.MCUtil;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.event.AfterManaChangeEvent;
import jayms.spellbound.event.CastSpellEvent;
import jayms.spellbound.event.ManaChangeEvent;
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
			Hologram hologram = HologramAPI.createHologram(sbp.getBukkitPlayer().getEyeLocation().add(0, 1, 0), sp.getDisplayName() + " " + sp.getPower() + "p");
			hologram.spawn();
			new BukkitRunnable() {

				@Override
				public void run() {
					hologram.despawn();
					sp.enable(sbp);
				}
			}.runTaskLater(running.getSelf(), 4L);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSlotChangeSpellDisplay(PlayerItemHeldEvent e) {
		
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
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSlotChangeWandSwitch(PlayerItemHeldEvent e) {
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		SpellBoundPlayer sbp = sbph.getSpellBoundPlayer(e.getPlayer());
		
		int slot = e.getNewSlot();
		
		if (sbp.isBattleMode()) {
			Inventory inv = sbp.getBukkitPlayer().getInventory();
			inv.clear(e.getPreviousSlot());
			inv.setItem(slot, sbp.getSelectedWand().getItemStack());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemDrop(PlayerDropItemEvent e) {
		
		if (e.isCancelled()) {
			return;
		}
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		SpellBoundPlayer sbp = sbph.getSpellBoundPlayer(e.getPlayer());
		
		if (sbp.isBattleMode()) {
			if (!sbp.assureSelectedWand()) {
				return;
			}
			e.setCancelled(true);
			
			int insideSlot = sbp.getSlotInside();
			
			if (insideSlot == -1) {
				PlayerInventory inv = sbp.getBukkitPlayer().getInventory();
				sbph.toggleBattleMode(sbp);
				new BukkitRunnable() {
					
					@Override
					public void run() {
						MCUtil.clear(inv, inv.getHeldItemSlot(), 1);
						sbp.getBukkitPlayer().updateInventory();
					}
				}.runTaskLater(running.getSelf(), 1L);
			}else {
				sbp.setSlotInside(-1);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
	public void onManaChange(ManaChangeEvent e) {
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onAfterManaChange(AfterManaChangeEvent e) {
		
		SpellBoundPlayer sbp = e.getSpellBoundPlayer();
		
		if (sbp.isBattleMode()) {
			sbp.showMana();
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onUpdate(UpdateEvent e) {
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		
		for (SpellBoundPlayer sbp : sbph.getCachedPlayers()) {
			sbp.regenerateMana();
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			
			SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
			
			Player player = (Player) e.getEntity();
			
			SpellBoundPlayer sbp = sbph.getSpellBoundPlayer(player);
			
			sbp.gotHit();
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSpellCast(CastSpellEvent e) {
		
		SpellBoundPlayer sbp = e.getSpellBoundPlayer();
		
		sbp.castedSpell();
	}
}
