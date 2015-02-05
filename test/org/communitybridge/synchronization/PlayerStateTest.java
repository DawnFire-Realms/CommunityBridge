package org.communitybridge.synchronization;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.communitybridge.main.CommunityBridge;
import org.communitybridge.main.Configuration;
import org.communitybridge.main.Environment;
import org.communitybridge.main.WebApplication;
import org.communitybridge.permissionhandlers.PermissionHandler;
import org.communitybridge.synchronization.dao.MoneyDao;
import org.communitybridge.utility.Log;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(YamlConfiguration.class)
public class PlayerStateTest
{
	private static final String PLAYER_NAME = RandomStringUtils.randomAlphabetic(9);
	private static final String PRIMARY_GROUP_NAME = RandomStringUtils.randomAlphabetic(7);
	private static final String USER_ID = RandomStringUtils.randomNumeric(3);
	private static final String PRIMARY_GROUP_ID = RandomStringUtils.randomNumeric(2);
	private static final List<String> GROUP_NAMES = new ArrayList<String>(Arrays.asList(new String[] {RandomStringUtils.randomAlphabetic(7), RandomStringUtils.randomAlphabetic(7), RandomStringUtils.randomAlphabetic(7)}));
	private static final List<String> GROUP_IDS = new ArrayList<String>(Arrays.asList(new String[] {RandomStringUtils.randomNumeric(2), RandomStringUtils.randomNumeric(2), RandomStringUtils.randomNumeric(2)}));

	private static final UUID UUID = new UUID(RandomUtils.nextLong(), RandomUtils.nextLong());

	private Environment environment = new Environment();
	private Economy economy = mock(Economy.class);
	private PermissionHandler permissionHandler = mock(PermissionHandler.class);
	private Configuration configuration = mock(Configuration.class);
  private CommunityBridge plugin = mock(CommunityBridge.class);
	private Log log = mock(Log.class);
	private Player player = mock(Player.class);
	private MoneyDao money = mock(MoneyDao.class);
	private WebApplication webApplication = mock(WebApplication.class);

	private YamlConfiguration playerData = mock(YamlConfiguration.class);
	private File playerFile = mock(File.class);
	private File oldPlayerFile = mock(File.class);

	@InjectMocks
	private PlayerState state = new PlayerState();

	@Before
	public void beforeEach() {
		environment.setConfiguration(configuration);
		environment.setEconomy(economy);
		environment.setLog(log);
		environment.setPermissionHandler(permissionHandler);
		environment.setPlugin(plugin);
		environment.setWebApplication(webApplication);
		configuration.simpleSynchronizationGroupsTreatedAsPrimary = new ArrayList<String>();
		configuration.simpleSynchronizationGroupsTreatedAsPrimary.add(PRIMARY_GROUP_NAME);
		configuration.groupSynchronizationActive = true;
		configuration.webappPrimaryGroupEnabled = true;
		configuration.webappSecondaryGroupEnabled = true;
		when(player.getUniqueId()).thenReturn(UUID);
		when(player.getName()).thenReturn(PLAYER_NAME);
		when(webApplication.getUserPrimaryGroupID(USER_ID)).thenReturn(PRIMARY_GROUP_ID);
		when(webApplication.getUserSecondaryGroupIDs(USER_ID)).thenReturn(GROUP_IDS);
		when(permissionHandler.supportsPrimaryGroups()).thenReturn(true);
		when(permissionHandler.getPrimaryGroup(player)).thenReturn(PRIMARY_GROUP_NAME);
		when(permissionHandler.getGroups(player)).thenReturn(GROUP_NAMES);
	}

