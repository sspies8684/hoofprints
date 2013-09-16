package net.decix.rs.managers;

import static net.decix.bgpstack.util.Utility.readBooleanFromScript;
import static net.decix.bgpstack.util.Utility.readIntFromScript;
import static net.decix.bgpstack.util.Utility.runScript;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;

import net.decix.bgpstack.util.AntProperties;
import net.decix.rs.RSTestcaseException;
import net.decix.rs.conf.ASPathFilter;
import net.decix.rs.conf.Configuration;
import net.decix.rs.conf.Neighbor;
import net.decix.rs.conf.PrefixFilter;

public class RemoteScriptManager extends RouteserverManager
{
	private AntProperties properties;

	private static final String HOSTNAME = "script-testhost";

	public RemoteScriptManager(Configuration startupConfiguration, File propertiesFile) throws RSTestcaseException
	{
		super(startupConfiguration);
		readScriptProperties(propertiesFile);
		runControlMasterThread();
	}

	private void runControlMasterThread()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					runScript(new File(properties.getProperty("script.control_master")), properties.getProperty("script.ip"));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public RemoteScriptManager(Configuration startupConfiguration) throws RSTestcaseException
	{
		super(startupConfiguration);
		throw new RSTestcaseException("Please use another constructor to tell about properties");
	}

	@Override
	public int getCPU() throws RSTestcaseException
	{
		try
		{
			return readIntFromScript(new File(properties.getProperty("script.cpu")), properties.getProperty("script.ip"));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("script manager misconfiguration: (" + e.getClass() + ") " + e.getMessage());
		}
	}

	@Override
	public boolean isRunning() throws RSTestcaseException
	{
		try
		{
			return readBooleanFromScript(new File(properties.getProperty("script.is_running")), properties.getProperty("script.ip"));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public void loadConfiguration(Configuration config) throws RSTestcaseException
	{
		File configScript = new File(properties.getProperty("script.config"));
		try
		{
			runScript(configScript, properties.getProperty("script.ip"), "startup", HOSTNAME, STANDARDPASSWORD, Long.toString(config.getAsn()), listenAddress.getHostAddress());

			for (Neighbor n : config.getNeighbors())
				runScript(configScript, properties.getProperty("script.ip"), "add_neighbor", n.getAddress().getHostAddress(), Long.toString(n.getAsn()), Boolean.toString(n.isRsClient()), n.isAttributeUnchanged() + "", n.getDescription());

			for (Neighbor n : config.getNeighbors())
				for (ASPathFilter f : n.getAsPathFilters())
					runScript(configScript, properties.getProperty("script.ip"), "add_as_filter", n.getAddress().getHostAddress(), f.getSourceAs() + "", f.isAllowed() + "");

			for (Neighbor n : config.getNeighbors())
			{
				for (PrefixFilter f : n.getOutFilters())
					runScript(configScript, properties.getProperty("script.ip"), "add_neighbor_prefix_filter", "out", n.getAddress().getHostAddress(), f.getPrefix().toString(), f.getUpperBound() + "", f.getLowerBound() + "", f.isAllowed() + "");
				for (PrefixFilter f : n.getInFilters())
					runScript(configScript, properties.getProperty("script.ip"), "add_neighbor_prefix_filter", "in", n.getAddress().getHostAddress(), f.getPrefix().toString(), f.getUpperBound() + "", f.getLowerBound() + "", f.isAllowed() + "");
			}

			for (PrefixFilter f : config.getGlobalFilters())
				runScript(configScript, properties.getProperty("script.ip"), "add_global_prefix_filter", f.getPrefix().toString(), f.getUpperBound() + "", f.getLowerBound() + "", f.isAllowed() + "");

			runScript(configScript, properties.getProperty("script.ip"), "deploy");
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}

		cycle();

	}

	private void cycle() throws RSTestcaseException
	{
		if (isRunning())
		{
			stopRouteServer();
			while (isRunning())
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					throw new RSTestcaseException("Could not cycle RS");
				}
			startRouteserver();
		}
	}

	@Override
	public void readProperties() throws RSTestcaseException
	{
		properties = new AntProperties();
		try
		{
			properties.load(new File("conf/test.properties"));
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}
	}

	public void readScriptProperties(File propertiesFile) throws RSTestcaseException
	{
		try
		{
			properties.load(propertiesFile);
			if (properties.containsProperty("script.ip"))
				listenAddress = InetAddress.getByName(properties.getProperty("script.ip"));
			else
				listenAddress = InetAddress.getLocalHost();

			if (properties.containsProperty("script.ip6"))
				listen6Address = InetAddress.getByName(properties.getProperty("script.ip6"));
			else
				listen6Address = Inet6Address.getLocalHost();

			if (properties.containsProperty("script.port"))
				listenPort = properties.getPropertyAsInt("script.port");

		}
		catch (Exception e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public void startRouteserver() throws RSTestcaseException
	{
		try
		{
			runScript(new File(properties.getProperty("script.start")), properties.getProperty("script.ip"), Integer.toString(listenPort), listenAddress.getHostAddress(), "sspies", "sspies");
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}
	}

	@Override
	public void stopRouteServer() throws RSTestcaseException
	{
		try
		{
			runScript(new File(properties.getProperty("script.stop")), properties.getProperty("script.ip"));
		}
		catch (IOException e)
		{
			throw new RSTestcaseException("script manager misconfiguration: " + e.getMessage());
		}

	}

	@Override
	public AntProperties getProperties() throws RSTestcaseException
	{
		return properties;
	}

	@Override
	public int getMemory() throws RSTestcaseException
	{
		try
		{
			return readIntFromScript(new File(properties.getProperty("script.mem")), properties.getProperty("script.ip")) / 1024;
		}
		catch (Exception e)
		{
			throw new RSTestcaseException("script manager misconfiguration: (" + e.getClass() + ") " + e.getMessage());
		}
	}

	@Override
	public String toString()
	{
		return "RemoteScriptManager:" + properties.getProperty("script.title");
	}
}
