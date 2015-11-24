package jayms.spellbound.items.wands;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import jayms.spellbound.spells.SpellType;

public interface IWand {
	
	UUID getUniqueId();
	
	void setManaPercentage(SpellType type, int set);
	
	int getManaPercentage(SpellType type);
	
	void setHealthPercentage(SpellType type, int set);
	
	int getHealthPercentage(SpellType type);
	
	void setCooldownPercentage(SpellType type, int set);
	
	int getCooldownPercentage(SpellType type);
	
	void setDamagePercentage(SpellType type, int set);
	
	int getDamagePercentage(SpellType type);
	
	void setKnockbackPercentage(SpellType type, int set);
	
	int getKnockbackPercentage(SpellType type);
	
	void setBackfirePercentage(SpellType type, int set);
	
	int getBackfirePercentage(SpellType type);
	
	ItemStack getItemStack();
	
	String getName();
	
	void unregister();
	
	boolean equalsItem(ItemStack it);
	
}
