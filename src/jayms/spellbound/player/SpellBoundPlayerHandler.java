package jayms.spellbound.player;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import jayms.plugin.db.Database;
import jayms.plugin.packet.EntityMethods;
import jayms.plugin.packet.ExperienceMethods;
import jayms.plugin.packet.TitleMethods;
import jayms.spellbound.SpellBoundPlugin;
import jayms.spellbound.items.wands.Wand;

public class SpellBoundPlayerHandler implements Listener {

	public static final String MYSQL_CREATE_SBP_TABLE_STMT = "CREATE TABLE sbp (" + "UniqueID VARCHAR(255) NOT NULL,"
			+ "MaxMana DOUBLE(12, 2)," + "Mana DOUBLE(12, 2)," + "ManaRegenRate DOUBLE(12, 2),"
			+ "ManaRegenRateTime BIGINT(19),"
			+ "Slot1ID INT(32),"
			+ "Slot2ID INT(32),"
			+ "Slot3ID INT(32),"
			+ "Slot4ID INT(32),"
			+ "Slot5ID INT(32),"
			+ "Slot6ID INT(32),"
			+ "Slot7ID INT(32),"
			+ "Slot8ID INT(32),"
			+ "Slot9ID INT(32),"
			+ "PRIMARY KEY(UniqueID));";

	public static final String SQLITE_CREATE_SBP_TABLE_STMT = "CREATE TABLE sbp ("
			+ "UniqueID TEXT NOT NULL PRIMARY KEY," + "MaxMana NUMERIC," + "Mana NUMERIC," + "ManaRegenRate NUMERIC,"
			+ "ManaRegenRateTime INTEGER,"
			+ "Slot1ID INTEGER,"
			+ "Slot2ID INTEGER,"
			+ "Slot3ID INTEGER,"
			+ "Slot4ID INTEGER,"
			+ "Slot5ID INTEGER,"
			+ "Slot6ID INTEGER,"
			+ "Slot7ID INTEGER,"
			+ "Slot8ID INTEGER,"
			+ "Slot9ID INTEGER"
			+ ");";

	public static final String MYSQL_CREATE_BINDS_TABLE_STMT = "CREATE TABLE binds (ID INT(32) NOT NULL AUTO INCREMENT,"
			+ "Slot INTEGER(9)," + "BindingSlot1 VARCHAR(255)," + "BindingSlot2 VARCHAR(255),"
			+ "BindingSlot3 VARCHAR(255)," + "BindingSlot4 VARCHAR(255)," + "BindingSlot5 VARCHAR(255),"
			+ "BindingSlot6 VARCHAR(255)," + "BindingSlot7 VARCHAR(255)," + "BindingSlot8 VARCHAR(255),"
			+ "BindingSlot9 VARCHAR(255)" + "PRIMARY KEY(UniqueID));";

	public static final String SQLITE_CREATE_BINDS_TABLE_STMT = "CREATE TABLE binds (ID INTEGER NOT NULL PRIMARY KEY," + "Slot INTEGER," + "BindingSlot1 TEXT," + "BindingSlot2 TEXT,"
			+ "BindingSlot3 TEXT," + "BindingSlot4 TEXT," + "BindingSlot5 TEXT," + "BindingSlot6 TEXT,"
			+ "BindingSlot7 TEXT," + "BindingSlot8 TEXT," + "BindingSlot9 TEXT" + ");";

	public static final String INSERT_SBP_STMT = "INSERT INTO sbp (UniqueID," + "MaxMana," + "Mana," + "ManaRegenRate,"
			+ "ManaRegenRateTime,"
			+ "Slot1ID,"
			+ "Slot2ID,"
			+ "Slot3ID,"
			+ "Slot4ID,"
			+ "Slot5ID,"
			+ "Slot6ID,"
			+ "Slot7ID,"
			+ "Slot8ID,"
			+ "Slot9ID"
			+ ")" + "VALUES (" 
			+ "'%key%'," 
			+ "%maxMana%," 
			+ "%mana%," 
			+ "%manaRegenRate%,"
			+ "%manaRegenRateTime%," 
			+ "%slot1ID%," 
			+ "%slot2ID%," 
			+ "%slot3ID%," 
			+ "%slot4ID%," 
			+ "%slot5ID%," 
			+ "%slot6ID%," 
			+ "%slot7ID%," 
			+ "%slot8ID%," 
			+ "%slot9ID%" 
			+ ");";

