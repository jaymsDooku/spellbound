package jayms.spellbound.items.wands;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import jayms.java.mcpe.common.util.PatternUtil;
import jayms.java.mcpe.nbt.CompoundTag;
import jayms.java.mcpe.nbt.IntTag;
import jayms.java.mcpe.nbt.ListTag;
import jayms.java.mcpe.nbt.StringTag;
import jayms.spellbound.Main;
import jayms.spellbound.spells.SpellType;

public class Wand implements IWand {

	//paths
	public static final String WAND_ID = "WandID";
	public static final String MANA_PERCENT = "WandManaPercentage";
	public static final String HEALTH_PERCENT = "WandHealthPercentage";
	public static final String CD_PERCENT = "WandCooldownPercentage";
	public static final String BACKFIRE_PERCENT = "WandBackfirePercentage";
	public static final String DMG_PERCENT = "WandDamagePercentage";
	public static final String KNOCKBACK_PERCENT = "WandKnockbackPercentage";
	
	public static final String MANA_DISPLAY = "ManaCost";
	public static final String HEALTH_DISPLAY = "HealthCost";
	public static final String CD_DISPLAY = "Cooldown";
	public static final String BACKFIRE_DISPLAY = "Backfire";
	public static final String DMG_DISPLAY = "Damage";
	public static final String KNOCKBACK_DISPLAY = "Knockback";
	
	public static final String[] VALID_PATHS = new String[] {
		MANA_PERCENT,
		HEALTH_PERCENT,
		CD_PERCENT,
		BACKFIRE_PERCENT,
		DMG_PERCENT,
		KNOCKBACK_PERCENT
	};
	
	public static final Map<String, String> PATH_TO_DISPLAY = new HashMap<>();
	
	static {
		PATH_TO_DISPLAY.put(MANA_PERCENT, MANA_DISPLAY);
		PATH_TO_DISPLAY.put(HEALTH_PERCENT, HEALTH_DISPLAY);
		PATH_TO_DISPLAY.put(CD_PERCENT, CD_DISPLAY);
		PATH_TO_DISPLAY.put(BACKFIRE_PERCENT, BACKFIRE_DISPLAY);
		PATH_TO_DISPLAY.put(DMG_PERCENT, DMG_PERCENT);
		PATH_TO_DISPLAY.put(KNOCKBACK_PERCENT, KNOCKBACK_DISPLAY);
	}
	
	private String wandName;
	private UUID uuid;
	private ItemStack it;
	private EnumMap<SpellType, Map<String, Integer>> stats = new EnumMap<>(SpellType.class);

	public Wand(String name) {
		this.uuid = UUID.randomUUID();
		this.wandName = name;
		Main.self.getWandHandler().registerWand(this);
	}

	public Wand(ItemStack it) {
		this.it = it;
		loadStats();
		Main.self.getWandHandler().registerWand(this);
	}

	private void loadStats() {

		if (it == null) {
			throw new NullPointerException("There is no item to load stats from!");
		}
		
		if (!isWand(it)) {
			Main.log.log(Level.SEVERE, "This item is not a wand!");
			return;
		}
		
		wandName = it.getItemMeta().getDisplayName();
		jayms.java.mcpe.nms.ItemStack nmsIt = new jayms.java.mcpe.nms.ItemStack(it);
		CompoundTag root = nmsIt.getTag();
		uuid = UUID.fromString(root.getString(WAND_ID));
		SpellType[] values = SpellType.values();
		for (int t = 0; t < values.length; t++) {
			SpellType st =  values[t];
			CompoundTag compound = root.getCompound(st.toString());
			for (int i = 0; i < VALID_PATHS.length; i++) {
				String path = VALID_PATHS[i];
				int compRes = compound.getInt(path);
				if (Integer.valueOf(compRes) == null) {
					compRes = 0;
				}
				setPercentage(st, path, compRes);
				System.out.println("Wand - Path:" + path + " Value: " + compRes);
			}
		}
	}

	private Map<String, Integer> getStats(SpellType type) {
		Map<String, Integer> result = stats.get(type);
		if (result == null) {
			result = new HashMap<>();
		}
		return result;
	}
	
	private void checkValidStatPath(String stat) {
		if (!Arrays.asList(VALID_PATHS).contains(stat)) {
			throw new IllegalArgumentException("Stat is not of a valid path!");
		}
	}

	private void setPercentage(SpellType type, String stat, int set) {
		checkValidStatPath(stat);
		Map<String, Integer> conts = getStats(type);
		conts.put(stat, set);
		stats.put(type, conts);
	}

	private int getPercentage(SpellType type, String stat) {
		checkValidStatPath(stat);
		Map<String, Integer> conts = getStats(type);
		Integer result = conts.get(stat);
		if (result == null) {
			result = 0;
		}
		return result;
	}

