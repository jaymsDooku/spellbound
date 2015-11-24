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

import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;

public class WandHandler implements Listener {

	private final SpellBoundPlugin running;
	
	private Set<Wand> registeredWands = new HashSet<>();
	
	public WandHandler(SpellBoundPlugin running) {
		this.running = running;
	}
	
	public void initialize() {
		for (Player player : running.getSelf().getServer().getOnlinePlayers()) {
			Inventory inv = player.getInventory();
			for (ItemStack it : inv) {
				if (it == null) continue;
				if (Wand.isWand(it)) {
					registerWand(new Wand(running, it));
				}
			}
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
				registerWand(new Wand(running, it));
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
		SpellBoundPlayer sbp = running.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		registerPlayersWands(sbp);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent e) {
		SpellBoundPlayer sbp = running.getSpellBoundPlayerHandler().getSpellBoundPlayer(e.getPlayer());
		unregisterPlayersWands(sbp);
	}
	
	
}