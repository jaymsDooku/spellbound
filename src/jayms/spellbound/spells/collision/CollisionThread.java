package jayms.spellbound.spells.collision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import jayms.java.mcpe.common.collect.Tuple;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.spells.data.CommonData;
import jayms.spellbound.spells.data.SpellData;

//completely redundant
public class CollisionThread implements Runnable {

	private SpellHandler sh;
	private SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
	private Thread thread = new Thread(this);
	private boolean running = false;
	
	public CollisionThread(SpellHandler sh) {
		this.sh = sh;
		thread.setName("Collision");
	}
	
	public void start() {
		if (running) {
			return;
		}
		running = true;
		thread.start();
		Main.log.info("started thread");
	}
	
	private ListMultimap<CollisionPriority, CommonData> sortSpells() {
		ListMultimap<CollisionPriority, CommonData> spells = MultimapBuilder.enumKeys(CollisionPriority.class).arrayListValues().build();
		
		for (Spell sp : sh.getRegisteredSpells()) {
			Set<SpellBoundPlayer> players = sbph.getCachedPlayers();
			for (SpellBoundPlayer sbp : players) {
				SpellData sd = sbp.getSpellData(sp);
				if (sd instanceof CommonData) {
					CommonData cd = (CommonData) sd;
					spells.put(sp.getPriority(), cd);
				}
			}
		}
		
		return spells;
	}
	
	private Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>> handleCollision(Tuple<CollisionPriority, CommonData> cdTup, Tuple<CollisionPriority, CommonData> cd1Tup, ListMultimap<CollisionPriority, CommonData> spells) {
		CommonData cd = cdTup.getB();
		CommonData cd1 = cdTup.getB();
		if (cd.uuid.equals(cd1.uuid)) {
			return null;
		}
		Spell sp = cd.parent;
		Spell sp1 = cd1.parent;
		double range = sp.getCollisionRange();
		double range1 = sp1.getCollisionRange();
		double useRange = range;
		if (range1 > range) {
			useRange = range1;
		}
		if (cd.loc.distance(cd1.loc) < useRange) {
			return doCollision(cdTup, cd1Tup, spells);
		}
		return null;
	}
	
	private Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>> doCollision(Tuple<CollisionPriority, CommonData> cdTup, Tuple<CollisionPriority, CommonData> cd1Tup, ListMultimap<CollisionPriority, CommonData> spells) {
		CommonData cd = cdTup.getB();
		CommonData cd1 = cd1Tup.getB();
		SpellBoundPlayer user = sbph.getSpellBoundPlayer(cd.user);
		SpellBoundPlayer user1 = sbph.getSpellBoundPlayer(cd1.user);
		Spell spell = cd.parent;
		Spell spell1 = cd1.parent;
		//int hits = cd.hits;
		//int hits1 = cd1.hits;
		int compare = spell.compareTo(spell1);
		Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>> result = new Tuple<>(null, null);
		switch (compare) {
		case 1:
			spell.disable(user, true);
			result.setA(new Tuple<>(cdTup.getA(), cd));
			//hits1 = hits1 - 1;
			//cd1.hits = hits1;
			//if (cd1.hits <= 0) {
				//spell1.disable(user1, true);
			//}
			break;
		case 0:
			spell.disable(user, true);
			spell1.disable(user1, true);
			result.setA(new Tuple<>(cdTup.getA(), cd));
			result.setB(new Tuple<>(cd1Tup.getA(), cd1));
			break;
		case -1:
			spell1.disable(user1, true);
			result.setB(new Tuple<>(cd1Tup.getA(), cd1));
			//hits = hits - 1;
			//cd.hits = hits;
			//if (cd.hits <= 0) {
				//spell.disable(user, true);
			//}
			break;
		default:
			//spell.getCollisionHandler().handle(user);
			//spell1.getCollisionHandler().handle(user1);
			break;
		}
		return result;
	}

	@Override
	public void run() {
		Main.log.info(Boolean.toString(running));
		while (running) {
			ListMultimap<CollisionPriority, CommonData> spells = sortSpells();
			if (!spells.isEmpty()) {
				Main.log.info(spells.toString());
				CollisionPriority[] vals = CollisionPriority.values();
				List<Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>>> toRemove = new ArrayList<>();
				for (int i = 0; i < vals.length; i++) {
					CollisionPriority pr = vals[i];
					List<CommonData> datas = spells.get(pr);
					for (CommonData cd : datas) {
						Iterator<Entry<CollisionPriority, CommonData>> spellIt = spells.entries().iterator();
						while (spellIt.hasNext()) {
							Entry<CollisionPriority, CommonData> spellen = spellIt.next();
							Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>> tup = handleCollision(new Tuple<>(pr, cd), new Tuple<>(spellen.getKey(), spellen.getValue()), spells);
							if (tup != null) {
								toRemove.add(tup);
							}
						}
					}
				}
				if (!toRemove.isEmpty()) {
					for (Tuple<Tuple<CollisionPriority, CommonData>, Tuple<CollisionPriority, CommonData>> tup : toRemove) {
						Tuple<CollisionPriority, CommonData> tup1 = tup.getA();
						Tuple<CollisionPriority, CommonData> tup2 = tup.getB();
						spells.remove(tup1.getA(), tup2.getB());
					}
				}
			}
		}
	}
	
	public void stop() {
		if (!running) {
			return;
		}
		running = false;
		thread.interrupt();
	}
}
