package com.gmail.nossr50;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.nossr50.commands.general.AddlevelsCommand;
import com.gmail.nossr50.commands.general.AddxpCommand;
import com.gmail.nossr50.commands.general.InspectCommand;
import com.gmail.nossr50.commands.general.McstatsCommand;
import com.gmail.nossr50.commands.general.MmoeditCommand;
import com.gmail.nossr50.commands.general.MmoupdateCommand;
import com.gmail.nossr50.commands.general.XprateCommand;
import com.gmail.nossr50.commands.mc.McabilityCommand;
import com.gmail.nossr50.commands.mc.MccCommand;
import com.gmail.nossr50.commands.mc.McgodCommand;
import com.gmail.nossr50.commands.mc.McmmoCommand;
import com.gmail.nossr50.commands.mc.McrefreshCommand;
import com.gmail.nossr50.commands.mc.McremoveCommand;
import com.gmail.nossr50.commands.mc.MctopCommand;
import com.gmail.nossr50.commands.party.ACommand;
import com.gmail.nossr50.commands.party.AcceptCommand;
import com.gmail.nossr50.commands.party.InviteCommand;
import com.gmail.nossr50.commands.party.PCommand;
import com.gmail.nossr50.commands.party.PartyCommand;
import com.gmail.nossr50.commands.party.PtpCommand;
import com.gmail.nossr50.commands.skills.AcrobaticsCommand;
import com.gmail.nossr50.commands.skills.ArcheryCommand;
import com.gmail.nossr50.commands.skills.AxesCommand;
import com.gmail.nossr50.commands.skills.ExcavationCommand;
import com.gmail.nossr50.commands.skills.FishingCommand;
import com.gmail.nossr50.commands.skills.HerbalismCommand;
import com.gmail.nossr50.commands.skills.MiningCommand;
import com.gmail.nossr50.commands.skills.RepairCommand;
import com.gmail.nossr50.commands.skills.ScythesCommand;
import com.gmail.nossr50.commands.skills.SwordsCommand;
import com.gmail.nossr50.commands.skills.TamingCommand;
import com.gmail.nossr50.commands.skills.UnarmedCommand;
import com.gmail.nossr50.commands.skills.WoodcuttingCommand;
import com.gmail.nossr50.commands.spout.MchudCommand;
import com.gmail.nossr50.commands.spout.XplockCommand;
import com.gmail.nossr50.config.LoadProperties;
import com.gmail.nossr50.config.LoadTreasures;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.listeners.mcBlockListener;
import com.gmail.nossr50.listeners.mcEntityListener;
import com.gmail.nossr50.listeners.mcPlayerListener;
import com.gmail.nossr50.locale.mcLocale;
import com.gmail.nossr50.party.Party;
import com.gmail.nossr50.runnables.SpoutStart;
import com.gmail.nossr50.runnables.mcBleedTimer;
import com.gmail.nossr50.runnables.mcSaveTimer;
import com.gmail.nossr50.runnables.mcTimer;

public class mcMMO extends JavaPlugin {

	public static String maindirectory = "plugins" + File.separator + "mcMMO";
	public static File file = new File(maindirectory + File.separator + "config.yml");
	public static File blocksFile = new File(maindirectory,"blocksPlaced.dat");
	public static File versionFile = new File(maindirectory, "VERSION");

	private final mcPlayerListener playerListener = new mcPlayerListener(this);
	private final mcBlockListener blockListener = new mcBlockListener(this);
	private final mcEntityListener entityListener = new mcEntityListener(this);

	// Alias - Command
	public HashMap<String, String> aliasMap = new HashMap<String, String>();
	public HashMap<Entity, Integer> arrowTracker = new HashMap<Entity, Integer>();
	public HashMap<Integer, Player> tntTracker = new HashMap<Integer, Player>();

	public static Database database = null;

	// Config file stuff
	LoadProperties config;
	LoadTreasures config2;

	// blocks placed data
	public static Set<BlockLoc> blocksPlaced;


	// Jar stuff
	public static File mcmmo;

