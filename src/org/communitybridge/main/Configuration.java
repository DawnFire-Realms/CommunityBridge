package org.communitybridge.main;


import java.io.File;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.communitybridge.utility.Log;
import org.communitybridge.achievement.Achievement;
import org.communitybridge.achievement.AchievementAvatar;
import org.communitybridge.achievement.AchievementGroup;
import org.communitybridge.achievement.AchievementPostCount;
import org.communitybridge.achievement.AchievementSectionPostCount;

/**
 * Class for storing configuration information loaded from the yaml files.
 *
 * @author Feaelin (Iain E. Davis) <iain@ruhlendavis.org>
 */
public class Configuration
{
	private CommunityBridge plugin;
	private Log log;

	// Internationalization
	public String locale;
	public Messages messages = new Messages();

	// General Section
	public String logLevel;
	public boolean usePluginMetrics;
	
	public boolean useAchievements;
	public List<Achievement> achievements = new ArrayList<Achievement>();

	public String permissionsSystem;

	public String autoEveryUnit;
	public boolean autoSync;
	public long autoSyncEvery;
	
	public boolean syncDuringJoin;
	public boolean syncDuringQuit;

	public String applicationURL;
	private String dateFormatString;
	public SimpleDateFormat dateFormat;

	// Database Section
	public String databaseHost;
	public String databasePort;
	public String databaseName;
	public String databaseUsername;
	public String databasePassword;

	// Linking Section
	public boolean linkingAutoRemind;
	public long linkingAutoEvery;
	public boolean linkingNotifyRegistered;
	public boolean linkingNotifyUnregistered;
	public boolean linkingKickUnregistered;

	public String linkingUnregisteredGroup;
	public String linkingRegisteredGroup;
	public boolean linkingNotifyPlayerGroup;
	public boolean linkingRegisteredFormerUnregisteredOnly;

	public boolean linkingUsesKey;
	public String linkingTableName;
	public String linkingUserIDColumn;
	public String linkingPlayerNameColumn;
	public String linkingKeyName;
	public String linkingKeyColumn;
	public String linkingValueColumn;
	public String simpleSynchronizationSuperUserID;

	// Avatar config
	public boolean avatarEnabled;
	public String	avatarTableName;
	public String	avatarUserIDColumn;
	public String	avatarAvatarColumn;
	
	// Post count config
	public boolean postCountEnabled;
	public String	postCountTableName;
	public String	postCountUserIDColumn;
	public String postCountPostCountColumn;
	
	// Requirements Section
	public boolean requireAvatar;
	public boolean requireMinimumPosts;
	public int requirePostsPostCount;

	// Statistics Tracking Settings
	public boolean statisticsEnabled;
	public String statisticsTableName;
	public String statisticsUserIDColumn;
	public boolean statisticsUsesKey;
	public String statisticsKeyColumn;
	public String statisticsValueColumn;
	public boolean statisticsUsesInsert;
	public String statisticsInsertMethod;
	public String statisticsThemeID;
	public String statisticsThemeIDColumn;
	
	public boolean onlineStatusEnabled;
	public String onlineStatusColumnOrKey;
	public String onlineStatusValueOffline;
	public String onlineStatusValueOnline;

	public boolean lastonlineEnabled;
	public String lastonlineColumnOrKey;
	public String lastonlineFormattedColumnOrKey;

	public boolean gametimeEnabled;
	public String gametimeColumnOrKey;
	public String gametimeFormattedColumnOrKey;

	public boolean levelEnabled;
	public String levelColumnOrKey;

	public boolean totalxpEnabled;
	public String totalxpColumnOrKey;

	public boolean currentxpEnabled;
	public String currentxpColumnOrKey;
	public String currentxpFormattedColumnOrKey;

	public boolean lifeticksEnabled;
	public String lifeticksColumnOrKey;
	public String lifeticksFormattedColumnOrKey;

	public boolean healthEnabled;
	public String healthColumnOrKey;

	public boolean walletEnabled;
	public String walletColumnOrKey;

	// Web App group configuration
	// - primary
	public boolean webappPrimaryGroupEnabled;
	public String webappPrimaryGroupTable;
	public String webappPrimaryGroupUserIDColumn;
	public boolean webappPrimaryGroupUsesKey;
	public String webappPrimaryGroupGroupIDColumn;
	public String webappPrimaryGroupKeyName;
	public String webappPrimaryGroupKeyColumn;

	// - secondary
	public boolean webappSecondaryGroupEnabled;
	public String webappSecondaryGroupTable;
	public String webappSecondaryGroupUserIDColumn;
	public String webappSecondaryGroupGroupIDColumn;
	public String webappSecondaryGroupKeyName;
	public String webappSecondaryGroupKeyColumn;
	public String webappSecondaryGroupGroupIDDelimiter;
	// junction, single-column, key-value
	public String webappSecondaryGroupStorageMethod;

	public boolean simpleSynchronizationEnabled;
	public String simpleSynchronizationDirection;
	public String simpleSynchronizationFirstDirection;
	public boolean simpleSynchronizationPrimaryGroupNotify;
	public Map<String, Object> simpleSynchronizationGroupMap = new HashMap<String, Object>();
	public List<String> simpleSynchronizationGroupsTreatedAsPrimary = new ArrayList<String>();

	// Ban synchronization
	public boolean banSynchronizationEnabled;
	public String banSynchronizationMethod;
	
	public List<String> banSynchronizationGroupIDs = new ArrayList<String>();
	
	public String banSynchronizationTableName;
	public String banSynchronizationUserIDColumn;
	public String banSynchronizationReasonColumn;
	public String banSynchronizationStartTimeColumn;
	public String banSynchronizationEndTimeColumn;
	
	// These are not in the config.yml. They are calculated.
	public boolean playerDataRequired;
	public boolean permissionsSystemRequired;
	public boolean groupSynchronizationActive;
	public boolean economyEnabled;

	/**
	 * Constructor for the configuration class.
	 *
	 * @param CommunityBridge The plugin object of this plugin.
	 */
	public Configuration(CommunityBridge plugin, Log log)
	{
		this.plugin = plugin;
		this.log = log;
		load();
		loadMessages();
		loadAchievements();
		report();
	}

