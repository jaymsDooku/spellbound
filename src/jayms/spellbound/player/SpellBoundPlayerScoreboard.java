package jayms.spellbound.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import jayms.java.mcpe.common.CooldownHandler;
import jayms.java.mcpe.common.util.ServerUtil;
import jayms.spellbound.bind.BindingBelt;
import jayms.spellbound.bind.Slot;
import jayms.spellbound.spells.Spell;

public class SpellBoundPlayerScoreboard implements Listener {

	private static final String FOOTER = ChatColor.DARK_RED + "-----------------";
	private static final String LINE_START = ChatColor.DARK_RED + "-- ";
	private static final String LINE_END = ChatColor.DARK_RED + " ---";
	private static final String SLOT = ChatColor.RED + "Slot ";
	private static final String MAIN_SLOT = ChatColor.RED + "Main Slot ";
	private static final String COLON = ChatColor.DARK_RED + ": ";

	private SpellBoundPlayer sbp;
	private Scoreboard scoreboard;
	private Objective sideBarObj;
	private Objective healthObj;

	public SpellBoundPlayerScoreboard(SpellBoundPlayer sbp) {
		this.sbp = sbp;
		this.sbp.setScoreboard(this);
	}

	public void show() {
		sbp.getBukkitPlayer().setScoreboard(scoreboard);
	}

	public void hide() {
		sbp.getBukkitPlayer().setScoreboard(ServerUtil.getServer().getScoreboardManager().getNewScoreboard());
	}

	public void updateBoard() {
		
		Player player = sbp.getBukkitPlayer(); 
		player.setHealth(player.getHealth());
		
		scoreboard = ServerUtil.getServer().getScoreboardManager().getNewScoreboard();
		sideBarObj = scoreboard.registerNewObjective("main", "dummy");
		sideBarObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		sideBarObj.setDisplayName(ChatColor.DARK_RED + "--- " + ChatColor.RED + ChatColor.UNDERLINE + "SpellBoard"
				+ ChatColor.DARK_RED + " ---");
		healthObj = scoreboard.registerNewObjective("showhealth", "health");
		healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
		healthObj.setDisplayName("/ 20");
		
		int nextScore = 10;
		if (sbp.isBattleMode()) {

			int insideSlot = sbp.getSlotInside();
			BindingBelt bb = sbp.getBinds();

			for (int i = 0; i < Slot.SLOT_COUNT; i++) {
				String intoScore = null;
				int realInd = i + 1;
				if (insideSlot != -1) {
					Spell sp = bb.getSpell(insideSlot, realInd);
					if (sp != null) {
						CooldownHandler<SpellBoundPlayer> cdh = sp.getCooldowns();
						boolean cooldown = cdh.isOnCooldown(sbp);
						intoScore = getSlotString(realInd) + (cooldown ? (cdh.timeLeft(sbp) == 0 ?  "" : ChatColor.STRIKETHROUGH) : "")
								+ sp.getDisplayName().toString() + LINE_END;
					} else {
						intoScore = getSlotString(realInd) + ChatColor.DARK_GRAY + "None" + LINE_END;
					}
				}else {
					intoScore = getMainSlotString(realInd);
				}
				sideBarObj.getScore(intoScore).setScore(nextScore);
				nextScore--;
			}
		}
		sideBarObj.getScore(FOOTER).setScore(nextScore);
	}
	
	public void updateAndShow() {
		updateBoard();
		show();
	}

	public String getSlotString(int slot) {
		return LINE_START + SLOT + slot + COLON;
	}
	
	public String getMainSlotString(int slot) {
		return LINE_START + MAIN_SLOT + slot + LINE_END;
	}
}