	/**
	 * Things to be run when the plugin is enabled.
	 */
	@Override
	public void onEnable() {
		final Plugin thisPlugin = this;
		mcmmo = this.getFile();
		new File(maindirectory).mkdir();

		if (!versionFile.exists()) {
			updateVersion();
		} else {
			String vnum = readVersion();

			// This will be changed to whatever version preceded when we
			// actually need updater code.
			// Version 1.0.48 is the first to implement this, no checking before
			// that version can be done.
			if (vnum.equalsIgnoreCase("1.0.48")) {
				updateFrom(1);
			}

			// Just add in more else if blocks for versions that need updater
			// code. Increment the updateFrom age int as we do so.
			// Catch all for versions not matching and no specific code being
			// needed
			else if (!vnum.equalsIgnoreCase(this.getDescription().getVersion())) {
				updateFrom(-1);
			}
		}

		this.config = new LoadProperties(this);
		this.config.load();

		this.config2 = new LoadTreasures(this);
		this.config2.load();

		Party.getInstance().loadParties();
		new Party(this);

		loadBlocksPlaced();

		if (!LoadProperties.useMySQL) {
			Users.getInstance().loadUsers();
		}

		PluginManager pm = getServer().getPluginManager();

		Bukkit.getScheduler().scheduleSyncDelayedTask(this,
				new SpoutStart(this), 20); // Schedule Spout Activation 1 second
		// after start-up

		// Register events
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(entityListener, this);

		PluginDescriptionFile pdfFile = this.getDescription();

		// Setup the leaderboards
		if (LoadProperties.useMySQL) {
			database = new Database(this);
			database.createStructure();
		} else {
			Leaderboard.makeLeaderboards();
		}

		for (Player player : getServer().getOnlinePlayers()) {
			Users.addUser(player); // In case of reload add all users back into
			// PlayerProfile
		}

		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

		BukkitScheduler scheduler = getServer().getScheduler();

		// Periodic save timer (Saves every 10 minutes)
		scheduler.scheduleSyncRepeatingTask(this, new mcSaveTimer(this), 0,
				LoadProperties.saveInterval * 1200);
		// Regen & Cooldown timer (Runs every second)
		scheduler.scheduleSyncRepeatingTask(this, new mcTimer(this), 0, 20);
		// Bleed timer (Runs every two seconds)
		scheduler
		.scheduleSyncRepeatingTask(this, new mcBleedTimer(this), 0, 40);

		registerCommands();

		if (LoadProperties.statsTracking) {
			// Plugin Metrics running in a new thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// create a new metrics object
						Metrics metrics = new Metrics();

						// 'this' in this context is the Plugin object
						metrics.beginMeasuringPlugin(thisPlugin);
					} catch (IOException e) {
						System.out.println("Failed to submit stats.");
					}
				}
			}).start();
		}
	}


	@SuppressWarnings("unchecked")
	private void loadBlocksPlaced() {
		try {
			ObjectInputStream reader = new ObjectInputStream(new FileInputStream(blocksFile));
			blocksPlaced = (Set<BlockLoc>) reader.readObject();
			reader.close();

			Bukkit.getLogger().info("mcMMO: Loaded " + (blocksPlaced == null? 0 : blocksPlaced.size()) + " blocks placed");
		} catch (IOException ioe) {
			Bukkit.getLogger().warning("Could not load blocks placed data: " + ioe);
		} catch (ClassNotFoundException cnfe) {
			Bukkit.getLogger().warning("Location data is not of class Set: " + cnfe);
		} finally {
			if (blocksPlaced == null) {
				blocksPlaced = new HashSet<BlockLoc>();
			}
		}
	}


	private void saveBlocksPlaced() {
		try {
			ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(blocksFile, false));
			writer.writeObject(blocksPlaced);
			writer.close();
		} catch (IOException ioe) {
			Bukkit.getLogger().warning("Could not save blocks placed data: " + ioe);
		}
	}


	public static void removeBlockPlacedMetadata(Block b, Plugin p) {
		b.removeMetadata("mcmmoPlacedBlock", p);
		mcMMO.blocksPlaced.remove(new BlockLoc(b));
	}

	public static void setBlockPlacedMetadata(Block block, Plugin plugin) {
		block.setMetadata("mcmmoPlacedBlock", new FixedMetadataValue(plugin, true));
		mcMMO.blocksPlaced.add(new BlockLoc(block));
	}

	public static boolean isBlockPlaced(Block block) {
		return block.hasMetadata("mcmmoPlacedBlock") || mcMMO.blocksPlaced.contains(new BlockLoc(block));
	}


	/**
	 * Get profile of the player. </br> This function is designed for API usage.
	 * 
	 * @param player
	 *            Player whose profile to get
	 * @return the PlayerProfile object
	 */
	public PlayerProfile getPlayerProfile(Player player) {
		return Users.getProfile(player);
	}

	/**
	 * Things to be run when the plugin is disabled.
	 */
	@Override
	public void onDisable() {

		// Make sure to save player information if the server shuts down
		for (PlayerProfile x : Users.getProfiles().values()) {
			x.save();
		}

		saveBlocksPlaced();

		Bukkit.getServer().getScheduler().cancelTasks(this); // This removes our
		// tasks
		System.out.println("mcMMO was disabled."); // How informative!
	}

	/**
	 * Register the commands.
	 */
	private void registerCommands() {

		// Register aliases with the aliasmap (used in the
		// playercommandpreprocessevent to ugly alias them to actual commands)
		// Skills commands
		aliasMap.put(mcLocale.getString("m.SkillAcrobatics").toLowerCase(),
				"acrobatics");
		aliasMap.put(mcLocale.getString("m.SkillArchery").toLowerCase(),
				"archery");
		aliasMap.put(mcLocale.getString("m.SkillAxes").toLowerCase(), "axes");
		aliasMap.put(mcLocale.getString("m.SkillExcavation").toLowerCase(),
				"excavation");
		aliasMap.put(mcLocale.getString("m.SkillFishing").toLowerCase(),
				"fishing");
		aliasMap.put(mcLocale.getString("m.SkillHerbalism").toLowerCase(),
				"herbalism");
		aliasMap.put(mcLocale.getString("m.SkillMining").toLowerCase(),
				"mining");
		aliasMap.put(mcLocale.getString("m.SkillRepair").toLowerCase(),
				"repair");
		aliasMap.put(mcLocale.getString("m.SkillSwords").toLowerCase(),
				"swords");
		aliasMap.put(mcLocale.getString("m.SkillScythes").toLowerCase(),
				"scythes");
		aliasMap.put(mcLocale.getString("m.SkillTaming").toLowerCase(),
				"taming");
		aliasMap.put(mcLocale.getString("m.SkillUnarmed").toLowerCase(),
				"unarmed");
		aliasMap.put(mcLocale.getString("m.SkillWoodCutting").toLowerCase(),
				"woodcutting");

		// Register commands
		// Skills commands
		getCommand("acrobatics").setExecutor(new AcrobaticsCommand());
		getCommand("archery").setExecutor(new ArcheryCommand());
		getCommand("axes").setExecutor(new AxesCommand());
		getCommand("excavation").setExecutor(new ExcavationCommand());
		getCommand("fishing").setExecutor(new FishingCommand());
		getCommand("herbalism").setExecutor(new HerbalismCommand());
		getCommand("mining").setExecutor(new MiningCommand());
		getCommand("repair").setExecutor(new RepairCommand());
		getCommand("swords").setExecutor(new SwordsCommand());
		getCommand("scythes").setExecutor(new ScythesCommand());
		getCommand("taming").setExecutor(new TamingCommand());
		getCommand("unarmed").setExecutor(new UnarmedCommand());
		getCommand("woodcutting").setExecutor(new WoodcuttingCommand());

		// mc* commands
		if (LoadProperties.mcremoveEnable) {
			getCommand("mcremove").setExecutor(new McremoveCommand());
		}

		if (LoadProperties.mcabilityEnable) {
			getCommand("mcability").setExecutor(new McabilityCommand());
		}

		if (LoadProperties.mccEnable) {
			getCommand("mcc").setExecutor(new MccCommand());
		}

		if (LoadProperties.mcgodEnable) {
			getCommand("mcgod").setExecutor(new McgodCommand());
		}

		if (LoadProperties.mcmmoEnable) {
			getCommand("mcmmo").setExecutor(new McmmoCommand());
		}

		if (LoadProperties.mcrefreshEnable) {
			getCommand("mcrefresh").setExecutor(new McrefreshCommand(this));
		}

		if (LoadProperties.mctopEnable) {
			getCommand("mctop").setExecutor(new MctopCommand());
		}

		if (LoadProperties.mcstatsEnable) {
			getCommand("mcstats").setExecutor(new McstatsCommand());
		}

		// Party commands
		if (LoadProperties.acceptEnable) {
			getCommand("accept").setExecutor(new AcceptCommand());
		}

		if (LoadProperties.aEnable) {
			getCommand("a").setExecutor(new ACommand());
		}

		if (LoadProperties.inviteEnable) {
			getCommand("invite").setExecutor(new InviteCommand(this));
		}

		if (LoadProperties.partyEnable) {
			getCommand("party").setExecutor(new PartyCommand());
		}

		if (LoadProperties.pEnable) {
			getCommand("p").setExecutor(new PCommand());
		}

		if (LoadProperties.ptpEnable) {
			getCommand("ptp").setExecutor(new PtpCommand(this));
		}

		// Other commands
		if (LoadProperties.addxpEnable) {
			getCommand("addxp").setExecutor(new AddxpCommand(this));
		}

		if (LoadProperties.addlevelsEnable) {
			getCommand("addlevels").setExecutor(new AddlevelsCommand(this));
		}

		if (LoadProperties.mmoeditEnable) {
			getCommand("mmoedit").setExecutor(new MmoeditCommand());
		}

		if (LoadProperties.inspectEnable) {
			getCommand("inspect").setExecutor(new InspectCommand(this));
		}

		if (LoadProperties.xprateEnable) {
			getCommand("xprate").setExecutor(new XprateCommand());
		}

		getCommand("mmoupdate").setExecutor(new MmoupdateCommand());

		// Spout commands
		if (LoadProperties.xplockEnable) {
			getCommand("xplock").setExecutor(new XplockCommand());
		}

		getCommand("mchud").setExecutor(new MchudCommand());
	}

	/**
	 * Update mcMMO from a given version </p> It is important to always assume
	 * that you are updating from the lowest possible version. Thus, every block
	 * of updater code should be complete and self-contained; finishing all SQL
	 * transactions and closing all file handlers, such that the next block of
	 * updater code if called will handle updating as expected.
	 * 
	 * @param age
	 *            Specifies which updater code to run
	 */
	public void updateFrom(int age) {

		// No updater code needed, just update the version.
		if (age == -1) {
			updateVersion();
			return;
		}

		// Updater code from age 1 goes here
		if (age <= 1) {
			// Since age 1 is an example for now, we will just let it do
			// nothing.
		}

		// If we are updating from age 1 but we need more to reach age 2, this
		// will run too.
		if (age <= 2) {

		}
		updateVersion();
	}

	/**
	 * Update the version file.
	 */
	public void updateVersion() {
		try {
			versionFile.createNewFile();
			BufferedWriter vout = new BufferedWriter(
					new FileWriter(versionFile));
			vout.write(this.getDescription().getVersion());
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Get the current mcMMO version.
	 * 
	 * @return a String representing the current mcMMO version
	 */
	public String readVersion() {
		byte[] buffer = new byte[(int) versionFile.length()];
		BufferedInputStream f = null;

		try {
			f = new BufferedInputStream(new FileInputStream(versionFile));
			f.read(buffer);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException ignored) {
				}
			}
		}
		return new String(buffer);
	}

	/*
	 * Boilerplate Custom Config Stuff
	 */

	private FileConfiguration treasuresConfig = null;
	private File treasuresConfigFile = null;

	/**
	 * Reload the Treasures.yml file.
	 */
	public void reloadTreasuresConfig() {
		if (treasuresConfigFile == null) {
			treasuresConfigFile = new File(getDataFolder(), "treasures.yml");
		}

		treasuresConfig = YamlConfiguration
				.loadConfiguration(treasuresConfigFile);
		InputStream defConfigStream = getResource("treasures.yml"); // Look for
		// defaults
		// in the
		// jar

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			treasuresConfig.setDefaults(defConfig);
		}
	}

	/**
	 * Get the Treasures config information.
	 * 
	 * @return the configuration object for treasures.yml
	 */
	public FileConfiguration getTreasuresConfig() {
		if (treasuresConfig == null) {
			reloadTreasuresConfig();
		}

		return treasuresConfig;
	}

	/**
	 * Save the Treasures config informtion.
	 */
	public void saveTreasuresConfig() {
		if (treasuresConfig == null || treasuresConfigFile == null) {
			return;
		}

		try {
			treasuresConfig.save(treasuresConfigFile);
		} catch (IOException ex) {
			Bukkit.getLogger().severe(
					"Could not save config to " + treasuresConfigFile
					+ ex.toString());
		}
	}
}
