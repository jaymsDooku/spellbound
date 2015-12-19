package jayms.spellbound.bind;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import jayms.java.mcpe.sql.Database;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellHandler;

public class BindingBelt {
	
	private final SpellBoundPlayer parent;
	private Spell[][] spells = new Spell[Slot.SLOT_COUNT][Slot.SLOT_COUNT];
	
	public BindingBelt(SpellBoundPlayer parent) {
		this.parent = parent;
	}
	
	public boolean bind(int slot, int subSlot, Spell spell, boolean replace) {
		slot = slot-1;
		subSlot = subSlot-1;
		checkForExceed(slot);
		checkForExceed(subSlot);
		
		Spell sp = getSpell(slot+1, subSlot+1);
		
		boolean bind = true;
		
		if (sp != null) {
			if (!replace) {
				bind = false;
			}
		}
		
		if (bind) {
			spells[slot][subSlot] = spell;
			autoSave();
			return true;
		}
		return false;
	}
	
	private void autoSave() {
		if (Main.self.getYAMLFileMCExt().getFC().getBoolean("Settings.AutoSaveBinds", true)) {
			save();
		}
	}
	
	public boolean unbind(int slot, int subSlot) {
		slot = slot-1;
		subSlot = subSlot-1;
		checkForExceed(slot);
		checkForExceed(subSlot);
		
		if (getSpell(slot, subSlot) == null) {
			return false;
		}
		
		spells[slot][subSlot] = null;
		autoSave();
		return true;
	}
	
	public void unbindAll(Spell spell) {
		for (int i = 0; i < spells.length; i++) {
			Spell[] subSpells = spells[i];
			for (int j = 0; j < subSpells.length; j++) {
				if (spell.equals(subSpells[j])) {
					unbind(i, j);
				}
			}
		}
	}
	
	public void unbindAll() {
		for (int i = 0; i < spells.length; i++) {
			Spell[] subSpells = spells[i];
			for (int j = 0; j < subSpells.length; j++) {
				unbind(i, j);
			}
		}
	}
	
	public Spell getSpell(int slot, int subSlot) {
		slot = slot-1;
		subSlot = subSlot-1;
		checkForExceed(slot);
		checkForExceed(subSlot);
		
		return spells[slot][subSlot];
	}
	
	private void checkForExceed(int slot) {
		if (slot < 0 || slot > spells.length) {
			throw new IllegalArgumentException("Slot exceeds SLOT_COUNT!");
		}
	}

	public void load() {
		Database db = Main.self.getSQL();
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		
		for (int i = 0; i < Slot.SLOT_COUNT; i++) {
			int slot = i+1;
			String selectBindsstmt = SpellBoundPlayerHandler.SELECT_BINDS_STMT;
			selectBindsstmt = selectBindsstmt.replace("%id%", Integer.toString(parent.getSlotID(slot)));
			ResultSet rs = db.readQuery(selectBindsstmt);
			for (int j = 0; j < Slot.SLOT_COUNT; j++) {
				int subSlot = j+1;
				try {
					String sp = rs.getString("BindingSlot" + Integer.toString(subSlot));
					if (sp != null) {
						SpellHandler sh = Main.self.getSpellHandler();
						Spell sptb = sh.getSpell(sp);
						bind(slot, subSlot, sptb, true);
						Main.log.log(Level.INFO, "Bound A Spell:\n" 
						+ "SpellDN: " + sptb.getDisplayName() + "\n"
						+ "Slot: " + slot + "\n"
						+ "SubSlot: " + subSlot);
					}else {
						Main.log.log(Level.INFO, "Spell is null Slot: " + slot + " SubSlot: " + subSlot);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void save() {
		Database db = Main.self.getSQL();
		
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		
		if (!sbph.spellBoundPlayerExistsInDB(parent.getBukkitPlayer().getUniqueId())) {
			throw new RuntimeException("There is no record to save to!");
		}
		
		for (int slot = 0; slot < Slot.SLOT_COUNT; slot++) {
			String bindsstmt = SpellBoundPlayerHandler.UPDATE_BINDS_STMT;
			int slotTu = slot + 1;
			bindsstmt = bindsstmt.replace("%slot%", Integer.toString(slotTu));
			for (int subSlot = 0; subSlot < Slot.SLOT_COUNT; subSlot++) {
				int subSlotTu = subSlot + 1;
				Spell spell = getSpell(slotTu, subSlotTu);
				bindsstmt = bindsstmt.replace("%bindingSlot" + subSlotTu + "%",
						spell == null ? "null" : "'" + spell.getUniqueName() + "'");
			}
			bindsstmt = bindsstmt.replace("%id%", Integer.toString(parent.getSlotID(slotTu)));
			db.modifyQuery(bindsstmt);
		}
	}
}
