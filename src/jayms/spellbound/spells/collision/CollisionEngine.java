package jayms.spellbound.spells.collision;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.Location;

import jayms.java.mcpe.common.collect.Tuple;
import jayms.java.mcpe.common.util.ServerUtil;
import jayms.spellbound.Main;
import jayms.spellbound.player.SpellBoundPlayer;
import jayms.spellbound.player.SpellBoundPlayerHandler;
import jayms.spellbound.spells.Spell;
import jayms.spellbound.spells.SpellHandler;
import jayms.spellbound.spells.data.CommonData;
import jayms.spellbound.spells.data.SpellData;
import jayms.spellbound.spells.data.TopData;

public class CollisionEngine implements Runnable {

	private int taskId = -1;
	
	public CollisionEngine() {
	}
	
	public void start() {
		if (!(taskId <= -1)) {
			return;
		}
		taskId = ServerUtil.getScheduler().runTaskTimer(Main.self, this, 0L, 1L).getTaskId();
	}
	
	@Override
	public void run() {
		SpellHandler sh = Main.self.getSpellHandler();
		SpellBoundPlayerHandler sbph = Main.self.getSpellBoundPlayerHandler();
		Set<SpellBoundPlayer> players = sbph.getCachedPlayers();
		for (SpellBoundPlayer sbp : players) {
			for (Spell sp : sh.getRegisteredSpells()) {
				SpellData sd = sbp.getSpellData(sp);
				if (sd != null) {
					if (sd instanceof TopData) {
						TopData td = (TopData) sd;
						if (td instanceof CommonData) {
							CommonData cd = (CommonData) td;
							SortedMap<CommonData, SpellBoundPlayer> spells = getSpellsAroundSpell(cd, cd.parent.getCollisionRange());
							handleCollision(new Tuple<>(sbp, cd), spells);
						}
					}
				}
			}
		}
	}
	
	public void handleCollision(Tuple<SpellBoundPlayer, CommonData> cdTup, SortedMap<CommonData, SpellBoundPlayer> spells) {
		for (Entry<CommonData, SpellBoundPlayer> entry : spells.entrySet()) {
			CommonData collideCd = entry.getKey();
			CommonData cd = cdTup.getB();
			SpellBoundPlayer sbp = cdTup.getA();
			SpellBoundPlayer collideSbp = entry.getValue();
			Spell collideSp = collideCd.parent;
			Spell cdSp = cd.parent;
			
			int compare = cdSp.compareTo(collideSp);
			
			switch (compare) {
			case 1:
				cdSp.getCollisionHandler().handle(new Tuple<>(sbp, CollisionResult.DESTROYED));
				collideSp.getCollisionHandler().handle(new Tuple<>(collideSbp, CollisionResult.SUCCESS));
				break;
			case 0:
				cdSp.getCollisionHandler().handle(new Tuple<>(sbp, CollisionResult.DESTROYED));
				collideSp.getCollisionHandler().handle(new Tuple<>(collideSbp, CollisionResult.DESTROYED));
				break;
			case -1:
				collideSp.getCollisionHandler().handle(new Tuple<>(collideSbp, CollisionResult.DESTROYED));
				cdSp.getCollisionHandler().handle(new Tuple<>(sbp, CollisionResult.SUCCESS));
				break;
			default:
				break;
			}
		}
	}
	
	public SortedMap<CommonData, SpellBoundPlayer> getSpellsAroundSpell(CommonData cd, double range) {
		//Set<CommonData> result = Sets.newHashSet();
		SortedMap<CommonData, SpellBoundPlayer> result = new TreeMap<>(new SpellRangeComparator(cd));
		
		for (SpellBoundPlayer sbp : Main.self.getSpellBoundPlayerHandler().getCachedPlayers()) {
			for (Spell sp : Main.self.getSpellHandler().getRegisteredSpells()) {
				SpellData sd = sbp.getSpellData(sp);
				if (sd != null) {
					if (sd instanceof TopData) {
						TopData td = (TopData) sd;
						if (td instanceof CommonData) {
							CommonData foundCd = (CommonData) td;
							if (!foundCd.uuid.equals(cd.uuid)) {
								Location cdLoc = cd.loc;
								Location foundLoc = foundCd.loc;
								if (cdLoc.distance(foundLoc) <= range) {
									result.put(foundCd, sbp);
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public void cancel() {
		if (taskId <= -1) {
			return;
		}
		ServerUtil.getScheduler().cancelTask(taskId);
	}
	
	public class SpellRangeComparator implements Comparator<CommonData> {

		private CommonData point;
		
		public SpellRangeComparator(CommonData point) {
			this.point = point;
		}
		
		@Override
		public int compare(CommonData fCd, CommonData sCd) {
			Location pointLoc = point.loc;
			Location fCdLoc = fCd.loc;
			Location sCdLoc = sCd.loc;
			
			double fCdrange = pointLoc.distance(fCdLoc);
			double sCdrange = pointLoc.distance(sCdLoc);
			
			if (fCdrange > sCdrange) {
				return 1;
			}else if (fCdrange == sCdrange) {
				return 0;
			}else {
				return -1;
			}
		}
		
	}
}
