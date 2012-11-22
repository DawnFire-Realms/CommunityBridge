package org.ruhlendavis.mc.communitybridge;

import net.netmanagers.community.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Implements the PermissionHandler interface for PermissionsEx.
 * 
 * @author Feaelin
 */
public class PermissionHandlerPermissionsEx implements PermissionHandler
{
	/**
	 * Actual constructor.
	 * 
	 * @throws IllegalStateException when PermissionsEx is not present or disabled.
	 */
	public PermissionHandlerPermissionsEx() throws IllegalStateException
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx");

		if (plugin != null && plugin.isEnabled())
		{}
		else
		{
			throw new IllegalStateException("bPermissions is either not present or not enabled.");
		}
	}
	/**
	 * Dummy constructor for unit testing purposes.
	 * 
	 * @param dummy Any boolean value (not used)
	 * @throws IllegalStateException Not actually thrown as this is a dummy method
	 */
	public PermissionHandlerPermissionsEx(boolean dummy) throws IllegalStateException
	{}
	
 /**
	 * Asks permissions system if a player is the member of a given group.
	 * 
	 * @param groupName String containing name of group to check
	 * @param player    String containing name of player to check 
	 * @return boolean which is true if the the player is a member of the group
	 */
	@Override
	public boolean isMemberOfGroup(String playerName, String groupName)
	{
		try
		{
			return PermissionsEx.getUser(playerName).inGroup(groupName, false);
		}
		catch (Error e)
		{
			Main.log.severe(e.getMessage());
		}
		
		return false;
	}
}
