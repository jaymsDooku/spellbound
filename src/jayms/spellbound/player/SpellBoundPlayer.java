package jayms.spellbound.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mysql.jdbc.Statement;

import jayms.plugin.db.Database;
import jayms.plugin.io.IO;
import jayms.plugin.util.Rate;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.bind.BindingBelt;
import jayms.spellbound.bind.Slot;
import jayms.spellbound.items.wands.Wand;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellData;

public class SpellBoundPlayer implements IO {

	private final SpellBoundPlugin running;
	private final Player bukkitPlayer;

	private BindingBelt binds;
	private Map<String, SpellData> spellData = new HashMap<>();
	
	private double maxMana;
	private double mana;
	private Rate manaRegen;
	
	private Wand selectedWand;
	private boolean battleMode = false;
	private int slotInside = -1;

	public SpellBoundPlayer(SpellBoundPlugin running, Player bukkitPlayer) {
		this.running = running;
		this.bukkitPlayer = bukkitPlayer;
		this.binds = new BindingBelt(this.running, this);
		initialize();
	}

	private void initialize() {
		FileConfiguration config = running.getConfiguration();
		this.maxMana = config.getDouble("SpellBoundPlayerDefaults.maxMana");
		this.mana = config.getDouble("SpellBoundPlayerDefaults.mana");
		double rate = config.getDouble("SpellBoundPlayerDefaults.manaRegen.rate");
		long rateTime = config.getLong("SpellBoundPlayerDefaults.manaRegen.rateTime");
		this.manaRegen = new Rate(rate, rateTime, mana, maxMana);
	}

