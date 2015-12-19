package jayms.spellbound.items.wands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;

public class WandHandler implements Listener {
	
	private Set<Wand> registeredWands = new HashSet<>();
	
	public WandHandler() {
		Main.self.getEventDispatcher().registerListener(this);
	}
	
	public void initialize() {
		for (SpellBoundPlayer sbp : Main.self.getSpellBoundPlayerHandler().getCachedPlayers()) {
			registerPlayersWands(sbp);
		}
	}
	
	public boolean registerWand(Wand wand) {
		return registeredWands.add(wand);
	}
	
	public boolean unregisterWand(Wand wand) {
		return registeredWands.remove(wand);
	}
	
	public Wand getWand(ItemStack it) {
		
		if (!Wand.isWand(it)) {
			return null;
		}
		
		for (Wand wand : registeredWands) {
			if (wand.getUniqueId().equals(Wand.extractUUIDFromWandItem(it))) {
				return wand;
			}
		}
		return null;
	}
	
	public void registerPlayersWands(SpellBoundPlayer sbp) {
		Player player = sbp.getBukkitPlayer();
		Inventory inv = player.getInventory();
		for (ItemStack it : inv) {
			if (it == null) continue;
			if (Wand.isWand(it)) {
				registerWand(new Wand(it));
			}
		}
	}
	
	public void unregisterPlayersWands(SpellBoundPlayer sbp) {
		Set<Wand> wands = sbp.getWands();
		for (Wand w : wands) {
			w.unregister();
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e) {
		SpellBoundPlayer sbp = Main.self.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		registerPlayersWands(sbp);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent e) {
		SpellBoundPlayer sbp = Main.self.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		unregisterPlayersWands(sbp);
	}
	
	
}