	public static final String UPDATE_SBP_STMT = "UPDATE sbp SET " + "MaxMana = %maxMana%," + "Mana = %mana%,"
			+ "ManaRegenRate = %manaRegenRate%," + "ManaRegenRateTime = %manaRegenRateTime% "
			+ "WHERE UniqueID = '%key%';";

	public static final String INSERT_BINDS_STMT = "INSERT INTO binds (" + "Slot," + "BindingSlot1,"
			+ "BindingSlot2," + "BindingSlot3," + "BindingSlot4," + "BindingSlot5," + "BindingSlot6," + "BindingSlot7,"
			+ "BindingSlot8," + "BindingSlot9)" + "VALUES (" + "%slot%," + "%bindingSlot1%,"
			+ "%bindingSlot2%," + "%bindingSlot3%," + "%bindingSlot4%," + "%bindingSlot5%," + "%bindingSlot6%,"
			+ "%bindingSlot7%," + "%bindingSlot8%," + "%bindingSlot9%" + ");";

	public static final String UPDATE_BINDS_STMT = "UPDATE binds SET "
			+ "Slot = %slot%,"
			+ "BindingSlot1 = %bindingSlot1%,"
			+ "BindingSlot2 = %bindingSlot2%,"
			+ "BindingSlot3 = %bindingSlot3%,"
			+ "BindingSlot4 = %bindingSlot4%,"
			+ "BindingSlot5 = %bindingSlot5%,"
			+ "BindingSlot6 = %bindingSlot6%,"
			+ "BindingSlot7 = %bindingSlot7%,"
			+ "BindingSlot8 = %bindingSlot8%,"
			+ "BindingSlot9 = %bindingSlot9% "
			+ "WHERE `ID` = %id%;";

	public static final String CHECK_FOR_RECORD_STMT = "SELECT count(1) FROM sbp WHERE UniqueID = '%key%';";
	
	public static final String SELECT_SBP_STMT = "SELECT * FROM SBP WHERE UniqueID = '%key%';";
	
	public static final String SELECT_SBP_SLOTID_STMT = "SELECT Slot%int%ID FROM SBP WHERE UniqueID = '%key%'";
	
	public static final String SELECT_BINDS_STMT = "SELECT * FROM BINDS WHERE ID = %id%";

	private final SpellBoundPlugin sbPlugin;
	private Database db;

	private Map<UUID, SpellBoundPlayer> cache = new HashMap<>();
	
	private Map<SpellBoundPlayer, ItemStack[]> battleModeToggle = new HashMap<>(); 
	
	public SpellBoundPlayerHandler(SpellBoundPlugin sbPlugin) {
		this.sbPlugin = sbPlugin;
		this.db = this.sbPlugin.getDatabase();
		initialize();
	}

	private void initialize() {
		if (db.isMySQL()) {
			if (!db.tableExists("sbp")) {
				db.modifyQuery(MYSQL_CREATE_SBP_TABLE_STMT);
			}
			if (!db.tableExists("binds")) {
				db.modifyQuery(MYSQL_CREATE_BINDS_TABLE_STMT);
			}
		} else {
			if (!db.tableExists("sbp")) {
				db.modifyQuery(SQLITE_CREATE_SBP_TABLE_STMT);
			}
			if (!db.tableExists("binds")) {
				db.modifyQuery(SQLITE_CREATE_BINDS_TABLE_STMT);
			}
		}
	}

