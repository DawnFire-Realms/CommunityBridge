package org.communitybridge.synchronization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.communitybridge.main.Environment;

public class PlayerState
{
	private Environment environment;
	private Player player;
	private String userID = "";

	private String webappPrimaryGroupID = "";
	private List<String> webappGroupIDs = new ArrayList<String>();

	private String permissionsSystemPrimaryGroupName = "";
	private List<String> permissionsSystemGroupNames= new ArrayList<String>();

	private double minecraftWallet = 0;
	private double webApplicationWallet = 0;

	private boolean isNewFile;

	private FileConfiguration playerData = new YamlConfiguration();
	private File playerFile;
	private File oldPlayerFile;

	public PlayerState(Environment environment, Player player, String userID)
	{
		this.environment = environment;
		this.player = player;
		this.userID = userID;
		setupPlayerFile(player);
	}

	PlayerState(Environment environment, Player player, String userID, YamlConfiguration playerData, File playerFile, File oldPlayerFile)
	{
		this.environment = environment;
		this.player = player;
		this.userID = userID;
		this.playerData = playerData;
		this.playerFile = playerFile;
		this.oldPlayerFile = oldPlayerFile;
	}

	public void generate()
	{
		if (environment.getConfiguration().economyEnabled && environment.getConfiguration().walletEnabled)
		{
			minecraftWallet = environment.getEconomy().getBalance(player);
			webApplicationWallet = environment.getWebApplication().getBalance(userID);
		}
		if (environment.getConfiguration().groupSynchronizationActive)
		{
			if (environment.getConfiguration().webappSecondaryGroupEnabled)
			{
				permissionsSystemGroupNames = environment.getPermissionHandler().getGroups(player);
				webappGroupIDs = environment.getWebApplication().getUserSecondaryGroupIDs(userID);
			}
			if (environment.getConfiguration().webappPrimaryGroupEnabled)
			{
				permissionsSystemPrimaryGroupName = getPrimaryGroupName();
				webappPrimaryGroupID = environment.getWebApplication().getUserPrimaryGroupID(userID);
			}
		}
	}

	public void load()
	{
		if (playerFile.exists())
		{
			loadFromFile(playerFile);
		}
		else
		{
			if (oldPlayerFile.exists())
			{
				loadFromFile(oldPlayerFile);
			}
			else
			{
				isNewFile = true;
				permissionsSystemGroupNames = new ArrayList<String>();
				permissionsSystemPrimaryGroupName = "";
				webappPrimaryGroupID = "";
				webappGroupIDs = new ArrayList<String>();
			}
		}
	}

	public void save() throws IOException
	{
		playerData.set("last-known-name", player.getName());
		playerData.set("minecraft-money", minecraftWallet);
		playerData.set("permissions-system.primary-group-name", permissionsSystemPrimaryGroupName);
		playerData.set("permissions-system.group-names", permissionsSystemGroupNames);
		playerData.set("webapp.primary-group-id", webappPrimaryGroupID);
		playerData.set("webapp.group-ids", webappGroupIDs);

		playerData.save(playerFile);
	}

	public PlayerState copy()
	{
		PlayerState copy = new PlayerState(environment, player, userID);
		copy.isNewFile = isNewFile;
		copy.setMinecraftWallet(minecraftWallet);
		copy.setPermissionsSystemGroupNames(permissionsSystemGroupNames);
		copy.setPermissionsSystemPrimaryGroupName(permissionsSystemPrimaryGroupName);
		copy.setWebappGroupIDs(webappGroupIDs);
		copy.setWebappPrimaryGroupID(webappPrimaryGroupID);
		return copy;
	}

	private void loadFromFile(File file)
	{
		playerData = YamlConfiguration.loadConfiguration(file);
		minecraftWallet = playerData.getDouble("minecraft-money", 0.0);
		permissionsSystemGroupNames = playerData.getStringList("permissions-system.group-names");
		permissionsSystemPrimaryGroupName = playerData.getString("permissions-system.primary-group-name", "");
		webappGroupIDs = playerData.getStringList("webapp.group-ids");
		webappPrimaryGroupID = playerData.getString("webapp.primary-group-id", "");
		isNewFile = false;
	}

	public String getWebappPrimaryGroupID()
	{
		return webappPrimaryGroupID;
	}

	public void setWebappPrimaryGroupID(String webappPrimaryGroupID)
	{
		this.webappPrimaryGroupID = webappPrimaryGroupID;
	}

	public List<String> getWebappGroupIDs()
	{
		return webappGroupIDs;
	}

	public void setWebappGroupIDs(List<String> webappGroupIDs)
	{
		this.webappGroupIDs = webappGroupIDs;
	}

	public String getPermissionsSystemPrimaryGroupName()
	{
		return permissionsSystemPrimaryGroupName;
	}

	public void setPermissionsSystemPrimaryGroupName(String permissionsSystemPrimaryGroupName)
	{
		this.permissionsSystemPrimaryGroupName = permissionsSystemPrimaryGroupName;
	}

	public List<String> getPermissionsSystemGroupNames()
	{
		return permissionsSystemGroupNames;
	}

	public void setPermissionsSystemGroupNames(List<String> permissionsSystemGroupNames)
	{
		this.permissionsSystemGroupNames = permissionsSystemGroupNames;
	}

	public boolean isIsNewFile()
	{
		return isNewFile;
	}

	private void setupPlayerFile(Player player)
	{
		File playerFolder = new File(environment.getPlugin().getDataFolder(), "Players");
		playerFile = new File(playerFolder, player.getUniqueId().toString() + ".yml");
		oldPlayerFile = new File(playerFolder, player.getName() + ".yml");
	}

	public double getMinecraftWallet()
	{
		return minecraftWallet;
	}

	public void setMinecraftWallet(double wallet)
	{
		this.minecraftWallet = wallet;
	}

	public double getWebApplicationWallet()
	{
		return webApplicationWallet;
	}

	public void setWebApplicationWallet(double wallet)
	{
		this.webApplicationWallet = wallet;
	}

	private String getPrimaryGroupName()
	{
		if (environment.getPermissionHandler().supportsPrimaryGroups())
		{
			return environment.getPermissionHandler().getPrimaryGroup(player);
		}
		else
		{
			for (String groupName : environment.getConfiguration().simpleSynchronizationGroupsTreatedAsPrimary)
			{
				if (permissionsSystemGroupNames.contains(groupName))
				{
					permissionsSystemGroupNames.remove(groupName);
					return groupName;
				}
			}
			return "";
		}
	}
}
