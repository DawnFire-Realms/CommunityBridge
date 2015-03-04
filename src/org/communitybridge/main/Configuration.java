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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.communitybridge.utility.Log;
import org.communitybridge.achievement.Achievement;
import org.communitybridge.achievement.AchievementAvatar;
import org.communitybridge.achievement.AchievementGroup;
import org.communitybridge.achievement.AchievementPostCount;
import org.communitybridge.achievement.AchievementSectionPostCount;
import org.communitybridge.permissionhandlers.PermissionHandler;
import org.communitybridge.permissionhandlers.PermissionHandlerBPermissions;
import org.communitybridge.permissionhandlers.PermissionHandlerGroupManager;
import org.communitybridge.permissionhandlers.PermissionHandlerPermissionsBukkit;
import org.communitybridge.permissionhandlers.PermissionHandlerPermissionsEx;
import org.communitybridge.permissionhandlers.PermissionHandlerVault;
import org.communitybridge.permissionhandlers.PermissionHandlerZPermissions;

public class Configuration
{
	private static final String DATABASE_CONFIGURATION_PROBLEM = "Database configuration problem. Reload canceled.";

	private Environment environment;
	private CommunityBridge plugin;
	private Log log;
	private SQL sql;

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
	public String databaseBindingAddress;

	// Linking Section
	public long linkingAutoEvery;
	public boolean linkingAutoRemind;
	public boolean linkingNotifyRegistered;
	public boolean linkingNotifyUnregistered;
	public boolean linkingKickUnregistered;

	public String linkingMethod;
	public String linkingUnregisteredGroup;
	public String linkingRegisteredGroup;
	public boolean linkingNotifyPlayerGroup;
	public boolean linkingRegisteredFormerUnregisteredOnly;
	public boolean linkingUnregisterFormerRegistered;

	public boolean linkingUsesKey;
	public String linkingTableName;
	public String linkingUserIDColumn;
	public String linkingIdentifierColumn;
	public String linkingKeyName;
	public String linkingKeyColumn;
	public String linkingValueColumn;

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
	public Map<String, Object> webappSecondaryAdditionalColumns = new HashMap<String, Object>();

	// junction, single-column, key-value, multiple-key-value
	public String webappSecondaryGroupStorageMethod;

	// Simple synchronization
	public boolean simpleSynchronizationEnabled;
	public boolean simpleSynchronizationPrimaryGroupNotify;
	public String simpleSynchronizationDirection;
	public String simpleSynchronizationFirstDirection;
	public String simpleSynchronizationSuperUserID;
	public Map<String, Object> simpleSynchronizationGroupMap = new HashMap<String, Object>();
	public List<String> simpleSynchronizationGroupsTreatedAsPrimary = new ArrayList<String>();
	public List<String> simpleSynchronizationWebappSecondaryGroupsTreatedAsPrimary = new ArrayList<String>();

	// Ban synchronization
	public boolean banSynchronizationEnabled;
	public String banSynchronizationDirection;
	public String banSynchronizationMethod;

	// Both user and table Methods
	public String banSynchronizationTableName;
	public String banSynchronizationUserIDColumn;

	// Group Method
	public String banSynchronizationBanGroup;
	public String banSynchronizationBanGroupType;

	// User Method
	public String banSynchronizationBanColumn;
	public String banSynchronizationValueBanned;
	public String banSynchronizationValueNotBanned;

	// Table Method
	public String banSynchronizationReasonColumn;
	public String banSynchronizationStartTimeColumn;
	public String banSynchronizationEndTimeColumn;
	public String banSynchronizationBanGroupIDColumn;
	public String banSynchronizationBanGroupID;

	// These are not in the config.yml. They are calculated.
	public boolean playerDataRequired;
	public boolean permissionsSystemRequired;
	public boolean groupSynchronizationActive;
	public boolean economyEnabled;

	public Configuration(Environment environment)
	{
		this.environment = environment;
		this.plugin = environment.getPlugin();
		this.log = environment.getLog();
		load();

		if (enableSQL(false) == false)
		{
			plugin.deactivate();
			return;
		}

		loadMessages();
		loadAchievements();
		report();
	}

