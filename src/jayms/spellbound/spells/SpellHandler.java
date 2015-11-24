package jayms.spellbound.spells;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import jayms.spellbound.SpellBoundPlugin;

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
	
}