	/**
	 * Analyze the configuration for potential problems.
	 *
	 * Checks for the existence of the specified tables and columns within those
	 * tables.
	 *
	 * @param SQL SQL query object.
	 * @return boolean True if the configuration is okay.
	 */
	public boolean analyzeConfiguration(SQL sql)
	{
		boolean status;
		boolean temp;

		// Linking table section.
		status = checkTable(sql, "player-user-linking.table-name", linkingTableName);
		if (status)
		{
			status = status & checkColumn(sql, "player-user-linking.user-id-column", linkingTableName, linkingUserIDColumn);
			if (linkingUsesKey)
			{
				temp = checkColumn(sql, "player-user-linking.key-column", linkingTableName , linkingKeyColumn);
				status = status & temp;
				if (temp)
				{
					checkKeyColumnForKey(sql, "player-user-linking.key-name", linkingTableName, linkingKeyColumn,	linkingKeyName);
				}

				status = status & checkColumn(sql, "player-user-linking.value-column", linkingTableName, linkingValueColumn);
			}
			else
			{
				status = status & checkColumn(sql, "player-user-linking.playername-column", linkingTableName, linkingPlayerNameColumn);
			}
		}

		if (avatarEnabled)
		{
			temp = checkTable(sql, "app-avatar-config.table-name", avatarTableName);
			if (temp)
			{
				temp = temp & checkColumn(sql, "app-avatar-config.user-id-column", avatarTableName, avatarUserIDColumn);
				temp = temp & checkColumn(sql, "app-avatar-config.avatar-column", avatarTableName, avatarAvatarColumn);
			}
			if (!temp)
			{
				log.warning("Temporarily disabling avatar features due to previous error(s).");
				avatarEnabled = false;
				requireAvatar = false;
			}
		}

		if (postCountEnabled)
		{
			temp = checkTable(sql, "app-post-count-config.table-name", postCountTableName);
			status = status & temp;
			if (temp)
			{
				temp = temp & checkColumn(sql, "app-post-count-config.user-id-column", postCountTableName, postCountUserIDColumn);
				temp = temp & checkColumn(sql, "app-post-count-config.post-count-column", postCountTableName, postCountPostCountColumn);
			}
			if (!temp)
			{
				postCountEnabled = false;
				requireMinimumPosts = false;
				log.warning("Temporarily disabling features dependent on post count config due to previous errors.");
			}

		}

		if (statisticsEnabled)
		{
			temp = checkTable(sql, "statistics.table-name", statisticsTableName);
			status = status & temp;
			if (temp)
			{
				status = status & checkColumn(sql, "statistics.user-id-column", statisticsTableName, statisticsUserIDColumn);
				
				if (statisticsUsesInsert && statisticsInsertMethod.startsWith("smf"))
				{
					status = status & checkColumn(sql, "statistics.theme-id-column", statisticsTableName, statisticsThemeIDColumn);
					checkKeyColumnForKey(sql, "statistics.theme-id", statisticsTableName, statisticsThemeIDColumn, statisticsThemeID);
				}
				
				if (statisticsUsesKey)
				{
					temp = checkColumn(sql, "statistics.key-column", statisticsTableName, statisticsKeyColumn);
					temp = temp & checkColumn(sql, "statistics.value-column", statisticsTableName, statisticsValueColumn);
					status = status & temp;
					if (temp)
					{
						if (onlineStatusEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.online-status.column-or-key-name", statisticsTableName, statisticsKeyColumn,	onlineStatusColumnOrKey);
						}
						if (lastonlineEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.last-online.column-or-key-name", statisticsTableName, statisticsKeyColumn,	lastonlineColumnOrKey);
							if (!lastonlineFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey(sql, "statistics.trackers.last-online.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	lastonlineFormattedColumnOrKey);
							}
						}
						if (gametimeEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.game-time.column-or-key-name", statisticsTableName, statisticsKeyColumn,	gametimeColumnOrKey);
							if (!gametimeFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey(sql, "statistics.trackers.game-time.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	gametimeFormattedColumnOrKey);
							}
							if (!lastonlineEnabled)
							{
								log.warning("Game time statistic tracker requires last online tracker to be enabled. Temporarily disabling gametime tracker.");
								gametimeEnabled = false;
							}
						}
						if (levelEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.level.column-or-key-name", statisticsTableName, statisticsKeyColumn,	levelColumnOrKey);
						}
						if (totalxpEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.total-xp.column-or-key-name", statisticsTableName, statisticsKeyColumn, totalxpColumnOrKey);
						}
						if (currentxpEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.current-xp.column-or-key-name", statisticsTableName, statisticsKeyColumn, currentxpColumnOrKey);
							if (!currentxpFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey(sql, "statistics.trackers.current-xp.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,currentxpFormattedColumnOrKey);
							}
						}
						if (healthEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.health.column-or-key-name", statisticsTableName, statisticsKeyColumn, healthColumnOrKey);
						}
						if (lifeticksEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.lifeticks.column-or-key-name", statisticsTableName, statisticsKeyColumn,	lifeticksColumnOrKey);
							if (!lifeticksFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey(sql, "statistics.trackers.lifeticks.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	lifeticksFormattedColumnOrKey);
							}
						}
						if (walletEnabled)
						{
							checkKeyColumnForKey(sql, "statistics.trackers.wallet.column-or-key-name", statisticsTableName, statisticsKeyColumn, walletColumnOrKey);
						}
					}
				}
				else
				{
					if (onlineStatusEnabled && !checkColumn(sql, "statistics.trackers.online-status.column-or-key-name", statisticsTableName,	onlineStatusColumnOrKey))
					{
						onlineStatusEnabled = false;
					}

					if (lastonlineEnabled)
					{
						if(!checkColumn(sql, "statistics.trackers.last-online.column-or-key-name", statisticsTableName,	lastonlineColumnOrKey))
						{
							lastonlineEnabled = false;
						}
						if (!lastonlineFormattedColumnOrKey.isEmpty() && !checkColumn(sql, "statistics.trackers.last-online.formatted-column-or-key-name", statisticsTableName, lastonlineFormattedColumnOrKey))
						{
							lastonlineFormattedColumnOrKey = "";
						}
					}

					if (gametimeEnabled)
					{
						if (!checkColumn(sql, "statistics.trackers.game-time.column-or-key-name", statisticsTableName,	gametimeColumnOrKey))
						{
							gametimeEnabled = false;
						}

						if (!gametimeFormattedColumnOrKey.isEmpty() && !checkColumn(sql, "statistics.trackers.game-time.formatted-column-or-key-name", statisticsTableName, gametimeFormattedColumnOrKey))
						{
							gametimeFormattedColumnOrKey = "";
						}
						
						if (!lastonlineEnabled)
						{
							log.warning("Gametime tracker requires lastonline tracker to be enabled. Temporarily disabling gametime tracker.");
							gametimeEnabled = false;
							gametimeFormattedColumnOrKey = "";
						}
					}
					
					if (levelEnabled && !checkColumn(sql, "statistics.trackers.level.column-or-key-name", statisticsTableName,	levelColumnOrKey))
					{
						levelEnabled = false;
					}

					if (totalxpEnabled && !checkColumn(sql, "statistics.trackers.total-xp.column-or-key-name", statisticsTableName, totalxpColumnOrKey))
					{
						totalxpEnabled = false;
					}

					if (currentxpEnabled)
					{
						if (!checkColumn(sql, "statistics.trackers.current-xp.column-or-key-name", statisticsTableName,	currentxpColumnOrKey))
						{
							currentxpEnabled = false;
						}
						
						if (!currentxpFormattedColumnOrKey.isEmpty() && !checkColumn(sql, "statistics.trackers.current-xp.formatted-column-or-key-name", statisticsTableName, currentxpFormattedColumnOrKey))
						{
							currentxpFormattedColumnOrKey = "";
						}
					}

					if (healthEnabled && !checkColumn(sql, "statistics.trackers.health.column-or-key-name", statisticsTableName, healthColumnOrKey))
					{
						healthEnabled = false;
					}

					if (lifeticksEnabled)
					{
						if (!checkColumn(sql, "statistics.trackers.lifeticks.column-or-key-name", statisticsTableName,	lifeticksColumnOrKey))
						{
							lifeticksEnabled = false;
						}
						
						if (!lifeticksFormattedColumnOrKey.isEmpty() && !checkColumn(sql, "statistics.trackers.lifeticks.formatted-column-or-key-name", statisticsTableName, lifeticksFormattedColumnOrKey))
						{
								lifeticksFormattedColumnOrKey = "";
						}
					}

					if (walletEnabled && !checkColumn(sql, "statistics.trackers.wallet.column-or-key-name", statisticsTableName, walletColumnOrKey))
					{
						walletEnabled = false;
					}

					if (!(onlineStatusEnabled || lastonlineEnabled || gametimeEnabled || levelEnabled || totalxpEnabled || currentxpEnabled || healthEnabled || lifeticksEnabled || walletEnabled))
					{
						log.warning("Statistics tracking is enabled, but none of the individual trackers are enabled. Temporarily disabling statistics tracking.");
						statisticsEnabled = false;
					}
				}
			}
		}

		if (webappPrimaryGroupEnabled)
		{
			temp = checkTable(sql, "app-group-config.primary.table-name", webappPrimaryGroupTable);
			temp = temp & checkColumn(sql, "app-group-config.primary.user-id-column", webappPrimaryGroupTable, webappPrimaryGroupUserIDColumn);
			temp = temp & checkColumn(sql, "app-group-config.primary.group-id-column", webappPrimaryGroupTable, webappPrimaryGroupGroupIDColumn);
			if (webappPrimaryGroupUsesKey)
			{
				temp = temp & checkColumn(sql, "app-group-config.primary.key-column", webappPrimaryGroupTable, webappPrimaryGroupKeyColumn);
				if (temp)
				{
					checkKeyColumnForKey(sql, "app-group-config.primary.key-name", webappPrimaryGroupTable, webappPrimaryGroupKeyColumn, webappPrimaryGroupKeyName);
				}
				else
				{
					webappPrimaryGroupEnabled = false;
					log.warning("Web application primary group disabled due to prior errors.");
				}
			}
		}

		if (webappSecondaryGroupEnabled)
		{
			temp = checkTable(sql, "app-group-config.secondary.table-name", webappSecondaryGroupTable);
			temp = temp & checkColumn(sql, "app-group-config.secondary.user-id-column", webappSecondaryGroupTable, webappSecondaryGroupUserIDColumn);
			temp = temp & checkColumn(sql, "app-group-config.secondary.group-id-column", webappSecondaryGroupTable, webappSecondaryGroupGroupIDColumn);
			if (webappSecondaryGroupStorageMethod.startsWith("mul") || webappSecondaryGroupStorageMethod.startsWith("key"))
			{
				temp = temp & checkColumn(sql, "app-group-config.secondary.key-column", webappSecondaryGroupTable, webappSecondaryGroupKeyColumn);
				if (temp)
				{
					checkKeyColumnForKey(sql, "app-group-config.secondary.key-name", webappSecondaryGroupTable, webappSecondaryGroupKeyColumn, webappSecondaryGroupKeyName);
				}
			}
			if (!temp)
			{
				webappSecondaryGroupEnabled = false;
				log.warning("Web application secondary groups disabled due to prior errors.");
			}
		}
		
		if (simpleSynchronizationEnabled && webappPrimaryGroupEnabled == false && webappSecondaryGroupEnabled == false)
		{
			simpleSynchronizationEnabled = false;
			log.severe("Simple synchronization disabled due to prior errors.");
		}

		// This one needs to be performed after the one above, in case the one above disables sync.
		if (simpleSynchronizationEnabled && checkSuperUserID(sql) == false)
		{
			simpleSynchronizationEnabled = false;
			log.severe("Simple synchronization disabled due to prior errors.");
		}
		
		if (banSynchronizationEnabled && banSynchronizationMethod.startsWith("tab"))
		{
			temp = checkTable(sql, "ban-synchronization.table-name", banSynchronizationTableName);
			temp = temp & checkColumn(sql, "ban-synchronization.banned-user-id-column", banSynchronizationTableName, banSynchronizationUserIDColumn);
			temp = temp & checkColumn(sql, "ban-synchronization.ban-reason-column", banSynchronizationTableName, banSynchronizationReasonColumn);
			temp = temp & checkColumn(sql, "ban-synchronization.ban-start-column", banSynchronizationTableName, banSynchronizationStartTimeColumn);
			temp = temp & checkColumn(sql, "ban-synchronization.ban-end-column", banSynchronizationTableName, banSynchronizationEndTimeColumn);
			if (!temp)
			{
				log.severe("Temporarily disabling ban synchronization due to previous errors.");
				banSynchronizationEnabled = false;
			}
		}
		
		if (playerDataRequired)
		{
			File playerData = new File(plugin.getDataFolder(), "Players");

			if (playerData.exists())
			{
				if (!playerData.isDirectory())
				{
					log.severe("There is a file named Players in the CommunityBridge plugin folder preventing creation of the data directory.");
					// Here we disable anything that relies on the player data folder.
					simpleSynchronizationEnabled = false;
					webappPrimaryGroupEnabled = false;
					webappSecondaryGroupEnabled = false;
				}
			}
			else
			{
				boolean success = playerData.mkdirs();
				if (!success)
				{
					log.severe("Error when creating the CommunityBridge/Players folder.");
					// Here we disable anything that relies on the player data folder.
					simpleSynchronizationEnabled = false;
					webappPrimaryGroupEnabled = false;
					webappSecondaryGroupEnabled = false;
				}
			}
		}

		return status;
	}

	/**
	 * Check to see if a given column exists on a specific table.
	 *
	 * @param SQL SQL query object.
	 * @param keyName
	 * @param String containing the name of the table.
	 * @param String containing the name of the column.
	 * @return boolean True if the column exists on the table.
	 */
	private boolean checkColumn(SQL sql, String keyName, String tableName, String columnName)
	{
		ResultSet result;
		String errorBase;
		errorBase = "Error while checking '" + keyName
							+ "' set to '" + columnName + "': ";

		if (columnName.isEmpty())
		{
			log.severe(errorBase + "Empty column name.");
			return false;
		}
		
		try
		{
			result = sql.sqlQuery("SHOW COLUMNS FROM `" + tableName	+ "` LIKE '" + columnName + "'");

			if (result != null && result.next())
			{
				return true;
			}
			else
			{
				log.severe(errorBase + "Column does not exist.");
				return false;
			}
		}
		catch (SQLException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (MalformedURLException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (InstantiationException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (IllegalAccessException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
	}

	private void checkKeyColumnForKey(SQL sql, String yamlKeyName, String tableName,	String keyColumn,	String keyName)
	{
		String errorBase = "Error while checking " + yamlKeyName + ": ";
		String query = "SELECT COUNT(*) FROM `" + tableName + "` "
								 + "WHERE `" + keyColumn + "` = '" + keyName + "'";

		try
		{
			ResultSet result = sql.sqlQuery(query);

			if (result.next())
			{
				if (result.getInt(1) == 0)
				{
					log.warning("There are no rows containing " + keyName
												 + " in the " + keyColumn + " column, on the "
												 + tableName + " table.");
				}
			}
			else
			{
					log.warning("Empty result set while checking: " + yamlKeyName);
			}
		}
		catch (SQLException e)
		{
			log.severe(errorBase + e.getMessage());
		}
		catch (MalformedURLException e)
		{
			log.severe(errorBase + e.getMessage());
		}
		catch (InstantiationException e)
		{
			log.severe(errorBase + e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			log.severe(errorBase + e.getMessage());
		}
	}

	/**
	 * Check to see if a table exists.
	 *
	 * @param SQL An SQL query object.
	 * @param String containing the category label.
	 * @param String containing the name of the table to check.
	 * @return boolean True if the table exists.
	 */
	private boolean checkTable(SQL sql, String keyName, String tableName)
	{
		ResultSet result;
		String errorBase;
		errorBase = "Error while checking '" + keyName
							+ "' set to '" + tableName + "': ";

		try
		{
			result = sql.sqlQuery("SHOW TABLES LIKE '" + tableName + "'");

			if (result != null)
			{
				if (result.next())
				{
					return true;
				}
				log.severe(errorBase + "Table does not exist.");
			}
			return false;
		}
		catch (SQLException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (MalformedURLException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (InstantiationException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
		catch (IllegalAccessException e)
		{
			log.severe(errorBase + e.getMessage());
			return false;
		}
	}

	public String getGroupNameByGroupID(String groupID)
	{
		return (String)simpleSynchronizationGroupMap.get(groupID);
	}

	public String getWebappGroupIDbyGroupName(String groupName)
	{
		for (Entry<String, Object> entry: simpleSynchronizationGroupMap.entrySet())
		{
			if (groupName.equalsIgnoreCase((String)entry.getValue()))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Loads the configuration information from the yaml file.
	 *
	 * @param CommunityBridge The plugin object for this plugin.
	 */
	private void load()
	{
		plugin.saveDefaultConfig();
		loadSettings(plugin.getConfig());
	}

	/**
	 * Loads the individual settings into our config object from the YAML
	 * configuration.
	 * 
	 * @param FileConfiguration The file configuration to load the settings from. 
	 */
	private void loadSettings(FileConfiguration config)
	{
		logLevel = config.getString("general.log-level", "config");
		// We do this here so that the rest of the config methods can use the
		// logger with the level set as the user likes it.
		log.setLevel(logLevel);

		usePluginMetrics = config.getBoolean("general.plugin-metrics", true);
		useAchievements = config.getBoolean("general.use-achievements", false);
		if (useAchievements)
		{
			economyEnabled = true;
		}
		permissionsSystem = config.getString("general.permissions-system", "");

		autoEveryUnit = config.getString("general.auto-every-unit", "ticks").toLowerCase();
		autoSync = config.getBoolean("general.auto-sync", false);
		autoSyncEvery = config.getLong("general.auto-sync-every", 24000L);
		syncDuringJoin = config.getBoolean("general.sync-during-join", true);
		syncDuringQuit = config.getBoolean("general.sync-during-quit", true);

		applicationURL = config.getString("general.application-url", "http://www.example.org/");
		
		loadDateFormat(config);
		
		// Database Section
		databaseHost = config.getString("database.hostname", "");
		databasePort = config.getString("database.port", "");
		databaseName = config.getString("database.name", "");
		databaseUsername = config.getString("database.username", "");
		databasePassword = config.getString("database.password", "");

		// Linking Section
		linkingKickUnregistered = config.getBoolean("player-user-linking.kick-unregistered", false);
		linkingAutoRemind = config.getBoolean("player-user-linking.auto-remind", false);
		linkingAutoEvery = config.getLong("player-user-linking.auto-remind-every", 12000L);
		linkingNotifyRegistered = config.getBoolean("player-user-linking.notify-registered-player", true);
		linkingNotifyUnregistered = config.getBoolean("player-user-linking.notify-unregistered-player", true);

		linkingUnregisteredGroup = config.getString("player-user-linking.unregistered-player-group", "");
		linkingRegisteredGroup = config.getString("player-user-linking.registered-player-group", "");
		linkingNotifyPlayerGroup = config.getBoolean("player-user-linking.notify-player-of-group", false);
		linkingRegisteredFormerUnregisteredOnly = config.getBoolean("player-user-linking.registered-former-unregistered-only", false);

		linkingUsesKey = config.getBoolean("player-user-linking.uses-key", false);
		linkingTableName = config.getString("player-user-linking.table-name", "");
		linkingUserIDColumn = config.getString("player-user-linking.user-id-column", "");
		linkingPlayerNameColumn = config.getString("player-user-linking.playername-column", "");

		linkingKeyName = config.getString("player-user-linking.key-name", "");
		linkingKeyColumn = config.getString("player-user-linking.key-column", "");
		linkingValueColumn = config.getString("player-user-linking.value-column", "");

		avatarEnabled = config.getBoolean("app-avatar-config.enabled");
		if (avatarEnabled)
		{
			avatarTableName = config.getString("app-avatar-config.table-name", "");
			avatarUserIDColumn = config.getString("app-avatar-config.user-id-column", "");
			avatarAvatarColumn = config.getString("app-avatar-config.avatar-column", "");
		}

		postCountEnabled = config.getBoolean("app-post-count-config.enabled", false);
		if (postCountEnabled)
		{
			postCountTableName = config.getString("app-post-count-config.table-name", "");
			postCountUserIDColumn = config.getString("app-post-count-config.user-id-column", "");
			postCountPostCountColumn = config.getString("app-post-count-config.post-count-column", "");
		}

		// Requirements Section
		requireAvatar = config.getBoolean("requirement.avatar", false) && avatarEnabled;
		requireMinimumPosts = config.getBoolean("requirement.post-count.enabled", false) && postCountEnabled;
		requirePostsPostCount = config.getInt("requirement.post-count.minimum", 0);

		// Statistics Tracking Settings
		statisticsEnabled = config.getBoolean("statistics.enabled", false);

		statisticsTableName = config.getString("statistics.table-name", "");
		statisticsUserIDColumn = config.getString("statistics.user-id-column", "");
		statisticsUsesKey = config.getBoolean("statistics.uses-key", false);

		if (statisticsUsesKey)
		{
			statisticsKeyColumn = config.getString("statistics.key-column", "");
			statisticsValueColumn = config.getString("statistics.value-column", "");
		}

		statisticsUsesInsert = config.getBoolean("statistics.insert.enabled", false);
		
		if (statisticsUsesInsert)
		{
			statisticsInsertMethod = config.getString("statistics.insert.method", "generic").toLowerCase();
			if (!statisticsInsertMethod.startsWith("gen") && !statisticsInsertMethod.startsWith("smf"))
			{
				log.severe("Invalid statistics insert before method: " + statisticsInsertMethod);
				log.severe("Disabling statistics until the problem is corrected.");
				statisticsEnabled = false;
			}
			statisticsThemeIDColumn = config.getString("statistics.insert.theme-id-column", "id_theme");
			statisticsThemeID = config.getString("statistics.insert.theme-id", "1");
		}
		
		onlineStatusEnabled = config.getBoolean("statistics.trackers.online-status.enabled", false);
		onlineStatusColumnOrKey = config.getString("statistics.trackers.online-status.column-or-key-name", "");
		onlineStatusValueOnline = config.getString("statistics.trackers.online-status.online-value", "");
		onlineStatusValueOffline = config.getString("statistics.trackers.online-status.offline-value", "");

		lastonlineEnabled = config.getBoolean("statistics.trackers.last-online.enabled", false);
		lastonlineColumnOrKey = config.getString("statistics.trackers.last-online.column-or-key-name", "");
		lastonlineFormattedColumnOrKey = config.getString("statistics.trackers.last-online.formatted-column-or-key-name", "");

		gametimeEnabled = config.getBoolean("statistics.trackers.game-time.enabled", false);
		gametimeColumnOrKey = config.getString("statistics.trackers.game-time.column-or-key-name", "");
		gametimeFormattedColumnOrKey = config.getString("statistics.trackers.game-time.formatted-column-or-key-name", "");

		levelEnabled = config.getBoolean("statistics.trackers.level.enabled", false);
		levelColumnOrKey = config.getString("statistics.trackers.level.column-or-key-name", "");

		totalxpEnabled = config.getBoolean("statistics.trackers.total-xp.enabled", false);
		totalxpColumnOrKey = config.getString("statistics.trackers.total-xp.column-or-key-name", "");

		currentxpEnabled = config.getBoolean("statistics.trackers.current-xp.enabled", false);
		currentxpColumnOrKey = config.getString("statistics.trackers.current-xp.column-or-key-name", "");
		currentxpFormattedColumnOrKey = config.getString("statistics.trackers.current-xp.formatted-column-or-key-name", "");

		healthEnabled = config.getBoolean("statistics.trackers.health.enabled", false);
		healthColumnOrKey = config.getString("statistics.trackers.health.column-or-key-name", "");

		lifeticksEnabled = config.getBoolean("statistics.trackers.lifeticks.enabled", false);
		lifeticksColumnOrKey = config.getString("statistics.trackers.lifeticks.column-or-key-name", "");
		lifeticksFormattedColumnOrKey = config.getString("statistics.trackers.lifeticks.formatted-column-or-key-name", "");

		walletEnabled = config.getBoolean("statistics.trackers.wallet.enabled", false);
		walletColumnOrKey = config.getString("statistics.trackers.wallet.column-or-key-name", "");

		// Web App group configuration
		// - Primary
		webappPrimaryGroupEnabled = config.getBoolean("app-group-config.primary.enabled", false);
		webappPrimaryGroupTable = config.getString("app-group-config.primary.table-name", "");
		webappPrimaryGroupUserIDColumn = config.getString("app-group-config.primary.user-id-column", "");
		webappPrimaryGroupUsesKey = config.getBoolean("app-group-config.primary.uses-key", false);
		webappPrimaryGroupGroupIDColumn = config.getString("app-group-config.primary.group-id-column", "");
		webappPrimaryGroupKeyName = config.getString("app-group-config.primary.key-name", "");
		webappPrimaryGroupKeyColumn = config.getString("app-group-config.primary.key-column", "");

		webappSecondaryGroupEnabled = config.getBoolean("app-group-config.secondary.enabled", false);
		webappSecondaryGroupTable = config.getString("app-group-config.secondary.table-name", "");
		webappSecondaryGroupUserIDColumn = config.getString("app-group-config.secondary.user-id-column", "");
		webappSecondaryGroupGroupIDColumn = config.getString("app-group-config.secondary.group-id-column", "");
		webappSecondaryGroupKeyName = config.getString("app-group-config.secondary.key-name", "");
		webappSecondaryGroupKeyColumn = config.getString("app-group-config.secondary.key-column", "");
		webappSecondaryGroupGroupIDDelimiter = config.getString("app-group-config.secondary.group-id-delimiter", "");
		// junction, single-column, key-value
		webappSecondaryGroupStorageMethod = config.getString("app-group-config.secondary.storage-method", "").toLowerCase();

		// Simple synchronization
		simpleSynchronizationSuperUserID = config.getString("simple-synchronization.super-user-user-id", "");
		simpleSynchronizationEnabled = config.getBoolean("simple-synchronization.enabled", false);
		simpleSynchronizationDirection = config.getString("simple-synchronization.direction", "two-way").toLowerCase();
		simpleSynchronizationFirstDirection = config.getString("simple-synchronization.first-direction", "two-way").toLowerCase();
		simpleSynchronizationPrimaryGroupNotify = config.getBoolean("simple-synchronization.primary-group-change-notify", false);
		simpleSynchronizationGroupMap = config.getConfigurationSection("simple-synchronization.group-mapping").getValues(false);
		simpleSynchronizationGroupsTreatedAsPrimary = config.getStringList("simple-synchronization.groups-treated-as-primary");

		// Ban synchronization
		banSynchronizationEnabled = config.getBoolean("ban-synchronization.enabled", false);
		banSynchronizationMethod = config.getString("ban-synchronization.method", "table").toLowerCase();
		banSynchronizationGroupIDs = config.getStringList("ban-synchronization.ban-group-ids");
		banSynchronizationTableName = config.getString("ban-synchronization.table", "");
		banSynchronizationUserIDColumn = config.getString("ban-synchronization.banned-user-id-column", "");
		banSynchronizationReasonColumn = config.getString("ban-synchronization.ban-reason-column", "");
		banSynchronizationStartTimeColumn = config.getString("ban-synchronization.ban-start-column", "");
		banSynchronizationEndTimeColumn = config.getString("ban-synchronization.ban-end-column", "");
		
		// These are calculated from settings above.
		groupSynchronizationActive = simpleSynchronizationEnabled && (webappPrimaryGroupEnabled || webappSecondaryGroupEnabled);
		playerDataRequired = groupSynchronizationActive;
		permissionsSystemRequired = !linkingUnregisteredGroup.isEmpty() || !linkingRegisteredGroup.isEmpty() || groupSynchronizationActive;
	}

	/**
	 * Soft disables any features that depend on a Permissions System.
	 */
	public void disableFeaturesDependentOnPermissions()
	{
		groupSynchronizationActive = false;
		simpleSynchronizationEnabled = false;
		linkingUnregisteredGroup = "";
		linkingRegisteredGroup = "";
	}
	
	/**
	 * Loads the messages from the message file.
	 *
	 * @param CommunityBridge This plugin's plugin object.
	 */
	private void loadMessages()
	{
		final String messageFilename = "messages.yml";
		Map<String, Object> values;

		YamlConfiguration messagesConfig = obtainYamlConfigurationHandle(messageFilename);

		Set<String> rootSet = messagesConfig.getKeys(false);
		
		if (rootSet.isEmpty())
		{
			log.severe("The messages.yml file is empty. Replace with a valid file and reload.");
			return;
		}
		else if (rootSet.size() > 1)
		{
			log.warning("Multiple top level keys in messages.yml. Assuming the first top level key is the correct one.");
		}

		locale = rootSet.iterator().next();
		log.info("Detected locale: " + locale);
		
		ConfigurationSection configSection = messagesConfig.getConfigurationSection(locale);
		
		// Read the key-value pairs from the configuration
		values = configSection.getValues(false);
		
		if (values.isEmpty())
		{
			log.severe("Language identifier found but no message keys found. Replace with a valid file and reload.");
			return;
		}
		
		messages.clear();
		// Store them in our own HashMap.
		for (Map.Entry<String, Object> entry : values.entrySet())
		{
			String message = (String)entry.getValue();
			message = message.replace("~APPURL~", applicationURL);
			message = message.replace("~MINIMUMPOSTCOUNT~", Integer.toString(requirePostsPostCount));

			messages.put(entry.getKey(), message);
		}
	}
	
	private void loadAchievements()
	{
		final String filename = "achievements.yml";
		YamlConfiguration achievementConfig;

		achievementConfig = obtainYamlConfigurationHandle(filename);

		Set<String> rootSet = achievementConfig.getKeys(false);
		
		if (rootSet.isEmpty())
		{
			log.warning("The achievements.yml file is empty.");
			return;
		}
		
		for (String key : rootSet)
		{
			if (key.equalsIgnoreCase("avatar"))
			{
				AchievementAvatar achievement = new AchievementAvatar();
				achievement.loadFromYamlPath(achievementConfig, key);
				achievements.add(achievement);
			}
			else if (key.equalsIgnoreCase("groups"))
			{
				ConfigurationSection groupsSection = achievementConfig.getConfigurationSection(key);
				if (groupsSection == null)
				{
					continue;
				}
				Set<String> groupNames = groupsSection.getKeys(false);
				for (String groupName : groupNames)
				{
					AchievementGroup achievement = new AchievementGroup();
					achievement.setGroupName(groupName);
					achievement.loadFromYamlPath(achievementConfig, key + "." + groupName);
					achievements.add(achievement);
				}
			}
			else if (key.equalsIgnoreCase("post-counts"))
			{
				ConfigurationSection postCountSection = achievementConfig.getConfigurationSection(key);
				if (postCountSection == null)
				{
					continue;
				}
				Set<String> postCounts = postCountSection.getKeys(false);
				for (String postCount : postCounts)
				{
					AchievementPostCount achievement = new AchievementPostCount();
					achievement.setPostCount(postCount);
					achievement.loadFromYamlPath(achievementConfig, key + "." + postCount);
					achievements.add(achievement);
				}
			}
			else if (key.equalsIgnoreCase("section-post-counts"))
			{
				ConfigurationSection sectionsSection = achievementConfig.getConfigurationSection(key);
				if (sectionsSection == null)
				{
					continue;
				}
				Set<String> sections = sectionsSection.getKeys(false);
				for (String sectionID : sections)
				{
					ConfigurationSection postCountSection = sectionsSection.getConfigurationSection(sectionID);
					if (postCountSection == null)
					{
						continue;
					}
					Set<String> postCounts = postCountSection.getKeys(false);
					for (String postCount : postCounts)
					{
						AchievementSectionPostCount achievement = new AchievementSectionPostCount();
						achievement.setPostCount(postCount);
						achievement.setSectionID(sectionID);
						achievement.loadFromYamlPath(achievementConfig, key + "." + sectionID + "." + postCount);
						achievements.add(achievement);
					}
				}
			}
		}
	}

	/**
	 * Reloads the configuration either from config.yml or specified file.
	 * 
	 * @param filename File to load from, will default to config.yml if null/empty.
	 * @return On error, the error message. Otherwise will be null.
	 */
	public String reload(String filename)
	{
		loadMessages();
		loadAchievements();
		
		if (filename == null || filename.isEmpty() || filename.equals("config.yml"))
		{
			plugin.deactivate();
			plugin.reloadConfig();
			load();
			
			plugin.activate();
			return null;
		}

		File configFile = new File(plugin.getDataFolder(), filename);

		if (configFile.exists())
		{
			plugin.deactivate();
			loadSettings(YamlConfiguration.loadConfiguration(configFile));
			plugin.activate();
			return null;
		}
		else
		{
			return "Specified file does not exist. Reload canceled.";
		}
	}

	/**
	 * Method for printing the configuration out to the logging system.
	 *
	 */
	public final void report()
	{
		// General Section
		log.config(    "Log level                            : " + logLevel);
		log.config(    "Plugin metrics enabled               : " + usePluginMetrics);
		log.config(    "Use achievements                     : " + useAchievements);
		log.config(    "Permissions system                   : " + permissionsSystem);
		log.config(    "Economy enabled                      : " + economyEnabled);
		log.config(    "Autosync                             : " + autoSync);
		if (autoSync)
		{
			log.config(  "Autosync every                       : " + autoSyncEvery + " " + autoEveryUnit);
		}
		
		log.config(    "Synchronize during join event        : " + syncDuringJoin);
		log.config(    "Synchronize during quit event        : " + syncDuringQuit);
		
		log.config(    "Application url                      : " + applicationURL);
		log.config(    "Date Format                          : " + dateFormatString);

		// Database Section
		log.config(    "Database hostname                    : " + databaseHost);
		log.config(    "Database port                        : " + databasePort);
		log.config(    "Database name                        : " + databaseName);
		log.config(    "Database username                    : " + databaseUsername);

		// Linking Section
		log.config(    "Linking auto reminder                : " + linkingAutoRemind);
		if (linkingAutoRemind)
		{
			log.config(  "Linking auto reminder every          : " + linkingAutoEvery + " " + autoEveryUnit);
		}
		log.config(    "Linking notify registered            : " + linkingNotifyRegistered);
		log.config(    "Linking notify unregistered          : " + linkingNotifyUnregistered);
		log.config(    "Linking kick unregistered            : " + linkingKickUnregistered);

		log.config(    "Linking unregistered group           : " + linkingUnregisteredGroup);
		log.config(    "Linking registered group             : " + linkingRegisteredGroup);
		log.config(    "Linking notify player of group       : " + linkingNotifyPlayerGroup);
		log.config(    "Linking reg former unregistered only : " + linkingRegisteredFormerUnregisteredOnly);
		log.config(    "Linking uses key-value pair          : " + linkingUsesKey);
		log.config(    "Linking table name                   : " + linkingTableName);
		log.config(    "Linking user ID column               : " + linkingUserIDColumn);
		if (linkingUsesKey)
		{
			log.config(  "Linking key-value pair key name      : " + linkingKeyName);
			log.config(  "Linking key-value pair key column    : " + linkingKeyColumn);
			log.config(  "Linking key-value pair value column  : " + linkingValueColumn);
		}
		else
		{
			log.config(  "Linking player name column           : " + linkingPlayerNameColumn);
		}

		log.config(    "Avatars config enabled               : " + avatarEnabled);
		if (avatarEnabled)
		{
			log.config(  "Avatar table name                    : " + avatarTableName);
			log.config(  "Avatar user ID column                : " + avatarUserIDColumn);
			log.config(  "Avatar avatar column                 : " + avatarAvatarColumn);
		}
		
		log.config(    "Post count config enabled            : " + postCountEnabled);
		if (postCountEnabled)
			log.config(  "Post count table name                : " + postCountTableName);
			log.config(  "Post count user ID column            : " + postCountUserIDColumn);
			log.config(  "Post count post count column         : " + postCountPostCountColumn);

		log.config(    "Require avatars                      : " + requireAvatar);
		log.config(    "Require minimum posts                : " + requireMinimumPosts);
		if (requireMinimumPosts)
		{
			log.config(  "Require minimum post count           : " + requirePostsPostCount);
		}

		log.config(    "Tracking statistics                  : " + statisticsEnabled);
		if (statisticsEnabled)
		{
			log.config(  "Tracking table name                  : " + statisticsTableName);
			log.config(  "Tracking user ID column              : " + statisticsUserIDColumn);
			log.config(  "Tracking uses key                    : " + statisticsUsesKey);
			if (statisticsUsesKey)
			{
				log.config("Tracking key column                  : " + statisticsKeyColumn);
				log.config("Tracking value column                : " + statisticsValueColumn);
			}
			log.config(  "Tracking uses insert                 : " + statisticsUsesInsert);
			if (statisticsUsesInsert)
			{
				log.config("Tracking insert method               : " + statisticsInsertMethod);
				log.config("Tracking insert theme column         : " + statisticsThemeIDColumn);
				log.config("Tracking insert theme ID             : " + statisticsThemeID);
			}
			log.config(  "Tracking online status               : " + onlineStatusEnabled);
			if (onlineStatusEnabled)
			{
				log.config("Tracking online status column/key    : " + onlineStatusColumnOrKey);
				log.config("Tracking online status online value  : " + onlineStatusValueOnline);
				log.config("Tracking online status offline value : " + onlineStatusValueOffline);
			}
			log.config(  "Tracking last online                 : " + lastonlineEnabled);
			if (lastonlineEnabled)
			{
				log.config("Tracking last online column/key      : " + lastonlineColumnOrKey);
				log.config("Tracking last online formatted co/key: " + lastonlineFormattedColumnOrKey);
			}
			log.config(  "Tracking game time                   : " + gametimeEnabled);
			if (gametimeEnabled)
			{
				log.config("Tracking game time column/key        : " + gametimeColumnOrKey);
				log.config("Tracking game time formatted co/key  : " + gametimeFormattedColumnOrKey);
			}
			log.config(  "Tracking level                       : " + levelEnabled);
			if (levelEnabled)
			{
				log.config("Tracking level column/key            : " + levelColumnOrKey);
			}
			if (totalxpEnabled)
			{
				log.config("Tracking total XP column/key         : " + totalxpColumnOrKey);
			}
			if (currentxpEnabled)
			{
				log.config("Tracking current XP column/key       : " + currentxpColumnOrKey);
				log.config("Tracking current XP formatted co/key : " + currentxpFormattedColumnOrKey);
			}
			if (lifeticksEnabled)
			{
				log.config("Tracking lifeticks column/key        : " + lifeticksColumnOrKey);
				log.config("Tracking lifeticks formatted co/key  : " + lifeticksFormattedColumnOrKey);
			}
			if (healthEnabled)
			{
				log.config("Tracking health column/key           : " + healthColumnOrKey);
			}
			if (walletEnabled)
			{
				log.config("Tracking wallet column/key           : " + walletColumnOrKey);
			}
		}

		if (webappPrimaryGroupEnabled)
		{
			log.config(  "Primary group table                  : " + webappPrimaryGroupTable);
			log.config(  "Primary group user id column         : " + webappPrimaryGroupUserIDColumn);
			log.config(  "Primary group group id column        : " + webappPrimaryGroupGroupIDColumn);
			log.config(  "Primary group uses key               : " + webappPrimaryGroupUsesKey);
			if (webappPrimaryGroupUsesKey)
			{
				log.config("Primary group key name               : " + webappPrimaryGroupKeyName);
				log.config("Primary group key column             : " + webappPrimaryGroupKeyColumn);
			}
		}

		if (webappSecondaryGroupEnabled)
		{
			log.config(  "Secondary group table                : " + webappSecondaryGroupTable);
			log.config(  "Secondary group user id column       : " + webappSecondaryGroupUserIDColumn);
			log.config(  "Secondary group group id column      : " + webappSecondaryGroupGroupIDColumn);
			log.config(  "Secondary group storage method       : " + webappSecondaryGroupStorageMethod);

			if (webappSecondaryGroupStorageMethod.startsWith("sin") || webappSecondaryGroupStorageMethod.startsWith("key"))
			{
				log.config("Secondary group id delimiter         : " + webappSecondaryGroupGroupIDDelimiter);
			}

			if (webappSecondaryGroupStorageMethod.startsWith("mul") || webappSecondaryGroupStorageMethod.startsWith("key"))
			{
				log.config("Secondary group key name             : " + webappSecondaryGroupKeyName);
				log.config("Secondary group key column           : " + webappSecondaryGroupKeyColumn);
			}
		}

		log.config(    "Simple synchronization enabled       : " + simpleSynchronizationEnabled);
		if (simpleSynchronizationEnabled)
		{
			log.config(  "Simple synchronization direction     : " + simpleSynchronizationDirection);
			log.config(  "Simple synchronization notification  : " + simpleSynchronizationPrimaryGroupNotify);
			log.config(  "Simple synchronization P-groups      : " + simpleSynchronizationGroupsTreatedAsPrimary.toString());
		}
		
		log.config(    "Ban synchronization enabled          : " + banSynchronizationEnabled);
		if (banSynchronizationEnabled)
		{
			log.config(  "Ban synchronization method           : " + banSynchronizationMethod);
			log.config(  "Ban synchronization group IDs        : " + banSynchronizationGroupIDs);
			log.config(  "Ban synchronization table name       : " + banSynchronizationTableName);
			log.config(  "Ban synchronization user ID column   : " + banSynchronizationUserIDColumn);
			log.config(  "Ban synchronization reason column    : " + banSynchronizationReasonColumn);
			log.config(  "Ban synchronization start time column: " + banSynchronizationStartTimeColumn);
			log.config(  "Ban synchronization end time column  : " + banSynchronizationEndTimeColumn);
		}
	}

	private boolean checkSuperUserID(SQL sql)
	{
		String errorBase = "Error while checking super user user id: ";
		String query = "SELECT `" + linkingUserIDColumn + "`"
									 + " FROM `" + linkingTableName + "`"
									 + " WHERE `" + linkingUserIDColumn + "` = '" + simpleSynchronizationSuperUserID + "'";
	
		if (simpleSynchronizationSuperUserID.isEmpty())
		{
			log.severe("The super-user's user ID setting is not set.");
			return false;
		}
		
		try
		{
			ResultSet result = sql.sqlQuery(query);
			if (result == null || result.next() == false || result.getString(linkingUserIDColumn).isEmpty())
			{
				log.severe("The super-user's user ID not found.");
				return false;
			}
			return true;
		}
		catch (SQLException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (MalformedURLException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (InstantiationException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (IllegalAccessException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
	}

	private void loadDateFormat(FileConfiguration config)
	{
		dateFormatString = config.getString("general.date-format", "yyyy-MM-dd hh:mm:ss a");
		try
		{
			dateFormat = new SimpleDateFormat(dateFormatString);
		}
		catch (IllegalArgumentException exception)
		{
			log.warning("Invalid date format: " + exception.getMessage());
			dateFormatString = "yyyy-MM-dd hh:mm:ss a";
			dateFormat = new SimpleDateFormat(dateFormatString);
		}
	}

	private YamlConfiguration obtainYamlConfigurationHandle(final String filename)
	{
		final File dataFolder = plugin.getDataFolder();
		File file = new File(dataFolder, filename);
		
		if (!file.exists())
		{
			plugin.saveResource(filename, false);
			file = new File(dataFolder, filename);
		}
		
		return YamlConfiguration.loadConfiguration(file);
	}
}
