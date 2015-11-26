package jayms.spellbound.spells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;

import jayms.plugin.util.tuple.Tuple;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.spells.data.CommonData;
import jayms.spellbound.spells.data.SpellData;
import jayms.spellbound.spells.data.TopData;

public class SpellHandler {

	private SpellBoundPlugin running;
	private SpellListener listener;
	private Set<Spell> registeredSpells = new HashSet<>();
	
	public SpellHandler(SpellBoundPlugin running) {
		this.running = running;
		this.listener = new SpellListener(this.running);
		this.running.getEventDispatcher().registerListener(this.listener);
	}
	
	public void registerSpell(Spell spell) {
		
		if (spell.getUniqueName().equalsIgnoreCase("null")) {
			running.getLogger().log(Level.WARNING, "This cannot be called 'null' therefore it has been blocked from registration!");
		}
		
		Spell sp = getSpell(spell.getUniqueName());
		
		if (sp != null) {
			running.getLogger().log(Level.INFO, "This spell: " +  spell.getUniqueName() +" is already registered!");
			return;
		}
		
		registeredSpells.add(spell);
	}
	
	public Spell getSpell(String uniqueName) {
		for (Spell s : registeredSpells) {
			if (s.getUniqueName().equals(uniqueName)) {
				return s;
			}
		}
		return null;
	}
	
	public Set<Spell> getRegisteredSpells() {
		return Collections.unmodifiableSet(registeredSpells);
	}
	
	public List<Tuple<Spell, SpellBoundPlayer>> getSpellAroundPoint(Location loc, double range, UUID... except) {
		
		List<Tuple<Spell, SpellBoundPlayer>> result = new ArrayList<>();
		Set<Spell> registeredSpells = getRegisteredSpells();
		Set<SpellBoundPlayer> players = running.getSpellBoundPlayerHandler().getCachedPlayers();
		
		for (Spell sp : registeredSpells) {
			for (SpellBoundPlayer sbp : players) {
				SpellData sd = sbp.getSpellData(sp);
				if (sd != null) {
					if (sd instanceof TopData) {
					TopData td = (TopData) sd;
					if (Arrays.asList(except).contains(td.uuid)) continue;
						if (sd instanceof CommonData) {
							CommonData data = (CommonData) sd;
							System.out.println(data);
							System.out.println(data.loc);
							if (data.loc.distance(loc) < range) {
								result.add(new Tuple<>(sp, sbp));
							}
						}
					}
				}
			}
		}
		
		return result;
	}
}