	@Override
	public void load() {
		Database db = running.getDatabase();
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		
		checkForSQLRecord();
		
		String selectSBPstmt = SpellBoundPlayerHandler.SELECT_SBP_STMT;
		
		ResultSet rs = db.readQuery(selectSBPstmt);
		try {
			if (rs.next()) {
				maxMana = rs.getDouble("MaxMana");
				mana = rs.getDouble("Mana");
				manaRegen = new Rate(rs.getDouble("ManaRegenRate"), rs.getLong("ManaRegenRateTime"), mana, maxMana);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		binds.load();
	}

	@Override
	public void save() {
		Database db = running.getDatabase();

		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();

		int[] slotIds = new int[Slot.SLOT_COUNT];
		
		String sbpstmt = null;
		if (!sbph.spellBoundPlayerExistsInDB(bukkitPlayer.getUniqueId())) {
			sbpstmt = SpellBoundPlayerHandler.INSERT_SBP_STMT;
			for (int slot = 0; slot < Slot.SLOT_COUNT; slot++) {
				String bindsstmt = SpellBoundPlayerHandler.INSERT_BINDS_STMT;
				bindsstmt = bindsstmt.replace("%slot%", Integer.toString(slot+1));
				for (int subSlot = 0; subSlot < Slot.SLOT_COUNT; subSlot++) {
					Spell spell = binds.getSpell(slot + 1, subSlot + 1);
					bindsstmt = bindsstmt.replace("%bindingSlot" + (subSlot + 1) + "%",
							spell == null ? "null" : "'" + spell.getUniqueName() + "'");
				}
				Connection conn = db.getConnection();
				try {
					PreparedStatement ps = conn.prepareStatement(bindsstmt, Statement.RETURN_GENERATED_KEYS);
					ps.executeUpdate();
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						int id = rs.getInt(1);
						slotIds[slot] = id;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		} else {
			sbpstmt = SpellBoundPlayerHandler.UPDATE_SBP_STMT;
			binds.save();
		}
		sbpstmt = sbpstmt.replace("%key%", bukkitPlayer.getUniqueId().toString());
		sbpstmt = sbpstmt.replace("%maxMana%", Double.toString(maxMana));
		sbpstmt = sbpstmt.replace("%mana%", Double.toString(mana));
		sbpstmt = sbpstmt.replace("%manaRegenRate%", Double.toString(manaRegen.getRate()));
		sbpstmt = sbpstmt.replace("%manaRegenRateTime%", Long.toString(manaRegen.getRateTime()));
		for (int i = 0; i < slotIds.length; i++) {
			sbpstmt = sbpstmt.replace("%slot" + (i+1) + "ID%", Integer.toString(slotIds[i]));
		}
		db.modifyQuery(sbpstmt);
		System.out.println("Saved!");
	}
	
	public int getSlotID(int slot) {
		
		if (slot < 0 || slot > Slot.SLOT_COUNT) {
			throw new IllegalArgumentException("Slot exceeds limits!");
		}
		
		Database db = running.getDatabase();
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
		
		checkForSQLRecord();
		
		String sbpSlotIdstmt = SpellBoundPlayerHandler.SELECT_SBP_SLOTID_STMT;
		sbpSlotIdstmt = sbpSlotIdstmt.replace("%int%", Integer.toString(slot));
		sbpSlotIdstmt = sbpSlotIdstmt.replace("%key%", bukkitPlayer.getUniqueId().toString());
		ResultSet rs = db.readQuery(sbpSlotIdstmt);
		try {
			if (rs.next()) {
				return rs.getInt("Slot" + slot + "ID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Something has really gone wrong when getting the SlotID!");
	}
	
	public void checkForSQLRecord() {
		
		SpellBoundPlayerHandler sbph = running.getSpellBoundPlayerHandler();
	
		if (!sbph.spellBoundPlayerExistsInDB(bukkitPlayer.getUniqueId())) {
			throw new RuntimeException("Player doesn't have a record to load from!");
		}
	}

	public BindingBelt getBinds() {
		return binds;
	}

	public void setBinds(BindingBelt binds) {
		this.binds = binds;
	}

	public double getMaxMana() {
		return maxMana;
	}

	public void setMaxMana(double maxMana) {

		if (maxMana < 0 || maxMana > Double.MAX_VALUE) {
			throw new IllegalArgumentException("MaxMana can't be lower be this number!");
		}

		this.maxMana = maxMana;
	}

	public double getMana() {
		return mana;
	}

	public void setMana(double mana) {

		if (mana < 0 || mana > Double.MAX_VALUE) {
			throw new IllegalArgumentException("Mana can't be lower be this number!");
		}

		this.mana = mana;
	}

	public Rate getManaRegen() {
		return manaRegen;
	}

	public void setManaRegen(Rate manaRegen) {
		this.manaRegen = manaRegen;
	}

	public boolean isBattleMode() {
		return battleMode;
	}

	public void setBattleMode(boolean battleMode) {
		this.battleMode = battleMode;
	}

	public int getSlotInside() {
		return slotInside;
	}

	public void setSlotInside(int slotInside) {
		this.slotInside = slotInside;
	}

	public Wand getSelectedWand() {
		return selectedWand;
	}

	public void setSelectedWand(Wand selectedWand) {
		if (!hasWand(selectedWand)) {
			throw new IllegalArgumentException("This player doesn't have the wand to set!");
		}
		this.selectedWand = selectedWand;
	}
	
	public boolean assureSelectedWand() {
		
		if (hasSelectedWand()) {
			return true;
		}
		
		Set<Wand> wands = getWands();
		if (!wands.isEmpty()) {
			System.out.println(wands.iterator().next());
			setSelectedWand(wands.iterator().next());
			return true;
		}
		return false;
	}
	
	public Map<Wand, Integer> getWandsWithSlots() {
		
		Map<Wand, Integer> result = new HashMap<>();
		
		Inventory inv = bukkitPlayer.getInventory();
		
		ItemStack[] contents = inv.getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack it = contents[i];
			if (it == null) continue;
			if (Wand.isWand(it)) {
				result.put(running.getWandHandler().getWand(it), i);
			}
		}
		
		return result;
	}
	
	public Set<Wand> getWands() {
		return getWandsWithSlots().keySet();
	}
	
	public boolean hasSelectedWand() {
		return selectedWand != null;
	}
	
	public boolean hasWand(Wand wand) {
		return getWands().contains(wand);
	}
	
	public int indexOfWand(Wand wand) {
		
		if (!hasWand(wand)) {
			return -1;
		}
		
		return getWandsWithSlots().get(wand);
	}

	public void putSpellData(Spell spell, SpellData data) {
		if (data == null && spellData.containsKey(spell.getUniqueName())) {
			spellData.remove(spell.getUniqueName());
		}
		spellData.put(spell.getUniqueName(), data);
	}

	public SpellData getSpellData(Spell spell) {
		return spellData.get(spell.getUniqueName());
	}

	public Player getBukkitPlayer() {
		return bukkitPlayer;
	}

}