	private ItemStack applyStats() {
		if (it == null) {
			it = new ItemStack(Material.STICK, 1);
		}
		jayms.java.mcpe.nms.ItemStack nmsIt = new jayms.java.mcpe.nms.ItemStack(it);
		CompoundTag root = nmsIt.hasTag() ? nmsIt.getTag() : new CompoundTag();
		root.set(WAND_ID, new StringTag(uuid.toString()));
		if (!root.hasKey("display")) {
			root.set("display", new CompoundTag());
		}
		CompoundTag display = root.getCompound("display");
		display.setString("Name", wandName);
		ListTag lore = new ListTag();
		SpellType[] values = SpellType.values();
		for (int t = 0; t < values.length; t++) {
			SpellType st = values[t];
			String stTs = st.toString();
			if (!root.hasKey(stTs)) {
				root.set(stTs, new CompoundTag());
			}
			CompoundTag compound = root.getCompound(stTs);
			for (int i = 0; i < VALID_PATHS.length; i++) {
				String path = VALID_PATHS[i];
				int perc = getPercentage(st, path);
				compound.set(path, new IntTag(perc));
				if (perc != 0) {
					lore.add(new StringTag(ChatColor.translateAlternateColorCodes('&', "&0(" + st.ampersandCode() + st.getShortened() + "&0)" + st.ampersandCode() + PATH_TO_DISPLAY.get(path) + "&7: &6" + perc + "&7%")));
				}
			}
			root.set(st.toString(), compound);
		}
		lore.add(new StringTag(ChatColor.translateAlternateColorCodes('&', "&cWand ID&7:&6 " + root.getString(WAND_ID))));
		display.set("Lore", lore);
		root.set("display", display);
		nmsIt.setTag(root);
		
		it = nmsIt.toItemStack();
		
		return it;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public void setManaPercentage(SpellType type, int set) {
		setPercentage(type, MANA_PERCENT, set);
	}

	@Override
	public int getManaPercentage(SpellType type) {
		return getPercentage(type, MANA_PERCENT);
	}
	
	@Override
	public void setHealthPercentage(SpellType type, int set) {
		setPercentage(type, HEALTH_PERCENT, set);
	}

	@Override
	public int getHealthPercentage(SpellType type) {
		return getPercentage(type, HEALTH_PERCENT);
	}

	@Override
	public void setCooldownPercentage(SpellType type, int set) {
		setPercentage(type, CD_PERCENT, set);
	}

	@Override
	public int getCooldownPercentage(SpellType type) {
		return getPercentage(type, CD_PERCENT);
	}

	@Override
	public void setDamagePercentage(SpellType type, int set) {
		setPercentage(type, DMG_PERCENT, set);
	}

	@Override
	public int getDamagePercentage(SpellType type) {
		return getPercentage(type, DMG_PERCENT);
	}
	
	@Override
	public void setKnockbackPercentage(SpellType type, int set) {
		setPercentage(type, KNOCKBACK_PERCENT, set);
	}
	
	@Override
	public int getKnockbackPercentage(SpellType type) {
		return getPercentage(type, KNOCKBACK_PERCENT);
	}
	
	@Override
	public void setBackfirePercentage(SpellType type, int set) {
		setPercentage(type, BACKFIRE_PERCENT, set);
	}

	@Override
	public int getBackfirePercentage(SpellType type) {
		return getPercentage(type, BACKFIRE_PERCENT);
	}

	@Override
	public ItemStack getItemStack() {
		return applyStats();
	}
	
	@Override
	public String getName() {
		return wandName;
	}
	
	@Override
	public void unregister() {
		Main.self.getWandHandler().unregisterWand(this);
	}
	
	public static boolean isWand(ItemStack it) {
		if (it.getType() != Material.STICK) {
			return false;
		}

		jayms.java.mcpe.nms.ItemStack nmsIt = new jayms.java.mcpe.nms.ItemStack(it);
		if (!nmsIt.hasTag()) {
			return false;
		}
		CompoundTag root = nmsIt.getTag();
		if (!root.hasKey(WAND_ID)) {
			return false;
		}
		String wuuid = root.getString(WAND_ID);
		return PatternUtil.canBeUUID(wuuid);
	}
	
	public static UUID extractUUIDFromWandItem(ItemStack it) {
		
		if (!isWand(it)) {
			return null;
		}
		
		net.minecraft.server.v1_8_R3.ItemStack nmsIt = CraftItemStack.asNMSCopy(it);
		return UUID.fromString(nmsIt.getTag().getString(WAND_ID));
	}

	@Override
	public boolean equalsItem(ItemStack it) {
		
		if (!isWand(it)) {
			return false;
		}
		
		Wand w = Main.self.getWandHandler().getWand(it);
		return w.getUniqueId().equals(uuid);
	}
}
