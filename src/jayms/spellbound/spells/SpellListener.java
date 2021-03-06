package jayms.spellbound.spells;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
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
import jayms.java.mcpe.common.CooldownChangeEvent;
import jayms.java.mcpe.common.CooldownHandler;
import jayms.java.mcpe.common.util.MCUtil;
import jayms.java.mcpe.common.util.PacketUtil;
import jayms.java.mcpe.event.UpdateEvent;
import jayms.spellbound.Main;
import jayms.spellbound.event.AfterManaChangeEvent;
import jayms.spellbound.event.CastSpellEvent;
import jayms.spellbound.event.ManaChangeEvent;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;

public class SpellListener implements Listener {

	private Set<Entity> holograms = new HashSet<>();
	
	public SpellListener() {
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onClick(PlayerAnimationEvent e) {
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
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
			Hologram hologram = HologramAPI.createHologram(sbp.getBukkitPlayer().getEyeLocation().add(0, 1, 0), sp.getDisplayName() + " " + sp.getPowerColor() + sp.getPower() + "p");
			hologram.spawn();
			holograms.add(hologram.getAttachedTo());
			new BukkitRunnable() {

				@Override
				public void run() {
					sp.enable(sbp);
					hologram.despawn();
					holograms.remove(hologram.getAttachedTo());
				}
			}.runTaskLater(Main.self, 5L);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSlotChangeSpellDisplay(PlayerItemHeldEvent e) {
		
		SpellBoundPlayer sbp = Main.self.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		
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
		
		PacketUtil.sendActionbar(spTs, sbp.getBukkitPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSlotChangeWandSwitch(PlayerItemHeldEvent e) {
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
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
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
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
						ItemStack it = sbp.getBukkitPlayer().getItemInHand();
						if (it != null) {
							if (it.getAmount() <= 0) {
								it.setAmount(1);
							}
						}
					}
				}.runTaskLater(Main.self, 1L);
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
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		
		for (SpellBoundPlayer sbp : sbph.getCachedPlayers()) {
			sbp.regenerateMana();
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			
			SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
			
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
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCooldownChange(CooldownChangeEvent<SpellBoundPlayer> e) {
		CooldownHandler<SpellBoundPlayer> ch = e.getCooldownHandler();
		
		for (SpellBoundPlayer sbp : ch.getElements().keySet()) {
			sbp.getScoreboard().updateAndShow();
		}
	}
	
	public Set<Entity> getHologramEntitys() {
		return Collections.unmodifiableSet(holograms);
	}
}