	@Test
	public void generateSetsPrimaryGroupId() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);

		assertEquals(PRIMARY_GROUP_ID, state.getWebappPrimaryGroupID());
	}

	@Test
	public void generateWhenGroupSynchronizationInactiveDoesNotSetPrimaryGroupId() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = false;
		state.generate(environment, player, USER_ID);

		assertEquals("", state.getWebappPrimaryGroupID());
	}

	@Test
	public void generateWhenPrimaryGroupInactiveDoesNotSetPrimaryGroupId() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = true;
		configuration.webappPrimaryGroupEnabled = false;
		state.generate(environment, player, USER_ID);

		assertEquals("", state.getWebappPrimaryGroupID());
	}

	@Test
	public void generateSetsGroupIds() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		for (String id : GROUP_IDS)
		{
			assertTrue(id + "missing", state.getWebappGroupIDs().contains(id));
		}
	}

	@Test
	public void generateWhenGroupSynchronizationInactiveDoesNotSetGroupIds() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = false;
		state.generate(environment, player, USER_ID);

		assertTrue(state.getWebappGroupIDs().isEmpty());
	}

	@Test
	public void generateWhenSecondaryGroupInactiveDoesNotSetGroupIds() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = true;
		configuration.webappSecondaryGroupEnabled = false;
		state.generate(environment, player, USER_ID);

		assertTrue(state.getWebappGroupIDs().isEmpty());
	}

	@Test
	public void generateSetsPrimaryGroupName() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		assertEquals(PRIMARY_GROUP_NAME, state.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void generateWhenPrimaryGroupNotSupportedSetsPrimaryGroupName() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		when(permissionHandler.supportsPrimaryGroups()).thenReturn(false);
		GROUP_NAMES.add(PRIMARY_GROUP_NAME);
		state.generate(environment, player, USER_ID);
		assertEquals(PRIMARY_GROUP_NAME, state.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void generateWhenPrimaryGroupNotSupportedSetsBlankOnNotFound() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		when(permissionHandler.supportsPrimaryGroups()).thenReturn(false);
		state.generate(environment, player, USER_ID);
		assertEquals("", state.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void generateWhenGroupSynchronizationInactiveDoesNotSetPrimaryGroupName() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = false;
		state.generate(environment, player, USER_ID);

		assertEquals("", state.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void generateSetsGroupNames() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);

		for (String group : GROUP_NAMES)
		{
			assertTrue(group + "missing", state.getPermissionsSystemGroupNames().contains(group));
		}
	}

	@Test
	public void generateWhenGroupSynchronizationInactiveDoesNotSetGroupNames() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		configuration.groupSynchronizationActive = false;
		state.generate(environment, player, USER_ID);

		assertTrue(state.getPermissionsSystemGroupNames().isEmpty());
	}

	@Test
	public void generateSetsMinecraftWallet() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double wallet = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;

		when(economy.getBalance(player)).thenReturn(wallet);

		state.generate(environment, player, USER_ID);

		assertEquals(wallet, state.getMinecraftWallet(), 0);
	}

	@Test
	public void generateSetsWebApplicationWallet() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double wallet = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;
		when(money.getBalance(environment, USER_ID)).thenReturn(wallet);
		state.generate(environment, player, USER_ID);

		assertEquals(wallet, state.getWebApplicationWallet(), 0);
	}

	@Test
	public void copyNeverReturnsNull() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();
		assertNotNull(copy);
	}

	@Test
	public void copyCopiesPrimaryGroupId() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();
		assertEquals(state.getWebappPrimaryGroupID(), copy.getWebappPrimaryGroupID());
	}

	@Test
	public void copyCopiesGroupIds() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();
		assertEquals(state.getWebappGroupIDs(), copy.getWebappGroupIDs());
	}

	@Test
	public void copyCopiesPrimaryGroupName() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();

		assertEquals(state.getPermissionsSystemPrimaryGroupName(), copy.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void copyCopiesGroupNames() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();

		assertEquals(state.getPermissionsSystemGroupNames(), copy.getPermissionsSystemGroupNames());
	}

	@Test
	public void copyCopiesMinecraftMoney() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double wallet = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;

		when(economy.getBalance(player)).thenReturn(wallet);
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();

		assertEquals(wallet, copy.getMinecraftWallet(), 0);
	}

	@Test
	public void copyCopiesWebApplicationMoney() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double wallet = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;

		when(money.getBalance(environment, USER_ID)).thenReturn(wallet);
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();

		assertEquals(wallet, copy.getWebApplicationWallet(), 0);
	}

	@Test
	public void copyCopiesNewFile() throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		state.generate(environment, player, USER_ID);
		PlayerState copy = state.copy();

		assertEquals(state.isIsNewFile(), copy.isIsNewFile());
	}

	@Test
	public void saveSavesData() throws IOException, IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double mcWallet = RandomUtils.nextDouble() + 1;
		double wbWallet = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;

		when(economy.getBalance(player)).thenReturn(mcWallet);
		when(money.getBalance(environment, USER_ID)).thenReturn(wbWallet);

		state.generate(environment, player, USER_ID);
		state.save(player, playerFile, environment.getLog());

		verify(playerData).set("last-known-name", PLAYER_NAME);
		verify(playerData).set("minecraft-money", mcWallet);
		verify(playerData).set("web-application-money", wbWallet);
		verify(playerData).set("permissions-system.primary-group-name", PRIMARY_GROUP_NAME);
		verify(playerData).set("permissions-system.group-names", GROUP_NAMES);
		verify(playerData).set("webapp.primary-group-id", PRIMARY_GROUP_ID);
		verify(playerData).set("webapp.group-ids", GROUP_IDS);
		verify(playerData).save(any(File.class));
	}

	@Test
	public void saveHandlesIOException() throws IOException, IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double money = RandomUtils.nextDouble() + 1;
		configuration.economyEnabled = true;
		configuration.walletEnabled = true;

		when(economy.getBalance(player)).thenReturn(money);
		doNothing().when(playerData).set(anyString(), anyString());
		String exceptionMessage = RandomStringUtils.randomAlphabetic(18);
		doThrow(new IOException(exceptionMessage)).when(playerData).save(any(File.class));

		state.generate(environment, player, USER_ID);
		state.save(player, playerFile, environment.getLog());
		verify(log).severe("Exception while saving player state for " + player.getName() + ": " + exceptionMessage);
	}

	@Test
	public void loadHandlesNewFile() throws IOException, IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		when(playerFile.exists()).thenReturn(false);
		when(oldPlayerFile.exists()).thenReturn(false);
		state.load(playerFile);
		assertEquals(true, state.isIsNewFile());
		assertEquals("", state.getWebappPrimaryGroupID());
		assertTrue("Group name list should be empty", state.getPermissionsSystemGroupNames().isEmpty());
		assertTrue("Group id list should be empty", state.getWebappGroupIDs().isEmpty());
		assertEquals("", state.getPermissionsSystemPrimaryGroupName());
	}

	@Test
	public void loadLoadsData() throws IOException, IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		double mcWallet = RandomUtils.nextDouble() + 1;
		double waWallet = RandomUtils.nextDouble() + 1;

		when(playerFile.exists()).thenReturn(true);
		when(oldPlayerFile.exists()).thenReturn(true);
		PowerMockito.mockStatic(YamlConfiguration.class);
		when(YamlConfiguration.loadConfiguration(playerFile)).thenReturn(playerData);
		when(playerData.getDouble("minecraft-money", 0)).thenReturn(mcWallet);
		when(playerData.getDouble("web-application-money", 0)).thenReturn(waWallet);
		when(playerData.getStringList("permissions-system.group-names")).thenReturn(GROUP_NAMES);
		when(playerData.getString("permissions-system.primary-group-name", "")).thenReturn(PRIMARY_GROUP_NAME);
		when(playerData.getStringList("webapp.group-ids")).thenReturn(GROUP_IDS);
		when(playerData.getString("webapp.primary-group-id", "")).thenReturn(PRIMARY_GROUP_ID);
		state.load(playerFile);

		assertEquals(false, state.isIsNewFile());
		assertEquals(mcWallet, state.getMinecraftWallet(), 0);
		assertEquals(waWallet, state.getWebApplicationWallet(), 0);
		assertEquals(PRIMARY_GROUP_ID, state.getWebappPrimaryGroupID());
		assertEquals(PRIMARY_GROUP_NAME, state.getPermissionsSystemPrimaryGroupName());

		for (String group : GROUP_NAMES)
		{
			assertTrue(group + " missing.", state.getPermissionsSystemGroupNames().contains(group));
		}

		for (String id : GROUP_IDS)
		{
			assertTrue(id + " missing.", state.getWebappGroupIDs().contains(id));
		}
	}
}