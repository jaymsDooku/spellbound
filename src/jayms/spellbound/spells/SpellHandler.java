package jayms.spellbound.spells;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Sets;

import jayms.spellbound.Main;
import jayms.spellbound.spells.collision.CollisionEngine;

public class SpellHandler {
	
	private CollisionEngine collision;
	private SpellListener listener;
	private Set<Spell> registeredSpells = Sets.newConcurrentHashSet(new HashSet<Spell>());
	
	public SpellHandler() {
		this.listener = new SpellListener();
		this.collision = new CollisionEngine();
		collision.start();
		Main.self.getEventDispatcher().registerListener(this.listener);
	}
	
	public void registerSpell(Spell spell) {
		
		if (spell.getUniqueName().equalsIgnoreCase("null")) {
			Main.log.log(Level.WARNING, "This cannot be called 'null' therefore it has been blocked from registration!");
		}
		
		Spell sp = getSpell(spell.getUniqueName());
		
		if (sp != null) {
			Main.log.info("This spell: " +  spell.getUniqueName() +" is already registered!");
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
	
	public SpellListener getListener() {
		return listener;
	}
}