	public boolean analyze()
	{
		boolean status = true;
		boolean temp;

		// Linking table section.
		if (!linkingMethod.startsWith("bot") && !linkingMethod.startsWith("uui") && !linkingMethod.startsWith("nam"))
		{
			status = false;
			log.severe("Invalid linking method in the player-user-linking section of the configuration.");
		}

		status = status & checkTable("player-user-linking.table-name", linkingTableName);
		if (status)
		{
			status = status & checkColumn("player-user-linking.user-id-column", linkingTableName, linkingUserIDColumn);
			if (linkingUsesKey)
			{
				temp = checkColumn("player-user-linking.key-column", linkingTableName , linkingKeyColumn);
				status = status & temp;
				if (temp)
				{
					checkKeyColumnForKey("player-user-linking.key-name", linkingTableName, linkingKeyColumn,	linkingKeyName);
				}

				status = status & checkColumn("player-user-linking.value-column", linkingTableName, linkingValueColumn);
			}
			else
			{
				status = status & checkColumn("player-user-linking.identifier-column", linkingTableName, linkingIdentifierColumn);
			}
		}

		if (avatarEnabled)
		{
			temp = checkTable("app-avatar-config.table-name", avatarTableName);
			if (temp)
			{
				temp = temp & checkColumn("app-avatar-config.user-id-column", avatarTableName, avatarUserIDColumn);
				temp = temp & checkColumn("app-avatar-config.avatar-column", avatarTableName, avatarAvatarColumn);
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
			temp = checkTable("app-post-count-config.table-name", postCountTableName);
			status = status & temp;
			if (temp)
			{
				temp = temp & checkColumn("app-post-count-config.user-id-column", postCountTableName, postCountUserIDColumn);
				temp = temp & checkColumn("app-post-count-config.post-count-column", postCountTableName, postCountPostCountColumn);
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
			temp = checkTable("statistics.table-name", statisticsTableName);
			status = status & temp;
			if (temp)
			{
				status = status & checkColumn("statistics.user-id-column", statisticsTableName, statisticsUserIDColumn);

				if (statisticsUsesInsert && statisticsInsertMethod.startsWith("smf"))
				{
					status = status & checkColumn("statistics.theme-id-column", statisticsTableName, statisticsThemeIDColumn);
					checkKeyColumnForKey("statistics.theme-id", statisticsTableName, statisticsThemeIDColumn, statisticsThemeID);
				}

				if (statisticsUsesKey)
				{
					temp = checkColumn("statistics.key-column", statisticsTableName, statisticsKeyColumn);
					temp = temp & checkColumn("statistics.value-column", statisticsTableName, statisticsValueColumn);
					status = status & temp;
					if (temp)
					{
						if (onlineStatusEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.online-status.column-or-key-name", statisticsTableName, statisticsKeyColumn,	onlineStatusColumnOrKey);
						}
						if (lastonlineEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.last-online.column-or-key-name", statisticsTableName, statisticsKeyColumn,	lastonlineColumnOrKey);
							if (!lastonlineFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey("statistics.trackers.last-online.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	lastonlineFormattedColumnOrKey);
							}
						}
						if (gametimeEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.game-time.column-or-key-name", statisticsTableName, statisticsKeyColumn,	gametimeColumnOrKey);
							if (!gametimeFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey("statistics.trackers.game-time.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	gametimeFormattedColumnOrKey);
							}
							if (!lastonlineEnabled)
							{
								log.warning("Game time statistic tracker requires last online tracker to be enabled. Temporarily disabling gametime tracker.");
								gametimeEnabled = false;
							}
						}
						if (levelEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.level.column-or-key-name", statisticsTableName, statisticsKeyColumn,	levelColumnOrKey);
						}
						if (totalxpEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.total-xp.column-or-key-name", statisticsTableName, statisticsKeyColumn, totalxpColumnOrKey);
						}
						if (currentxpEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.current-xp.column-or-key-name", statisticsTableName, statisticsKeyColumn, currentxpColumnOrKey);
							if (!currentxpFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey("statistics.trackers.current-xp.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,currentxpFormattedColumnOrKey);
							}
						}
						if (healthEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.health.column-or-key-name", statisticsTableName, statisticsKeyColumn, healthColumnOrKey);
						}
						if (lifeticksEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.lifeticks.column-or-key-name", statisticsTableName, statisticsKeyColumn,	lifeticksColumnOrKey);
							if (!lifeticksFormattedColumnOrKey.isEmpty())
							{
								checkKeyColumnForKey("statistics.trackers.lifeticks.formatted-column-or-key-name", statisticsTableName, statisticsKeyColumn,	lifeticksFormattedColumnOrKey);
							}
						}
						if (walletEnabled)
						{
							checkKeyColumnForKey("statistics.trackers.wallet.column-or-key-name", statisticsTableName, statisticsKeyColumn, walletColumnOrKey);
						}
					}
				}
				else
				{
					if (onlineStatusEnabled && !checkColumn("statistics.trackers.online-status.column-or-key-name", statisticsTableName,	onlineStatusColumnOrKey))
					{
						onlineStatusEnabled = false;
					}

					if (lastonlineEnabled)
					{
						if(!checkColumn("statistics.trackers.last-online.column-or-key-name", statisticsTableName,	lastonlineColumnOrKey))
						{
							lastonlineEnabled = false;
						}
						if (!lastonlineFormattedColumnOrKey.isEmpty() && !checkColumn("statistics.trackers.last-online.formatted-column-or-key-name", statisticsTableName, lastonlineFormattedColumnOrKey))
						{
							lastonlineFormattedColumnOrKey = "";
						}
					}

					if (gametimeEnabled)
					{
						if (!checkColumn("statistics.trackers.game-time.column-or-key-name", statisticsTableName,	gametimeColumnOrKey))
						{
							gametimeEnabled = false;
						}

						if (!gametimeFormattedColumnOrKey.isEmpty() && !checkColumn("statistics.trackers.game-time.formatted-column-or-key-name", statisticsTableName, gametimeFormattedColumnOrKey))
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

					if (levelEnabled && !checkColumn("statistics.trackers.level.column-or-key-name", statisticsTableName,	levelColumnOrKey))
					{
						levelEnabled = false;
					}

					if (totalxpEnabled && !checkColumn("statistics.trackers.total-xp.column-or-key-name", statisticsTableName, totalxpColumnOrKey))
					{
						totalxpEnabled = false;
					}

					if (currentxpEnabled)
					{
						if (!checkColumn("statistics.trackers.current-xp.column-or-key-name", statisticsTableName,	currentxpColumnOrKey))
						{
							currentxpEnabled = false;
						}

						if (!currentxpFormattedColumnOrKey.isEmpty() && !checkColumn("statistics.trackers.current-xp.formatted-column-or-key-name", statisticsTableName, currentxpFormattedColumnOrKey))
						{
							currentxpFormattedColumnOrKey = "";
						}
					}

					if (healthEnabled && !checkColumn("statistics.trackers.health.column-or-key-name", statisticsTableName, healthColumnOrKey))
					{
						healthEnabled = false;
					}

					if (lifeticksEnabled)
					{
						if (!checkColumn("statistics.trackers.lifeticks.column-or-key-name", statisticsTableName,	lifeticksColumnOrKey))
						{
							lifeticksEnabled = false;
						}

						if (!lifeticksFormattedColumnOrKey.isEmpty() && !checkColumn("statistics.trackers.lifeticks.formatted-column-or-key-name", statisticsTableName, lifeticksFormattedColumnOrKey))
						{
								lifeticksFormattedColumnOrKey = "";
						}
					}

					if (walletEnabled && !checkColumn("statistics.trackers.wallet.column-or-key-name", statisticsTableName, walletColumnOrKey))
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
			temp = checkTable("app-group-config.primary.table-name", webappPrimaryGroupTable);
			temp = temp & checkColumn("app-group-config.primary.user-id-column", webappPrimaryGroupTable, webappPrimaryGroupUserIDColumn);
			temp = temp & checkColumn("app-group-config.primary.group-id-column", webappPrimaryGroupTable, webappPrimaryGroupGroupIDColumn);
			if (webappPrimaryGroupUsesKey)
			{
				temp = temp & checkColumn("app-group-config.primary.key-column", webappPrimaryGroupTable, webappPrimaryGroupKeyColumn);
				if (temp)
				{
					checkKeyColumnForKey("app-group-config.primary.key-name", webappPrimaryGroupTable, webappPrimaryGroupKeyColumn, webappPrimaryGroupKeyName);
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
			temp = checkTable("app-group-config.secondary.table-name", webappSecondaryGroupTable);
			temp = temp & checkColumn("app-group-config.secondary.user-id-column", webappSecondaryGroupTable, webappSecondaryGroupUserIDColumn);
			temp = temp & checkColumn("app-group-config.secondary.group-id-column", webappSecondaryGroupTable, webappSecondaryGroupGroupIDColumn);
			if (webappSecondaryGroupStorageMethod.startsWith("mul") || webappSecondaryGroupStorageMethod.startsWith("key"))
			{
				temp = temp & checkColumn("app-group-config.secondary.key-column", webappSecondaryGroupTable, webappSecondaryGroupKeyColumn);
				if (temp)
				{
					checkKeyColumnForKey("app-group-config.secondary.key-name", webappSecondaryGroupTable, webappSecondaryGroupKeyColumn, webappSecondaryGroupKeyName);
				}
			}
			if (webappSecondaryGroupStorageMethod.startsWith("jun"))
			{
				for (String columnName : webappSecondaryAdditionalColumns.keySet())
				{
					temp = temp & checkColumn("app-group-config.secondary.additional-columns." + columnName, webappSecondaryGroupTable, columnName);
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
		if (simpleSynchronizationEnabled && checkSuperUserID() == false)
		{
			simpleSynchronizationEnabled = false;
			log.severe("Simple synchronization disabled due to prior errors.");
		}

		if (simpleSynchronizationEnabled && webappPrimaryGroupEnabled && environment.getPermissionHandler().supportsPrimaryGroups() == false && simpleSynchronizationGroupsTreatedAsPrimary.isEmpty())
		{
			log.warning("The permissions system does not support primary groups and primary group synchronization is enabled...but there are no groups listed in the 'groups treated as primary' configuration option.");
		}

		if (banSynchronizationEnabled)
		{
			temp = checkTable("ban-synchronization.table-name", banSynchronizationTableName);
			temp = temp & checkColumn("ban-synchronization.banned-user-id-column", banSynchronizationTableName, banSynchronizationUserIDColumn);
			if (banSynchronizationMethod.startsWith("tab"))
			{
				temp = temp & checkColumnIfNotEmpty("ban-synchronization.ban-reason-column", banSynchronizationTableName, banSynchronizationReasonColumn);
				temp = temp & checkColumnIfNotEmpty("ban-synchronization.ban-start-column", banSynchronizationTableName, banSynchronizationStartTimeColumn);
				temp = temp & checkColumnIfNotEmpty("ban-synchronization.ban-end-column", banSynchronizationTableName, banSynchronizationEndTimeColumn);
				temp = temp & checkColumnIfNotEmpty("ban-synchronization.ban-group-id-column", banSynchronizationTableName, banSynchronizationBanGroupIDColumn);
			}
			else if (banSynchronizationMethod.startsWith("use"))
			{
				temp = temp & checkColumn("ban-synchronization.ban-column", banSynchronizationTableName, banSynchronizationBanColumn);
			}
			else if (banSynchronizationMethod.startsWith("gro"))
			{
				if (banSynchronizationBanGroupType.startsWith("pri"))
				{
					if (!webappPrimaryGroupEnabled)
					{
						log.severe("Need to enable web application primary group configuration for primary ban group.");
						temp = false;
					}
				}
				else if (banSynchronizationBanGroupType.startsWith("sec"))
				{
					if (!webappSecondaryGroupEnabled)
					{
						log.severe("Need to enable web application secondary group configuration for the ban group.");
						temp = false;
					}
				}
				else
				{
					log.severe("Need to specify primary or secondary group configuration for the ban group.");
					temp = false;
				}
			}
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

	private boolean checkColumnIfNotEmpty(String keyName, String tableName, String columnName)
	{
		if (columnName.isEmpty())
		{
			return true;
		}
		return checkColumn(keyName, tableName, columnName);
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
	private boolean checkColumn(String keyName, String tableName, String columnName)
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

	private void checkKeyColumnForKey(String yamlKeyName, String tableName,	String keyColumn,	String keyName)
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
	private boolean checkTable(String keyName, String tableName)
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
		if (groupName != null && !groupName.isEmpty())
		{
			for (Entry<String, Object> entry: simpleSynchronizationGroupMap.entrySet())
			{
				if (groupName.equalsIgnoreCase((String)entry.getValue()))
				{
					return entry.getKey();
				}
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
		databaseBindingAddress = config.getString("database.binding-address", "").toLowerCase();

		if (databaseBindingAddress.startsWith("min"))
		{
			databaseBindingAddress = Bukkit.getIp();
		}

		// Linking Section
		linkingMethod = config.getString("player-user-linking.linking-method", "both");
		linkingKickUnregistered = config.getBoolean("player-user-linking.kick-unregistered", false);
		linkingAutoRemind = config.getBoolean("player-user-linking.auto-remind", false);
		linkingAutoEvery = config.getLong("player-user-linking.auto-remind-every", 12000L);
		linkingNotifyRegistered = config.getBoolean("player-user-linking.notify-registered-player", true);
		linkingNotifyUnregistered = config.getBoolean("player-user-linking.notify-unregistered-player", true);

		linkingUnregisteredGroup = config.getString("player-user-linking.unregistered-player-group", "");
		linkingRegisteredGroup = config.getString("player-user-linking.registered-player-group", "");
		linkingNotifyPlayerGroup = config.getBoolean("player-user-linking.notify-player-of-group", false);
		linkingRegisteredFormerUnregisteredOnly = config.getBoolean("player-user-linking.registered-former-unregistered-only", true);
		linkingUnregisterFormerRegistered = config.getBoolean("player-user-linking.unregister-former-registered", false);

		linkingUsesKey = config.getBoolean("player-user-linking.uses-key", false);
		linkingTableName = config.getString("player-user-linking.table-name", "");
		linkingUserIDColumn = config.getString("player-user-linking.user-id-column", "");
		linkingIdentifierColumn = config.getString("player-user-linking.identifier-column", "");

		if (linkingIdentifierColumn.isEmpty())
		{
			linkingIdentifierColumn = config.getString("player-user-linking.playername-column", "");
			if (!linkingIdentifierColumn.isEmpty())
			{
				log.severe("the configuration option player-user-linking.playername-column is deprecated. Change playername-column to identifier-column in the player-user-linking section.");
			}
		}

		linkingKeyName = config.getString("player-user-linking.key-name", "");
		linkingKeyColumn = config.getString("player-user-linking.key-column", "");
		linkingValueColumn = config.getString("player-user-linking.value-column", "");

		avatarEnabled = config.getBoolean("app-avatar-config.enabled", false);
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

		if (config.contains("app-group-config.secondary.additional-columns"))
		{
			webappSecondaryAdditionalColumns = config.getConfigurationSection("app-group-config.secondary.additional-columns").getValues(false);
		}

		// Simple synchronization
		simpleSynchronizationSuperUserID = config.getString("simple-synchronization.super-user-user-id", "");
		simpleSynchronizationEnabled = config.getBoolean("simple-synchronization.enabled", false);
		simpleSynchronizationDirection = config.getString("simple-synchronization.direction", "two-way").toLowerCase();
		simpleSynchronizationFirstDirection = config.getString("simple-synchronization.first-direction", "two-way").toLowerCase();
		simpleSynchronizationPrimaryGroupNotify = config.getBoolean("simple-synchronization.primary-group-change-notify", false);

		if (config.contains("simple-synchronization.group-mapping"))
		{
			simpleSynchronizationGroupMap = config.getConfigurationSection("simple-synchronization.group-mapping").getValues(false);
		}

		if (config.contains("simple-synchronization.groups-treated-as-primary"))
		{
			simpleSynchronizationGroupsTreatedAsPrimary = config.getStringList("simple-synchronization.groups-treated-as-primary");
		}

		if (config.contains("simple-synchronization.webapp-secondary-groups-treated-as-primary"))
		{
			simpleSynchronizationWebappSecondaryGroupsTreatedAsPrimary = config.getStringList("simple-synchronization.webapp-secondary-groups-treated-as-primary");
		}

		// Ban synchronization
		banSynchronizationEnabled = config.getBoolean("ban-synchronization.enabled", false);
		banSynchronizationDirection = config.getString("simple-synchronization.direction", "two-way").toLowerCase();
		banSynchronizationMethod = config.getString("ban-synchronization.method", "table").toLowerCase();

		banSynchronizationTableName = config.getString("ban-synchronization.table-name", "");
		banSynchronizationUserIDColumn = config.getString("ban-synchronization.banned-user-id-column", "");

		if (banSynchronizationMethod.startsWith("use"))
		{
			banSynchronizationBanColumn = config.getString("ban-synchronization.ban-column", "");
			banSynchronizationValueBanned = config.getString("ban-synchronization.value-banned", "");
			banSynchronizationValueNotBanned = config.getString("ban-synchronization.value-notbanned", "");
		}
		else if (banSynchronizationMethod.startsWith("tab"))
		{
			banSynchronizationReasonColumn = config.getString("ban-synchronization.ban-reason-column", "");
			banSynchronizationStartTimeColumn = config.getString("ban-synchronization.ban-start-column", "");
			banSynchronizationEndTimeColumn = config.getString("ban-synchronization.ban-end-column", "");
			banSynchronizationBanGroupIDColumn = config.getString("ban-synchronization.ban-group-id-column", "");
			banSynchronizationBanGroupID = config.getString("ban-synchronization.ban-group-id", "");
		}
		else if (banSynchronizationMethod.startsWith("gro"))
		{
			banSynchronizationBanGroupType = config.getString("ban-synchronization.group-type", "");
			banSynchronizationBanGroup = config.getString("ban-synchronization.banned-group", "");
		}

		// These are calculated from settings above.
		groupSynchronizationActive = simpleSynchronizationEnabled && (webappPrimaryGroupEnabled || webappSecondaryGroupEnabled);
		playerDataRequired = groupSynchronizationActive;
		permissionsSystemRequired = !linkingUnregisteredGroup.isEmpty() || !linkingRegisteredGroup.isEmpty() || groupSynchronizationActive;

		if (permissionsSystemRequired)
		{
			environment.setPermissionHandler(selectPermissionsHandler());
		}
	}

	private PermissionHandler selectPermissionsHandler()
	{
		try
		{
			if (permissionsSystem.equalsIgnoreCase("PEX"))
			{
				environment.getLog().config("Permissions System: PermissionsEx (PEX)");
				return new PermissionHandlerPermissionsEx();
			}
			else if (permissionsSystem.equalsIgnoreCase("bPerms"))
			{
				environment.getLog().config("Permissions System: bPermissions (bPerms)");
				return new PermissionHandlerBPermissions();
			}
			else if (permissionsSystem.equalsIgnoreCase("GroupManager"))
			{
				environment.getLog().config("Permissions System: GroupManager");
				return new PermissionHandlerGroupManager();
			}
			else if (permissionsSystem.equalsIgnoreCase("PermsBukkit"))
			{
				environment.getLog().config("Permissions System: PermissionsBukkit (PermsBukkit)");
				return new PermissionHandlerPermissionsBukkit();
			}
			else if (permissionsSystem.equalsIgnoreCase("Vault"))
			{
				environment.getLog().config("Permissions System: Vault");
				return new PermissionHandlerVault();
			}
			else if (permissionsSystem.equalsIgnoreCase("zPermissions"))
			{
				environment.getLog().config("Permissions System: ZPermissions");
				return new PermissionHandlerZPermissions();
			}
			else
			{
				environment.getLog().severe("Unknown permissions system in config.yml. Features dependent on a permissions system disabled.");
				disableFeaturesDependentOnPermissions();
			}
		}
		catch (IllegalStateException e)
		{
			environment.getLog().severe(e.getMessage());
			environment.getLog().severe("Disabling features dependent on a permissions system.");
			disableFeaturesDependentOnPermissions();
		}
		return null;
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
				AchievementAvatar achievement = new AchievementAvatar(environment);
				achievement.load(achievementConfig, key);
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
					AchievementGroup achievement = new AchievementGroup(environment);
					achievement.setGroupName(groupName);
					achievement.load(achievementConfig, key + "." + groupName);
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
					AchievementPostCount achievement = new AchievementPostCount(environment);
					achievement.setPostCount(postCount);
					achievement.load(achievementConfig, key + "." + postCount);
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
						AchievementSectionPostCount achievement = new AchievementSectionPostCount(environment);
						achievement.setPostCount(postCount);
						achievement.setSectionID(sectionID);
						achievement.load(achievementConfig, key + "." + sectionID + "." + postCount);
						achievements.add(achievement);
					}
				}
			}
		}
	}

	public String reload(String filename)
	{
		loadMessages();
		loadAchievements();

		if (filename == null || filename.isEmpty() || filename.equals("config.yml"))
		{
			plugin.deactivate();
			plugin.reloadConfig();
			load();

			if (enableSQL(true) == false)
			{
				plugin.deactivate();
				return DATABASE_CONFIGURATION_PROBLEM;
			}

			plugin.activate();
			return null;
		}

		File configFile = new File(plugin.getDataFolder(), filename);

		if (configFile.exists())
		{
			plugin.deactivate();
			loadSettings(YamlConfiguration.loadConfiguration(configFile));

			if (enableSQL(true) == false)
			{
				plugin.deactivate();
				return DATABASE_CONFIGURATION_PROBLEM;
			}

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
		if (!databaseBindingAddress.isEmpty())
		{
			log.config(    "Database binding address             : " + databaseBindingAddress);
		}
		log.config(    "Database hostname                    : " + databaseHost);
		log.config(    "Database port                        : " + databasePort);
		log.config(    "Database name                        : " + databaseName);
		log.config(    "Database username                    : " + databaseUsername);

		// Linking Section
		log.config(    "Linking method                       : " + linkingMethod);
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
		log.config(    "Linking unregister former registered : " + linkingUnregisterFormerRegistered);
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
			log.config(  "Linking identifier column            : " + linkingIdentifierColumn);
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
		{
			log.config(  "Post count table name                : " + postCountTableName);
			log.config(  "Post count user ID column            : " + postCountUserIDColumn);
			log.config(  "Post count post count column         : " + postCountPostCountColumn);
		}

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

			if (webappSecondaryGroupStorageMethod.startsWith("jun"))
			{
				for (String columnName : webappSecondaryAdditionalColumns.keySet())
				{
					String output = columnName + " (" + webappSecondaryAdditionalColumns.get(columnName) + ")";
					log.config("Secondary group additional column    : " + output);
				}
			}
		}

		log.config(    "Simple synchronization enabled       : " + simpleSynchronizationEnabled);
		if (simpleSynchronizationEnabled)
		{
			log.config(  "Simple synchronization direction     : " + simpleSynchronizationDirection);
			log.config(  "Simple synchronization firstdirection: " + simpleSynchronizationFirstDirection);
			log.config(  "Simple synchronization notification  : " + simpleSynchronizationPrimaryGroupNotify);
			log.config(  "Simple synchronization P-groups      : " + simpleSynchronizationGroupsTreatedAsPrimary.toString());
			log.config(  "Simple synchronization WSP-groups    : " + simpleSynchronizationWebappSecondaryGroupsTreatedAsPrimary.toString());
		}

		log.config(    "Ban synchronization enabled          : " + banSynchronizationEnabled);
		if (banSynchronizationEnabled)
		{
			log.config(  "Ban synchronization direction        : " + banSynchronizationDirection);
			log.config(  "Ban synchronization method           : " + banSynchronizationMethod);
			log.config(  "Ban synchronization table name       : " + banSynchronizationTableName);
			log.config(  "Ban synchronization user ID column   : " + banSynchronizationUserIDColumn);
			if (banSynchronizationMethod.startsWith("tab"))
			{
				log.config("Ban synchronization reason column    : " + banSynchronizationReasonColumn);
				log.config("Ban synchronization start time column: " + banSynchronizationStartTimeColumn);
				log.config("Ban synchronization end time column  : " + banSynchronizationEndTimeColumn);
				log.config("Ban synchronization group id column  : " + banSynchronizationBanGroupIDColumn);
				log.config("Ban synchronization group id         : " + banSynchronizationBanGroupID);
			}
			else if (banSynchronizationMethod.startsWith("use"))
			{
				log.config("Ban synchronization ban column       : " + banSynchronizationBanColumn);
				log.config("Ban synchronization banned value     : " + banSynchronizationValueBanned);
				log.config("Ban synchronization not banned value : " + banSynchronizationValueNotBanned);
			}
			else if (banSynchronizationMethod.startsWith("gro"))
			{
				log.config("Ban synchronization group            : " + banSynchronizationBanGroup);
				log.config("Ban synchronization group type       : " + banSynchronizationBanGroupType);
			}
		}
	}

	private boolean checkSuperUserID()
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

		if (simpleSynchronizationSuperUserID.matches("ignore")) {
			log.warning("The super-user's user ID check is disabled. This is not recommended!");
			return true;
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

	private boolean enableSQL(boolean reload)
	{
		if (reload)
		{
			environment.getSql().close();
		}

		environment.setSql(new SQL(environment.getLog(),
						databaseHost + ":" + databasePort,
						databaseName + "",
						databaseUsername + "",
						databasePassword + "",
						databaseBindingAddress));

		environment.getSql().initialize();
		if (environment.getSql().checkConnection() == false)
		{
			environment.getLog().severe("Disabling CommunityBridge due to previous error.");
			return false;
		}
		sql = environment.getSql();
		return analyze();
	}
}