	public boolean spellBoundPlayerExistsInDB(UUID uuid) {
		try {
			return db.readQuery(CHECK_FOR_RECORD_STMT.replace("%key%", uuid.toString())).getInt(1) != 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public SpellBoundPlayer createNewSpellBoundPlayer(UUID uuid) {

		if (cache.containsKey(uuid)) {
			throw new IllegalArgumentException("This UUID already has a player!");
		}

		if (spellBoundPlayerExistsInDB(uuid)) {
			throw new IllegalArgumentException("This UUID already exists in the database!");
		}

		Player player = sbPlugin.getSelf().getServer().getPlayer(uuid);

		if (player == null) {
			throw new IllegalArgumentException("This UUID isn't even a player!");
		}

		SpellBoundPlayer result = new SpellBoundPlayer(sbPlugin, player);
		result.save();

		return result;
	}

	public SpellBoundPlayer getSpellBoundPlayer(UUID uuid) {

		SpellBoundPlayer sbp = cache.get(uuid);

		if (sbp == null) {
			if (spellBoundPlayerExistsInDB(uuid)) {

				Player player = sbPlugin.getSelf().getServer().getPlayer(uuid);

				if (player == null) {
					throw new IllegalArgumentException("This UUID isn't even a player!");
				}

				sbp = new SpellBoundPlayer(sbPlugin, player);
				sbp.load();
				cache.put(sbp.getBukkitPlayer().getUniqueId(), sbp);
			} else {
				sbp = createNewSpellBoundPlayer(uuid);
			}
		}

		return sbp;
	}

	public SpellBoundPlayer getSpellBoundPlayer(String name) {

		Player player = sbPlugin.getSelf().getServer().getPlayer(name);

		if (player == null) {
			throw new IllegalArgumentException("This name doesn't correspond to a player!");
		}

		return getSpellBoundPlayer(player);
	}

	public SpellBoundPlayer getSpellBoundPlayer(Player player) {
		return getSpellBoundPlayer(player.getUniqueId());
	}
	
	public boolean toggleBattleMode(SpellBoundPlayer sbp) {
		
		if (!sbp.isBattleMode()) {
			if (!sbp.assureSelectedWand()) {
				return false;
			}
			Wand wand = sbp.getSelectedWand();
			if (wand == null) {
				throw new RuntimeException("Wand has become null! Something has gone very wrong!");
			}
			sbp.setBattleMode(true);
			PlayerInventory inv = sbp.getBukkitPlayer().getInventory();
			battleModeToggle.put(sbp, inv.getContents());
			inv.clear();
			inv.setItem(0, wand.getItemStack());
			inv.setHeldItemSlot(0);
			sbp.showMana();
		}else {
			sbp.setBattleMode(false);
			sbp.setSlotInside(-1);
			ItemStack[] bmtItems = battleModeToggle.remove(sbp);
			Inventory inv = sbp.getBukkitPlayer().getInventory();
			inv.setContents(bmtItems);
			sbp.hideMana();
		}
		return true;
	}
	
	private void handleWelcome(SpellBoundPlayer sbp) {
		TitleMethods.sendTitle("&7Welcome to &4S&cp&4e&cl&4l&cb&4o&cu&4n&cd ", 2, 5, 2, sbp.getBukkitPlayer());
		TitleMethods.sendSubTitle("&7Powered by: &4PluginEnterprise", 2, 5, 2, sbp.getBukkitPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		SpellBoundPlayer sbp = getSpellBoundPlayer(e.getPlayer());
		handleWelcome(sbp);
		
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		SpellBoundPlayer sbp = getSpellBoundPlayer(e.getPlayer());
		if (sbp.isBattleMode()) {
			toggleBattleMode(sbp);
		}
		sbp.save();
		cache.remove(sbp.getBukkitPlayer().getUniqueId());
	}
	
	public Set<SpellBoundPlayer> getCachedPlayers() {
		
		Set<SpellBoundPlayer> result = new HashSet<>();
		
		result.addAll(cache.values());
		
		return result;
	}
}
